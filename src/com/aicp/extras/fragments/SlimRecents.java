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

import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.MasterSwitchPreference;

public class SlimRecents extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String RECENT_PANEL_LEFTY_MODE = "recent_panel_lefty_mode";

    private SwitchPreference mRecentPanelLeftyMode;
    private MasterSwitchPreference mAppSidebar;

    @Override
    protected int getPreferenceResource() {
        return R.xml.slim_recents;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecentPanelLeftyMode = (SwitchPreference) findPreference(RECENT_PANEL_LEFTY_MODE);
        mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);
        mAppSidebar =
                (MasterSwitchPreference) findPreference(Settings.System.USE_RECENT_APP_SIDEBAR);
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean recentLeftyMode = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_PANEL_GRAVITY, Gravity.RIGHT) == Gravity.LEFT;
        mRecentPanelLeftyMode.setChecked(recentLeftyMode);

        mAppSidebar.reloadValue();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.RECENT_PANEL_GRAVITY,
                    ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
            return true;
        } else {
            return false;
        }
    }

    private boolean screenPinningEnabled() {
        return Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.LOCK_TO_APP_ENABLED, 0) != 0;
    }

}
