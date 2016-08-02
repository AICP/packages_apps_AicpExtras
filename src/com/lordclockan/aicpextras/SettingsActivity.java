/*
 * Copyright (C) 2016 AICP
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

package com.lordclockan.aicpextras;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import com.lordclockan.R;

import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsActivityFragment()).commit();
    }

    public static class SettingsActivityFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "SettingsActivity";

        private static final String PREF_AE_NAV_DRAWER_OPACITY = "ae_drawer_opacity";
        private static final String PREF_AE_NAV_DRAWER_BG_COLOR = "ae_drawer_bg_color";
        private static final String PREF_AE_SETTINGS_RESTORE_DEFAULTS = "ae_settings_restore_defaults";
        private static final String PREF_AE_NAV_HEADER_BG_IMAGE_OPACITY = "ae_header_bg_image_opacity";

        private static final int DEFAULT_NAV_HEADER_COLOR = 0xFF303030;

        private SeekBarPreferenceCham mNavDrawerOpacity;
        private ColorPickerPreference mNavDrawerBgColor;
        private SeekBarPreferenceCham mNavHeaderBgImageOpacity;
        private Preference mAeRestoreDefaults;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();


            int intColor;
            String hexColor;

            mNavDrawerOpacity = (SeekBarPreferenceCham) prefSet.findPreference(PREF_AE_NAV_DRAWER_OPACITY);
            mNavDrawerOpacity.setValue(Settings.System.getInt(resolver,
                    Settings.System.AE_NAV_DRAWER_OPACITY, 178));
            mNavDrawerOpacity.setOnPreferenceChangeListener(this);

            mNavDrawerBgColor = (ColorPickerPreference) prefSet.findPreference(PREF_AE_NAV_DRAWER_BG_COLOR);
            mNavDrawerBgColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.AE_NAV_DRAWER_BG_COLOR, DEFAULT_NAV_HEADER_COLOR);
            hexColor = String.format("#%08x", (DEFAULT_NAV_HEADER_COLOR & intColor));
            mNavDrawerBgColor.setSummary(hexColor);
            mNavDrawerBgColor.setNewPreviewColor(intColor);

            mNavHeaderBgImageOpacity = (SeekBarPreferenceCham) prefSet.findPreference(PREF_AE_NAV_HEADER_BG_IMAGE_OPACITY);
            mNavHeaderBgImageOpacity.setValue(Settings.System.getInt(resolver,
                    Settings.System.AE_NAV_HEADER_BG_IMAGE_OPACITY, 200));
            mNavHeaderBgImageOpacity.setOnPreferenceChangeListener(this);

            mAeRestoreDefaults = prefSet.findPreference(PREF_AE_SETTINGS_RESTORE_DEFAULTS);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mNavDrawerOpacity) {
                int alpha = ((Integer) newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_OPACITY, alpha);
                return true;
            } else if (preference == mNavDrawerBgColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_BG_COLOR, intHex);
                return true;
            } else if (preference == mNavHeaderBgImageOpacity) {
                int alpha = ((Integer) newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_HEADER_BG_IMAGE_OPACITY, alpha);
                return true;
            }
            return false;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mAeRestoreDefaults) {
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_OPACITY, 178);
                mNavDrawerOpacity.setValue(Settings.System.getInt(resolver,
                        Settings.System.AE_NAV_DRAWER_OPACITY, 178));
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_BG_COLOR, DEFAULT_NAV_HEADER_COLOR);
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_HEADER_BG_IMAGE_OPACITY, 200);
                int intColor = Settings.System.getInt(resolver,
                        Settings.System.AE_NAV_DRAWER_BG_COLOR, DEFAULT_NAV_HEADER_COLOR);
                String hexColor = String.format("#%08x", (DEFAULT_NAV_HEADER_COLOR & intColor));
                mNavDrawerBgColor.setSummary(hexColor);
                mNavDrawerBgColor.setNewPreviewColor(intColor);
                Snackbar.make(getView(), R.string.values_restored_title,
                        Snackbar.LENGTH_LONG).show();
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }
    }
}
