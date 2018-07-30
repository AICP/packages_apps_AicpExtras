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

import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.preference.MasterSwitchPreference;
import com.android.internal.utils.du.DUActionUtils;
import com.aicp.extras.R;

public class Navigation extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_NAVBAR_VISIBILITY = "navigation_bar_visible";
    private static final String KEY_EDGE_GESTURES_ENABLED = "edge_gestures_enabled";

    private MasterSwitchPreference mNavbarVisibility;
    private MasterSwitchPreference mEdgeGestures;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean showing = Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.NAVIGATION_BAR_VISIBLE,
                DUActionUtils.hasNavbarByDefault(getActivity()) ? 1 : 0) != 0;

        mNavbarVisibility = (MasterSwitchPreference) findPreference(KEY_NAVBAR_VISIBILITY);
        mEdgeGestures = (MasterSwitchPreference) findPreference(KEY_EDGE_GESTURES_ENABLED);
        mNavbarVisibility.setOnPreferenceChangeListener(this);
        mEdgeGestures.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavbarVisibility
                || preference == mEdgeGestures) {
            updateDependencies((Boolean) newValue ? preference : null);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mNavbarVisibility.reloadValue();
        mEdgeGestures.reloadValue();
        updateDependencies(null);
    }

    private void updateDependencies(Preference enabledNavigationMode) {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        for (int i = 0; i < preferenceScreen.getPreferenceCount(); i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (enabledNavigationMode != null
                    && enabledNavigationMode != preference
                    && preference instanceof MasterSwitchPreference
                    && ((MasterSwitchPreference) preference).isChecked()) {
                // Only one navigation mode at the time!
                ((MasterSwitchPreference) preference).setCheckedPersisting(false);
            }
        }
    }
}
