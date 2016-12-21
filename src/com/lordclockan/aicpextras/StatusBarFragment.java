package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import net.margaritov.preference.colorpicker.ColorPickerPreference;
import android.support.v4.app.Fragment;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;

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

        private static final String PREF_TRAFFIC = "traffic";
        private static final String KEY_SHOW_FOURG = "show_fourg";
        private static final String PREF_BATTERY_BAR = "batterybar";
        private static final String KEY_AICP_LOGO_COLOR = "status_bar_aicp_logo_color";
        private static final String KEY_AICP_LOGO_STYLE = "status_bar_aicp_logo_style";
        private static final String PREF_CARRIE_LABEL = "carrierlabel";
        private static final String PREF_TICKER = "ticker";

        private Preference mTraffic;
        private SwitchPreference mShowFourG;
        private Preference mBatteryBar;
        private ColorPickerPreference mAicpLogoColor;
        private ListPreference mAicpLogoStyle;
        private Preference mCarrierLabel;
        private Preference mTicker;

        public SettingsPreferenceFragment() {
        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.statusbar_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();
            Context context = getActivity();
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            mTraffic = prefSet.findPreference(PREF_TRAFFIC);
            mBatteryBar = prefSet.findPreference(PREF_BATTERY_BAR);
            mCarrierLabel = prefSet.findPreference(PREF_CARRIE_LABEL);
            mTicker = prefSet.findPreference(PREF_TICKER);

            // Show 4G
            mShowFourG = (SwitchPreference) prefSet.findPreference(KEY_SHOW_FOURG);
            PackageManager pm = getActivity().getPackageManager();
            if (!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                prefSet.removePreference(mShowFourG);
            }

            mAicpLogoStyle = (ListPreference) findPreference(KEY_AICP_LOGO_STYLE);
            int aicpLogoStyle = Settings.System.getIntForUser(resolver,
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

            if (!cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
                prefSet.removePreference(mCarrierLabel);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mTraffic) {
                Intent intent = new Intent(getActivity(), Traffic.class);
                getActivity().startActivity(intent);
                return true;
            } else if (preference == mBatteryBar) {
                Intent intent = new Intent(getActivity(), BatteryBar.class);
                getActivity().startActivity(intent);
                return true;
            } else if (preference == mCarrierLabel) {
                Intent intent = new Intent(getActivity(), CarrierLabel.class);
                getActivity().startActivity(intent);
            } else if (preference == mTicker) {
                Intent intent = new Intent(getActivity(), Ticker.class);
                getActivity().startActivity(intent);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mAicpLogoColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.parseInt(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_AICP_LOGO_COLOR, intHex);
                return true;
            } else if (preference == mAicpLogoStyle) {
                int aicpLogoStyle = Integer.parseInt((String) newValue);
                int index = mAicpLogoStyle.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(
                        resolver, Settings.System.STATUS_BAR_AICP_LOGO_STYLE, aicpLogoStyle,
                        UserHandle.USER_CURRENT);
                mAicpLogoStyle.setSummary(
                        mAicpLogoStyle.getEntries()[index]);
                return true;
            }
            return false;
        }
    }
}
