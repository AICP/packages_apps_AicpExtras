/*
 * Copyright (C) 2017 AICP
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

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

import com.android.internal.util.aicp.DeviceUtils;

public class QuickSettings extends BaseSettingsFragment
    implements OnPreferenceChangeListener {

    private static final String PREF_BRIGHTNESS_ICON_POSITION = "brightness_icon_position";
    private static final String PREF_QS_STYLE_DARK = "qs_style_dark";
    private static final String KEY_FPC_QUICK_PULLDOWN = "status_bar_quick_qs_pulldown_fp";

    private SwitchPreference mBrightnessIconPosition;

    @Override
    protected int getPreferenceResource() {
        return R.xml.quick_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBrightnessIconPosition = (SwitchPreference) findPreference(PREF_BRIGHTNESS_ICON_POSITION);
        mBrightnessIconPosition.setOnPreferenceChangeListener(this);

        mQsStyleDark = (SwitchPreference) findPreference(PREF_QS_STYLE_DARK);
        mQsStyleDark.setOnPreferenceChangeListener(this);

        mFPQuickPullDown = (SwitchPreference) findPreference(KEY_FPC_QUICK_PULLDOWN);
        if(!DeviceUtils.deviceSupportsFingerPrint(getActivity())){
            mFPQuickPullDown.getParent().removePreference(mFPQuickPullDown);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBrightnessIconPosition) {
            Util.showSystemUiRestartDialog(getContext());
            return true;
        } else if (preference == mQsStyleDark) {
            Util.showSystemUiRestartDialog(getContext());
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }
}
