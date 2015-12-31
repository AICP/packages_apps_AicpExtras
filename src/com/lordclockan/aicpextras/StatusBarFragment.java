package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import net.margaritov.preference.colorpicker.ColorPickerPreference;
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

    public static class SettingsPreferenceFragment extends PreferenceFragment 
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {
        }

        private String PREF_TRAFFIC = "traffic";
        private String PREF_CARRIE_LABEL = "carrierlabel";
        private String PREF_BATTERY_BAR = "batterybar";
        private String PREF_STATUSBAR_WEATHER = "statusbar_weather";
        private static final String KEY_AICP_LOGO_COLOR = "status_bar_aicp_logo_color";
        private static final String KEY_AICP_LOGO_STYLE = "status_bar_aicp_logo_style";

        private Preference mTraffic;
        private Preference mCarrierLabel;
        private Preference mBatteryBar;
        private Preference mStatusbarWeather;
        private ColorPickerPreference mAicpLogoColor;
        private ListPreference mAicpLogoStyle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.statusbar_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();

            mTraffic = prefSet.findPreference(PREF_TRAFFIC);
            mCarrierLabel = prefSet.findPreference(PREF_CARRIE_LABEL);
            mBatteryBar = prefSet.findPreference(PREF_BATTERY_BAR);
            mStatusbarWeather = prefSet.findPreference(PREF_STATUSBAR_WEATHER);

            mAicpLogoStyle = (ListPreference) findPreference(KEY_AICP_LOGO_STYLE);
            int aicpLogoStyle = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.STATUS_BAR_AICP_LOGO_STYLE, 0,
                    UserHandle.USER_CURRENT);
            mAicpLogoStyle.setValue(String.valueOf(aicpLogoStyle));
            mAicpLogoStyle.setSummary(mAicpLogoStyle.getEntry());
            mAicpLogoStyle.setOnPreferenceChangeListener(this);

            // Aicp logo color
            mAicpLogoColor =
                (ColorPickerPreference) prefSet.findPreference(KEY_AICP_LOGO_COLOR);
            mAicpLogoColor.setOnPreferenceChangeListener(this);
            int intColor = Settings.System.getInt(resolver,
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
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mAicpLogoColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_AICP_LOGO_COLOR, intHex);
                return true;
            } else if (preference == mAicpLogoStyle) {
                int aicpLogoStyle = Integer.valueOf((String) newValue);
                int index = mAicpLogoStyle.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(
                        getContentResolver(), Settings.System.STATUS_BAR_AICP_LOGO_STYLE, aicpLogoStyle,
                        UserHandle.USER_CURRENT);
                mAicpLogoStyle.setSummary(
                        mAicpLogoStyle.getEntries()[index]);
                return true;
            }
            return false;
        }
    }
}
