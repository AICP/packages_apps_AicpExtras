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
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SystemSettingIntListPreference;

public class Notifications extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TICKER_MODE = "status_bar_show_ticker";
    private static final String TICKER_MODE_ANIMATION =
            "status_bar_ticker_animation_mode";

    private SystemSettingIntListPreference mTickerMode;
    private SystemSettingIntListPreference mTickerModeAnimation;

    @Override
    protected int getPreferenceResource() {
        return R.xml.notifications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        mTickerMode =
                (SystemSettingIntListPreference) findPreference(TICKER_MODE);
        mTickerMode.setOnPreferenceChangeListener(this);

        mTickerModeAnimation =
                (SystemSettingIntListPreference) findPreference(TICKER_MODE_ANIMATION);
        int tickerMode = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0,
                UserHandle.USER_CURRENT);
        mTickerModeAnimation.setEnabled(tickerMode > 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mTickerMode)) {
             int value = Integer.parseInt((String) newValue);
             mTickerModeAnimation.setEnabled(value > 0);
             return true;
        }
        return false;
    }
}
