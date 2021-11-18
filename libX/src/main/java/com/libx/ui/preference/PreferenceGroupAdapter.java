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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.libx.ui.R;

import java.util.ArrayList;
import java.util.List;

@RestrictTo(LIBRARY_GROUP_PREFIX)
public class PreferenceGroupAdapter extends RecyclerView.Adapter<PreferenceViewHolder>
        implements Preference.OnPreferenceChangeInternalListener,
        PreferenceGroup.PreferencePositionCallback {

    private PreferenceGroup mPreferenceGroup;
    private List<Preference> mPreferences;
    private List<Preference> mVisiblePreferences;
    private final List<PreferenceResourceDescriptor> mPreferenceResourceDescriptors;
    private final Handler mHandler;
    private final Runnable mSyncRunnable = this::updatePreferences;

    public PreferenceGroupAdapter(PreferenceGroup preferenceGroup) {
        mPreferenceGroup = preferenceGroup;
        mHandler = new Handler();

        // This adapter should be notified when preferences are added or removed from the group
        mPreferenceGroup.setOnPreferenceChangeInternalListener(this);

        mPreferences = new ArrayList<>();
        mVisiblePreferences = new ArrayList<>();
        mPreferenceResourceDescriptors = new ArrayList<>();

        if (mPreferenceGroup instanceof PreferenceScreen) {
            setHasStableIds(((PreferenceScreen) mPreferenceGroup).shouldUseGeneratedIds());
        } else {
            setHasStableIds(true);
        }
        // Initial sync to generate mPreferences and mVisiblePreferences and display the visible
        // preferences in the RecyclerView
        updatePreferences();
    }

    @SuppressWarnings("WeakerAccess") /* synthetic access */
    void updatePreferences() {
        for (final Preference preference : mPreferences) {
            // Clear out the listeners in anticipation of some items being removed. This listener
            // will be set again on any remaining preferences when we flatten the group.
            preference.setOnPreferenceChangeInternalListener(null);
        }
        // Attempt to reuse the current array size when creating the new array for efficiency
        final int size = mPreferences.size();
        mPreferences = new ArrayList<>(size);
        flattenPreferenceGroup(mPreferences, mPreferenceGroup);

        final List<Preference> oldVisibleList = mVisiblePreferences;

        // Create a new variable so we can pass into DiffUtil without using a synthetic accessor
        // to access the private mVisiblePreferences
        final List<Preference> visiblePreferenceList = createVisiblePreferencesList(
                mPreferenceGroup);

        mVisiblePreferences = visiblePreferenceList;

        final PreferenceManager preferenceManager = mPreferenceGroup.getPreferenceManager();
        if (preferenceManager != null
                && preferenceManager.getPreferenceComparisonCallback() != null) {
            final PreferenceManager.PreferenceComparisonCallback comparisonCallback =
                    preferenceManager.getPreferenceComparisonCallback();
            final DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldVisibleList.size();
                }

                @Override
                public int getNewListSize() {
                    return visiblePreferenceList.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return comparisonCallback.arePreferenceItemsTheSame(
                            oldVisibleList.get(oldItemPosition),
                            visiblePreferenceList.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return comparisonCallback.arePreferenceContentsTheSame(
                            oldVisibleList.get(oldItemPosition),
                            visiblePreferenceList.get(newItemPosition));
                }
            });

            result.dispatchUpdatesTo(this);
        } else {
            notifyDataSetChanged();
        }

        for (final Preference preference : mPreferences) {
            preference.clearWasDetached();
        }
    }

    private void flattenPreferenceGroup(List<Preference> preferences, PreferenceGroup group) {
        group.sortPreferences();
        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final Preference preference = group.getPreference(i);

            preferences.add(preference);

            final PreferenceResourceDescriptor descriptor = new PreferenceResourceDescriptor(
                    preference);
            if (!mPreferenceResourceDescriptors.contains(descriptor)) {
                mPreferenceResourceDescriptors.add(descriptor);
            }

            if (preference instanceof PreferenceGroup) {
                final PreferenceGroup nestedGroup = (PreferenceGroup) preference;
                if (nestedGroup.isOnSameScreenAsChildren()) {
                    flattenPreferenceGroup(preferences, nestedGroup);
                }
            }

            preference.setOnPreferenceChangeInternalListener(this);
        }
    }

    private List<Preference> createVisiblePreferencesList(PreferenceGroup group) {
        int visiblePreferenceCount = 0;
        final List<Preference> visiblePreferences = new ArrayList<>();
        final List<Preference> collapsedPreferences = new ArrayList<>();

        final int groupSize = group.getPreferenceCount();
        for (int i = 0; i < groupSize; i++) {
            final Preference preference = group.getPreference(i);

            if (!preference.isVisible()) {
                continue;
            }

            if (!isGroupExpandable(group)
                    || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                visiblePreferences.add(preference);
            } else {
                collapsedPreferences.add(preference);
            }

            // PreferenceGroups do not count towards the maximal number of preferences to show
            if (!(preference instanceof PreferenceGroup)) {
                visiblePreferenceCount++;
                continue;
            }

            PreferenceGroup innerGroup = (PreferenceGroup) preference;
            if (!innerGroup.isOnSameScreenAsChildren()) {
                continue;
            }

            if (isGroupExpandable(group) && isGroupExpandable(innerGroup)) {
                throw new IllegalStateException(
                        "Nesting an expandable group inside of another expandable group is not "
                                + "supported!");
            }

            // Recursively generate nested list of visible preferences
            final List<Preference> innerList = createVisiblePreferencesList(innerGroup);

            for (Preference inner : innerList) {
                if (!isGroupExpandable(group)
                        || visiblePreferenceCount < group.getInitialExpandedChildrenCount()) {
                    visiblePreferences.add(inner);
                } else {
                    collapsedPreferences.add(inner);
                }
                visiblePreferenceCount++;
            }
        }

        // If there are any visible preferences being hidden, add an expand button to show the rest
        // of the preferences. Clicking the expand button will show all the visible preferences.
        if (isGroupExpandable(group)
                && visiblePreferenceCount > group.getInitialExpandedChildrenCount()) {
            final ExpandButton expandButton = createExpandButton(group, collapsedPreferences);
            visiblePreferences.add(expandButton);
        }
        return visiblePreferences;
    }

    private ExpandButton createExpandButton(final PreferenceGroup group,
                                            List<Preference> collapsedPreferences) {
        final ExpandButton preference = new ExpandButton(
                group.getContext(),
                collapsedPreferences,
                group.getId()
        );
        preference.setOnPreferenceClickListener(preference1 -> {
            group.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
            onPreferenceHierarchyChange(preference1);
            final PreferenceGroup.OnExpandButtonClickListener listener =
                    group.getOnExpandButtonClickListener();
            if (listener != null) {
                listener.onExpandButtonClick();
            }
            return true;
        });
        return preference;
    }

    private boolean isGroupExpandable(PreferenceGroup preferenceGroup) {
        return preferenceGroup.getInitialExpandedChildrenCount() != Integer.MAX_VALUE;
    }

    public Preference getItem(int position) {
        if (position < 0 || position >= getItemCount()) return null;
        return mVisiblePreferences.get(position);
    }

    @Override
    public int getItemCount() {
        return mVisiblePreferences.size();
    }

    @Override
    public long getItemId(int position) {
        if (!hasStableIds()) {
            return RecyclerView.NO_ID;
        }
        return this.getItem(position).getId();
    }

    @Override
    public void onPreferenceChange(Preference preference) {
        final int index = mVisiblePreferences.indexOf(preference);
        // If we don't find the preference, we don't need to notify anyone
        if (index != -1) {
            // Send the preference as a placeholder to ensure the view holder is recycled in place
            notifyItemChanged(index, preference);
        }
    }

    @Override
    public void onPreferenceHierarchyChange(Preference preference) {
        mHandler.removeCallbacks(mSyncRunnable);
        mHandler.post(mSyncRunnable);
    }

    @Override
    public void onPreferenceVisibilityChange(Preference preference) {
        onPreferenceHierarchyChange(preference);
    }

    @Override
    public int getItemViewType(int position) {
        final Preference preference = this.getItem(position);

        PreferenceResourceDescriptor descriptor = new PreferenceResourceDescriptor(preference);

        int viewType = mPreferenceResourceDescriptors.indexOf(descriptor);
        if (viewType == -1) {
            viewType = mPreferenceResourceDescriptors.size();
            mPreferenceResourceDescriptors.add(descriptor);
        }
        return viewType;
    }

    @Override
    @NonNull
    public PreferenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final PreferenceResourceDescriptor descriptor = mPreferenceResourceDescriptors.get(
                viewType);
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        TypedArray a
                = parent.getContext().obtainStyledAttributes(null, R.styleable.BackgroundStyle);
        Drawable background
                = a.getDrawable(R.styleable.BackgroundStyle_android_selectableItemBackground);
        if (background == null) {
            background = AppCompatResources.getDrawable(parent.getContext(),
                    android.R.drawable.list_selector_background);
        }
        a.recycle();

        final View view = inflater.inflate(descriptor.mLayoutResId, parent, false);
        if (view.getBackground() == null) {
            ViewCompat.setBackground(view, background);
        }

        final ViewGroup widgetFrame = view.findViewById(android.R.id.widget_frame);
        if (widgetFrame != null) {
            if (descriptor.mWidgetLayoutResId != 0) {
                inflater.inflate(descriptor.mWidgetLayoutResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }

        return new PreferenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
        final Preference preference = getItem(position);
        preference.onBindViewHolder(holder);
    }

    @Override
    public int getPreferenceAdapterPosition(String key) {
        final int size = mVisiblePreferences.size();
        for (int i = 0; i < size; i++) {
            final Preference candidate = mVisiblePreferences.get(i);
            if (TextUtils.equals(key, candidate.getKey())) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getPreferenceAdapterPosition(Preference preference) {
        final int size = mVisiblePreferences.size();
        for (int i = 0; i < size; i++) {
            final Preference candidate = mVisiblePreferences.get(i);
            if (candidate != null && candidate.equals(preference)) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    private static class PreferenceResourceDescriptor {
        int mLayoutResId;
        int mWidgetLayoutResId;
        String mClassName;

        PreferenceResourceDescriptor(Preference preference) {
            mClassName = preference.getClass().getName();
            mLayoutResId = preference.getLayoutResource();
            mWidgetLayoutResId = preference.getWidgetLayoutResource();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PreferenceResourceDescriptor)) {
                return false;
            }
            final PreferenceResourceDescriptor other = (PreferenceResourceDescriptor) o;
            return mLayoutResId == other.mLayoutResId
                    && mWidgetLayoutResId == other.mWidgetLayoutResId
                    && TextUtils.equals(mClassName, other.mClassName);
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + mLayoutResId;
            result = 31 * result + mWidgetLayoutResId;
            result = 31 * result + mClassName.hashCode();
            return result;
        }
    }
}
