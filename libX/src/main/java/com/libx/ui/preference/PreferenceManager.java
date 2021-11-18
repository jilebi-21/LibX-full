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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class PreferenceManager {

    public static final String KEY_HAS_SET_DEFAULT_VALUES = "_has_set_default_values";
    private static final int STORAGE_DEFAULT = 0;
    private static final int STORAGE_DEVICE_PROTECTED = 1;

    private Context mContext;
    private long mNextId = 0;

    @Nullable
    private SharedPreferences mSharedPreferences;

    @Nullable
    private PreferenceDataStore mPreferenceDataStore;

    @Nullable
    private SharedPreferences.Editor mEditor;

    private boolean mNoCommit;
    private String mSharedPreferencesName;
    private int mSharedPreferencesMode;
    private int mStorage = STORAGE_DEFAULT;

    private PreferenceScreen mPreferenceScreen;

    private PreferenceComparisonCallback mPreferenceComparisonCallback;
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private OnDisplayPreferenceDialogListener mOnDisplayPreferenceDialogListener;
    private OnNavigateToScreenListener mOnNavigateToScreenListener;

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public PreferenceManager(Context context) {
        mContext = context;

        setSharedPreferencesName(getDefaultSharedPreferencesName(context));
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context),
                getDefaultSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        return context.getPackageName() + "_preferences";
    }

    private static int getDefaultSharedPreferencesMode() {
        return Context.MODE_PRIVATE;
    }

    public static void setDefaultValues(Context context, int resId, boolean readAgain) {
        // Use the default shared preferences name and mode
        setDefaultValues(context, getDefaultSharedPreferencesName(context),
                getDefaultSharedPreferencesMode(), resId, readAgain);
    }

    public static void setDefaultValues(Context context, String sharedPreferencesName,
                                        int sharedPreferencesMode, int resId, boolean readAgain) {
        final SharedPreferences defaultValueSp = context.getSharedPreferences(
                KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);

        if (readAgain || !defaultValueSp.getBoolean(KEY_HAS_SET_DEFAULT_VALUES, false)) {
            final PreferenceManager pm = new PreferenceManager(context);
            pm.setSharedPreferencesName(sharedPreferencesName);
            pm.setSharedPreferencesMode(sharedPreferencesMode);
            pm.inflateFromResource(context, resId, null);

            defaultValueSp.edit()
                    .putBoolean(KEY_HAS_SET_DEFAULT_VALUES, true)
                    .apply();
        }
    }

    @RestrictTo(LIBRARY_GROUP_PREFIX)
    public PreferenceScreen inflateFromResource(Context context, int resId,
                                                PreferenceScreen rootPreferences) {
        // Block commits
        setNoCommit(true);

        final PreferenceInflater inflater = new PreferenceInflater(context, this);
        rootPreferences = (PreferenceScreen) inflater.inflate(resId, rootPreferences);
        rootPreferences.onAttachedToHierarchy(this);

        // Unblock commits
        setNoCommit(false);

        return rootPreferences;
    }

    public PreferenceScreen createPreferenceScreen(Context context) {
        final PreferenceScreen preferenceScreen = new PreferenceScreen(context, null);
        preferenceScreen.onAttachedToHierarchy(this);
        return preferenceScreen;
    }

    long getNextId() {
        synchronized (this) {
            return mNextId++;
        }
    }

    public String getSharedPreferencesName() {
        return mSharedPreferencesName;
    }

    public void setSharedPreferencesName(String sharedPreferencesName) {
        mSharedPreferencesName = sharedPreferencesName;
        mSharedPreferences = null;
    }

    public int getSharedPreferencesMode() {
        return mSharedPreferencesMode;
    }

    public void setSharedPreferencesMode(int sharedPreferencesMode) {
        mSharedPreferencesMode = sharedPreferencesMode;
        mSharedPreferences = null;
    }

    public void setStorageDefault() {
        mStorage = STORAGE_DEFAULT;
        mSharedPreferences = null;
    }

    public void setStorageDeviceProtected() {
        mStorage = STORAGE_DEVICE_PROTECTED;
        mSharedPreferences = null;
    }

    public boolean isStorageDefault() {
        return mStorage == STORAGE_DEFAULT;
    }

    public boolean isStorageDeviceProtected() {
        return mStorage == STORAGE_DEVICE_PROTECTED;
    }

    @Nullable
    public PreferenceDataStore getPreferenceDataStore() {
        return mPreferenceDataStore;
    }

    public void setPreferenceDataStore(PreferenceDataStore dataStore) {
        mPreferenceDataStore = dataStore;
    }

    public SharedPreferences getSharedPreferences() {
        if (getPreferenceDataStore() != null) {
            return null;
        }

        if (mSharedPreferences == null) {
            final Context storageContext;
            if (mStorage == STORAGE_DEVICE_PROTECTED) {
                storageContext = ContextCompat.createDeviceProtectedStorageContext(mContext);
            } else {
                storageContext = mContext;
            }

            mSharedPreferences = storageContext.getSharedPreferences(mSharedPreferencesName,
                    mSharedPreferencesMode);
        }

        return mSharedPreferences;
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceScreen;
    }

    public boolean setPreferences(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != mPreferenceScreen) {
            if (mPreferenceScreen != null) {
                mPreferenceScreen.onDetached();
            }
            mPreferenceScreen = preferenceScreen;
            return true;
        }

        return false;
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    @Nullable
    public <T extends Preference> T findPreference(@NonNull CharSequence key) {
        if (mPreferenceScreen == null) {
            return null;
        }

        return mPreferenceScreen.findPreference(key);
    }

    SharedPreferences.Editor getEditor() {
        if (mPreferenceDataStore != null) {
            return null;
        }

        if (mNoCommit) {
            if (mEditor == null) {
                mEditor = getSharedPreferences().edit();
            }

            return mEditor;
        } else {
            return getSharedPreferences().edit();
        }
    }

    boolean shouldCommit() {
        return !mNoCommit;
    }

    private void setNoCommit(boolean noCommit) {
        if (!noCommit && mEditor != null) {
            mEditor.apply();
        }
        mNoCommit = noCommit;
    }

    public Context getContext() {
        return mContext;
    }

    public PreferenceComparisonCallback getPreferenceComparisonCallback() {
        return mPreferenceComparisonCallback;
    }

    public void setPreferenceComparisonCallback(
            PreferenceComparisonCallback preferenceComparisonCallback) {
        mPreferenceComparisonCallback = preferenceComparisonCallback;
    }

    public OnDisplayPreferenceDialogListener getOnDisplayPreferenceDialogListener() {
        return mOnDisplayPreferenceDialogListener;
    }

    public void setOnDisplayPreferenceDialogListener(
            OnDisplayPreferenceDialogListener onDisplayPreferenceDialogListener) {
        mOnDisplayPreferenceDialogListener = onDisplayPreferenceDialogListener;
    }

    public void showDialog(Preference preference) {
        if (mOnDisplayPreferenceDialogListener != null) {
            mOnDisplayPreferenceDialogListener.onDisplayPreferenceDialog(preference);
        }
    }

    public OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return mOnPreferenceTreeClickListener;
    }

    public void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener listener) {
        mOnPreferenceTreeClickListener = listener;
    }

    public OnNavigateToScreenListener getOnNavigateToScreenListener() {
        return mOnNavigateToScreenListener;
    }

    public void setOnNavigateToScreenListener(OnNavigateToScreenListener listener) {
        mOnNavigateToScreenListener = listener;
    }

    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(Preference preference);
    }

    public interface OnDisplayPreferenceDialogListener {
        void onDisplayPreferenceDialog(Preference preference);
    }

    public interface OnNavigateToScreenListener {
        void onNavigateToScreen(PreferenceScreen preferenceScreen);
    }

    public static abstract class PreferenceComparisonCallback {
        public abstract boolean arePreferenceItemsTheSame(Preference p1, Preference p2);
        public abstract boolean arePreferenceContentsTheSame(Preference p1, Preference p2);
    }

    public static class SimplePreferenceComparisonCallback extends PreferenceComparisonCallback {
        @Override
        public boolean arePreferenceItemsTheSame(Preference p1, Preference p2) {
            return p1.getId() == p2.getId();
        }

        @Override
        public boolean arePreferenceContentsTheSame(Preference p1, Preference p2) {
            if (p1.getClass() != p2.getClass()) {
                return false;
            }
            if (p1 == p2 && p1.wasDetached()) {
                // Defensively handle the case where a preference was removed, updated and re-added.
                // Hopefully this is rare.
                return false;
            }
            if (!TextUtils.equals(p1.getTitle(), p2.getTitle())) {
                return false;
            }
            if (!TextUtils.equals(p1.getSummary(), p2.getSummary())) {
                return false;
            }
            final Drawable p1Icon = p1.getIcon();
            final Drawable p2Icon = p2.getIcon();
            if (!Objects.equals(p1Icon, p2Icon)) {
                return false;
            }
            if (p1.isEnabled() != p2.isEnabled()) {
                return false;
            }
            if (p1.isSelectable() != p2.isSelectable()) {
                return false;
            }
            if (p1 instanceof TwoStatePreference) {
                if (((TwoStatePreference) p1).isChecked()
                        != ((TwoStatePreference) p2).isChecked()) {
                    return false;
                }
            }
            if (p1 instanceof DropDownPreference && p1 != p2) {
                // Different object, must re-bind spinner adapter
                return false;
            }

            return true;
        }
    }

}
