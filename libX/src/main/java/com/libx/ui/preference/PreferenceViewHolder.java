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
import android.util.SparseArray;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.RestrictTo;
import androidx.preference.AndroidResources;
import androidx.recyclerview.widget.RecyclerView;

import com.libx.ui.R;

public class PreferenceViewHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> mCachedViews = new SparseArray<>(4);
    private boolean mDividerAllowedAbove;
    private boolean mDividerAllowedBelow;

    @SuppressLint("RestrictedApi")
    PreferenceViewHolder(View itemView) {
        super(itemView);

        // Pre-cache the views that we know in advance we'll want to find
        mCachedViews.put(android.R.id.title, itemView.findViewById(android.R.id.title));
        mCachedViews.put(android.R.id.summary, itemView.findViewById(android.R.id.summary));
        mCachedViews.put(android.R.id.icon, itemView.findViewById(android.R.id.icon));
        mCachedViews.put(R.id.icon_frame, itemView.findViewById(R.id.icon_frame));
        mCachedViews.put(AndroidResources.ANDROID_R_ICON_FRAME,
                itemView.findViewById(AndroidResources.ANDROID_R_ICON_FRAME));
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    public static PreferenceViewHolder createInstanceForTests(View itemView) {
        return new PreferenceViewHolder(itemView);
    }

    public View findViewById(@IdRes int id) {
        final View cachedView = mCachedViews.get(id);
        if (cachedView != null) {
            return cachedView;
        } else {
            final View v = itemView.findViewById(id);
            if (v != null) {
                mCachedViews.put(id, v);
            }
            return v;
        }
    }

    public boolean isDividerAllowedAbove() {
        return mDividerAllowedAbove;
    }

    public void setDividerAllowedAbove(boolean allowed) {
        mDividerAllowedAbove = allowed;
    }

    public boolean isDividerAllowedBelow() {
        return mDividerAllowedBelow;
    }

    public void setDividerAllowedBelow(boolean allowed) {
        mDividerAllowedBelow = allowed;
    }
}
