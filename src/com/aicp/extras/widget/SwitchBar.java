/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.aicp.extras.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.aicp.extras.R;

import androidx.annotation.ColorInt;
import com.android.settingslib.utils.BuildCompatUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * SwitchBar is a View with a customized Switch.
 * This component is used as the main switch of the page
 * to enable or disable the prefereces on the page.
 */

public class SwitchBar extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

    public interface OnSwitchChangeListener {
        /**
         * Called when the checked state of the Switch has changed.
         *
         * @param switchView The Switch view whose state has changed.
         * @param isChecked  The new checked state of switchView.
         */
        void onSwitchChanged(Switch switchView, boolean isChecked);
    }

    private final List<OnSwitchChangeListener> mSwitchChangeListeners = new ArrayList<>();
    @ColorInt
    private int mBackgroundColor;
    @ColorInt
    private int mBackgroundActivatedColor;
    protected TextView mTextView;
    protected Switch mSwitch;
    private Drawable mBackgroundOn;
    private Drawable mBackgroundOff;
    private Drawable mBackgroundDisabled;
    private View mFrameView;

    public SwitchBar(Context context) {
        this(context, null);
    }

    public SwitchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.settingslib_main_switch_bar, this);

        if (!BuildCompatUtils.isAtLeastS()) {
            final TypedArray a = context.obtainStyledAttributes(
                    new int[]{android.R.attr.colorAccent});
            mBackgroundActivatedColor = a.getColor(0, 0);
            mBackgroundColor = context.getColor(R.color.material_grey_600);
            a.recycle();
        }
        setFocusable(true);
        setClickable(true);
        mFrameView = findViewById(R.id.frame);
        mTextView = (TextView) findViewById(R.id.switch_text);
        mSwitch = (Switch) findViewById(android.R.id.switch_widget);
        if (BuildCompatUtils.isAtLeastS()) {
            mBackgroundOn = getContext().getDrawable(R.drawable.settingslib_switch_bar_bg_on);
            mBackgroundOff = getContext().getDrawable(R.drawable.settingslib_switch_bar_bg_off);
            mBackgroundDisabled = getContext().getDrawable(
                    R.drawable.settingslib_switch_bar_bg_disabled);
        }
        addOnSwitchChangeListener((switchView, isChecked) -> setChecked(isChecked));
        setChecked(mSwitch.isChecked());
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    androidx.preference.R.styleable.Preference, 0 /*defStyleAttr*/,
                    0 /*defStyleRes*/);
            final CharSequence title = a.getText(
                    androidx.preference.R.styleable.Preference_android_title);
            setTitle(title);
            a.recycle();
        }
        setBackground(true);
    }

    public void setBackground(boolean isChecked) {
        if (!BuildCompatUtils.isAtLeastS()) {
            setBackgroundColor(isChecked ? mBackgroundActivatedColor : mBackgroundColor);
        } else {
            mFrameView.setBackground(isChecked ? mBackgroundOn : mBackgroundOff);
        }
    }

    @Override
    public boolean performClick() {
        return mSwitch.performClick();
    }

    /**
     * Update the switch status
     */
    public void setChecked(boolean checked) {
        if (mSwitch != null) {
            mSwitch.setChecked(checked);
        }
        setBackground(checked);
    }

    /**
     * Return the status of the Switch
     */
    public boolean isChecked() {
        return mSwitch.isChecked();
    }

    /**
     * Enable or disable the text and switch.
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTextView.setEnabled(enabled);
        mSwitch.setEnabled(enabled);
        if (BuildCompatUtils.isAtLeastS()) {
            if (enabled) {
                mFrameView.setBackground(isChecked() ? mBackgroundOn : mBackgroundOff);
            } else {
                mFrameView.setBackground(mBackgroundDisabled);
            }
        }
    }

    /**
     * Return the Switch
     */
    public final Switch getSwitch() {
        return mSwitch;
    }

    /**
     * Set the title text
     */
    public void setTitle(CharSequence text) {
        if (mTextView != null) {
            mTextView.setText(text);
        }
    }

    /**
     * Show the SwitchBar
     */
    public void show() {
        if (!isShowing()) {
            setVisibility(View.VISIBLE);
            mSwitch.setOnCheckedChangeListener(this);
        }
    }

    /**
     * Hide the SwitchBar
     */
    public void hide() {
        if (isShowing()) {
            setVisibility(View.GONE);
            mSwitch.setOnCheckedChangeListener(null);
        }
    }

    /**
     * Return the displaying status of SwitchBar
     */
    public boolean isShowing() {
        return (getVisibility() == View.VISIBLE);
    }

    public void propagateChecked(boolean isChecked) {
        setBackground(isChecked);
        final int count = mSwitchChangeListeners.size();
        for (int n = 0; n < count; n++) {
            mSwitchChangeListeners.get(n).onSwitchChanged(mSwitch, isChecked);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        propagateChecked(isChecked);
    }

    /**
     * Adds a listener for switch changes
     */
    public void addOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (!mSwitchChangeListeners.contains(listener)) {
            mSwitchChangeListeners.add(listener);
        }
    }

    /**
     * Remove a listener for switch changes
     */
    public void removeOnSwitchChangeListener(OnSwitchChangeListener listener) {
        mSwitchChangeListeners.remove(listener);
    }

    static class SavedState extends BaseSavedState {
        boolean mChecked;
        boolean mVisible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mChecked = (Boolean) in.readValue(null);
            mVisible = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(mChecked);
            out.writeValue(mVisible);
        }

        @Override
        public String toString() {
            return "SwitchBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + mChecked
                    + " visible=" + mVisible + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.mChecked = mSwitch.isChecked();
        ss.mVisible = isShowing();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());

        mSwitch.setChecked(ss.mChecked);
        setChecked(ss.mChecked);
        setBackground(ss.mChecked);
        setVisibility(ss.mVisible ? View.VISIBLE : View.GONE);
        mSwitch.setOnCheckedChangeListener(ss.mVisible ? this : null);
        requestLayout();
    }
}