package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;

import com.lordclockan.R;

public class StatusBarFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment {

        public SettingsPreferenceFragment() {
        }

        private String PREF_TRAFFIC = "traffic";
        private String PREF_CARRIE_LABEL = "carrierlabel";
        private String PREF_BATTERY_BAR = "batterybar";
        private String PREF_STATUSBAR_WEATHER = "statusbar_weather";
        private static final String KEY_AICP_LOGO_COLOR = "status_bar_aicp_logo_color";

        private Preference mTraffic;
        private Preference mCarrierLabel;
        private Preference mBatteryBar;
        private Preference mStatusbarWeather;
        private ColorPickerPreference mAicpLogoColor;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.statusbar_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mTraffic = prefSet.findPreference(PREF_TRAFFIC);
            mCarrierLabel = prefSet.findPreference(PREF_CARRIE_LABEL);
            mBatteryBar = prefSet.findPreference(PREF_BATTERY_BAR);
            mStatusbarWeather = prefSet.findPreference(PREF_STATUSBAR_WEATHER);

        // Aicp logo color
        mAicpLogoColor =
            (ColorPickerPreference) prefSet.findPreference(KEY_AICP_LOGO_COLOR);
        mAicpLogoColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getContentResolver(),
                Settings.System.STATUS_BAR_AICP_LOGO_COLOR, 0xffffffff);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
            mAicpLogoColor.setSummary(hexColor);
            mAicpLogoColor.setNewPreviewColor(intColor);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mTraffic) {
                Intent intent = new Intent(getActivity(), Traffic.class);
                getActivity().startActivity(intent);
            } else if (preference == mCarrierLabel) {
                Intent intent = new Intent(getActivity(), CarrierLabel.class);
                getActivity().startActivity(intent);
            } else if (preference == mBatteryBar) {
                Intent intent = new Intent(getActivity(), BatteryBar.class);
                getActivity().startActivity(intent);
            } else if (preference == mStatusbarWeather) {
                Intent intent = new Intent(getActivity(), StatusBarWeather.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            } else if (preference == mAicpLogoColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_AICP_LOGO_COLOR, intHex);
            return true;
            }
            return false;
        }
    }
}
