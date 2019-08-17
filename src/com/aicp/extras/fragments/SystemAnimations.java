/*
 * Copyright (C) 2017-2018 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.aicp.extras.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.FinishingAnimationDrawable;
import com.aicp.extras.utils.SuShell;
import com.aicp.extras.utils.SuTask;
import com.aicp.extras.utils.Util;

public class SystemAnimations extends BaseSettingsFragment {

    private static final String TAG = SystemAnimations.class.getSimpleName();

    private static final String PREF_CUSTOM_BOOTANIM = "custom_bootanimation";

    // Custom bootanimation
    private static final int REQUEST_PICK_BOOT_ANIMATION = 201;
    private static final String BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip";
    private static final String BACKUP_PATH = new File(Environment
            .getExternalStorageDirectory(), "/AICP_backup").getAbsolutePath();

    private Preference mCustomBootAnimation;

    // Custom bootanimation
    private ImageView mBootanimationView;
    private TextView mBootanimationError;
    private AlertDialog mCustomBootAnimationDialog;
    private FinishingAnimationDrawable mBootanimationPart1;
    private AnimationDrawable mBootanimationPart2;
    private String mBootanimationErrormsg;
    private String mBootAnimationPath;

    @Override
    protected int getPreferenceResource() {
        return R.xml.system_animations;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        // Custom bootanimation
        mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM);
        resetBootAnimation();
        if (new File(BACKUP_PATH).mkdirs()) {
            Log.d(TAG, "Created bootanimation backup dir");
        } else {
            Log.d(TAG, "Did not create bootanimation backup dir");
        }
        Util.requireRoot(getActivity(), mCustomBootAnimation);

    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mCustomBootAnimation) {
            openBootAnimationDialog();
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                if (data == null) {
                    // Nothing returned by user, probably pressed back button in file manager
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

    /**
     * Resets boot animation path. Essentially clears temporary-set boot animation
     * set by the user from the dialog.
     *
     * @return returns true if a boot animation exists (user or system). false otherwise.
     */
    private boolean resetBootAnimation() {
        if (new File(BOOTANIMATION_SYSTEM_PATH).exists()) {
            mBootAnimationPath = BOOTANIMATION_SYSTEM_PATH;
            return true;
        } else {
            mBootAnimationPath = "";
            return false;
        }
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
            builder.setPositiveButton(R.string.bootanimation_apply,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            installBootAnim(dialog, mBootAnimationPath);
                            resetBootAnimation();
                        }
                    });
        }
        builder.setNeutralButton(R.string.bootanimation_set_custom,
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
                            Toast.makeText(getActivity(),
                                    R.string.bootanimation_install_file_manager_error,
                                    Toast.LENGTH_SHORT).show();
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
        mBootanimationError = (TextView) layout.findViewById(R.id.textViewError);
        mBootanimationView = (ImageView) layout.findViewById(R.id.imageViewPreview);
        mBootanimationView.setVisibility(View.GONE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mBootanimationView.setLayoutParams(new LinearLayout.LayoutParams(size.x / 2, size.y / 2));
        mBootanimationError.setText(R.string.bootanimation_creating_preview);
        builder.setView(layout);
        mCustomBootAnimationDialog = builder.create();
        mCustomBootAnimationDialog.setOwnerActivity(getActivity());
        mCustomBootAnimationDialog.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                createBootanimationPreview(mBootAnimationPath);
            }
        });
        thread.start();
    }

    private void createBootanimationPreview(String path) {
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
            mBootanimationErrormsg = getActivity()
                    .getString(R.string.bootanimation_error_reading_zip_file);
            mBootanimationErrorHandler.sendEmptyMessage(0);
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
        mBootanimationPart1 = new FinishingAnimationDrawable();
        mBootanimationPart2 = new AnimationDrawable();
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
                        mBootanimationPart1.addFrame(new BitmapDrawable(getResources(),
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
                        mBootanimationPart2.addFrame(new BitmapDrawable(getResources(),
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
            mBootanimationErrormsg = getActivity()
                    .getString(R.string.bootanimation_error_creating_preview);
            mBootanimationErrorHandler.sendEmptyMessage(0);
            return;
        } finally {
            try {
                if (zipfile != null) {
                    zipfile.close();
                }
            } catch (IOException e) {
                // we tried
            }
        }

        if (!partName2.isEmpty()) {
            Log.d(TAG, "Multipart Animation");
            mBootanimationPart1.setOneShot(false);
            mBootanimationPart2.setOneShot(false);
            mBootanimationPart1.setOnAnimationFinishedListener(
                    new FinishingAnimationDrawable.OnAnimationFinishedListener() {
                        @Override
                        public void onAnimationFinished() {
                            Log.d(TAG, "First part finished");
                            mBootanimationView.setImageDrawable(mBootanimationPart2);
                            mBootanimationPart1.stop();
                            mBootanimationPart2.start();
                        }
                    });
        } else {
            mBootanimationPart1.setOneShot(false);
        }
        mBootanimationFinishedHandler.sendEmptyMessage(0);
    }

    private Handler mBootanimationErrorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBootanimationView.setVisibility(View.GONE);
            mBootanimationError.setText(mBootanimationErrormsg);
        }
    };

    private Handler mBootanimationFinishedHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mBootanimationView.setImageDrawable(mBootanimationPart1);
            mBootanimationView.setVisibility(View.VISIBLE);
            mBootanimationError.setVisibility(View.GONE);
            mBootanimationPart1.start();
        }
    };

    private void installBootAnim(DialogInterface dialog, String bootAnimationPath) {
        DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy_HHmmss");
        Date date = new Date();
        String current = (dateFormat.format(date));
        new InstallBootAnimTask(getActivity()).execute(current, bootAnimationPath);
    }

    private class InstallBootAnimTask extends SuTask<String> {

        public InstallBootAnimTask(Context context) {
            super(context);
        }

        private boolean mSuccess = true;

        @Override
        protected void sudoInBackground(String... params) throws SuShell.SuDeniedException {
            if (params.length != 2) {
                Log.e(TAG, "InstallBootAnimTask: invalid params count");
                mSuccess = false;
                return;
            }
            String current = params[0];
            String bootAnimationPath = params[1];
            SuShell.runWithSuCheck("mount -o rw,remount /system",
                    "cp -f " + BOOTANIMATION_SYSTEM_PATH + " " +
                            BACKUP_PATH + "/bootanimation_backup_" + current + ".zip",
                    "cp -f " + bootAnimationPath + " " + BOOTANIMATION_SYSTEM_PATH,
                    "chmod 644 " + BOOTANIMATION_SYSTEM_PATH,
                    "mount -o ro,remount /system");
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result && mSuccess) {
                Toast.makeText(getActivity(), R.string.bootanimation_install_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

}
