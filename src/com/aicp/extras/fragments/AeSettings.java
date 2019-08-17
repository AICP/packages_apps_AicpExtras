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

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import android.widget.Toast;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.Constants;
import com.aicp.extras.LauncherActivity;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

public class AeSettings extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PREF_THEME = "ae_theme";

    private static final String PREF_AE_LAUNCHER = "ae_launcher_enabled";

    private ComponentName mAeLauncherComponent;

    private ListPreference mTheme;
    private SwitchPreference mAeLauncher;

    @Override
    protected int getPreferenceResource() {
        return R.xml.ae_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pm = getContext().getPackageManager();
        mAeLauncherComponent = new ComponentName(getContext(),
            LauncherActivity.class);

        mTheme = (ListPreference) findPreference(PREF_THEME);
        mTheme.setOnPreferenceChangeListener(this);
        mAeLauncher = (SwitchPreference) findPreference(PREF_AE_LAUNCHER);
        mAeLauncher.setChecked(pm.getComponentEnabledSetting(mAeLauncherComponent) !=
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
        mAeLauncher.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.setSummaryToValue(mTheme);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTheme) {
            Util.setSummaryToValue(mTheme, newValue);
            if (!mTheme.getValue().equals(newValue)) {
                getActivity().recreate();
            }
            return true;
        } else if (preference == mAeLauncher) {
            setAeLauncherEnabled((Boolean) newValue);
            return true;
        } else {
            return false;
        }
    }

    private void setAeLauncherEnabled(boolean enabled) {
        PackageManager pm = getContext().getPackageManager();
        pm.setComponentEnabledSetting(mAeLauncherComponent, enabled
                ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(getContext(), R.string.ae_launcher_enabled_update, Toast.LENGTH_LONG)
                .show();
    }
}
