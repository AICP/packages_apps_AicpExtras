/*
 * Copyright (C) 2016-2019 crDroid Android Project
 * Copyright (C) 2020 Havoc-OS
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
 * limitations under the License
 */

package com.aicp.extras.preference;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.os.VibrationEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.*;

import com.android.settingslib.widget.SettingsThemeHelper;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;

import com.aicp.extras.R;
import com.aicp.extras.utils.Util;
import com.aicp.extras.utils.VibrationUtils;

public class ProperSeekBarPreference extends Preference implements Slider.OnChangeListener,
        Slider.OnSliderTouchListener, View.OnClickListener, View.OnLongClickListener {
    protected final String TAG = getClass().getName();
    private static final String SETTINGS_NS = "http://schemas.android.com/apk/res/com.android.settings";
    private static final String SETTINGS_NS_ALT = "http://schemas.android.com/apk/res-auto";
    protected static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";

    protected int mInterval = 1;
    protected boolean mShowSign = false;
    protected String mUnits = "";
    protected boolean mContinuousUpdates = false;
    protected String mTextStart, mTextEnd;
    protected boolean mShowButtons;

    protected int mMinValue = 0;
    protected int mMaxValue = 100;
    protected int mDefaultValue;

    protected int mValue;

    protected TextView mValueTextView;
    protected ImageView mResetImageView;
    protected ImageView mMinusImageView;
    protected ImageView mPlusImageView;
    protected Slider mSlider;

    protected boolean mTrackingTouch = false;
    protected int mTrackingValue;

    private final Context mContext;

    public ProperSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProperSeekBarPreference);
        try {
            mShowSign = a.getBoolean(R.styleable.ProperSeekBarPreference_showSign, mShowSign);
            String units = a.getString(R.styleable.ProperSeekBarPreference_units);
            if (units != null) mUnits = units;
            mContinuousUpdates = a.getBoolean(
                    R.styleable.ProperSeekBarPreference_continuousUpdates, false);
            mTextStart = a.getString(R.styleable.ProperSeekBarPreference_textStart);
            mTextEnd = a.getString(R.styleable.ProperSeekBarPreference_textEnd);
            mShowButtons = a.getBoolean(R.styleable.ProperSeekBarPreference_showButtons, true);
        } finally {
            a.recycle();
        }

        String newInterval = attrs.getAttributeValue(SETTINGS_NS, "interval");
        if (newInterval != null) {
            mInterval = Integer.parseInt(newInterval);
        }
        if (newInterval == null) {
            newInterval = attrs.getAttributeValue(SETTINGS_NS_ALT, "interval");
            if (newInterval != null) mInterval = Integer.parseInt(newInterval);
        }
        if (newInterval == null) {
            newInterval = attrs.getAttributeValue(ANDROIDNS, "interval");
            if (newInterval != null) mInterval = Integer.parseInt(newInterval);
        }

        mMinValue = attrs.getAttributeIntValue(SETTINGS_NS, "min", mMinValue);
        if (mMinValue == 0) {
            int min = attrs.getAttributeIntValue(SETTINGS_NS_ALT, "min", mMinValue);
            if (min != 0) mMinValue = min;
        }
        if (mMinValue == 0) {
            int min = attrs.getAttributeIntValue(ANDROIDNS, "min", mMinValue);
            if (min != 0) mMinValue = min;
        }

        mMaxValue = attrs.getAttributeIntValue(ANDROIDNS, "max", mMaxValue);
        if (mMaxValue == 100) {
            int max = attrs.getAttributeIntValue(SETTINGS_NS, "max", mMaxValue);
            if (max != 100) mMaxValue = max;
        }
        if (mMaxValue == 100) {
            int max = attrs.getAttributeIntValue(SETTINGS_NS_ALT, "max", mMaxValue);
            if (max != 100) mMaxValue = max;
        }
        if (mMaxValue < mMinValue) {
            mMaxValue = mMinValue;
        }

        setLayoutResource(R.layout.preference_proper_seekbar);

        mContext = context;
    }

    public ProperSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProperSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context,
                        androidx.preference.R.attr.seekBarPreferenceStyle,
                        com.android.internal.R.attr.seekBarPreferenceStyle));
    }

    public ProperSeekBarPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mSlider = (Slider) holder.findViewById(R.id.slider);
        mSlider.setValueTo(mMaxValue);
        mSlider.setValueFrom(mMinValue);
        mSlider.setValue(mValue);
        mSlider.setEnabled(isEnabled());
        mSlider.setLabelBehavior(LabelFormatter.LABEL_GONE);
        mSlider.setTickVisible(false);
        if (mInterval > 0) {
            mSlider.setStepSize(mInterval);
        } else {
            Log.w(TAG, "Step size is zero or invalid: " + mInterval);
        }

        // Set up slider size
        if (SettingsThemeHelper.isExpressiveTheme(getContext())) {
            Resources res = getContext().getResources();
            mSlider.setTrackHeight(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_track_height));
            // need to drop 1.12.0 to Android
            mSlider.setTrackInsideCornerSize(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_track_inside_corner_size));
            mSlider.setTrackStopIndicatorSize(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_track_stop_indicator_size));
            mSlider.setThumbWidth(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_thumb_width));
            mSlider.setThumbHeight(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_thumb_height));
            mSlider.setThumbElevation(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_thumb_elevation));
            mSlider.setThumbStrokeWidth(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_thumb_stroke_width));
            mSlider.setThumbTrackGapSize(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_thumb_track_gap_size));
            mSlider.setTickActiveRadius(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R.dimen
                    .settingslib_expressive_slider_tick_radius));
            mSlider.setTickInactiveRadius(res.getDimensionPixelSize(
                    com.android.settingslib.widget.preference.slider.R
                    .dimen.settingslib_expressive_slider_tick_radius));
        }

        mValueTextView = (TextView) holder.findViewById(R.id.value);
        mResetImageView = (ImageView) holder.findViewById(R.id.reset);
        mMinusImageView = (ImageView) holder.findViewById(R.id.minus);
        mPlusImageView = (ImageView) holder.findViewById(R.id.plus);

        if (mTextEnd != null || mTextStart != null) {
            holder.findViewById(R.id.label_frame).setVisibility(View.VISIBLE);
            TextView startText = (TextView) holder.findViewById(android.R.id.text1);
            TextView endText = (TextView) holder.findViewById(android.R.id.text2);
            startText.setText(mTextStart);
            endText.setText(mTextEnd);
        }

        if (!mShowButtons) {
            mMinusImageView.setVisibility(View.GONE);
            mPlusImageView.setVisibility(View.GONE);
        }

        updateValueViews();

        mSlider.addOnChangeListener(this);
        mSlider.addOnSliderTouchListener(this);
        mResetImageView.setOnClickListener(this);
        mMinusImageView.setOnClickListener(this);
        mPlusImageView.setOnClickListener(this);
        mResetImageView.setOnLongClickListener(this);
        mMinusImageView.setOnLongClickListener(this);
        mPlusImageView.setOnLongClickListener(this);
    }

    protected int getLimitedValue(int v) {
        return v < mMinValue ? mMinValue : (v > mMaxValue ? mMaxValue : v);
    }


    protected String getTextValue(int v) {
        return String.valueOf(v) + mUnits;
    }

    protected void updateValueViews() {
        if (mValueTextView != null) {
            String textValue = getTextValue(mValue);
            if (mTrackingTouch && !mContinuousUpdates) {
                textValue = getTextValue(mTrackingValue);
            }
            mValueTextView.setText(textValue);
        }

        if (mResetImageView != null) {
            if (mValue == mDefaultValue || mTrackingTouch)
                mResetImageView.setVisibility(View.INVISIBLE);
            else
                mResetImageView.setVisibility(View.VISIBLE);
        }

        if (mMinusImageView != null) {
            if (mValue == mMinValue || mTrackingTouch) {
                mMinusImageView.setClickable(false);
                mMinusImageView.setColorFilter(Utils.getColorAttrDefaultColor(getContext(), android.R.attr.textColorTertiary),
                        PorterDuff.Mode.SRC_IN);
            } else {
                mMinusImageView.setClickable(true);
                mMinusImageView.clearColorFilter();
            }
        }

        if (mPlusImageView != null) {
            if (mValue == mMaxValue || mTrackingTouch) {
                mPlusImageView.setClickable(false);
                mPlusImageView.setColorFilter(Utils.getColorAttrDefaultColor(getContext(), android.R.attr.textColorTertiary),
                        PorterDuff.Mode.SRC_IN);
            } else {
                mPlusImageView.setClickable(true);
                mPlusImageView.clearColorFilter();
            }
        }
    }

    protected void changeValue(int newValue) {
    }

    @Override
    public void onValueChange(Slider slider, float value, boolean fromUser) {
        int newValue = getLimitedValue(Math.round(value));
        if (mTrackingTouch && !mContinuousUpdates) {
            mTrackingValue = newValue;
            VibrationUtils.doHapticFeedback(mContext, VibrationEffect.EFFECT_TEXTURE_TICK);
        } else if (mValue != newValue) {
            if (!callChangeListener(newValue)) {
                mSlider.setValue(mValue);
                return;
            }
            changeValue(newValue);
            persistInt(newValue);

            mValue = newValue;
        }
        updateValueViews();
    }

    @Override
    public void onStartTrackingTouch(Slider slider) {
        mTrackingValue = mValue;
        mTrackingTouch = true;
    }

    @Override
    public void onStopTrackingTouch(Slider slider) {
        mTrackingTouch = false;
        if (!mContinuousUpdates) {
            onValueChange(mSlider, mTrackingValue, false);
        }
        notifyChanged();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.reset) {
            Toast.makeText(getContext(), getContext().getString(
                    R.string.proper_seekbar_default_value_to_set, getTextValue(mDefaultValue)),
                    Toast.LENGTH_LONG).show();
        } else if (id == R.id.minus) {
            setValue(mValue - mInterval, true);
        } else if (id == R.id.plus) {
            setValue(mValue + mInterval, true);
        }
        VibrationUtils.doHapticFeedback(mContext, VibrationEffect.EFFECT_CLICK);
    }

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        if (id == R.id.reset) {
            setValue(mDefaultValue, true);
        } else if (id == R.id.minus) {
            int value = mMinValue;
            if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue < mValue * 2) {
                value = Math.floorDiv(mMaxValue + mMinValue, 2);
            }
            setValue(value, true);
        } else if (id == R.id.plus) {
            int value = mMaxValue;
            if (mMaxValue - mMinValue > mInterval * 2 && mMaxValue + mMinValue > mValue * 2) {
                value = -1 * Math.floorDiv(-1 * (mMaxValue + mMinValue), 2);
            }
            setValue(value, true);
        }
        return true;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray ta, int index) {
        mDefaultValue = ta.getInt(index, mMinValue);
        return mDefaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        mValue = getPersistedInt(mDefaultValue);
    }

    @Override
    public void setDefaultValue(Object defaultValue) {
        setDefaultValue((Integer) defaultValue, mSlider != null);
    }

    public void setDefaultValue(int newValue, boolean update) {
        newValue = getLimitedValue(newValue);
        if (mDefaultValue != newValue) {
            mDefaultValue = newValue;
            if (update)
                updateValueViews();
        }
    }

    public void setMax(int max) {
        mMaxValue = max;
        if (mSlider != null) mSlider.setValueTo(mMaxValue);
    }

    public int getMax() {
        return mMaxValue;
    }

    public void setMin(int min) {
        mMinValue = min;
        if (mSlider != null) mSlider.setValueFrom(mMinValue);
    }

    public void setValue(int newValue) {
        newValue = getLimitedValue(newValue);
        if (mSlider != null) {
            mSlider.setValue(newValue);
        } else {
            mValue = newValue;
        }
    }

    public void setValue(int newValue, boolean update) {
        newValue = getLimitedValue(newValue);
        if (mValue != newValue) {
            if (update) {
                if (mSlider != null) {
                    mSlider.setValue(newValue);
                } else {
                    mValue = newValue;
                }
            } else {
                mValue = newValue;
            }
        }
    }

    public int getValue() {
        return mValue;
    }

    public void refresh(int newValue) {
        setValue(newValue, mSlider != null);
    }

    public void setUnits(String units) {
        mUnits = units;
        updateValueViews();
    }

    public String getUnits() {
        return mUnits;
    }
}
