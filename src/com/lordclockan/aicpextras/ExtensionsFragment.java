package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;

import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.R;

import android.net.Uri;

public class ExtensionsFragment extends Fragment {

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

        private static final String PREF_APP_SIDE_BAR = "app_side_bar";
        private static final String PREF_PIE = "pa_pie_control";
        private static final String PREF_APP_CIRCLE_BAR = "app_circle_bar";
        private static final String PREF_WAKEBLOCK_APP = "wakeblock_app";

        private static final String WAKEBLOCK_APP_PACKAGE = "com.giovannibozzano.wakeblock";

        private Preference mAppSideBar;
        private Preference mPie;
        private Preference mAppCircleBar;
        private Preference mWakeblockApp;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.extensions_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mAppSideBar = prefSet.findPreference(PREF_APP_SIDE_BAR);
            mPie = prefSet.findPreference(PREF_PIE);
            mAppCircleBar = prefSet.findPreference(PREF_APP_CIRCLE_BAR);
            mWakeblockApp = prefSet.findPreference(PREF_WAKEBLOCK_APP);

        }

        @Override
        public void onResume() {
            super.onResume();

            updateWakeblockAppSummary();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            ContentResolver resolver = getActivity().getContentResolver();
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mAppSideBar) {
                Intent intent = new Intent(getActivity(), AppSidebar.class);
                getActivity().startActivity(intent);
            } else if (preference == mPie) {
                Intent intent = new Intent(getActivity(), PieControl.class);
                getActivity().startActivity(intent);
            } else if (preference == mAppCircleBar) {
                Intent intent = new Intent(getActivity(), AppCircleBar.class);
                getActivity().startActivity(intent);
            } else if (preference == mWakeblockApp) {
                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(
                        WAKEBLOCK_APP_PACKAGE);
                if (intent == null) {
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + WAKEBLOCK_APP_PACKAGE));
                }
                startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return true;
        }

        private void updateWakeblockAppSummary() {
            if (Helpers.isPackageInstalled(WAKEBLOCK_APP_PACKAGE, getActivity().getPackageManager())) {
                mWakeblockApp.setSummary(R.string.wakeblock_app_installed_summary);
            } else {
                mWakeblockApp.setSummary(R.string.wakeblock_app_not_installed_summary);
            }
        }

    }
}
