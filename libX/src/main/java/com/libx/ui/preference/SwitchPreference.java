/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;

import androidx.annotation.RestrictTo;
import androidx.core.content.res.TypedArrayUtils;

import com.libx.ui.R;
import com.libx.ui.views.ToggleSwitch;

@SuppressLint("RestrictedApi")
public class SwitchPreference extends TwoStatePreference {
    private final Listener mListener = new Listener();

    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SwitchPreference, defStyleAttr, defStyleRes);

        setSummaryOn(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOn,
                R.styleable.SwitchPreference_android_summaryOn));

        setSummaryOff(TypedArrayUtils.getString(a, R.styleable.SwitchPreference_summaryOff,
                R.styleable.SwitchPreference_android_summaryOff));

        setSwitchTextOn(TypedArrayUtils.getString(a,
                R.styleable.SwitchPreference_switchTextOn,
                R.styleable.SwitchPreference_android_switchTextOn));

        setSwitchTextOff(TypedArrayUtils.getString(a,
                R.styleable.SwitchPreference_switchTextOff,
                R.styleable.SwitchPreference_android_switchTextOff));

        setDisableDependentsState(TypedArrayUtils.getBoolean(a,
                R.styleable.SwitchPreference_disableDependentsState,
                R.styleable.SwitchPreference_android_disableDependentsState, false));

        a.recycle();
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                R.attr.switchPreferenceStyle,
                R.attr.switchPreferenceStyle));
    }

    public SwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ToggleSwitch switchView = (ToggleSwitch) holder.findViewById(android.R.id.switch_widget);
        View switchDivider = holder.findViewById(R.id.switch_divider);
        syncSwitchView(switchView);
        syncSummaryView(holder);

        boolean hasDest = hasDestination();
        switchView.setClickable(hasDest);
        if (hasDest) {
            switchDivider.setVisibility(View.VISIBLE);
            switchView.addOnCheckedChangeListener((buttonView, isChecked) -> {
            });
        } else switchDivider.setVisibility(View.GONE);
    }

    boolean hasDestination() {
        return (getIntent() != null) || (getFragment() != null);
    }

    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(getContext().getString(resId));
    }

    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }

    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(getContext().getString(resId));
    }

    @Override
    protected void onClick() {
        if (!hasDestination()) {
            super.onClick();
        }
    }

    @RestrictTo(LIBRARY)
    @Override
    protected void performClick(View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasDestination()) {
            setChecked(getPersistedBoolean(false));
        }
    }

    private void syncViewIfAccessibilityEnabled(View view) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (!accessibilityManager.isEnabled()) {
            return;
        }

        View switchView = view.findViewById(android.R.id.switch_widget);
        syncSwitchView(switchView);

        View summaryView = view.findViewById(android.R.id.summary);
        syncSummaryView(summaryView);
    }

    private void syncSwitchView(View view) {
        if (view instanceof ToggleSwitch) {
            ((ToggleSwitch) view).removeAllListeners();
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }
        if (view instanceof ToggleSwitch) {
            final ToggleSwitch switchView = (ToggleSwitch) view;
            switchView.setTextOn(mSwitchOn);
            switchView.setTextOff(mSwitchOff);
            switchView.addOnCheckedChangeListener(mListener);
        }
    }

    private class Listener implements CompoundButton.OnCheckedChangeListener {
        Listener() {
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.setChecked(!isChecked);
                return;
            }

            SwitchPreference.this.setChecked(isChecked);
        }
    }
}
