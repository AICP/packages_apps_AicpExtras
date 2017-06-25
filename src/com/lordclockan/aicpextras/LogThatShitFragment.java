package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.utils.Utils;
import com.lordclockan.aicpextras.SuShell;

public class LogThatShitFragment extends PreferenceFragment {

    private static final String TAG = LogThatShitFragment.class.getSimpleName();

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
    private CheckBoxPreference mKmsg;
    private CheckBoxPreference mDmesg;
    private Preference mAicpLogIt;

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
        if (preference == mAicpLogIt) {
            new CreateLogTask().execute(mLogcat.isChecked(), mKmsg.isChecked(), mDmesg.isChecked());
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
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

    public void makeLogcat() throws SuShell.SuDeniedException {
        SuShell.runWithSuCheck("logcat -d > " + LOGCAT_FILE +  "&& curl -s -X POST -T " + LOGCAT_FILE + " " + AICP_HASTE + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > " + HASTE_LOGCAT_KEY);
    }

    public void makeKmsg() throws SuShell.SuDeniedException {
        SuShell.runWithSuCheck("cat /proc/last_kmsg > " + KMSG_FILE +  "&& curl -s -X POST -T " + KMSG_FILE + " " + AICP_HASTE + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > " + HASTE_KMSG_KEY);
    }

    public void makeDmesg() throws SuShell.SuDeniedException {
        SuShell.runWithSuCheck("dmesg > " + DMESG_FILE +  "&& curl -s -X POST -T " + DMESG_FILE + " " + AICP_HASTE + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > " + HASTE_DMESG_KEY);
    }

    private class CreateLogTask extends AsyncTask<Boolean, Void, String> {

        private Exception mException = null;

        @Override
        protected String doInBackground(Boolean... params) {
            String sharingIntentString = "";
            if (params.length != 3) {
                Log.e(TAG, "CreateLogTask: invalid argument count");
                return sharingIntentString;
            }
            try {
                if (params[0]) {
                    makeLogcat();
                    sharingIntentString += "\nLogcat: " + Helpers.readStringFromFile(logcatHasteKey);
                }
                if (params[1]) {
                    makeKmsg();
                    sharingIntentString += "\nKmsg: " + Helpers.readStringFromFile(kmsgHasteKey);
                }
                if (params[2]) {
                    makeDmesg();
                    sharingIntentString += "\nDmesg: " + Helpers.readStringFromFile(dmesgHasteKey);
                }
            } catch (SuShell.SuDeniedException e) {
                mException = e;
            } catch (IOException e) {
                e.printStackTrace();
                mException = e;
            }
            return sharingIntentString;
        }

        @Override
        protected void onPostExecute(String param) {
            super.onPreExecute();
            if (mException instanceof SuShell.SuDeniedException) {
                Toast.makeText(getActivity(), getString(R.string.cannot_get_su_start),
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (param != null && param.length() > 1) {
                sharingIntentString = param.substring(1);
                logItDialog();
            }
        }
    }

    public void resetValues() {
        mLogcat.setChecked(false);
        mKmsg.setChecked(false);
        mDmesg.setChecked(false);
    }
}
