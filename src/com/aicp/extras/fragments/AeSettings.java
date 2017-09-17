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

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.Constants;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

public class AeSettings extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private ListPreference mTheme;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ae_settings);

        mTheme = (ListPreference) findPreference(Constants.PREF_THEME);
        mTheme.setOnPreferenceChangeListener(this);
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
        } else {
            return false;
        }
    }
}
