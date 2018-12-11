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
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

public class BatteryLight extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_CATEGORY_FAST_CHARGE = "fast_color_cat";
    private static final String KEY_CATEGORY_CHARGE_COLORS = "colors_list";
    private static final String KEY_CATEGORY_COLOR_BLEND = "blend_category";

    private SwitchPreference mOnlyFullyCharged;
    private SwitchPreference mBatteryBlend;
    private Preference mChargeColorsCategory;
    private Preference mFastChargeCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.battery_light;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mOnlyFullyCharged = (SwitchPreference)
                findPreference(Settings.System.OMNI_BATTERY_LIGHT_ONLY_FULLY_CHARGED);
        mOnlyFullyCharged.setOnPreferenceChangeListener(this);
        mBatteryBlend = (SwitchPreference) findPreference(Settings.System.BATTERY_LIGHT_BLEND);
        mBatteryBlend.setOnPreferenceChangeListener(this);
        mChargeColorsCategory = findPreference(KEY_CATEGORY_CHARGE_COLORS);
        mFastChargeCategory = findPreference(KEY_CATEGORY_FAST_CHARGE);

        // Preferences that need multi color LED
        Util.requireConfig(getActivity(), findPreference(KEY_CATEGORY_COLOR_BLEND),
                com.android.internal.R.bool.config_multiColorBatteryLed, true, false);
        Util.requireConfig(getActivity(), mChargeColorsCategory,
                com.android.internal.R.bool.config_multiColorBatteryLed, true, false);
        Util.requireConfig(getActivity(), mFastChargeCategory,
                com.android.internal.R.bool.config_multiColorBatteryLed, true, false);

        // Preferences that need fast charging
        Util.requireConfig(getActivity(), mFastChargeCategory,
                com.android.internal.R.bool.config_FastChargingLedSupported, true, false);

        updateDependencies(mOnlyFullyCharged.isChecked(), mBatteryBlend.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mOnlyFullyCharged) {
            updateDependencies((Boolean) newValue, mBatteryBlend.isChecked());
            return true;
        } else if (preference == mBatteryBlend) {
            updateDependencies(mOnlyFullyCharged.isChecked(), (Boolean) newValue);
            return true;
        } else {
            return false;
        }
    }

    private void updateDependencies(boolean onlyFullyCharged, boolean batteryBlend) {
        if (batteryBlend && !onlyFullyCharged) {
            mChargeColorsCategory.setEnabled(false);
            mFastChargeCategory.setEnabled(false);
        } else {
            // Dependencies from xml will still apply
            mChargeColorsCategory.setEnabled(true);
            mFastChargeCategory.setEnabled(true);
        }

    }

}
