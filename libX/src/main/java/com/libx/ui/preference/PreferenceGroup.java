/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.libx.ui.preference;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.collection.SimpleArrayMap;
import androidx.core.content.res.TypedArrayUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.libx.ui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class PreferenceGroup extends Preference {
    private static final String TAG = "PreferenceGroup";
    @SuppressWarnings("WeakerAccess") /* synthetic access */
    final SimpleArrayMap<String, Long> mIdRecycleCache = new SimpleArrayMap<>();
    private final Handler mHandler = new Handler();
    private final Runnable mClearRecycleCacheRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                mIdRecycleCache.clear();
            }
        }
    };

    private List<Preference> mPreferences;
    private boolean mOrderingAsAdded = true;
    private int mCurrentPreferenceOrder = 0;
    private boolean mAttachedToHierarchy = false;
    private int mInitialExpandedChildrenCount = Integer.MAX_VALUE;
    private OnExpandButtonClickListener mOnExpandButtonClickListener = null;

    @SuppressLint("RestrictedApi")
    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mPreferences = new ArrayList<>();

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.PreferenceGroup, defStyleAttr, defStyleRes);

        mOrderingAsAdded =
                TypedArrayUtils.getBoolean(a, R.styleable.PreferenceGroup_orderingFromXml,
                        R.styleable.PreferenceGroup_orderingFromXml, true);

        if (a.hasValue(R.styleable.PreferenceGroup_initialExpandedChildrenCount)) {
            setInitialExpandedChildrenCount((TypedArrayUtils.getInt(
                    a, R.styleable.PreferenceGroup_initialExpandedChildrenCount,
                    R.styleable.PreferenceGroup_initialExpandedChildrenCount, Integer.MAX_VALUE)));
        }
        a.recycle();
    }

    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public boolean isOrderingAsAdded() {
        return mOrderingAsAdded;
    }

    public void setOrderingAsAdded(boolean orderingAsAdded) {
        mOrderingAsAdded = orderingAsAdded;
    }

    public int getInitialExpandedChildrenCount() {
        return mInitialExpandedChildrenCount;
    }

    public void setInitialExpandedChildrenCount(int expandedCount) {
        if (expandedCount != Integer.MAX_VALUE && !hasKey()) {
            Log.e(TAG, getClass().getSimpleName()
                    + " should have a key defined if it contains an expandable preference");
        }
        mInitialExpandedChildrenCount = expandedCount;
    }

    public void addItemFromInflater(Preference preference) {
        addPreference(preference);
    }

    public int getPreferenceCount() {
        return mPreferences.size();
    }

    public Preference getPreference(int index) {
        return mPreferences.get(index);
    }

    public boolean addPreference(Preference preference) {
        if (mPreferences.contains(preference)) {
            return true;
        }
        if (preference.getKey() != null) {
            PreferenceGroup root = this;
            while (root.getParent() != null) {
                root = root.getParent();
            }
            final String key = preference.getKey();
            if (root.findPreference(key) != null) {
                Log.e(TAG, "Found duplicated key: \"" + key
                        + "\". This can cause unintended behaviour,"
                        + " please use unique keys for every preference.");
            }
        }

        if (preference.getOrder() == DEFAULT_ORDER) {
            if (mOrderingAsAdded) {
                preference.setOrder(mCurrentPreferenceOrder++);
            }

            if (preference instanceof PreferenceGroup) {
                // TODO: fix (method is called tail recursively when inflating,
                // so we won't end up properly passing this flag down to children
                ((PreferenceGroup) preference).setOrderingAsAdded(mOrderingAsAdded);
            }
        }

        int insertionIndex = Collections.binarySearch(mPreferences, preference);
        if (insertionIndex < 0) {
            insertionIndex = insertionIndex * -1 - 1;
        }

        if (!onPrepareAddPreference(preference)) {
            return false;
        }

        synchronized (this) {
            mPreferences.add(insertionIndex, preference);
        }

        final PreferenceManager preferenceManager = getPreferenceManager();
        final String key = preference.getKey();
        final long id;
        if (key != null && mIdRecycleCache.containsKey(key)) {
            id = mIdRecycleCache.get(key);
            mIdRecycleCache.remove(key);
        } else {
            id = preferenceManager.getNextId();
        }
        preference.onAttachedToHierarchy(preferenceManager, id);
        preference.assignParent(this);

        if (mAttachedToHierarchy) {
            preference.onAttached();
        }

        notifyHierarchyChanged();

        return true;
    }

    public boolean removePreference(Preference preference) {
        final boolean returnValue = removePreferenceInt(preference);
        notifyHierarchyChanged();
        return returnValue;
    }

    public boolean removePreferenceRecursively(@NonNull CharSequence key) {
        final Preference preference = findPreference(key);
        if (preference == null) {
            return false;
        }
        return preference.getParent().removePreference(preference);
    }

    private boolean removePreferenceInt(Preference preference) {
        synchronized (this) {
            preference.onPrepareForRemoval();
            if (preference.getParent() == this) {
                preference.assignParent(null);
            }
            boolean success = mPreferences.remove(preference);
            if (success) {
                // If this preference, or another preference with the same key, gets re-added
                // immediately, we want it to have the same id so that it can be correctly tracked
                // in the adapter by RecyclerView, to make it appear as if it has only been
                // seamlessly updated. If the preference is not re-added by the time the handler
                // runs, we take that as a signal that the preference will not be re-added soon
                // in which case it does not need to retain the same id.

                // If two (or more) preferences have the same (or null) key and both are removed
                // and then re-added, only one id will be recycled and the second (and later)
                // preferences will receive a newly generated id. This use pattern of the preference
                // API is strongly discouraged.
                final String key = preference.getKey();
                if (key != null) {
                    mIdRecycleCache.put(key, preference.getId());
                    mHandler.removeCallbacks(mClearRecycleCacheRunnable);
                    mHandler.post(mClearRecycleCacheRunnable);
                }
                if (mAttachedToHierarchy) {
                    preference.onDetached();
                }
            }

            return success;
        }
    }

    public void removeAll() {
        synchronized (this) {
            List<Preference> preferences = mPreferences;
            for (int i = preferences.size() - 1; i >= 0; i--) {
                removePreferenceInt(preferences.get(0));
            }
        }
        notifyHierarchyChanged();
    }

    protected boolean onPrepareAddPreference(Preference preference) {
        preference.onParentChanged(this, shouldDisableDependents());
        return true;
    }

    @SuppressWarnings({"TypeParameterUnusedInFormals", "unchecked"})
    @Nullable
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (TextUtils.equals(getKey(), key)) {
            return (T) this;
        }
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            final Preference preference = getPreference(i);
            final String curKey = preference.getKey();

            if (TextUtils.equals(curKey, key)) {
                return (T) preference;
            }

            if (preference instanceof PreferenceGroup) {
                final T returnedPreference = ((PreferenceGroup) preference).findPreference(key);
                if (returnedPreference != null) {
                    return returnedPreference;
                }
            }
        }
        return null;
    }

    protected boolean isOnSameScreenAsChildren() {
        return true;
    }

    @RestrictTo(LIBRARY)
    public boolean isAttached() {
        return mAttachedToHierarchy;
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    @Nullable
    public OnExpandButtonClickListener getOnExpandButtonClickListener() {
        return mOnExpandButtonClickListener;
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public void setOnExpandButtonClickListener(
            @Nullable OnExpandButtonClickListener onExpandButtonClickListener) {
        mOnExpandButtonClickListener = onExpandButtonClickListener;
    }

    @Override
    public void onAttached() {
        super.onAttached();

        // Mark as attached so if a preference is later added to this group, we
        // can tell it we are already attached
        mAttachedToHierarchy = true;

        // Dispatch to all contained preferences
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onAttached();
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();

        // We won't be attached to the activity anymore
        mAttachedToHierarchy = false;

        // Dispatch to all contained preferences
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onDetached();
        }
    }

    @Override
    public void notifyDependencyChange(boolean disableDependents) {
        super.notifyDependencyChange(disableDependents);

        // Child preferences have an implicit dependency on their containing
        // group. Dispatch dependency change to all contained preferences.
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).onParentChanged(this, disableDependents);
        }
    }

    void sortPreferences() {
        synchronized (this) {
            Collections.sort(mPreferences);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(Bundle container) {
        super.dispatchSaveInstanceState(container);

        // Dispatch to all contained preferences
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchSaveInstanceState(container);
        }
    }

    @Override
    protected void dispatchRestoreInstanceState(Bundle container) {
        super.dispatchRestoreInstanceState(container);

        // Dispatch to all contained preferences
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchRestoreInstanceState(container);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mInitialExpandedChildrenCount);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in saveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState groupState = (SavedState) state;
        mInitialExpandedChildrenCount = groupState.mInitialExpandedChildrenCount;
        super.onRestoreInstanceState(groupState.getSuperState());
    }

    public interface PreferencePositionCallback {
        int getPreferenceAdapterPosition(String key);
        int getPreferenceAdapterPosition(Preference preference);
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public interface OnExpandButtonClickListener {
        void onExpandButtonClick();
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        int mInitialExpandedChildrenCount;

        SavedState(Parcel source) {
            super(source);
            mInitialExpandedChildrenCount = source.readInt();
        }

        SavedState(Parcelable superState, int initialExpandedChildrenCount) {
            super(superState);
            mInitialExpandedChildrenCount = initialExpandedChildrenCount;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mInitialExpandedChildrenCount);
        }
    }
}
