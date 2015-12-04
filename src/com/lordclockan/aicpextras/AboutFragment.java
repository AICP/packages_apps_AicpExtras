package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;

import com.lordclockan.aicpextras.utils.Utils;

import com.lordclockan.R;

public class AboutFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment {

        private String PREF_GCOMMUNITY = "gplus";
        private String PREF_AICP_DOWNLOADS = "aicp_downloads";
        private String PREF_AICP_GERRIT = "aicp_gerrit";
        private String PREF_AICP_CHANGELOG = "aicp_changelog";

        private Preference mGcommunity;
        private Preference mAicpDownloads;
        private Preference mAicpGerrit;
        private Preference mAicpChangeLog;
        private Preference mStatsAicp;

        private static final String PREF_STATS_AICP = "aicp_stats";

        public static final String STATS_PACKAGE_NAME = "com.lordclockan";
        public static Intent INTENT_STATS = new Intent(Intent.ACTION_MAIN)
                .setClassName(STATS_PACKAGE_NAME, STATS_PACKAGE_NAME + ".romstats.AnonymousStats");

        public SettingsPreferenceFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.about_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();

            final ContentResolver resolver = getActivity().getContentResolver();

            mGcommunity = prefSet.findPreference(PREF_GCOMMUNITY);
            mAicpDownloads = prefSet.findPreference(PREF_AICP_DOWNLOADS);
            mAicpGerrit = prefSet.findPreference(PREF_AICP_GERRIT);
            mAicpChangeLog = prefSet.findPreference(PREF_AICP_CHANGELOG);
            mStatsAicp = prefSet.findPreference(PREF_STATS_AICP);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mGcommunity) {
                String url = "https://plus.google.com/communities/101008638920580274588";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else if (preference == mAicpDownloads) {
                String mDevice = Utils.getDevice(getContext());
                String url = "http://dwnld.aicp-rom.com/?device=" + mDevice;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else if (preference == mAicpGerrit) {
                String url = "http://gerrit.aicp-rom.com/#/q/status:open";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } else if (preference == mAicpChangeLog) {
                Intent intent = new Intent(getActivity(), ChangeLogActivity.class);
                getActivity().startActivity(intent);
            } else if (preference == mStatsAicp) {
                startActivity(INTENT_STATS);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            return false;
        }
    }
}
