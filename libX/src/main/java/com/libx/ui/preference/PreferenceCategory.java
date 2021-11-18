package com.libx.ui.preference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.TypedArrayUtils;

import com.libx.ui.R;

public class PreferenceCategory extends PreferenceGroup {

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressLint("RestrictedApi")
    public PreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceCategoryStyle, android.R.attr.preferenceCategoryStyle));
    }

    public PreferenceCategory(Context context) {
        this(context, null);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean shouldDisableDependents() {
        return !super.isEnabled();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setAccessibilityHeading(true);

        TextView titleView = (TextView) holder.findViewById(android.R.id.title);

        if (getTitle() == null || TextUtils.isEmpty(getTitle())) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setVisibility(View.VISIBLE);
        }

        int count = getPreferenceCount();
        if (count == 1) {
            getPreference(0).setPosition(Position.SINGLE);
        } else if (count > 1) {
            getPreference(0).setPosition(Position.TOP);
            getPreference(count - 1).setPosition(Position.BOTTOM);
        }
    }
}
