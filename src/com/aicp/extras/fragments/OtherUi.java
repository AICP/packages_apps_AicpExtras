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

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.SeekBarPreferenceCham;

public class OtherUi extends BaseSettingsFragment
    implements OnPreferenceChangeListener {

    private static final String KEY_VOLUME_DIALOG_TIMEOUT = "volume_dialog_timeout";

    private SeekBarPreferenceCham mVolumeDialogTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.other_ui);

        ContentResolver resolver = getActivity().getContentResolver();

        // Volume dialog timeout seekbar
        mVolumeDialogTimeout = (SeekBarPreferenceCham) findPreference(KEY_VOLUME_DIALOG_TIMEOUT);
        int volumeDialogTimeout = Settings.System.getInt(resolver,
                Settings.System.VOLUME_DIALOG_TIMEOUT, 3000);
        mVolumeDialogTimeout.setValue(volumeDialogTimeout / 1);
        mVolumeDialogTimeout.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mVolumeDialogTimeout) {
            int volDialogTimeout = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.VOLUME_DIALOG_TIMEOUT, volDialogTimeout * 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
