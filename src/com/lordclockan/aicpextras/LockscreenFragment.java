package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

import com.lordclockan.R;

public class LockscreenFragment extends Fragment {

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

        private static final String KEY_LOCKSCREEN_BLUR_RADIUS = "lockscreen_blur_radius";
        private static final String PREF_LOCKSCREEN_WEATHER = "lockscreen_weather";

        private SeekBarPreferenceCham mBlurRadius;
        private Preference mLockscreenWeather;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.lockscreen_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mBlurRadius = (SeekBarPreferenceCham) findPreference(KEY_LOCKSCREEN_BLUR_RADIUS);
            mBlurRadius.setValue(Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_BLUR_RADIUS, 14));
            mBlurRadius.setOnPreferenceChangeListener(this);

            mLockscreenWeather = prefSet.findPreference(PREF_LOCKSCREEN_WEATHER);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBlurRadius) {
                int width = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.LOCKSCREEN_BLUR_RADIUS, width);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mLockscreenWeather) {
                Intent intent = new Intent(getActivity(), Weather.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }
    }
}
