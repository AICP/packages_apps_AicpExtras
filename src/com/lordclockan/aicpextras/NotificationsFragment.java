package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cyanogenmod.providers.CMSettings;

import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import com.lordclockan.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

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

        private static final String TAG = NotificationsFragment.class.getSimpleName();

        private static final String PREF_QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";

        private SwitchPreference mBrightnessSlider;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.notifications_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();

            final ContentResolver resolver = getActivity().getContentResolver();

            // Brightness slider
            mBrightnessSlider = (SwitchPreference) prefSet.findPreference(PREF_QS_SHOW_BRIGHTNESS_SLIDER);
            mBrightnessSlider.setChecked(Settings.System.getIntForUser(resolver,
                    Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) == 1);
            mBrightnessSlider.setOnPreferenceChangeListener(this);
            int brightnessSlider = Settings.System.getIntForUser(resolver,
                    Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT);
            updateBrightnessSliderSummary(brightnessSlider);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBrightnessSlider) {
                Settings.System.putIntForUser(resolver,
                        Settings.System.QS_SHOW_BRIGHTNESS_SLIDER,
                        (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
                int brightnessSlider = Settings.System.getIntForUser(resolver,
                        Settings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1,
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
