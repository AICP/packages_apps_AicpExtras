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

package com.aicp.extras.smartnav;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.utils.ActionConstants;
import com.android.internal.utils.Config;
import com.android.internal.utils.ActionUtils;
import com.android.internal.utils.Config.ButtonConfig;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.aicp.gear.preference.SecureSettingIntListPreference;
import com.aicp.extras.preference.SecureSettingMasterSwitchPreference;

public class NavigationBarSettings extends BaseSettingsFragment implements Preference.OnPreferenceChangeListener {
    private static final String KEY_NAVBAR_MODE = "navigation_bar_mode";
    private static final String KEY_DEFAULT_NAVBAR_SETTINGS = "default_settings";
    private static final String KEY_FLING_NAVBAR_SETTINGS = "fling_settings";
    private static final String KEY_CATEGORY_NAVIGATION_INTERFACE = "category_navbar_interface";
    private static final String KEY_CATEGORY_NAVIGATION_GENERAL = "category_navbar_general";
    private static final String KEY_SMARTBAR_SETTINGS = "smartbar_settings";
    private static final String KEY_NAVIGATION_HEIGHT_LAND = "navigation_bar_height_landscape";
    private static final String KEY_NAVIGATION_WIDTH = "navigation_bar_width";
    private static final String KEY_PULSE_SETTINGS = "fling_pulse_enabled";

    private SecureSettingIntListPreference mNavbarMode;
    private PreferenceScreen mFlingSettings;
    private PreferenceCategory mNavGeneral;
    private PreferenceScreen mSmartbarSettings;
    private Preference mDefaultSettings;
    private SecureSettingMasterSwitchPreference mPulseSettings;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation_bar;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNavGeneral = (PreferenceCategory) findPreference(KEY_CATEGORY_NAVIGATION_GENERAL);
        mNavbarMode = (SecureSettingIntListPreference) findPreference(KEY_NAVBAR_MODE);
        mDefaultSettings = (Preference) findPreference(KEY_DEFAULT_NAVBAR_SETTINGS);
        mFlingSettings = (PreferenceScreen) findPreference(KEY_FLING_NAVBAR_SETTINGS);
        mSmartbarSettings = (PreferenceScreen) findPreference(KEY_SMARTBAR_SETTINGS);
        mPulseSettings = (SecureSettingMasterSwitchPreference) findPreference(KEY_PULSE_SETTINGS);

        int mode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.NAVIGATION_BAR_MODE,
                0);

        updateBarModeSettings(mode);
        mNavbarMode.setOnPreferenceChangeListener(this);

        final boolean canMove = ActionUtils.navigationBarCanMove();
        if (canMove) {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_HEIGHT_LAND));
        } else {
            mNavGeneral.removePreference(findPreference(KEY_NAVIGATION_WIDTH));
        }
    }

    private void updateBarModeSettings(int mode) {
        mNavbarMode.setValue(String.valueOf(mode));
        switch (mode) {
            case 0:
                mDefaultSettings.setEnabled(true);
                mDefaultSettings.setSelectable(true);
                mSmartbarSettings.setEnabled(false);
                mSmartbarSettings.setSelectable(false);
                mFlingSettings.setEnabled(false);
                mFlingSettings.setSelectable(false);
                break;
            case 1:
                mDefaultSettings.setEnabled(false);
                mDefaultSettings.setSelectable(false);
                mSmartbarSettings.setEnabled(true);
                mSmartbarSettings.setSelectable(true);
                mFlingSettings.setEnabled(false);
                mFlingSettings.setSelectable(false);
                break;
            case 2:
                mDefaultSettings.setEnabled(false);
                mDefaultSettings.setSelectable(false);
                mSmartbarSettings.setEnabled(false);
                mSmartbarSettings.setSelectable(false);
                mFlingSettings.setEnabled(true);
                mFlingSettings.setSelectable(true);
                break;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarMode)) {
            int mode = Integer.parseInt(((String) newValue).toString());
            updateBarModeSettings(mode);
            return true;
        }
        return false;
    }
}
