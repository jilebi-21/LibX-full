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

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

import com.libx.ui.R;
import com.libx.ui.preference.helpers.GlobalSettingsStore;
import com.libx.ui.preference.helpers.SecureSettingsStore;
import com.libx.ui.preference.helpers.SystemSettingsStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Preference implements Comparable<Preference> {

    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;

    private static final String CLIPBOARD_ID = "Preference";

    private final Context mContext;

    @Nullable
    private PreferenceManager mPreferenceManager;

    @Nullable
    private PreferenceDataStore mPreferenceDataStore;

    private long mId;

    private boolean mHasId;

    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;

    private int mOrder;
    private int mViewId = 0;
    private CharSequence mTitle;
    private CharSequence mSummary;
    private int mIconResId;
    private Drawable mIcon;
    private String mKey;
    private Intent mIntent;
    private String mFragment;
    private Bundle mExtras;
    private boolean mEnabled;
    private boolean mSelectable;
    private boolean mRequiresKey;
    private boolean mPersistent;
    private String mDependencyKey;
    private Object mDefaultValue;
    private boolean mDependencyMet = true;
    private boolean mParentDependencyMet = true;
    private final View.OnClickListener mClickListener = this::performClick;
    private boolean mVisible;
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;
    private boolean mHasSingleLineTitleAttr;
    private boolean mSingleLineTitle = true;
    private boolean mIconSpaceReserved;
    private boolean mCopyingEnabled;
    private boolean mShouldDisableView;
    private int mLayoutResId;
    private int mWidgetLayoutResId;
    private OnPreferenceChangeInternalListener mListener;
    private List<Preference> mDependents;
    private PreferenceGroup mParentGroup;
    private boolean mWasDetached;
    private boolean mBaseMethodCalled;
    private OnPreferenceCopyListener mOnCopyListener;
    private SummaryProvider mSummaryProvider;
    private Position mPosition = Position.MIDDLE;

    @SuppressLint("RestrictedApi")
    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.Preference, defStyleAttr, defStyleRes);

        mIconResId = TypedArrayUtils.getResourceId(a, R.styleable.Preference_icon,
                R.styleable.Preference_android_icon, 0);

        mKey = TypedArrayUtils.getString(a, R.styleable.Preference_key,
                R.styleable.Preference_android_key);

        mTitle = TypedArrayUtils.getText(a, R.styleable.Preference_title,
                R.styleable.Preference_android_title);

        mSummary = TypedArrayUtils.getText(a, R.styleable.Preference_summary,
                R.styleable.Preference_android_summary);

        mOrder = TypedArrayUtils.getInt(a, R.styleable.Preference_order,
                R.styleable.Preference_android_order, DEFAULT_ORDER);

        mFragment = TypedArrayUtils.getString(a, R.styleable.Preference_fragment,
                R.styleable.Preference_android_fragment);

        mLayoutResId = TypedArrayUtils.getResourceId(a, R.styleable.Preference_layout,
                R.styleable.Preference_android_layout, R.layout.preference_layout);

        mWidgetLayoutResId = TypedArrayUtils.getResourceId(a, R.styleable.Preference_widgetLayout,
                R.styleable.Preference_android_widgetLayout, 0);

        mEnabled = TypedArrayUtils.getBoolean(a, R.styleable.Preference_enabled,
                R.styleable.Preference_android_enabled, true);

        mSelectable = TypedArrayUtils.getBoolean(a, R.styleable.Preference_selectable,
                R.styleable.Preference_android_selectable, true);

        mPersistent = TypedArrayUtils.getBoolean(a, R.styleable.Preference_persistent,
                R.styleable.Preference_android_persistent, true);

        mDependencyKey = TypedArrayUtils.getString(a, R.styleable.Preference_dependency,
                R.styleable.Preference_android_dependency);

        mAllowDividerAbove = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerAbove,
                R.styleable.Preference_allowDividerAbove, mSelectable);

        mAllowDividerBelow = TypedArrayUtils.getBoolean(a, R.styleable.Preference_allowDividerBelow,
                R.styleable.Preference_allowDividerBelow, mSelectable);

        if (a.hasValue(R.styleable.Preference_defaultValue)) {
            mDefaultValue = onGetDefaultValue(a, R.styleable.Preference_defaultValue);
        } else if (a.hasValue(R.styleable.Preference_android_defaultValue)) {
            mDefaultValue = onGetDefaultValue(a, R.styleable.Preference_android_defaultValue);
        }

        mShouldDisableView = TypedArrayUtils.getBoolean(a, R.styleable.Preference_shouldDisableView,
                        R.styleable.Preference_android_shouldDisableView, true);

        mHasSingleLineTitleAttr = a.hasValue(R.styleable.Preference_singleLineTitle);
        if (mHasSingleLineTitleAttr) {
            mSingleLineTitle = TypedArrayUtils.getBoolean(a, R.styleable.Preference_singleLineTitle,
                    R.styleable.Preference_android_singleLineTitle, true);
        }

        mIconSpaceReserved = TypedArrayUtils.getBoolean(a, R.styleable.Preference_iconSpaceReserved,
                R.styleable.Preference_android_iconSpaceReserved, false);

        mVisible = TypedArrayUtils.getBoolean(a, R.styleable.Preference_isPreferenceVisible,
                R.styleable.Preference_isPreferenceVisible, true);

        mCopyingEnabled = TypedArrayUtils.getBoolean(a, R.styleable.Preference_enableCopying,
                R.styleable.Preference_enableCopying, false);

        int index = TypedArrayUtils.getInt(a, R.styleable.Preference_datastore,
                R.styleable.Preference_datastore, 3);
        DataStore store = DataStore.values()[index];
        switch(store){
            case SYSTEM: mPreferenceDataStore = new SystemSettingsStore(context.getContentResolver());
                break;
            case SECURE: mPreferenceDataStore = new SecureSettingsStore(context.getContentResolver());
                break;
            case GLOBAL: mPreferenceDataStore = new GlobalSettingsStore(context.getContentResolver());
                break;
            default: mPreferenceDataStore = null;
        }

        a.recycle();
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle,
                android.R.attr.preferenceStyle));
    }

    public Preference(Context context) {
        this(context, null);
    }

    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    public Intent getIntent() {
        return mIntent;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public String getFragment() {
        return mFragment;
    }

    public void setFragment(String fragment) {
        mFragment = fragment;
    }

    @Nullable
    public PreferenceDataStore getPreferenceDataStore() {
        if (mPreferenceDataStore != null) {
            return mPreferenceDataStore;
        } else if (mPreferenceManager != null) {
            return mPreferenceManager.getPreferenceDataStore();
        }

        return null;
    }

    public void setPreferenceDataStore(PreferenceDataStore dataStore) {
        mPreferenceDataStore = dataStore;
    }

    public Bundle getExtras() {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        return mExtras;
    }

    public Bundle peekExtras() {
        return mExtras;
    }

    public final int getLayoutResource() {
        return mLayoutResId;
    }

    public void setLayoutResource(int layoutResId) {
        mLayoutResId = layoutResId;
    }

    public final int getWidgetLayoutResource() {
        return mWidgetLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        mWidgetLayoutResId = widgetLayoutResId;
    }

    public Position getPosition() {
        return mPosition;
    }

    public void setPosition(Position position) {
        mPosition = position;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        View itemView = holder.itemView;
        Integer summaryTextColor = null;

        itemView.setOnClickListener(mClickListener);
        itemView.setId(mViewId);

        final TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (summaryView != null) {
            final CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(View.VISIBLE);
                summaryTextColor = summaryView.getCurrentTextColor();
            } else {
                summaryView.setVisibility(View.GONE);
            }
        }

        final TextView titleView = (TextView) holder.findViewById(android.R.id.title);
        if (titleView != null) {
            final CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(View.VISIBLE);
                if (mHasSingleLineTitleAttr) {
                    titleView.setSingleLine(mSingleLineTitle);
                }
                // If this Preference is not selectable, but still enabled, we should set the
                // title text colour to the same colour used for the summary text
                if (!isSelectable() && isEnabled() && summaryTextColor != null) {
                    titleView.setTextColor(summaryTextColor);
                }
            } else {
                titleView.setVisibility(View.GONE);
            }
        }

        final ImageView imageView = (ImageView) holder.findViewById(android.R.id.icon);
        if (imageView != null) {
            if (mIconResId != 0 || mIcon != null) {
                if (mIcon == null) {
                    mIcon = AppCompatResources.getDrawable(mContext, mIconResId);
                }
                if (mIcon != null) {
                    imageView.setImageDrawable(mIcon);
                }
            }
            if (mIcon != null) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
            }
        }

        View imageFrame = holder.findViewById(R.id.icon_frame);
        if (imageFrame == null) {
            imageFrame = holder.findViewById(android.R.id.icon_frame);
        }
        if (imageFrame != null) {
            if (mIcon != null) {
                imageFrame.setVisibility(View.VISIBLE);
            } else {
                imageFrame.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
            }
        }

        if (mShouldDisableView) {
            setEnabledStateOnViews(itemView, isEnabled());
        } else {
            setEnabledStateOnViews(itemView, true);
        }

        final boolean selectable = isSelectable();
        itemView.setFocusable(selectable);
        itemView.setClickable(selectable);

        holder.setDividerAllowedAbove(mAllowDividerAbove);
        holder.setDividerAllowedBelow(mAllowDividerBelow);

        final boolean copyingEnabled = isCopyingEnabled();

        if (copyingEnabled && mOnCopyListener == null) {
            mOnCopyListener = new OnPreferenceCopyListener(this);
        }
        itemView.setOnCreateContextMenuListener(copyingEnabled ? mOnCopyListener : null);
        itemView.setLongClickable(copyingEnabled);

        // Remove touch ripple if the view isn't selectable
        if (copyingEnabled && !selectable) {
            ViewCompat.setBackground(itemView, null);
        }

        //Background shape based on position
        if(!(this instanceof PreferenceGroup)) {
            switch ((mPosition != null) ? mPosition : Position.NONE) {
                case TOP:
                    itemView.setBackgroundResource(R.drawable.preference_decor_item_top);
                    break;
                case MIDDLE:
                    itemView.setBackgroundResource(R.drawable.preference_decor_item_middle);
                    break;
                case BOTTOM:
                    itemView.setBackgroundResource(R.drawable.preference_decor_item_bottom);
                    break;
                case SINGLE:
                    itemView.setBackgroundResource(R.drawable.preference_decor_item_single);
                    break;
                default:
                    itemView.setBackgroundColor(mContext.getResources().getColor(R.color.surface_color_variant, mContext.getTheme()));
                    break;
            }
        }
    }

    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);

        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    public int getOrder() {
        return mOrder;
    }

    public void setOrder(int order) {
        if (order != mOrder) {
            mOrder = order;

            // Reorder the list
            notifyHierarchyChanged();
        }
    }

    public void setViewId(int viewId) {
        mViewId = viewId;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        if ((title == null && mTitle != null) || (title != null && !title.equals(mTitle))) {
            mTitle = title;
            notifyChanged();
        }
    }

    public void setTitle(int titleResId) {
        setTitle(mContext.getString(titleResId));
    }

    public Drawable getIcon() {
        if (mIcon == null && mIconResId != 0) {
            mIcon = AppCompatResources.getDrawable(mContext, mIconResId);
        }
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        if (mIcon != icon) {
            mIcon = icon;
            mIconResId = 0;
            notifyChanged();
        }
    }

    public void setIcon(int iconResId) {
        setIcon(AppCompatResources.getDrawable(mContext, iconResId));
        mIconResId = iconResId;
    }

    @SuppressWarnings("unchecked")
    public CharSequence getSummary() {
        if (getSummaryProvider() != null) {
            return getSummaryProvider().provideSummary(this);
        }
        return mSummary;
    }

    public void setSummary(CharSequence summary) {
        if (getSummaryProvider() != null) {
            throw new IllegalStateException("Preference already has a SummaryProvider set.");
        }
        if (!TextUtils.equals(mSummary, summary)) {
            mSummary = summary;
            notifyChanged();
        }
    }

    public void setSummary(int summaryResId) {
        setSummary(mContext.getString(summaryResId));
    }

    public boolean isEnabled() {
        return mEnabled && mDependencyMet && mParentDependencyMet;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;

            // Enabled state can change dependent preferences' states, so notify
            notifyDependencyChange(shouldDisableDependents());

            notifyChanged();
        }
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelectable(boolean selectable) {
        if (mSelectable != selectable) {
            mSelectable = selectable;
            notifyChanged();
        }
    }

    public boolean getShouldDisableView() {
        return mShouldDisableView;
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        if (mShouldDisableView != shouldDisableView) {
            mShouldDisableView = shouldDisableView;
            notifyChanged();
        }
    }

    public final boolean isVisible() {
        return mVisible;
    }

    public final void setVisible(boolean visible) {
        if (mVisible != visible) {
            mVisible = visible;
            if (mListener != null) {
                mListener.onPreferenceVisibilityChange(this);
            }
        }
    }

    public final boolean isShown() {
        if (!isVisible()) {
            return false;
        }

        if (getPreferenceManager() == null) {
            // We are not attached to the hierarchy
            return false;
        }

        if (this == getPreferenceManager().getPreferenceScreen()) {
            // We are at the root preference, so this preference and its ancestors are visible
            return true;
        }

        PreferenceGroup parent = getParent();
        if (parent == null) {
            // We are not attached to the hierarchy
            return false;
        }

        return parent.isShown();
    }

    long getId() {
        return mId;
    }

    protected void onClick() {
    }

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;

        if (mRequiresKey && !hasKey()) {
            requireKey();
        }
    }

    void requireKey() {
        if (TextUtils.isEmpty(mKey)) {
            throw new IllegalStateException("Preference does not have a key assigned.");
        }

        mRequiresKey = true;
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(mKey);
    }

    public boolean isPersistent() {
        return mPersistent;
    }

    public void setPersistent(boolean persistent) {
        mPersistent = persistent;
    }

    protected boolean shouldPersist() {
        return mPreferenceManager != null && isPersistent() && hasKey();
    }

    public boolean isSingleLineTitle() {
        return mSingleLineTitle;
    }

    public void setSingleLineTitle(boolean singleLineTitle) {
        mHasSingleLineTitleAttr = true;
        mSingleLineTitle = singleLineTitle;
    }

    public boolean isIconSpaceReserved() {
        return mIconSpaceReserved;
    }

    public void setIconSpaceReserved(boolean iconSpaceReserved) {
        if (mIconSpaceReserved != iconSpaceReserved) {
            mIconSpaceReserved = iconSpaceReserved;
            notifyChanged();
        }
    }

    public boolean isCopyingEnabled() {
        return mCopyingEnabled;
    }

    public void setCopyingEnabled(boolean enabled) {
        if (mCopyingEnabled != enabled) {
            mCopyingEnabled = enabled;
            notifyChanged();
        }
    }

    @Nullable
    public final SummaryProvider getSummaryProvider() {
        return mSummaryProvider;
    }

    public final void setSummaryProvider(@Nullable SummaryProvider summaryProvider) {
        mSummaryProvider = summaryProvider;
        notifyChanged();
    }

    public boolean callChangeListener(Object newValue) {
        return mOnChangeListener == null || mOnChangeListener.onPreferenceChange(this, newValue);
    }

    public OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return mOnChangeListener;
    }

    public void setOnPreferenceChangeListener(
            OnPreferenceChangeListener onPreferenceChangeListener) {
        mOnChangeListener = onPreferenceChangeListener;
    }

    public OnPreferenceClickListener getOnPreferenceClickListener() {
        return mOnClickListener;
    }

    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        mOnClickListener = onPreferenceClickListener;
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected void performClick(View view) {
        performClick();
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public void performClick() {

        if (!isEnabled() || !isSelectable()) {
            return;
        }

        onClick();

        if (mOnClickListener != null && mOnClickListener.onPreferenceClick(this)) {
            return;
        }

        PreferenceManager preferenceManager = getPreferenceManager();
        if (preferenceManager != null) {
            PreferenceManager.OnPreferenceTreeClickListener listener = preferenceManager
                    .getOnPreferenceTreeClickListener();
            if (listener != null && listener.onPreferenceTreeClick(this)) {
                return;
            }
        }

        if (mIntent != null) {
            Context context = getContext();
            context.startActivity(mIntent);
        }
    }

    public Context getContext() {
        return mContext;
    }

    public SharedPreferences getSharedPreferences() {
        if (mPreferenceManager == null || getPreferenceDataStore() != null) {
            return null;
        }

        return mPreferenceManager.getSharedPreferences();
    }

    @Override
    public int compareTo(@NonNull Preference another) {
        if (mOrder != another.mOrder) {
            // Do order comparison
            return mOrder - another.mOrder;
        } else if (mTitle == another.mTitle) {
            // If titles are null or share same object comparison
            return 0;
        } else if (mTitle == null) {
            return 1;
        } else if (another.mTitle == null) {
            return -1;
        } else {
            // Do name comparison
            return mTitle.toString().compareToIgnoreCase(another.mTitle.toString());
        }
    }

    final void setOnPreferenceChangeInternalListener(OnPreferenceChangeInternalListener listener) {
        mListener = listener;
    }

    protected void notifyChanged() {
        if (mListener != null) {
            mListener.onPreferenceChange(this);
        }
    }

    protected void notifyHierarchyChanged() {
        if (mListener != null) {
            mListener.onPreferenceHierarchyChange(this);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    protected void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        mPreferenceManager = preferenceManager;

        if (!mHasId) {
            mId = preferenceManager.getNextId();
        }

        dispatchSetInitialValue();
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    protected void onAttachedToHierarchy(PreferenceManager preferenceManager, long id) {
        mId = id;
        mHasId = true;
        try {
            onAttachedToHierarchy(preferenceManager);
        } finally {
            mHasId = false;
        }
    }

    void assignParent(@Nullable PreferenceGroup parentGroup) {
        if (parentGroup != null && mParentGroup != null) {
            throw new IllegalStateException(
                    "This preference already has a parent. You must remove the existing parent "
                            + "before assigning a new one.");
        }
        mParentGroup = parentGroup;
    }

    public void onAttached() {
        // At this point, the hierarchy that this preference is in is connected
        // with all other preferences.
        registerDependency();
    }

    public void onDetached() {
        unregisterDependency();
        mWasDetached = true;
    }

    final boolean wasDetached() {
        return mWasDetached;
    }

    final void clearWasDetached() {
        mWasDetached = false;
    }

    private void registerDependency() {

        if (TextUtils.isEmpty(mDependencyKey)) return;

        Preference preference = findPreferenceInHierarchy(mDependencyKey);
        if (preference != null) {
            preference.registerDependent(this);
        } else {
            throw new IllegalStateException("Dependency \"" + mDependencyKey
                    + "\" not found for preference \"" + mKey + "\" (title: \"" + mTitle + "\"");
        }
    }

    private void unregisterDependency() {
        if (mDependencyKey != null) {
            final Preference oldDependency = findPreferenceInHierarchy(mDependencyKey);
            if (oldDependency != null) {
                oldDependency.unregisterDependent(this);
            }
        }
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    @Nullable
    protected <T extends Preference> T findPreferenceInHierarchy(@NonNull String key) {
        if (mPreferenceManager == null) {
            return null;
        }

        return mPreferenceManager.findPreference(key);
    }

    private void registerDependent(Preference dependent) {
        if (mDependents == null) {
            mDependents = new ArrayList<>();
        }

        mDependents.add(dependent);

        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(Preference dependent) {
        if (mDependents != null) {
            mDependents.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        final List<Preference> dependents = mDependents;

        if (dependents == null) {
            return;
        }

        final int dependentsCount = dependents.size();
        for (int i = 0; i < dependentsCount; i++) {
            dependents.get(i).onDependencyChanged(this, disableDependents);
        }
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        if (mDependencyMet == disableDependent) {
            mDependencyMet = !disableDependent;

            // Enabled state can change dependent preferences' states, so notify
            notifyDependencyChange(shouldDisableDependents());

            notifyChanged();
        }
    }

    public void onParentChanged(Preference parent, boolean disableChild) {
        if (mParentDependencyMet == disableChild) {
            mParentDependencyMet = !disableChild;

            // Enabled state can change dependent preferences' states, so notify
            notifyDependencyChange(shouldDisableDependents());

            notifyChanged();
        }
    }

    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    public String getDependency() {
        return mDependencyKey;
    }

    public void setDependency(String dependencyKey) {
        // Unregister the old dependency, if we had one
        unregisterDependency();

        // Register the new
        mDependencyKey = dependencyKey;
        registerDependency();
    }

    @Nullable
    public PreferenceGroup getParent() {
        return mParentGroup;
    }

    protected void onPrepareForRemoval() {
        unregisterDependency();
    }

    public void setDefaultValue(Object defaultValue) {
        mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        if (getPreferenceDataStore() != null) {
            onSetInitialValue(true, mDefaultValue);
            return;
        }

        // By now, we know if we are persistent.
        final boolean shouldPersist = shouldPersist();
        if (!shouldPersist || !getSharedPreferences().contains(mKey)) {
            if (mDefaultValue != null) {
                onSetInitialValue(false, mDefaultValue);
            }
        } else {
            onSetInitialValue(true, null);
        }
    }

    @Deprecated
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        onSetInitialValue(defaultValue);
    }

    protected void onSetInitialValue(@Nullable Object defaultValue) {
    }

    private void tryCommit(@NonNull SharedPreferences.Editor editor) {
        if (mPreferenceManager.shouldCommit()) {
            editor.apply();
        }
    }

    protected boolean persistString(String value) {
        if (!shouldPersist()) {
            return false;
        }

        // Shouldn't store null
        if (TextUtils.equals(value, getPersistedString(null))) {
            // It's already there, so the same as persisting
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putString(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putString(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getString(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getString(mKey, defaultReturnValue);
    }

    public boolean persistStringSet(Set<String> values) {
        if (!shouldPersist()) {
            return false;
        }

        // Shouldn't store null
        if (values.equals(getPersistedStringSet(null))) {
            // It's already there, so the same as persisting
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putStringSet(mKey, values);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putStringSet(mKey, values);
            tryCommit(editor);
        }
        return true;
    }

    public Set<String> getPersistedStringSet(Set<String> defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getStringSet(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getStringSet(mKey, defaultReturnValue);
    }

    protected boolean persistInt(int value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedInt(~value)) {
            // It's already there, so the same as persisting
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putInt(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putInt(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected int getPersistedInt(int defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getInt(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getInt(mKey, defaultReturnValue);
    }

    protected boolean persistFloat(float value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedFloat(Float.NaN)) {
            // It's already there, so the same as persisting
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putFloat(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putFloat(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected float getPersistedFloat(float defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getFloat(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getFloat(mKey, defaultReturnValue);
    }

    protected boolean persistLong(long value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedLong(~value)) {
            // It's already there, so the same as persisting
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putLong(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putLong(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected long getPersistedLong(long defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getLong(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getLong(mKey, defaultReturnValue);
    }

    protected boolean persistBoolean(boolean value) {
        if (!shouldPersist()) {
            return false;
        }

        if (value == getPersistedBoolean(!value)) {
            // It's already there, so the same as persisting
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putBoolean(mKey, value);
        } else {
            SharedPreferences.Editor editor = mPreferenceManager.getEditor();
            editor.putBoolean(mKey, value);
            tryCommit(editor);
        }
        return true;
    }

    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getBoolean(mKey, defaultReturnValue);
        }

        return mPreferenceManager.getSharedPreferences().getBoolean(mKey, defaultReturnValue);
    }

    /**
     * invoked when the activity is paused or resumed
     */
    protected void onPause() {
    }

    protected void onResume() {
    }

    @Override
    public String toString() {
        return getFilterableStringBuilder().toString();
    }

    StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title).append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary).append(' ');
        }
        if (sb.length() > 0) {
            // Drop the last space
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }

    void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!mBaseMethodCalled) {
                throw new IllegalStateException(
                        "Derived class did not call super.onSaveInstanceState()");
            }
            if (state != null) {
                container.putParcelable(mKey, state);
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    void dispatchRestoreInstanceState(Bundle container) {
        if (hasKey()) {
            Parcelable state = container.getParcelable(mKey);
            if (state != null) {
                mBaseMethodCalled = false;
                onRestoreInstanceState(state);
                if (!mBaseMethodCalled) {
                    throw new IllegalStateException(
                            "Derived class did not call super.onRestoreInstanceState()");
                }
            }
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }

    @CallSuper
    @Deprecated
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {
    }

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object newValue);
    }

    public interface OnPreferenceClickListener {
        boolean onPreferenceClick(Preference preference);
    }

    interface OnPreferenceChangeInternalListener {
        void onPreferenceChange(Preference preference);
        void onPreferenceHierarchyChange(Preference preference);
        void onPreferenceVisibilityChange(Preference preference);
    }

    public interface SummaryProvider<T extends Preference> {

        CharSequence provideSummary(T preference);
    }

    public static class BaseSavedState extends AbsSavedState {
        public static final Creator<BaseSavedState> CREATOR =
                new Creator<BaseSavedState>() {
                    @Override
                    public BaseSavedState createFromParcel(Parcel in) {
                        return new BaseSavedState(in);
                    }

                    @Override
                    public BaseSavedState[] newArray(int size) {
                        return new BaseSavedState[size];
                    }
                };

        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }
    }

    private static class OnPreferenceCopyListener implements View.OnCreateContextMenuListener,
            MenuItem.OnMenuItemClickListener {

        private final Preference mPreference;

        OnPreferenceCopyListener(Preference preference) {
            mPreference = preference;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            CharSequence summary = mPreference.getSummary();
            if (!mPreference.isCopyingEnabled() || TextUtils.isEmpty(summary)) {
                return;
            }
            menu.setHeaderTitle(summary);
            menu.add(Menu.NONE, Menu.NONE, Menu.NONE, R.string.copy)
                    .setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            ClipboardManager clipboard =
                    (ClipboardManager) mPreference.getContext().getSystemService(
                            Context.CLIPBOARD_SERVICE);
            CharSequence summary = mPreference.getSummary();
            ClipData clip = ClipData.newPlainText(CLIPBOARD_ID, summary);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(mPreference.getContext(),
                    mPreference.getContext().getString(R.string.preference_copied,
                            summary),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    public enum DataStore {
        SYSTEM, SECURE, GLOBAL, NONE
    }

    protected enum Position {
        TOP, MIDDLE, BOTTOM, SINGLE, NONE
    }
}
