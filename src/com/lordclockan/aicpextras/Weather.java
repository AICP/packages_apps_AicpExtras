/*
 * Copyright (C) 2015 The Dirty Unicorns Project
 * Copyright (C) 2015 AICP
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.lordclockan.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class Weather extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new WeatherFragment()).commit();
    }

    public static class WeatherFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "LockscreenWeather";

        private static final String PREF_CAT_COLORS = "weather_cat_colors";
        private static final String PREF_CAT_NOTIFICATIONS = "weather_cat_notifications";
        private static final String PREF_SHOW_LOCKSCREN_WEATHER = "weather_lockscreen_show";
        private static final String PREF_SHOW_LOCATION = "weather_show_location";
        private static final String PREF_STATUSBAR_WEATHER = "status_bar_show_weather";
        private static final String PREF_CONDITION_ICON = "weather_condition_icon";
        private static final String PREF_COLORIZE_ALL_ICONS = "weather_colorize_all_icons";
        private static final String PREF_TEXT_COLOR = "weather_text_color";
        private static final String PREF_ICON_COLOR = "weather_icon_color";
        private static final String PREF_RESET_WEATHER = "weather_reset";
        private static final String PREF_HIDE_WEATHER = "weather_hide_panel";
        private static final String PREF_NUMBER_OF_NOTIFICATIONS =
                "weather_number_of_notifications";

        private static final int MONOCHROME_ICON = 0;
        private static final int DEFAULT_COLOR = 0xffffffff;

        private SwitchPreference mShowLockscreenWeather;
        private SwitchPreference mShowLocation;
        private SwitchPreference mShowStatusbarWeather;
        private ListPreference mConditionIcon;
        private SwitchPreference mColorizeAllIcons;
        private ColorPickerPreference mTextColor;
        private ColorPickerPreference mIconColor;
        private Preference mResetWeather;
        private ListPreference mHideWeather;
        private ListPreference mNumberOfNotifications;

        private ContentResolver mResolver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            refreshSettings();
        }

        public void refreshSettings() {
            PreferenceScreen prefs = getPreferenceScreen();
            if (prefs != null) {
                prefs.removeAll();
            }

            addPreferencesFromResource(R.xml.weather);
            mResolver = getActivity().getContentResolver();

            int conditionIcon = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, MONOCHROME_ICON);
            boolean colorizeAllIcons = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS, 0) == 1;

            int intColor;
            String hexColor;

            PreferenceCategory catColors = (PreferenceCategory) findPreference(PREF_CAT_COLORS);
            mTextColor = (ColorPickerPreference) findPreference(PREF_TEXT_COLOR);
            mIconColor = (ColorPickerPreference) findPreference(PREF_ICON_COLOR);

            PreferenceCategory catNotifications =
                    (PreferenceCategory) findPreference(PREF_CAT_NOTIFICATIONS);
            mHideWeather =
                    (ListPreference) findPreference(PREF_HIDE_WEATHER);
            mNumberOfNotifications =
                    (ListPreference) findPreference(PREF_NUMBER_OF_NOTIFICATIONS);

            mShowLockscreenWeather = (SwitchPreference) findPreference(PREF_SHOW_LOCKSCREN_WEATHER);
            mShowLockscreenWeather.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER, 1) == 1);
            mShowLockscreenWeather.setOnPreferenceChangeListener(this);

            mShowLocation = (SwitchPreference) findPreference(PREF_SHOW_LOCATION);
            mShowLocation.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION, 1) == 1);
            mShowLocation.setOnPreferenceChangeListener(this);

            mConditionIcon = (ListPreference) findPreference(PREF_CONDITION_ICON);
            mConditionIcon.setValue(String.valueOf(conditionIcon));
            mConditionIcon.setSummary(mConditionIcon.getEntry());
            mConditionIcon.setOnPreferenceChangeListener(this);

            mColorizeAllIcons = (SwitchPreference) findPreference(PREF_COLORIZE_ALL_ICONS);
            mColorizeAllIcons.setChecked(colorizeAllIcons);
            mColorizeAllIcons.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR,
                    DEFAULT_COLOR);
            mTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setSummary(hexColor);
            mTextColor.setOnPreferenceChangeListener(this);

            mResetWeather = findPreference(PREF_RESET_WEATHER);

            int  hideWeather = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
            mHideWeather.setValue(String.valueOf(hideWeather));
            mHideWeather.setOnPreferenceChangeListener(this);

            if (hideWeather == 0) {
                mHideWeather.setSummary(R.string.weather_hide_panel_auto_summary);
                catNotifications.removePreference(mNumberOfNotifications);
            } else if (hideWeather == 1) {
                int numberOfNotifications = Settings.System.getInt(mResolver,
                       Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, 6);
                mNumberOfNotifications.setValue(String.valueOf(numberOfNotifications));
                mNumberOfNotifications.setSummary(mNumberOfNotifications.getEntry());
                mNumberOfNotifications.setOnPreferenceChangeListener(this);

                mHideWeather.setSummary(getString(R.string.weather_hide_panel_custom_summary,
                        mNumberOfNotifications.getEntry()));
            } else {
                mHideWeather.setSummary(R.string.weather_hide_panel_never_summary);
                catNotifications.removePreference(mNumberOfNotifications);
            }

            if ((conditionIcon == MONOCHROME_ICON)
                    || (conditionIcon != MONOCHROME_ICON && colorizeAllIcons)) {
                intColor = Settings.System.getInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR,
                        DEFAULT_COLOR);
                mIconColor.setNewPreviewColor(intColor);
                hexColor = String.format("#%08x", (0xffffffff & intColor));
                mIconColor.setSummary(hexColor);
                mIconColor.setOnPreferenceChangeListener(this);
            } else {
                catColors.removePreference(mIconColor);
            }

        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean value;
            String hex;
            int intHex;
            int intValue;
            int index;

            if (preference == mShowLockscreenWeather) {
                value = (Boolean) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_SHOW_WEATHER,
                        value ? 1 : 0);
                return true;
            } else if (preference == mShowLocation) {
                value = (Boolean) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION,
                        value ? 1 : 0);
                return true;
            } else if (preference == mConditionIcon) {
                intValue = Integer.valueOf((String) newValue);
                index = mConditionIcon.findIndexOfValue((String) newValue);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, intValue);
                mConditionIcon.setSummary(mConditionIcon.getEntries()[index]);
                refreshSettings();
                return true;
            } else if (preference == mColorizeAllIcons) {
                value = (Boolean) newValue;
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS,
                        value ? 1 : 0);
                refreshSettings();
                return true;
            } else if (preference == mTextColor) {
                hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mIconColor) {
                hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, intHex);
                preference.setSummary(hex);
                return true;
            } else if (preference == mHideWeather) {
                intValue = Integer.valueOf((String) newValue);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, intValue);
                refreshSettings();
                return true;
            } else if (preference == mNumberOfNotifications) {
                intValue = Integer.valueOf((String) newValue);
                Settings.System.putInt(mResolver,
                        Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, intValue);
                refreshSettings();
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mResetWeather) {
                showDialogInner(0);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        private void showDialogInner(int id) {
            DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
            newFragment.setTargetFragment(this, 0);
            newFragment.show(getFragmentManager(), "dialog " + id);
        }

        public static class MyAlertDialogFragment extends DialogFragment {

            public static MyAlertDialogFragment newInstance(int id) {
                MyAlertDialogFragment frag = new MyAlertDialogFragment();
                Bundle args = new Bundle();
                args.putInt("id", id);
                frag.setArguments(args);
                return frag;
            }

            WeatherFragment getOwner() {
            return (WeatherFragment) getTargetFragment();
        }

            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                int id = getArguments().getInt("id");
                switch (id) {
                    case 0:
                        return new AlertDialog.Builder(getActivity())
                                .setTitle(R.string.reset)
                                .setMessage(R.string.reset_values_message)
                                .setNegativeButton(R.string.cancel, null)
                                .setPositiveButton(R.string.reset_lockscreen_weather,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_SHOW_WEATHER, 0);
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION, 1);
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_WEATHER_CONDITION_ICON, 2);
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_WEATHER_COLORIZE_ALL_ICONS, 0);
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_WEATHER_TEXT_COLOR,
                                                        DEFAULT_COLOR);
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR,
                                                        DEFAULT_COLOR);
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
                                                Settings.System.putInt(getOwner().mResolver,
                                                        Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, 6);
                                                getOwner().refreshSettings();
                                            }
                                        })
                                .setNegativeButton(R.string.cancel, null)
                                .create();
                }
                throw new IllegalArgumentException("unknown id " + id);
            }

            @Override
            public void onCancel(DialogInterface dialog) {

            }
        }
    }
}
