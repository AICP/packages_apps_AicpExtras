package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.utils.Utils;
import com.lordclockan.aicpextras.SuShell;

public class LogThatShitFragment extends Fragment {

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

        private static final String TAG = "LogThatShitFragment";

        private static final String PREF_LOGCAT = "logcat";
        private static final String PREF_KMSG = "kmseg";
        private static final String PREF_DMESG = "dmesg";
        private static final String PREF_AICP_LOG_IT = "aicp_log_it";

        private static final String LOGCAT_FILE = new File(Environment
            .getExternalStorageDirectory(), "aicp_logcat.txt").getAbsolutePath();
        private static final String KMSG_FILE = new File(Environment
            .getExternalStorageDirectory(), "aicp_kmsg.txt").getAbsolutePath();
        private static final String DMESG_FILE = new File(Environment
            .getExternalStorageDirectory(), "aicp_dmesg.txt").getAbsolutePath();

        private static final String HASTE_LOGCAT_KEY = new File(Environment
                .getExternalStorageDirectory(), "aicp_haste_logcat_key").getAbsolutePath();
        private static final String HASTE_KMSG_KEY = new File(Environment
                .getExternalStorageDirectory(), "aicp_haste_kmsg_key").getAbsolutePath();
        private static final String HASTE_DMESG_KEY = new File(Environment
                .getExternalStorageDirectory(), "aicp_haste_dmesg_key").getAbsolutePath();

        private static final String AICP_HASTE = "http://haste.aicp-rom.com/documents";
        private static final File sdCardDirectory = Environment.getExternalStorageDirectory();
        private static final File logcatFile = new File(sdCardDirectory, "aicp_logcat.txt");
        private static final File logcatHasteKey = new File(sdCardDirectory, "aicp_haste_logcat_key");
        private static final File dmesgFile = new File(sdCardDirectory, "aicp_dmesg.txt");
        private static final File dmesgHasteKey = new File(sdCardDirectory, "aicp_haste_dmesg_key");
        private static final File kmsgFile = new File(sdCardDirectory, "aicp_kmsg.txt");
        private static final File kmsgHasteKey = new File(sdCardDirectory, "aicp_haste_kmsg_key");

        private CheckBoxPreference mLogcat;
        private int logcat = 0;
        private CheckBoxPreference mKmsg;
        private int kmsg = 0;
        private CheckBoxPreference mDmesg;
        private int dmesg = 0;
        private Preference mAicpLogIt;

        private int value = 0;
        private String sharingIntentString;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.log_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mLogcat = (CheckBoxPreference) prefSet.findPreference(PREF_LOGCAT);
            mKmsg = (CheckBoxPreference) prefSet.findPreference(PREF_KMSG);
            mDmesg = (CheckBoxPreference) prefSet.findPreference(PREF_DMESG);
            mAicpLogIt = prefSet.findPreference(PREF_AICP_LOG_IT);

            resetValues();

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mLogcat) {
                if (mLogcat.isChecked()) {
                    logcat = 1;
                } else {
                    logcat = 0;
                }
            } else if (preference == mKmsg) {
                if (mKmsg.isChecked()) {
                    kmsg = 4;
                } else {
                    kmsg = 0;
                }
            } else if (preference == mDmesg) {
                if (mDmesg.isChecked()) {
                    dmesg = 8;
                } else {
                    dmesg = 0;
                }
            } else if (preference == mAicpLogIt) {
                checkedPreferenceValues();
                switch (value) {
                    case 1:
                        makeLogcat();
                        try {
                            sharingIntentString = "Logcat: " + Helpers.readStringFromFile(logcatHasteKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        makeKmsg();
                        try {
                            sharingIntentString = "Kmsg: " + Helpers.readStringFromFile(kmsgHasteKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 8:
                        makeDmesg();
                        try {
                            sharingIntentString = "Dmesg: " + Helpers.readStringFromFile(dmesgHasteKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 5:
                        makeLogcat();
                        makeKmsg();
                        try {
                            sharingIntentString = "Logcat: " + Helpers.readStringFromFile(logcatHasteKey) +
                                    "\nKmsg: " + Helpers.readStringFromFile(kmsgHasteKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 9:
                        makeLogcat();
                        makeDmesg();
                        try {
                            sharingIntentString = "Logcat: " + Helpers.readStringFromFile(logcatHasteKey) +
                                    "\nDmesg: " + Helpers.readStringFromFile(dmesgHasteKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 13:
                        makeLogcat();
                        makeKmsg();
                        makeDmesg();
                        try {
                            sharingIntentString = "Logcat: " + Helpers.readStringFromFile(logcatHasteKey) +
                                    "\nKmsg: " + Helpers.readStringFromFile(kmsgHasteKey) +
                                    "\nDmesg: " + Helpers.readStringFromFile(dmesgHasteKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 12:
                        makeKmsg();
                        makeDmesg();
                        try {
                            sharingIntentString = "Kmsg: " + Helpers.readStringFromFile(kmsgHasteKey) +
                                    "\nDmesg: " + Helpers.readStringFromFile(dmesgHasteKey);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        Toast.makeText(getActivity(), "What??", Toast.LENGTH_LONG).show();
                        break;
                }
                if (value != 0) {
                    logItDialog();
                }
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            return false;
        }

        public void logItDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.log_it_dialog_title);
            builder.setMessage(R.string.logcat_warning);
            builder.setPositiveButton(R.string.share_title, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                    sharingIntent.setType("text/plain");
                    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.log_it_share_subject);
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, sharingIntentString);
                    startActivity(Intent.createChooser(sharingIntent, "Share via"));
                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        public void makeLogcat() {
            SuShell.runWithSu("logcat -d > " + LOGCAT_FILE +  "&& curl -s -X POST -T " + LOGCAT_FILE + " " + AICP_HASTE + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > " + HASTE_LOGCAT_KEY);
        }

        public void makeKmsg() {
            SuShell.runWithSu("cat /proc/last_kmsg > " + KMSG_FILE +  "&& curl -s -X POST -T " + KMSG_FILE + " " + AICP_HASTE + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > " + HASTE_KMSG_KEY);
        }

        public void makeDmesg() {
            SuShell.runWithSu("dmesg > " + DMESG_FILE +  "&& curl -s -X POST -T " + DMESG_FILE + " " + AICP_HASTE + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > " + HASTE_DMESG_KEY);
        }

        public void checkedPreferenceValues() {
            value = logcat + kmsg + dmesg;
        }

        public void resetValues() {
            mLogcat.setChecked(false);
            mKmsg.setChecked(false);
            mDmesg.setChecked(false);
            logcat = 0;
            kmsg = 0;
            dmesg = 0;
            value = 0;
        }
    }
}
