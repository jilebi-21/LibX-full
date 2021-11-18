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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.TypedArrayUtils;

import com.libx.ui.R;

@SuppressLint("RestrictedApi")
public abstract class DialogPreference extends Preference {

    private CharSequence mDialogTitle;
    private CharSequence mDialogMessage;
    private Drawable mDialogIcon;
    private CharSequence mPositiveButtonText;
    private CharSequence mNegativeButtonText;
    private int mDialogLayoutResId;

    public DialogPreference(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DialogPreference, defStyleAttr, defStyleRes);

        mDialogTitle = TypedArrayUtils.getString(a, R.styleable.DialogPreference_dialogTitle,
                R.styleable.DialogPreference_android_dialogTitle);
        if (mDialogTitle == null) {
            // Fall back on the regular title of the preference
            // (the one that is seen in the list)
            mDialogTitle = getTitle();
        }

        mDialogMessage = TypedArrayUtils.getString(a, R.styleable.DialogPreference_dialogMessage,
                R.styleable.DialogPreference_android_dialogMessage);

        mDialogIcon = TypedArrayUtils.getDrawable(a, R.styleable.DialogPreference_dialogIcon,
                R.styleable.DialogPreference_android_dialogIcon);

        mPositiveButtonText = TypedArrayUtils.getString(a,
                R.styleable.DialogPreference_positiveButtonText,
                R.styleable.DialogPreference_android_positiveButtonText);

        mNegativeButtonText = TypedArrayUtils.getString(a,
                R.styleable.DialogPreference_negativeButtonText,
                R.styleable.DialogPreference_android_negativeButtonText);

        mDialogLayoutResId = TypedArrayUtils.getResourceId(a,
                R.styleable.DialogPreference_dialogLayout,
                R.styleable.DialogPreference_android_dialogLayout, 0);

        a.recycle();
    }

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.dialogPreferenceStyle,
                android.R.attr.dialogPreferenceStyle));
    }

    public DialogPreference(Context context) {
        this(context, null);
    }

    public CharSequence getDialogTitle() {
        return mDialogTitle;
    }

    public void setDialogTitle(CharSequence dialogTitle) {
        mDialogTitle = dialogTitle;
    }

    public void setDialogTitle(int dialogTitleResId) {
        setDialogTitle(getContext().getString(dialogTitleResId));
    }

    public CharSequence getDialogMessage() {
        return mDialogMessage;
    }

    public void setDialogMessage(CharSequence dialogMessage) {
        mDialogMessage = dialogMessage;
    }

    public void setDialogMessage(int dialogMessageResId) {
        setDialogMessage(getContext().getString(dialogMessageResId));
    }

    public Drawable getDialogIcon() {
        return mDialogIcon;
    }

    public void setDialogIcon(Drawable dialogIcon) {
        mDialogIcon = dialogIcon;
    }

    public void setDialogIcon(int dialogIconRes) {
        mDialogIcon = AppCompatResources.getDrawable(getContext(), dialogIconRes);
    }

    public CharSequence getPositiveButtonText() {
        return mPositiveButtonText;
    }

    public void setPositiveButtonText(CharSequence positiveButtonText) {
        mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(int positiveButtonTextResId) {
        setPositiveButtonText(getContext().getString(positiveButtonTextResId));
    }

    public CharSequence getNegativeButtonText() {
        return mNegativeButtonText;
    }

    public void setNegativeButtonText(CharSequence negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(int negativeButtonTextResId) {
        setNegativeButtonText(getContext().getString(negativeButtonTextResId));
    }

    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        mDialogLayoutResId = dialogLayoutResId;
    }

    @Override
    protected void onClick() {
        getPreferenceManager().showDialog(this);
    }

    public interface TargetFragment {
        @SuppressWarnings("TypeParameterUnusedInFormals")
        @Nullable
        <T extends Preference> T findPreference(@NonNull CharSequence key);
    }
}
