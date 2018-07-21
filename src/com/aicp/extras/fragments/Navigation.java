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
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.preference.SecureSettingMasterSwitchPreference;
import com.android.internal.utils.du.DUActionUtils;
import com.aicp.extras.R;

public class Navigation extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String KEY_NAVBAR_VISIBILITY = "navigation_bar_visible";
    private static final String KEY_EDGE_GESTURES_ENABLED = "edge_gestures_enabled";

    private SecureSettingMasterSwitchPreference mNavbarVisibility;
    private SecureSettingMasterSwitchPreference mEdgeGestures;

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

        mNavbarVisibility = (SecureSettingMasterSwitchPreference)
                              findPreference(KEY_NAVBAR_VISIBILITY);
        mEdgeGestures = (SecureSettingMasterSwitchPreference)
                          findPreference(KEY_EDGE_GESTURES_ENABLED);
        mNavbarVisibility.setOnPreferenceChangeListener(this);
        mEdgeGestures.setOnPreferenceChangeListener(this);

        updatePrefs(showing);
    }

    private void updatePrefs(boolean showing) {
        mNavbarVisibility.setChecked(showing);
        mNavbarVisibility.setEnabled(showing);

        mEdgeGestures.setChecked(!showing);
        mEdgeGestures.setEnabled(!showing);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mNavbarVisibility)) {
            boolean showing = ((Boolean)newValue);
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.EDGE_GESTURES_ENABLED,
                    showing ? 0 : 1, UserHandle.USER_CURRENT);
            updatePrefs(showing);
            return true;
        } else if (preference.equals(mEdgeGestures)) {
            boolean showing = ((Boolean)newValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.NAVIGATION_BAR_VISIBLE,
                    showing ? 0 : 1);
            updatePrefs(!showing);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mNavbarVisibility.reloadValue();
        mEdgeGestures.reloadValue();
    }
}
