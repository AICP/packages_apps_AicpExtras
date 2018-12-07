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

import com.android.internal.utils.ActionUtils;

public class Navigation extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_NAVIGATION_BAR_VISIBLE =
            "navigation_bar_visible";

    private MasterSwitchPreference mNavbarPreference;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNavbarPreference =
                (MasterSwitchPreference) findPreference(KEY_NAVIGATION_BAR_VISIBLE);

        mNavbarPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavbarPreference) {
            updateButtonLights((boolean) newValue);
            return true;
        }
        return false;
    }

    private void updateButtonLights(boolean navbarEnabled) {
        Settings.System.putInt(getContentResolver(),
            Settings.System.BUTTON_BRIGHTNESS_ENABLED, !navbarEnabled ? 1 : 0);
    }
}
