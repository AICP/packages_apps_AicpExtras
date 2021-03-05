/*
 * Copyright (C) 2018 AICP
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
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

public class BatteryLight extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_CATEGORY_CHARGE_COLORS = "battery_light_cat";
    private static final String KEY_CATEGORY_COLOR_BLEND = "blend_category";

    private SwitchPreference mBatteryBlend;
    private PreferenceCategory mChargeColorsCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.battery_light;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChargeColorsCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_CHARGE_COLORS);
        mBatteryBlend = (SwitchPreference) findPreference(Settings.System.BATTERY_LIGHT_BLEND);
        mBatteryBlend.setOnPreferenceChangeListener(this);

        // Preferences for devices with multi color LED
        Util.requireConfig(getActivity(), mChargeColorsCategory,
                com.android.internal.R.bool.config_multiColorBatteryLed, true, false);
        Util.requireConfig(getActivity(), findPreference(KEY_CATEGORY_COLOR_BLEND),
                com.android.internal.R.bool.config_multiColorBatteryLed, true, false);

        // Preference for devices with a pulsing LED
        Util.requireConfig(getContext(),
                findPreference(Settings.System.BATTERY_LIGHT_LOW_BLINKING),
                com.android.internal.R.bool.config_ledCanPulse, true, false);

        updateDependencies(mBatteryBlend.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mBatteryBlend) {
            updateDependencies((Boolean) newValue);
            return true;
        }
        return false;
    }

    private void updateDependencies(boolean batteryBlend) {
        mChargeColorsCategory.setEnabled(!batteryBlend);
    }

}
