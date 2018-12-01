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

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.MasterSwitchPreference;

public class Navigation extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_NAVIGATION_BAR_ENABLED =
            "navigation_bar_visible";
    private static final int BUTTON_BRIGHTNESS_DEFAULT = 180;

    private MasterSwitchPreference mNavBarPreference;

    private boolean mButtonLightsEnabled;
    private boolean mHasNavigationBar;
    private int mButtonBrightness;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();

        mNavBarPreference =
                (MasterSwitchPreference) findPreference(KEY_NAVIGATION_BAR_ENABLED);

        mHasNavigationBar = getActivity().getResources()
                .getBoolean(com.android.internal.R.bool.config_showNavigationBar);

        mButtonLightsEnabled = Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BRIGHTNESS_ENABLED, 0) !=0;
        mButtonBrightness =  Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BRIGHTNESS, BUTTON_BRIGHTNESS_DEFAULT);

        mNavBarPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavBarPreference) {
            if (!mHasNavigationBar) updateButtonLights((boolean) newValue);
            return true;
        }
        return false;
    }

    private void updateButtonLights(boolean navbarEnabled) {
        ContentResolver resolver = getContentResolver();

        Settings.System.putInt(resolver, Settings.System.BUTTON_BRIGHTNESS_ENABLED,
                !navbarEnabled ? (mButtonLightsEnabled ? 1 : 0) : 0);
        Settings.System.putInt(resolver, Settings.System.BUTTON_BRIGHTNESS,
                !navbarEnabled ? (mButtonBrightness == 0 ?
                          BUTTON_BRIGHTNESS_DEFAULT : mButtonBrightness) : 0);
    }
}
