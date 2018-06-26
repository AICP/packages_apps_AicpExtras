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
import android.support.v7.preference.PreferenceCategory;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SystemSettingIntListPreference;

public class Notifications extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TICKER_CATEGORY = "status_bar_ticker_category";

    private SystemSettingIntListPreference mTickerMode;
    private PreferenceCategory mTickerCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.notifications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        mTickerMode = (SystemSettingIntListPreference)
                findPreference(Settings.System.STATUS_BAR_SHOW_TICKER);
        mTickerMode.setOnPreferenceChangeListener(this);
        mTickerCategory = (PreferenceCategory) findPreference(TICKER_CATEGORY);

        int tickerMode = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0,
                UserHandle.USER_CURRENT);
        updateTickerDependencies(tickerMode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mTickerMode)) {
             updateTickerDependencies(Integer.parseInt((String) newValue));
             return true;
        }
        return false;
    }

    private void updateTickerDependencies(int tickerMode) {
        // All preferences in ticker category except the switch itself depend on
        // ticker enabled state
        for (int i = 0; i < mTickerCategory.getPreferenceCount(); i++) {
            Preference preference = mTickerCategory.getPreference(i);
            if (preference != mTickerMode) {
                preference.setEnabled(tickerMode > 0);
            }
        }
    }
}
