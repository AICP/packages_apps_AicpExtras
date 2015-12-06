package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

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

        public SettingsPreferenceFragment() {
        }

        private static final String TAG = "AboutFragment";

        private static final String PREF_HIDDEN_YOGA = "hidden_anim";
        private static final String PREF_AICPLOGO_IMG = "aicp_logo";
        private String PREF_GCOMMUNITY = "gplus";
        private String PREF_AICP_DOWNLOADS = "aicp_downloads";
        private String PREF_AICP_GERRIT = "aicp_gerrit";
        private String PREF_AICP_CHANGELOG = "aicp_changelog";

        private PreferenceScreen mAicpLogo;
        private long[] mHits = new long[3];
        private Preference mGcommunity;
        private Preference mAicpDownloads;
        private Preference mAicpGerrit;
        private Preference mAicpChangeLog;
        private Preference mStatsAicp;

        private static final String PREF_STATS_AICP = "aicp_stats";

        public static final String STATS_PACKAGE_NAME = "com.lordclockan";
        public static Intent INTENT_STATS = new Intent(Intent.ACTION_MAIN)
                .setClassName(STATS_PACKAGE_NAME, STATS_PACKAGE_NAME + ".romstats.AnonymousStats");

        // Package name of the yoga
        public static final String YOGA_PACKAGE_NAME = "com.lordclockan";
        // Intent for launching the yoga actvity
        public static Intent INTENT_YOGA = new Intent(Intent.ACTION_MAIN)
                .setClassName(YOGA_PACKAGE_NAME, YOGA_PACKAGE_NAME + ".aicpextras.HiddenAnimActivity");

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.about_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();

            final ContentResolver resolver = getActivity().getContentResolver();

            mAicpLogo = (PreferenceScreen) findPreference(PREF_AICPLOGO_IMG);

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
            } else if (preference == mAicpLogo) {
                java.lang.System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
                mHits[mHits.length-1] = SystemClock.uptimeMillis();
                if  (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
                    startActivity(INTENT_YOGA);
                }
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            return false;
        }
    }
}
