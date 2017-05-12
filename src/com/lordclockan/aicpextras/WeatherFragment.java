package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;

import com.lordclockan.R;

public class WeatherFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {
        }

        private static final String CAT_LOCKSCREEN_WEATHER = "lock_screen_weather_category";
        private static final String CAT_HEADER_WEATHER = "header_weather_category";
        private static final String CAT_MISC_WEATHER = "misc_weather_category";

        private static final String PREF_LOCKSCREEN_WEATHER = "lock_screen_show_weather";
        private static final String PREF_HEADER_WEATHER = "header_weather_enabled";
        private static final String PREF_MISC_WINDSPEED = "omnijaws_windspeed_m_s";

        private PreferenceCategory mLockscreenWeatherCategory;
        private PreferenceCategory mHeaderWeatherCategory;
        private PreferenceCategory mMiscWeatherCategory;

        private Preference mLockscreenWeather;
        private Preference mHeaderWeather;
        private Preference mMiscWindspeed;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.weather_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mLockscreenWeatherCategory = (PreferenceCategory) findPreference(CAT_LOCKSCREEN_WEATHER);
            mHeaderWeatherCategory = (PreferenceCategory) findPreference(CAT_HEADER_WEATHER);
            mMiscWeatherCategory = (PreferenceCategory) findPreference(CAT_MISC_WEATHER);
            mLockscreenWeather = prefSet.findPreference(PREF_LOCKSCREEN_WEATHER);
            mHeaderWeather = prefSet.findPreference(PREF_HEADER_WEATHER);
            mMiscWindspeed = prefSet.findPreference(PREF_MISC_WINDSPEED);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            ContentResolver resolver = getActivity().getContentResolver();
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
