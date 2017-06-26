package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.lordclockan.R;

public class RootExtras extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        private static final String TAG = "RootExtras";

        private static final int REQUEST_PICK_BOOT_ANIMATION = 201;

        private static final String SPREF_ROOT_SUCCESS = "root_success_known";

        private static final String SELINUX = "selinux";
        private static final String SELINUX_PERSISTENCE = "selinux_persistence";
        private static final String PREF_CUSTOM_BOOTANIM = "custom_bootanimation";
        private static final String PREF_SYSTEMAPP_REMOVER = "system_app_remover";
        private static final String PREF_LOG_IT = "log_it";

        // Custom bootanimation
        private static final String BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip";
        private static final String BACKUP_PATH = new File(Environment
                .getExternalStorageDirectory(), "/AICP_ota").getAbsolutePath();

        private SwitchPreference mSelinux;
        private SwitchPreference mSelinuxPersistence;
        private Preference mCustomBootAnimation;
        private Preference mSystemappRemover;
        private Preference mLogIt;

        // Custom bootanimation
        private ImageView mView;
        private TextView mError;
        private AlertDialog mCustomBootAnimationDialog;
        private AnimationDrawable mAnimationPart1;
        private AnimationDrawable mAnimationPart2;
        private String mErrormsg;
        private String mBootAnimationPath;

        private Context mContext;

        private Toast mSuDeniedToast;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.root_extras);

            mSuDeniedToast = Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.cannot_get_su_start), Toast.LENGTH_LONG);

            //SELinux
            mSelinux = (SwitchPreference) findPreference(SELINUX);
            mSelinux.setOnPreferenceChangeListener(this);
            mSelinuxPersistence = (SwitchPreference) findPreference(SELINUX_PERSISTENCE);
            mSelinuxPersistence.setOnPreferenceChangeListener(this);
            mSelinuxPersistence.setChecked(getContext()
                    .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
                    .contains("selinux"));

            // Custom bootanimation
            mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM);
            mContext = getActivity();
            resetBootAnimation();

            mSystemappRemover = findPreference(PREF_SYSTEMAPP_REMOVER);
            mLogIt = findPreference(PREF_LOG_IT);

            if (savedInstanceState == null) {
                if (PreferenceManager.getDefaultSharedPreferences(mContext)
                        .getBoolean(SPREF_ROOT_SUCCESS, false)) {
                    // User has used root in the past, so probably knows what they're doing
                    new InitPrefsTask().execute();
                } else {
                    // Show a dialog to the user to inform about root requirement
                    new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.root_extras_msg)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // We're staying here, so do the rest of the initialization
                                        // that requires root
                                        new InitPrefsTask().execute();
                                    }
                            })
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        // User was to lazy to use the buttons to close the dialog,
                                        // so let's assume they know what they're doing and continue
                                        // with initialization, now the part that requires root
                                        new InitPrefsTask().execute();
                                    }
                            })
                            .show();
                    }
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mSelinux) {
                if (newValue.toString().equals("true")) {
                    new SwitchSelinuxTask().execute(true);
                    setSelinuxEnabled(true, mSelinuxPersistence.isChecked());
                } else if (newValue.toString().equals("false")) {
                    new SwitchSelinuxTask().execute(false);
                    setSelinuxEnabled(false, mSelinuxPersistence.isChecked());
                }
                return true;
            } else if (preference == mSelinuxPersistence) {
                setSelinuxEnabled(mSelinux.isChecked(), (Boolean) newValue);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             Preference preference) {
            if (preference == mCustomBootAnimation) {
                openBootAnimationDialog();
                return true;
            } else if (preference == mSystemappRemover) {
                Intent intent = new Intent(getActivity(), SystemappRemover.class);
                startActivity(intent);
                return true;
            } else if (preference == mLogIt) {
                Intent intent = new Intent(getActivity(), SubActivity.class);
                intent.putExtra(SubActivity.EXTRA_FRAGMENT_CLASS,
                        LogThatShitFragment.class.getName());
                intent.putExtra(SubActivity.EXTRA_TITLE,
                        getString(R.string.log_that_shit_title));
                startActivity(intent);
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                    if (data == null) {
                        //Nothing returned by user, probably pressed back button in file manager
                        return;
                    }
                    mBootAnimationPath = data.getData().getPath();
                    if (mBootAnimationPath.contains(":")) {
                        // mBootAnimationPath is not really a path yet, which we need
                        // for copying as root; so copy file to a known path first
                        mBootAnimationPath = BACKUP_PATH + File.separator + "tmpbootanim.zip";

                        InputStream inputStream = null;
                        FileOutputStream outputStream = null;

                        try {
                            inputStream = getActivity().getContentResolver()
                                    .openInputStream(data.getData());
                            outputStream = new FileOutputStream(mBootAnimationPath, false);
                            byte[] buffer = new byte[1024];
                            int length ;
                            while ((length = inputStream.read(buffer)) > 0) {
                                outputStream.write(buffer, 0, length);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (inputStream != null) inputStream.close();
                                if (outputStream != null) outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    openBootAnimationDialog();
                }
            }
        }

        private abstract class SuTask<Params> extends AsyncTask<Params, Void, Void> {

            protected Exception mException = null;

            abstract protected void sudoInBackground(Params... params)
                    throws SuShell.SuDeniedException;

            @Override
            protected Void doInBackground(Params... params) {
                try {
                    sudoInBackground(params);
                } catch (SuShell.SuDeniedException e) {
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                if (mException instanceof SuShell.SuDeniedException) {
                    mSuDeniedToast.show();
                    editor.putBoolean(SPREF_ROOT_SUCCESS, false);
                } else {
                    mSuDeniedToast.cancel();
                    editor.putBoolean(SPREF_ROOT_SUCCESS, true);
                }
                // Remember whether we had success for root required dialog
                editor.apply();
            }
        }

        private class InitPrefsTask extends SuTask<Void> {

            @Override
            protected void sudoInBackground(Void... params) throws SuShell.SuDeniedException {
                boolean selinuxEnforcing =
                        SuShell.runWithSuCheck("getenforce").contains("Enforcing");
                Log.d(TAG, "Detected selinux enforcing status: " + selinuxEnforcing);
                getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSelinuxStatus(selinuxEnforcing);
                        }
                });
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (mException instanceof SuShell.SuDeniedException) {
                    // Since we cannot get root access while trying to set up the fragment, leave
                    leave();
                }
            }
        }

        private void setSelinuxStatus(boolean status) {
            mSelinux.setChecked(status);
            if (status) {
                mSelinux.setSummary(R.string.selinux_enforcing_title);
            } else {
                mSelinux.setSummary(R.string.selinux_permissive_title);
            }
        }

        private void setSelinuxEnabled(boolean status, boolean persistent) {
            SharedPreferences.Editor editor =
                    getContext().getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
            if (persistent) {
                editor.putBoolean("selinux", status);
            } else {
                editor.remove("selinux");
            }
            editor.apply();
            setSelinuxStatus(status);
        }

        private class SwitchSelinuxTask extends SuTask<Boolean> {

            @Override
            protected void sudoInBackground(Boolean... params) {
                if (params.length != 1) {
                    Log.e(TAG, "SwitchSelinuxTask: invalid params count");
                    return;
                }
                try {
                    if (params[0]) {
                        SuShell.runWithSuCheck("setenforce 1");
                    } else {
                        SuShell.runWithSuCheck("setenforce 0");
                    }
                } catch (SuShell.SuDeniedException e) {
                    mException = e;
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (mException instanceof SuShell.SuDeniedException) {
                    // Did not work, so return to previous value
                    setSelinuxEnabled(!mSelinux.isChecked(), mSelinuxPersistence.isChecked());
                }
            }
        }


        /**
         * Resets boot animation path. Essentially clears temporary-set boot animation
         * set by the user from the dialog.
         *
         * @return returns true if a boot animation exists (user or system). false otherwise.
         */
        private boolean resetBootAnimation() {
            boolean bootAnimationExists = false;
            if (new File(BOOTANIMATION_SYSTEM_PATH).exists()) {
                mBootAnimationPath = BOOTANIMATION_SYSTEM_PATH;
                bootAnimationExists = true;
            } else {
                mBootAnimationPath = "";
            }
            return bootAnimationExists;
        }

        private void openBootAnimationDialog() {
            Log.d(TAG, "boot animation path: " + mBootAnimationPath);
            if (mCustomBootAnimationDialog != null) {
                mCustomBootAnimationDialog.cancel();
                mCustomBootAnimationDialog = null;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.bootanimation_preview);
            if (!mBootAnimationPath.isEmpty()
                    && (!BOOTANIMATION_SYSTEM_PATH.equalsIgnoreCase(mBootAnimationPath))) {
                builder.setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        installBootAnim(dialog, mBootAnimationPath);
                        resetBootAnimation();
                    }
                });
            }
            builder.setNeutralButton(R.string.set_custom_bootanimation,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            PackageManager packageManager = getActivity().getPackageManager();
                            Intent test = new Intent(Intent.ACTION_GET_CONTENT);
                            test.setType("application/zip");
                            List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                                    PackageManager.GET_ACTIVITIES);
                            if (!list.isEmpty()) {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                                intent.setType("application/zip");
                                startActivityForResult(intent, REQUEST_PICK_BOOT_ANIMATION);
                            } else {
                                //No app installed to handle the intent - file explorer required
                                Snackbar.make(getView(), R.string.install_file_manager_error,
                                        Snackbar.LENGTH_SHORT).show();
                            }

                        }
                    });
            builder.setNegativeButton(com.android.internal.R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            resetBootAnimation();
                            dialog.dismiss();
                        }
                    });
            LayoutInflater inflater =
                    (LayoutInflater) getActivity()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.dialog_bootanimation_preview,
                    (ViewGroup) getActivity()
                            .findViewById(R.id.bootanimation_layout_root));
            mError = (TextView) layout.findViewById(R.id.textViewError);
            mView = (ImageView) layout.findViewById(R.id.imageViewPreview);
            mView.setVisibility(View.GONE);
            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            mView.setLayoutParams(new LinearLayout.LayoutParams(size.x / 2, size.y / 2));
            mError.setText(R.string.creating_preview);
            builder.setView(layout);
            mCustomBootAnimationDialog = builder.create();
            mCustomBootAnimationDialog.setOwnerActivity(getActivity());
            mCustomBootAnimationDialog.show();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    createPreview(mBootAnimationPath);
                }
            });
            thread.start();
        }

        private void createPreview(String path) {
            File zip = new File(path);
            ZipFile zipfile = null;
            String desc = "";
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                zipfile = new ZipFile(zip);
                ZipEntry ze = zipfile.getEntry("desc.txt");
                inputStream = zipfile.getInputStream(ze);
                inputStreamReader = new InputStreamReader(inputStream);
                StringBuilder sb = new StringBuilder(0);
                bufferedReader = new BufferedReader(inputStreamReader);
                String read = bufferedReader.readLine();
                while (read != null) {
                    sb.append(read);
                    sb.append('\n');
                    read = bufferedReader.readLine();
                }
                desc = sb.toString();
            } catch (Exception handleAllException) {
                handleAllException.printStackTrace();
                mErrormsg = getActivity().getString(R.string.error_reading_zip_file);
                errorHandler.sendEmptyMessage(0);
                return;
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (IOException e) {
                    // we tried
                }
                try {
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                } catch (IOException e) {
                    // we tried
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    // moving on...
                }
            }

            String[] info = desc.replace("\\r", "").split("\\n");
            // ignore first two ints height and width
            int delay = Integer.parseInt(info[0].split(" ")[2]);
            String partName1 = info[1].split(" ")[3];
            String partName2;
            try {
                if (info.length > 2) {
                    partName2 = info[2].split(" ")[3];
                } else {
                    partName2 = "";
                }
            } catch (Exception e) {
                partName2 = "";
            }

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 4;
            mAnimationPart1 = new AnimationDrawable();
            mAnimationPart2 = new AnimationDrawable();
            try {
                for (Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
                     enumeration.hasMoreElements(); ) {
                    ZipEntry entry = enumeration.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    String partname = entry.getName().split("/")[0];
                    if (partName1.equalsIgnoreCase(partname)) {
                        InputStream partOneInStream = null;
                        try {
                            partOneInStream = zipfile.getInputStream(entry);
                            mAnimationPart1.addFrame(new BitmapDrawable(getResources(),
                                    BitmapFactory.decodeStream(partOneInStream,
                                            null, opt)), delay);
                        } finally {
                            if (partOneInStream != null) {
                                partOneInStream.close();
                            }
                        }
                    } else if (partName2.equalsIgnoreCase(partname)) {
                        InputStream partTwoInStream = null;
                        try {
                            partTwoInStream = zipfile.getInputStream(entry);
                            mAnimationPart2.addFrame(new BitmapDrawable(getResources(),
                                    BitmapFactory.decodeStream(partTwoInStream,
                                            null, opt)), delay);
                        } finally {
                            if (partTwoInStream != null) {
                                partTwoInStream.close();
                            }
                        }
                    }
                }
            } catch (IOException e1) {
                mErrormsg = getActivity().getString(R.string.error_creating_preview);
                errorHandler.sendEmptyMessage(0);
                return;
            }

            if (!partName2.isEmpty()) {
                Log.d(TAG, "Multipart Animation");
                mAnimationPart1.setOneShot(false);
                mAnimationPart2.setOneShot(false);
                mAnimationPart1.setOnAnimationFinishedListener(
                        new AnimationDrawable.OnAnimationFinishedListener() {
                            @Override
                            public void onAnimationFinished() {
                                Log.d(TAG, "First part finished");
                                mView.setImageDrawable(mAnimationPart2);
                                mAnimationPart1.stop();
                                mAnimationPart2.start();
                            }
                        });
            } else {
                mAnimationPart1.setOneShot(false);
            }
            finishedHandler.sendEmptyMessage(0);
        }

        private Handler errorHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mView.setVisibility(View.GONE);
                mError.setText(mErrormsg);
            }
        };

        private Handler finishedHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mView.setImageDrawable(mAnimationPart1);
                mView.setVisibility(View.VISIBLE);
                mError.setVisibility(View.GONE);
                mAnimationPart1.start();
            }
        };

        private void installBootAnim(DialogInterface dialog, String bootAnimationPath) {
            DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
            Date date = new Date();
            String current = (dateFormat.format(date));
            new InstallBootAnimTask().execute(current, bootAnimationPath);
        }

        private class InstallBootAnimTask extends AsyncTask<String, Void, Void> {
            private Exception mException = null;

            @Override
            protected Void doInBackground(String... params) {
                if (params.length != 2) {
                    Log.e(TAG, "InstallBootAnimTask: invalid params count");
                    return null;
                }
                String current = params[0];
                String bootAnimationPath = params[1];
                try {
                    SuShell.runWithSuCheck("mount -o rw,remount /system",
                            "cp -f /system/media/bootanimation.zip " + BACKUP_PATH + "/bootanimation_backup_" + current + ".zip",
                            "cp -f " + bootAnimationPath + " /system/media/bootanimation.zip",
                            "chmod 644 /system/media/bootanimation.zip",
                            "mount -o ro,remount /system");
                } catch (SuShell.SuDeniedException e) {
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void param) {
                super.onPreExecute();
                if (mException instanceof SuShell.SuDeniedException) {
                    Toast.makeText(getActivity(), getString(R.string.cannot_get_su_start), Toast.LENGTH_LONG).show();
                } else if (mException == null) {
                    Snackbar.make(getView(), R.string.bootanim_install_successful,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        }

        private void leave() {
            // Leave screen by restarting the activity - this should take us
            // to the initially viewed fragment when opening the activity
            getActivity().finish();
            startActivity(getActivity().getIntent());
        }
    }
}
