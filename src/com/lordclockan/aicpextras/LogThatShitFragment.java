package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.utils.Utils;
import com.lordclockan.aicpextras.SuShell;

public class LogThatShitFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = LogThatShitFragment.class.getSimpleName();

    private static final String PREF_LOGCAT = "logcat";
    private static final String PREF_KMSG = "kmseg";
    private static final String PREF_DMESG = "dmesg";
    private static final String PREF_AICP_LOG_IT = "aicp_log_it";
    private static final String PREF_SHARE_TYPE = "aicp_log_share_type";

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
    private static final File shareZipFile = new File(sdCardDirectory, "aicp_logs.zip");

    private static final int HASTE_MAX_LOG_SIZE = 400000;

    private CheckBoxPreference mLogcat;
    private CheckBoxPreference mKmsg;
    private CheckBoxPreference mDmesg;
    private Preference mAicpLogIt;
    private ListPreference mShareType;

    private String sharingIntentString;

    private boolean shareHaste = false;
    private boolean shareZip = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.log_layout);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mLogcat = (CheckBoxPreference) prefSet.findPreference(PREF_LOGCAT);
        mLogcat.setOnPreferenceChangeListener(this);
        mKmsg = (CheckBoxPreference) prefSet.findPreference(PREF_KMSG);
        mKmsg.setOnPreferenceChangeListener(this);
        mDmesg = (CheckBoxPreference) prefSet.findPreference(PREF_DMESG);
        mDmesg.setOnPreferenceChangeListener(this);
        mAicpLogIt = prefSet.findPreference(PREF_AICP_LOG_IT);
        mShareType = (ListPreference) prefSet.findPreference(PREF_SHARE_TYPE);
        mShareType.setOnPreferenceChangeListener(this);

        resetValues();

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLogcat) {
            mAicpLogIt.setEnabled((Boolean) newValue || mKmsg.isChecked() || mDmesg.isChecked());
            return true;
        } else if (preference == mKmsg) {
            mAicpLogIt.setEnabled((Boolean) newValue || mLogcat.isChecked() || mDmesg.isChecked());
            return true;
        } else if (preference == mDmesg) {
            mAicpLogIt.setEnabled((Boolean) newValue || mLogcat.isChecked() || mKmsg.isChecked());
            return true;
        } else if (preference == mShareType) {
            if ("0".equals(newValue)) {
                mShareType.setSummary(getString(R.string.log_it_share_type_haste));
                shareHaste = true;
                shareZip = false;
            } else if ("1".equals(newValue)) {
                mShareType.setSummary(getString(R.string.log_it_share_type_zip));
                shareHaste = false;
                shareZip = true;
            } else {
                mShareType.setSummary("");
                shareHaste = false;
                shareZip = false;
            }
            return true;
        } else {
            return false;
        }
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

    public void logZipDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.log_it_dialog_title);
        builder.setMessage(R.string.logcat_warning);
        builder.setPositiveButton(R.string.share_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("application/zip");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.log_it_share_subject);
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareZipFile));
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void makeLogcat() throws SuShell.SuDeniedException, IOException {
        String command = "logcat -d";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + LOGCAT_FILE
                    + "&& curl -s -X POST -T " + LOGCAT_FILE + " " + AICP_HASTE
                    + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > "
                            + HASTE_LOGCAT_KEY;
        } else {
            command += " > " + LOGCAT_FILE;
        }
        SuShell.runWithSuCheck(command);
    }

    public void makeKmsg() throws SuShell.SuDeniedException, IOException {
        String command = "cat /proc/last_kmsg";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + KMSG_FILE
                    + " && curl -s -X POST -T " + KMSG_FILE + " " + AICP_HASTE
                    + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > "
                            + HASTE_KMSG_KEY;
        } else {
            command += " > " + KMSG_FILE;
        }
        SuShell.runWithSuCheck(command);
    }

    public void makeDmesg() throws SuShell.SuDeniedException, IOException {
        String command = "dmesg";
        if (shareHaste) {
            command += " | tail -c " + HASTE_MAX_LOG_SIZE + " > " + DMESG_FILE
                    + "&& curl -s -X POST -T " + DMESG_FILE + " " + AICP_HASTE
                    + " | cut -d'\"' -f4 | echo \"http://haste.aicp-rom.com/$(cat -)\" > "
                            + HASTE_DMESG_KEY;
        } else {
            command += " > " + DMESG_FILE;
        }
        SuShell.runWithSuCheck(command);
    }

    private void createShareZip(boolean logcat, boolean kmsg, boolean dmesg) throws IOException {

        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(shareZipFile.getAbsolutePath())));
            if (logcat) {
                writeToZip(logcatFile, out);
            }
            if (kmsg) {
                writeToZip(kmsgFile, out);
            }
            if (dmesg) {
                writeToZip(dmesgFile, out);
            }
        } finally {
            if (out != null) out.close();
        }
    }
    private void writeToZip(File file, ZipOutputStream out) throws IOException {
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file.getAbsolutePath()));
            ZipEntry entry = new ZipEntry(file.getName());
            out.putNextEntry(entry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } finally {
            if (in != null) in.close();
        }
    }

    private class CreateLogTask extends AsyncTask<Boolean, Void, String> {

        private Exception mException = null;
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.log_it_logs_in_progress));
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
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
                    if (shareHaste) {
                        sharingIntentString += "\nLogcat: " + Helpers.readStringFromFile(logcatHasteKey);
                    }
                }
                if (params[1]) {
                    makeKmsg();
                    if (shareHaste) {
                        sharingIntentString += "\nKmsg: " + Helpers.readStringFromFile(kmsgHasteKey);
                    }
                }
                if (params[2]) {
                    makeDmesg();
                    if (shareHaste) {
                        sharingIntentString += "\nDmesg: " + Helpers.readStringFromFile(dmesgHasteKey);
                    }
                }
                if (shareZip) {
                    createShareZip(params[0], params[1], params[2]);
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
            super.onPostExecute(param);
            progressDialog.dismiss();
            if (mException instanceof SuShell.SuDeniedException) {
                Toast.makeText(getActivity(), getString(R.string.cannot_get_su_start),
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (shareHaste && param != null && param.length() > 1) {
                sharingIntentString = param.substring(1);
                logItDialog();
            }
            if (shareZip) {
                logZipDialog();
            }
        }
    }

    public void resetValues() {
        mLogcat.setChecked(false);
        mKmsg.setChecked(false);
        mDmesg.setChecked(false);
        mAicpLogIt.setEnabled(false);
        mShareType.setValue("1");
        mShareType.setSummary(mShareType.getEntry());
        shareHaste = false;
        shareZip = true;

    }
}
