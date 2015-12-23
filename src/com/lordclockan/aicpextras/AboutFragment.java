package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.utils.Utils;

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

        private String PREF_AICP_LOGCAT = "aicp_logcat";
        private static final String LOGCAT_FILE = new File(Environment
            .getExternalStorageDirectory(), "aicp_logcat.txt").getAbsolutePath();
        private static final String HASTE_KEY = new File(Environment
                .getExternalStorageDirectory(), "haste_key").getAbsolutePath();
        private static final String AICP_HASTE = "http://haste.aicp-rom.com/documents";
        private static final File sdCardDirectory = Environment.getExternalStorageDirectory();
        private static final File logcatFile = new File(sdCardDirectory, "aicp_logcat.txt");
        private static final File hasteKey = new File(sdCardDirectory, "haste_key");
        protected Process superUser;
        protected DataOutputStream dos;

        private PreferenceScreen mAicpLogo;
        private long[] mHits = new long[3];
        private Preference mGcommunity;
        private Preference mAicpDownloads;
        private Preference mAicpGerrit;
        private Preference mAicpChangeLog;
        private Preference mStatsAicp;
        private Preference mAicpLogcat;

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
            mAicpLogcat = prefSet.findPreference(PREF_AICP_LOGCAT);

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
            } else if (preference == mAicpLogcat) {
                try {
                    superUser = new ProcessBuilder("su", "-c", "/system/bin/sh").start();
                    dos = new DataOutputStream(superUser.getOutputStream());
                    dos.writeBytes("\n" + "ogcat -d > " + LOGCAT_FILE + "&& curl -s -X POST -T " + LOGCAT_FILE +
                    " " + AICP_HASTE + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > " + HASTE_KEY);
                    dos.flush();
                    dos.close();
                        logcatDialog();
		    } catch (Exception e) {
                        e.printStackTrace();
                }
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            return false;
        }

        public void logcatDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.logcat_title);
            builder.setMessage(R.string.logcat_warning);

            builder.setPositiveButton(R.string.make_logcat, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.logcat_share_subject);
                    try {
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, Helpers.readStringFromFile(hasteKey));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
}
