/*
 * Copyright (C) 2015 The Dirty Unicorns Project
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

import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SecureSettingIntListPreference;

public class PulseSettings extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = PulseSettings.class.getSimpleName();

    private static final int RENDER_STYLE_FADING_BARS = 0;
    private static final int RENDER_STYLE_SOLID_LINES = 1;

    SecureSettingIntListPreference mRenderMode;

    @Override
    protected int getPreferenceResource() {
        return R.xml.pulse_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRenderMode = (SecureSettingIntListPreference) findPreference("pulse_render_style");
        mRenderMode.setOnPreferenceChangeListener(this);
        int renderMode = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_RENDER_STYLE_URI, RENDER_STYLE_SOLID_LINES, UserHandle.USER_CURRENT);
        updateCategoryDependencies(renderMode);
    }

    private void updateCategoryDependencies(int renderMode) {
        PreferenceCategory fadingBarsCat = (PreferenceCategory)findPreference("pulse_fading_bars_category");
        fadingBarsCat.setEnabled(renderMode == RENDER_STYLE_FADING_BARS);
        PreferenceCategory solidBarsCat = (PreferenceCategory) findPreference("pulse_2");
        solidBarsCat.setEnabled(renderMode == RENDER_STYLE_SOLID_LINES);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mRenderMode)) {
            int mode = Integer.valueOf((String) newValue);
            updateCategoryDependencies(mode);
            return true;
        }
        return false;
    }
}
