/*
 * Copyright (C) 2015-2017 SlimRoms
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

package slim.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.UserHandle;
import android.preference.Preference;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.lordclockan.R;

import slim.utils.AttributeHelper;

/**
 * @hide
 */
public class SlimSeekBarPreference extends Preference
        implements OnSeekBarChangeListener {

    public int mInterval = 5;

    private View mView = null;
    private TextView mMonitorBox;
    private SeekBar mBar;

    int mDefaultValue = 60;
    int mSetDefault = -1;
    int mMultiply = -1;
    int mMinimum = -1;
    boolean mDisableText = false;
    boolean mDisablePercentageValue = false;
    boolean mIsMilliSeconds = false;

    //private int mSettingType;

    //private SlimPreferenceManager mSlimPreferenceManager = SlimPreferenceManager.get();
    private String mListDependency;
    private String[] mListDependencyValues;

    private OnPreferenceChangeListener mChanger;

    public SlimSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.slim_slider_preference);

        AttributeHelper a = new AttributeHelper(context, attrs, R.styleable.SlimSeekBarPreference);

        mSetDefault = a.getInt(R.styleable.SlimSeekBarPreference_defaultValueInt, mSetDefault);
        mDefaultValue = mSetDefault;
        mIsMilliSeconds = a.getBoolean(R.styleable.SlimSeekBarPreference_useMilliSeconds,
                mIsMilliSeconds);
        mDisablePercentageValue = !a.getBoolean(R.styleable.SlimSeekBarPreference_usePercentage,
                !mDisablePercentageValue);
        mDisableText = a.getBoolean(R.styleable.SlimSeekBarPreference_disableText, mDisableText);
        mInterval = a.getInt(R.styleable.SlimSeekBarPreference_interval, mInterval);
        mMinimum = a.getInt(R.styleable.SlimSeekBarPreference_minValue, mMinimum);
        mMultiply = a.getInt(R.styleable.SlimSeekBarPreference_multiplyValue, mMultiply);

        /*
        a = new AttributeHelper(context, attrs, slim.R.styleable.SlimPreference);

        mSettingType = SlimPreferenceManager.getSettingType(a);

        String list = a.getString(slim.R.styleable.SlimPreference_listDependency);
        if (!TextUtils.isEmpty(list)) {
            String[] listParts = list.split(":");
            if (listParts.length == 2) {
                mListDependency = listParts[0];
                mListDependencyValues = listParts[1].split("\\|");
            }
        }

        boolean hidePreference =
                a.getBoolean(slim.R.styleable.SlimPreference_hidePreference, false);
        int hidePreferenceInt = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceInt, -1);
        int intDep = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceIntDependency, 0);
        if (hidePreference || hidePreferenceInt == intDep) {
            setVisible(false);
        }
        */
    }

    /*
    @Override
    public void onAttached() {
        super.onAttached();
        if (mListDependency != null) {
            mSlimPreferenceManager.registerListDependent(
                    this, mListDependency, mListDependencyValues);
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if (mListDependency != null) {
            mSlimPreferenceManager.unregisterListDependent(this, mListDependency);
        }
    }
    */

    @Override
    public void onBindView(View view) {
        super.onBindView(view);

        mView = view;
        mMonitorBox = (TextView) view.findViewById(R.id.monitor_box);
        mBar = (SeekBar) view.findViewById(R.id.seek_bar);
        mBar.setOnSeekBarChangeListener(this);
        int progress = getPersistedInt(mSetDefault);
        if (mMinimum != -1) {
            progress -= mMinimum;
        }
        if (mMultiply > 0) {
            progress = progress / mMultiply;
        }
        mBar.setProgress(progress);
    }

    public void setInitValue(int progress) {
        mDefaultValue = progress;
        if (mBar != null) {
            mBar.setProgress(mDefaultValue);
        }
    }

    @Override
    public void setOnPreferenceChangeListener(
                OnPreferenceChangeListener onPreferenceChangeListener) {
        mChanger = onPreferenceChangeListener;
        super.setOnPreferenceChangeListener(onPreferenceChangeListener);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        progress = Math.round(((float) progress) / mInterval) * mInterval;
        seekBar.setProgress(progress);

        if (mMultiply > 0) {
            progress = progress * mMultiply;
        }

        if (mMinimum != -1) {
            progress += mMinimum;
        }

        if (progress == mSetDefault) {
            mMonitorBox.setText(R.string.default_string);
        } else if (!mDisableText) {
            if (mIsMilliSeconds) {
                mMonitorBox.setText(progress + " ms");
            } else if (!mDisablePercentageValue) {
                mMonitorBox.setText(progress + "%");
            } else {
                mMonitorBox.setText(Integer.toString(progress));
            }
        }
        if (mChanger != null) {
            mChanger.onPreferenceChange(this, Integer.toString(progress));
        }
        persistInt(progress);
    }

    public void disablePercentageValue(boolean disable) {
        mDisablePercentageValue = disable;
    }

    public void disableText(boolean disable) {
        mDisableText = disable;
    }

    public void setInterval(int inter) {
        mInterval = inter;
    }

    public void setDefault(int defaultVal) {
        mSetDefault = defaultVal;
    }

    public void multiplyValue(int val) {
        mMultiply = val;
    }

    public void minimumValue(int val) {
        mMinimum = val;
    }

    public void isMilliseconds(boolean millis) {
        mIsMilliSeconds = millis;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected boolean persistInt(int value) {
        if (shouldPersist()) {
            if (value == getPersistedInt(-1)) {
                return true;
            }
            //SlimPreferenceManager.putIntInSlimSettings(getContext(),
            //        mSettingType, getKey(), value);
            Settings.System.putInt(getContext().getContentResolver(),
                    getKey(), value);
            return true;
        }
        return false;
    }

    @Override
    protected int getPersistedInt(int defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        //return SlimPreferenceManager.getIntFromSlimSettings(getContext(), mSettingType,
        //        getKey(), defaultReturnValue);
        return Settings.System.getInt(getContext().getContentResolver(),
                getKey(), defaultReturnValue);
    }

    @Override
    protected boolean isPersisted() {
        return Settings.System.getStringForUser(getContext().getContentResolver(), getKey(),
                        UserHandle.USER_CURRENT) != null;
    }
}
