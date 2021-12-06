/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.libx.ui.views;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.libx.ui.R;

import java.util.ArrayList;
import java.util.List;

public class SwitchBar extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

    public interface OnSwitchChangeListener {
        void onSwitchChanged(SwitchCompat switchView, boolean isChecked);
    }

    private final List<OnSwitchChangeListener> mSwitchChangeListeners = new ArrayList<>();

    private final SwitchCompat mSwitchView;
    private final TextView mLabelView;
    private String mLabel;
    private String mOnText;
    private String mOffText;

    public SwitchBar(Context context) {
        this(context, null);
    }

    public SwitchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.switch_bar, this);

        mLabelView = findViewById(R.id.switchbar_text);
        mSwitchView = findViewById(R.id.switchbar_widget);

        // Prevent onSaveInstanceState() to be called as we are managing the state of the Switch on our own
        mSwitchView.setSaveEnabled(false);
        mSwitchView.setFocusable(false);
        mSwitchView.setClickable(false);

        setSwitchBarText(R.string.switch_text_on, R.string.switch_text_off);

        addOnSwitchChangeListener((switchView, isChecked) -> setTextViewLabel(isChecked));

        setFocusable(true);
        setClickable(true);
        setVisibility(View.GONE);
    }

    @Override
    public boolean performClick() {
        return getDelegatingView().performClick();
    }

    public void setTextViewLabel(boolean isChecked) {
        mLabel = isChecked ? mOnText : mOffText;
        updateText();
    }

    public void setSwitchBarText(int onTextId, int offTextId) {
        mOnText = getResources().getString(onTextId);
        mOffText = getResources().getString(offTextId);
        setTextViewLabel(isChecked());
    }

    public void setSwitchBarText(String onText, String offText) {
        mOnText = onText;
        mOffText = offText;
        setTextViewLabel(isChecked());
    }

    private void updateText() {
        mLabelView.setText(mLabel);
    }

    public void setChecked(boolean checked) {
        mSwitchView.setChecked(checked);
        setSelected(checked);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setTextViewLabel(selected);
    }

    public boolean isChecked() {
        return mSwitchView.isChecked();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mLabelView.setEnabled(enabled);
        mSwitchView.setEnabled(enabled);
    }

    View getDelegatingView() {
        return mSwitchView;
    }

    public final SwitchCompat getSwitch() {
        return mSwitchView;
    }

    public void show() {
        if (!isShowing()) {
            setVisibility(View.VISIBLE);
            mSwitchView.setOnCheckedChangeListener(this);
        }
    }

    public void hide() {
        if (isShowing()) {
            setVisibility(View.GONE);
            mSwitchView.setOnCheckedChangeListener(null);
        }
    }

    public boolean isShowing() {
        return (getVisibility() == View.VISIBLE);
    }

    public void propagateChecked(boolean isChecked) {
        final int count = mSwitchChangeListeners.size();
        for (int n = 0; n < count; n++) {
            mSwitchChangeListeners.get(n).onSwitchChanged(mSwitchView, isChecked);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        propagateChecked(isChecked);
        setSelected(isChecked);
    }

    public void addOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot add twice the same OnSwitchChangeListener");
        }
        mSwitchChangeListeners.add(listener);
    }

    public void removeOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (!mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot remove OnSwitchChangeListener");
        }
        mSwitchChangeListeners.remove(listener);
    }

    static class SavedState extends BaseSavedState {
        boolean checked;
        boolean visible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(null);
            visible = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
            out.writeValue(visible);
        }

        @NonNull
        @Override
        public String toString() {
            return "SwitchBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + checked
                    + " visible=" + visible + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);
        savedState.checked = mSwitchView.isChecked();
        savedState.visible = isShowing();
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;

        super.onRestoreInstanceState(savedState.getSuperState());

        mSwitchView.setChecked(savedState.checked);
        setTextViewLabel(savedState.checked);
        setVisibility(savedState.visible ? View.VISIBLE : View.GONE);
        mSwitchView.setOnCheckedChangeListener(savedState.visible ? this : null);

        requestLayout();
    }
}