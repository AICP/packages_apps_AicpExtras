/*
 * Copyright (C) 2017 The Dirty Unicorns Project
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

package com.aicp.extras.fragments;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.aicp.gear.preference.SeekBarPreferenceCham;

public class NavigationGestureSettings extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_SWIPE_LENGTH = "gesture_swipe_length";
    private static final String KEY_SWIPE_START = "gesture_swipe_start";
    private static final String KEY_SWIPE_TIMEOUT = "gesture_swipe_timeout";

    private SeekBarPreferenceCham mSwipeTriggerLength;
    private SeekBarPreferenceCham mSwipeTriggerStart;
    private SeekBarPreferenceCham mSwipeTriggerTimeout;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation_gestures;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSwipeTriggerLength = (SeekBarPreferenceCham) findPreference(KEY_SWIPE_LENGTH);
        int value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_BOTTOM_GESTURE_SWIPE_LIMIT,
                getSwipeLengthInPixel(getResources().getInteger(com.android.internal.R.integer.config_navgestureswipeminlength)));

        mSwipeTriggerLength.setMin(getSwipeLengthInPixel(40));
        mSwipeTriggerLength.setMax(getSwipeLengthInPixel(80));
        mSwipeTriggerLength.setValue(value);
        mSwipeTriggerLength.setOnPreferenceChangeListener(this);

        mSwipeTriggerStart = (SeekBarPreferenceCham) findPreference(KEY_SWIPE_START);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.BOTTOM_GESTURE_SWIPE_START,
                getResources().getInteger(com.android.internal.R.integer.config_navgestureswipestart));
        mSwipeTriggerStart.setValue(value);
        mSwipeTriggerStart.setOnPreferenceChangeListener(this);

        mSwipeTriggerTimeout = (SeekBarPreferenceCham) findPreference(KEY_SWIPE_TIMEOUT);
        value = Settings.System.getInt(getContentResolver(),
                Settings.System.OMNI_BOTTOM_GESTURE_TRIGGER_TIMEOUT,
                getResources().getInteger(com.android.internal.R.integer.config_navgestureswipetimout));
        mSwipeTriggerTimeout.setValue(value);
        mSwipeTriggerTimeout.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mSwipeTriggerLength)) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.OMNI_BOTTOM_GESTURE_SWIPE_LIMIT,
                    value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mSwipeTriggerStart)) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.BOTTOM_GESTURE_SWIPE_START,
                    value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference.equals(mSwipeTriggerTimeout)) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.OMNI_BOTTOM_GESTURE_TRIGGER_TIMEOUT,
                    value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private int getSwipeLengthInPixel(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
