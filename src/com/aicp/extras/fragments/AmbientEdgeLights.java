/*
 * Copyright (C) 2017-2020 AICP
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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;

import androidx.preference.Preference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SecureSettingIntListPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class AmbientEdgeLights { /* extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PULSE_AMBIENT_LIGHT_COLOR_MODE = "pulse_ambient_light_color_mode";
    private static final String PULSE_AMBIENT_LIGHT_COLOR = "pulse_ambient_light_color";

    private ColorPickerPreference mEdgeLightColorPref;
    private SecureSettingIntListPreference mEdgeLightColorModePref;

    @Override
    protected int getPreferenceResource() {
        return R.xml.ambient_edge_lights;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEdgeLightColorModePref = (SecureSettingIntListPreference) findPreference(PULSE_AMBIENT_LIGHT_COLOR_MODE);
        mEdgeLightColorModePref.setOnPreferenceChangeListener(this);
        mEdgeLightColorPref = (ColorPickerPreference) findPreference(PULSE_AMBIENT_LIGHT_COLOR);
        mEdgeLightColorPref.setOnPreferenceChangeListener(this);
        int edgeLightColorMode = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.PULSE_AMBIENT_LIGHT_COLOR_MODE, 1, UserHandle.USER_CURRENT);
        updateColorPrefs(edgeLightColorMode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();
        if (mEdgeLightColorModePref.equals(preference)) {
            int edgeLightColorMode = Integer.valueOf((String) newValue);
            updateColorPrefs(edgeLightColorMode);
            return true;
        }
        return false;
    }

    private void updateColorPrefs(int mode) {
        mEdgeLightColorPref.setEnabled(mode == 2);
    } */
}
