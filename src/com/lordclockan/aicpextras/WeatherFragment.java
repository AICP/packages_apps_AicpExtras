package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;

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

        private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";

        private static final String CAT_LOCKSCREEN_WEATHER = "lock_screen_weather_category";
        private static final String CAT_HEADER_WEATHER = "header_weather_category";
        private static final String CAT_STATUSBAR_WEATHER = "statusbar_weather_category";
        private static final String CAT_MISC_WEATHER = "misc_weather_category";

        private static final String PREF_LOCKSCREEN_WEATHER = "lock_screen_show_weather";
        private static final String PREF_HEADER_WEATHER = "header_weather_enabled";
        private static final String PREF_STATUS_BAR_WEATHER = "status_bar_weather";
        private static final String PREF_MISC_WINDSPEED = "omnijaws_windspeed_m_s";
        private static final String PREF_MISC_WINDDIRECTION = "omnijaws_winddirection_display";


        private PreferenceCategory mLockscreenWeatherCategory;
        private PreferenceCategory mHeaderWeatherCategory;
        private PreferenceCategory mStatusBarWeatherCategory;
        private PreferenceCategory mMiscWeatherCategory;

        private ListPreference mStatusBarWeather;
        private Preference mLockscreenWeather;
        private Preference mHeaderWeather;
        private Preference mMiscWindspeed;
        private Preference mMiscWindDirection;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.weather_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mLockscreenWeatherCategory = (PreferenceCategory) findPreference(CAT_LOCKSCREEN_WEATHER);
            mHeaderWeatherCategory = (PreferenceCategory) findPreference(CAT_HEADER_WEATHER);
            mStatusBarWeatherCategory = (PreferenceCategory) findPreference(CAT_STATUSBAR_WEATHER);
            mMiscWeatherCategory = (PreferenceCategory) findPreference(CAT_MISC_WEATHER);
            mLockscreenWeather = prefSet.findPreference(PREF_LOCKSCREEN_WEATHER);
            mHeaderWeather = prefSet.findPreference(PREF_HEADER_WEATHER);
            mMiscWindspeed = prefSet.findPreference(PREF_MISC_WINDSPEED);
            mMiscWindDirection = prefSet.findPreference(PREF_MISC_WINDDIRECTION);

            // Status bar weather
            mStatusBarWeather = (ListPreference) prefSet.findPreference(PREF_STATUS_BAR_WEATHER);
            PackageManager pm = getActivity().getPackageManager();
            if (mStatusBarWeather != null && (!Helpers.isPackageInstalled(WEATHER_SERVICE_PACKAGE, pm))) {
                mStatusBarWeatherCategory.removePreference(mStatusBarWeather);
                mLockscreenWeatherCategory.removePreference(mLockscreenWeather);
                mHeaderWeatherCategory.removePreference(mHeaderWeather);
                mMiscWeatherCategory.removePreference(mMiscWindspeed);
                mMiscWeatherCategory.removePreference(mMiscWindDirection);
            } else {
                int temperatureShow = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP, 0,
                    UserHandle.USER_CURRENT);
                mStatusBarWeather.setValue(String.valueOf(temperatureShow));
                if (temperatureShow == 0) {
                    mStatusBarWeather.setSummary(R.string.statusbar_weather_summary);
                } else {
                    mStatusBarWeather.setSummary(mStatusBarWeather.getEntry());
                }
                mStatusBarWeather.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mStatusBarWeather) {
                int temperatureShow = Integer.valueOf((String) newValue);
                int index = mStatusBarWeather.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver,
                        Settings.System.STATUS_BAR_SHOW_WEATHER_TEMP,
                        temperatureShow, UserHandle.USER_CURRENT);
                if (temperatureShow == 0) {
                    mStatusBarWeather.setSummary(R.string.statusbar_weather_summary);
                } else {
                    mStatusBarWeather.setSummary(
                            mStatusBarWeather.getEntries()[index]);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
