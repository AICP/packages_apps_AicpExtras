/*
 * Copyright (C) 2014-2015 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarWeather extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new StatusBarWeatherFragment()).commit();
    }

    public static class StatusBarWeatherFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "StatusBarWeather";

        private static final String STATUS_BAR_TEMPERATURE = "status_bar_temperature";
        private static final String STATUS_BAR_TEMPERATURE_STYLE = "status_bar_temperature_style";
        private static final String PREF_STATUS_BAR_WEATHER_COLOR = "status_bar_weather_color";
        private static final String PREF_STATUS_BAR_WEATHER_SIZE = "status_bar_weather_size";
        private static final String PREF_STATUS_BAR_WEATHER_FONT_STYLE = "status_bar_weather_font_style";

        private ListPreference mStatusBarTemperature;
        private ListPreference mStatusBarTemperatureStyle;
        private ColorPickerPreference mStatusBarTemperatureColor;
        private SeekBarPreferenceCham mStatusBarTemperatureSize;
        private ListPreference mStatusBarTemperatureFontStyle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.status_bar_weather);

            ContentResolver resolver = getActivity().getContentResolver();

            mStatusBarTemperature = (ListPreference) findPreference(STATUS_BAR_TEMPERATURE);
            int temperatureShow = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0,
                    UserHandle.USER_CURRENT);
            mStatusBarTemperature.setValue(String.valueOf(temperatureShow));
            mStatusBarTemperature.setSummary(mStatusBarTemperature.getEntry());
            mStatusBarTemperature.setOnPreferenceChangeListener(this);

            mStatusBarTemperatureStyle = (ListPreference) findPreference(STATUS_BAR_TEMPERATURE_STYLE);
            int temperatureStyle = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_WEATHER_TEMP_STYLE, 0,
                    UserHandle.USER_CURRENT);
            mStatusBarTemperatureStyle.setValue(String.valueOf(temperatureStyle));
            mStatusBarTemperatureStyle.setSummary(mStatusBarTemperatureStyle.getEntry());
            mStatusBarTemperatureStyle.setOnPreferenceChangeListener(this);

            mStatusBarTemperatureColor =
                    (ColorPickerPreference) findPreference(PREF_STATUS_BAR_WEATHER_COLOR);
            mStatusBarTemperatureColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_COLOR, 0xffffffff);
            String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mStatusBarTemperatureColor.setSummary(hexColor);
            mStatusBarTemperatureColor.setNewPreviewColor(intColor);

            mStatusBarTemperatureSize = (SeekBarPreferenceCham) findPreference(PREF_STATUS_BAR_WEATHER_SIZE);
            mStatusBarTemperatureSize.setValue(Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_SIZE, 14));
            mStatusBarTemperatureSize.setOnPreferenceChangeListener(this);

            mStatusBarTemperatureFontStyle = (ListPreference) findPreference(PREF_STATUS_BAR_WEATHER_FONT_STYLE);
            mStatusBarTemperatureFontStyle.setOnPreferenceChangeListener(this);
            mStatusBarTemperatureFontStyle.setValue(Integer.toString(Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_WEATHER_FONT_STYLE, 0)));
            mStatusBarTemperatureFontStyle.setSummary(mStatusBarTemperatureFontStyle.getEntry());

            updateWeatherOptions();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mStatusBarTemperature) {
                int temperatureShow = Integer.valueOf((String) newValue);
                int index = mStatusBarTemperature.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(
                        resolver, Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, temperatureShow,
                        UserHandle.USER_CURRENT);
                mStatusBarTemperature.setSummary(
                        mStatusBarTemperature.getEntries()[index]);
                updateWeatherOptions();
                return true;
            } else if (preference == mStatusBarTemperatureStyle) {
                int temperatureStyle = Integer.valueOf((String) newValue);
                int index = mStatusBarTemperatureStyle.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(
                        resolver, Settings.System.STATUS_BAR_WEATHER_TEMP_STYLE, temperatureStyle,
                        UserHandle.USER_CURRENT);
                mStatusBarTemperatureStyle.setSummary(
                        mStatusBarTemperatureStyle.getEntries()[index]);
                return true;
            } else if (preference == mStatusBarTemperatureColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_WEATHER_COLOR, intHex);
                return true;
            } else if (preference == mStatusBarTemperatureSize) {
                int width = ((Integer) newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_WEATHER_SIZE, width);
                return true;
            } else if (preference == mStatusBarTemperatureFontStyle) {
                int val = Integer.parseInt((String) newValue);
                int index = mStatusBarTemperatureFontStyle.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_WEATHER_FONT_STYLE, val);
                mStatusBarTemperatureFontStyle.setSummary(mStatusBarTemperatureFontStyle.getEntries()[index]);
                return true;
            }
            return false;
        }

        private void updateWeatherOptions() {
            if (Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0) == 0) {
                mStatusBarTemperatureStyle.setEnabled(false);
                mStatusBarTemperatureColor.setEnabled(false);
                mStatusBarTemperatureSize.setEnabled(false);
                mStatusBarTemperatureFontStyle.setEnabled(false);
            } else {
                mStatusBarTemperatureStyle.setEnabled(true);
                mStatusBarTemperatureColor.setEnabled(true);
                mStatusBarTemperatureSize.setEnabled(true);
                mStatusBarTemperatureFontStyle.setEnabled(true);
            }
        }
    }
}
