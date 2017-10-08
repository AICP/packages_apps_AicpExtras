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

import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

public class BatteryBar extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private ListPreference mBatteryBarPosition;

    @Override
    protected int getPreferenceResource() {
        return R.xml.battery_bar;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBatteryBarPosition =
                (ListPreference) findPreference(Settings.System.STATUSBAR_BATTERY_BAR);
        mBatteryBarPosition.setOnPreferenceChangeListener(this);

        updateBatteryBarDependencies(Integer.parseInt(mBatteryBarPosition.getValue()));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryBarPosition) {
            updateBatteryBarDependencies(Integer.parseInt((String) newValue));
            return true;
        } else {
            return false;
        }
    }

    private void updateBatteryBarDependencies(int batteryBarPosition) {
        // All preferences within this screen that don't have explicitely set a dependency
        // except the position preference depend on the battery bar position preference
        boolean enabled = batteryBarPosition != 0;
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (preference != mBatteryBarPosition && preference.getDependency() == null) {
                preference.setEnabled(enabled);
            }
        }
    }
}
