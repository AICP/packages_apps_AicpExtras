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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;

import com.lordclockan.R;

import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class SettingsActivity extends SubActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsActivityFragment()).commit();
    }

    public static class SettingsActivityFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "SettingsActivity";

        private static final String PREF_AE_CUSTOM_COLORS = "ae_custom_colors";
        private static final String PREF_AE_NAV_DRAWER_OPACITY = "ae_drawer_opacity";
        private static final String PREF_AE_NAV_DRAWER_BG_COLOR = "ae_drawer_bg_color";
        private static final String PREF_AE_SETTINGS_RESTORE_DEFAULTS = "ae_settings_restore_defaults";
        private static final String PREF_AE_NAV_HEADER_BG_IMAGE_OPACITY = "ae_header_bg_image_opacity";
        private static final String PREF_AE_NAV_DRAWER_CHECKED_TEXT = "ae_nav_drawer_checked_text";
        private static final String PREF_AE_NAV_DRAWER_UNCHECKED_TEXT = "ae_nav_drawer_unchecked_text";
        private static final String PREF_AE_SETTINGS_SUMMARY = "ae_settings_summary";

        private static final int DEFAULT_NAV_HEADER_COLOR = 0xFF303030;

        private SwitchPreference mCustomColors;
        private SeekBarPreferenceCham mNavDrawerOpacity;
        private ColorPickerPreference mNavDrawerBgColor;
        private SeekBarPreferenceCham mNavHeaderBgImageOpacity;
        private ColorPickerPreference mNavDrawerTextCheckedColor;
        private ColorPickerPreference mNavDrawerTextUncheckedColor;
        private Preference mAeRestoreDefaults;
        private Preference mCustomSummary;
        private String mCustomSummaryText;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();


            int intColor;
            String hexColor;

            mCustomColors = (SwitchPreference) prefSet.findPreference(PREF_AE_CUSTOM_COLORS);
            mCustomColors.setChecked(Settings.System.getInt(resolver,
                    Settings.System.AE_CUSTOM_COLORS, 0) != 0);
            mCustomColors.setOnPreferenceChangeListener(this);

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

            mNavDrawerTextCheckedColor = (ColorPickerPreference) prefSet.findPreference(PREF_AE_NAV_DRAWER_CHECKED_TEXT);
            mNavDrawerTextCheckedColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.AE_NAV_DRAWER_CHECKED_TEXT, 0xffffab00);
            hexColor = String.format("#%08x", (0xffffab00 & intColor));
            mNavDrawerTextCheckedColor.setSummary(hexColor);
            mNavDrawerTextCheckedColor.setNewPreviewColor(intColor);

            mNavDrawerTextUncheckedColor = (ColorPickerPreference) prefSet.findPreference(PREF_AE_NAV_DRAWER_UNCHECKED_TEXT);
            mNavDrawerTextUncheckedColor.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.AE_NAV_DRAWER_UNCHECKED_TEXT, 0xFFFFFFFF);
            hexColor = String.format("#%08x", (0xFFFFFFFF & intColor));
            mNavDrawerTextUncheckedColor.setSummary(hexColor);
            mNavDrawerTextUncheckedColor.setNewPreviewColor(intColor);

            mAeRestoreDefaults = prefSet.findPreference(PREF_AE_SETTINGS_RESTORE_DEFAULTS);

            mCustomSummary = (Preference) prefSet.findPreference(PREF_AE_SETTINGS_SUMMARY);
            updateCustomSummaryTextString();
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mCustomColors) {
                int enabled = ((Boolean) newValue) ? 1 : 0;
                Settings.System.putInt(resolver,
                        Settings.System.AE_CUSTOM_COLORS, enabled);
                return true;
            } else if (preference == mNavDrawerOpacity) {
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
            } else if (preference == mNavDrawerTextCheckedColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_CHECKED_TEXT, intHex);
                return true;
            } else if (preference == mNavDrawerTextUncheckedColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_UNCHECKED_TEXT, intHex);
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
                int intColor;
                String hexColor;

                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_OPACITY, 178);
                mNavDrawerOpacity.setValue(Settings.System.getInt(resolver,
                        Settings.System.AE_NAV_DRAWER_OPACITY, 178));

                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_HEADER_BG_IMAGE_OPACITY, 200);
                mNavHeaderBgImageOpacity.setValue(Settings.System.getInt(resolver,
                        Settings.System.AE_NAV_HEADER_BG_IMAGE_OPACITY, 200));

                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_BG_COLOR, DEFAULT_NAV_HEADER_COLOR);
                intColor = Settings.System.getInt(resolver,
                        Settings.System.AE_NAV_DRAWER_BG_COLOR, DEFAULT_NAV_HEADER_COLOR);
                hexColor = String.format("#%08x", (DEFAULT_NAV_HEADER_COLOR & intColor));
                mNavDrawerBgColor.setSummary(hexColor);
                mNavDrawerBgColor.setNewPreviewColor(intColor);

                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_CHECKED_TEXT, 0xffffab00);
                intColor = Settings.System.getInt(resolver,
                        Settings.System.AE_NAV_DRAWER_CHECKED_TEXT, 0xffffab00);
                hexColor = String.format("#%08x", (0xffffab00 & intColor));
                mNavDrawerTextCheckedColor.setSummary(hexColor);
                mNavDrawerTextCheckedColor.setNewPreviewColor(intColor);

                Settings.System.putInt(resolver,
                        Settings.System.AE_NAV_DRAWER_UNCHECKED_TEXT, 0xffffffff);
                intColor = Settings.System.getInt(resolver,
                        Settings.System.AE_NAV_DRAWER_UNCHECKED_TEXT, 0xffffffff);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mNavDrawerTextUncheckedColor.setSummary(hexColor);
                mNavDrawerTextUncheckedColor.setNewPreviewColor(intColor);

                Snackbar.make(getView(), R.string.values_restored_title,
                        Snackbar.LENGTH_LONG).show();
            } else if (preference == mCustomSummary) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.custom_summary_title);
                alert.setMessage(R.string.custom_summary_explain);

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                input.setText(TextUtils.isEmpty(mCustomSummaryText) ? "" : mCustomSummaryText);
                input.setSelection(input.getText().length());
                alert.setView(input);
                alert.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = ((Spannable) input.getText()).toString().trim();
                                Settings.System.putString(resolver, Settings.System.AE_SETTINGS_SUMMARY, value);
                                updateCustomSummaryTextString();
                            }
                        });
                alert.setNegativeButton(getString(android.R.string.cancel), null);
                alert.show();
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        private void updateCustomSummaryTextString() {
            mCustomSummaryText = Settings.System.getString(
                    getActivity().getContentResolver(), Settings.System.AE_SETTINGS_SUMMARY);

            if (TextUtils.isEmpty(mCustomSummaryText)) {
                mCustomSummary.setSummary(R.string.custom_summary_notset);
            } else {
                mCustomSummary.setSummary(mCustomSummaryText);
            }
        }
    }
}
