package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import cyanogenmod.providers.CMSettings;

public class NotificationsFragment extends Fragment {

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

        private static final String PREF_QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";

        private SwitchPreference mBrightnessSlider;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.notifications_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();

            final ContentResolver resolver = getActivity().getContentResolver();

            // Brightness slider
            mBrightnessSlider = (SwitchPreference) prefSet.findPreference(PREF_QS_SHOW_BRIGHTNESS_SLIDER);
            mBrightnessSlider.setChecked(CMSettings.System.getIntForUser(resolver,
                    CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) == 1);
            mBrightnessSlider.setOnPreferenceChangeListener(this);
            int brightnessSlider = CMSettings.System.getIntForUser(resolver,
                    CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT);
            updateBrightnessSliderSummary(brightnessSlider);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBrightnessSlider) {
                CMSettings.System.putIntForUser(resolver,
                        CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER,
                        (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
                int brightnessSlider = CMSettings.System.getIntForUser(resolver,
                        CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1,
                        UserHandle.USER_CURRENT);
                updateBrightnessSliderSummary(brightnessSlider);
                return true;
            }
            return false;
        }

        private void updateBrightnessSliderSummary(int value) {
            String summary = value != 0
                    ? getResources().getString(R.string.qs_brightness_slider_enabled)
                    : getResources().getString(R.string.qs_brightness_slider_disabled);
            mBrightnessSlider.setSummary(summary);
        }
    }
}
