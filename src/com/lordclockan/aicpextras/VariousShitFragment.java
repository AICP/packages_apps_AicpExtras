package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Point;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.utils.AbstractAsyncSuCMDProcessor;
import com.lordclockan.aicpextras.utils.CMDProcessor;
import com.lordclockan.aicpextras.widget.NumberPickerPreference;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

public class VariousShitFragment extends Fragment {

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

        private static final String TAG = "VariousShit";

        private static final int MIN_DELAY_VALUE = 1;
        private static final int MAX_DELAY_VALUE = 30;
        private static final int REQUEST_PICK_BOOT_ANIMATION = 201;

        private static final String KEY_CAMERA_SOUNDS = "camera_sounds";
        private static final String PROP_CAMERA_SOUND = "persist.sys.camera-sound";
        private static final String SCREENSHOT_TYPE = "screenshot_type";
        private static final String SCREENSHOT_DELAY = "screenshot_delay";
        private static final String SCREENSHOT_SUMMARY = "Delay is set to ";
        private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
        private static final String PREF_CUSTOM_BOOTANIM = "custom_bootanimation";
        private static final String BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip";
        private static final String BACKUP_PATH = new File(Environment
                .getExternalStorageDirectory(), "/AICP_ota").getAbsolutePath();
        private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
        private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
        private static final String SCROLLINGCACHE_DEFAULT = "1";
        private static final String KEY_VOLUME_DIALOG_TIMEOUT = "volume_dialog_timeout";

        private SwitchPreference mCameraSounds;
        private ListPreference mMsob;
        private ListPreference mScreenshotType;
        private NumberPickerPreference mScreenshotDelay;
        private Preference mCustomBootAnimation;
        private ImageView mView;
        private TextView mError;
        private AlertDialog mCustomBootAnimationDialog;
        private AnimationDrawable mAnimationPart1;
        private AnimationDrawable mAnimationPart2;
        private String mErrormsg;
        private String mBootAnimationPath;
        private ListPreference mScrollingCachePref;
        private SeekBarPreferenceCham mVolumeDialogTimeout;

        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.various_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mContext = getActivity();

            mMsob = (ListPreference) findPreference(PREF_MEDIA_SCANNER_ON_BOOT);
            mMsob.setValue(String.valueOf(Settings.System.getInt(resolver,
                    Settings.System.MEDIA_SCANNER_ON_BOOT, 0)));
            mMsob.setSummary(mMsob.getEntry());
            mMsob.setOnPreferenceChangeListener(this);

            mCameraSounds = (SwitchPreference) findPreference(KEY_CAMERA_SOUNDS);
            mCameraSounds.setChecked(SystemProperties.getBoolean(PROP_CAMERA_SOUND, true));
            mCameraSounds.setOnPreferenceChangeListener(this);

            mScreenshotType = (ListPreference) findPreference(SCREENSHOT_TYPE);
            int mScreenshotTypeValue = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, 0);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            mScreenshotType.setSummary(mScreenshotType.getEntry());
            mScreenshotType.setOnPreferenceChangeListener(this);

            mScreenshotDelay = (NumberPickerPreference) findPreference(SCREENSHOT_DELAY);
            mScreenshotDelay.setOnPreferenceChangeListener(this);
            mScreenshotDelay.setMinValue(MIN_DELAY_VALUE);
            mScreenshotDelay.setMaxValue(MAX_DELAY_VALUE);
            int ssDelay = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_DELAY, 1);
            mScreenshotDelay.setCurrentValue(ssDelay);
            updateScreenshotDelaySummary(ssDelay);

            // Custom bootanimation
            mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM);

            resetBootAnimation();

            mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
            mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                    SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
            mScrollingCachePref.setOnPreferenceChangeListener(this);

            // Volume dialog timeout seekbar
            mVolumeDialogTimeout = (SeekBarPreferenceCham) findPreference(KEY_VOLUME_DIALOG_TIMEOUT);
            int volumeDialogTimeout = System.getInt(resolver,
                    System.VOLUME_DIALOG_TIMEOUT, 3000);
            mVolumeDialogTimeout.setValue(volumeDialogTimeout / 1);
            mVolumeDialogTimeout.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mCameraSounds) {
               if (KEY_CAMERA_SOUNDS.equals(key)) {
                   if ((Boolean) newValue) {
                       SystemProperties.set(PROP_CAMERA_SOUND, "1");
                   } else {
                       SystemProperties.set(PROP_CAMERA_SOUND, "0");
                   }
                }
                return true;
            } else if (preference == mScreenshotType) {
                int mScreenshotTypeValue = Integer.parseInt(((String) newValue).toString());
                mScreenshotType.setSummary(
                        mScreenshotType.getEntries()[mScreenshotTypeValue]);
                Settings.System.putInt(resolver,
                        Settings.System.SCREENSHOT_TYPE, mScreenshotTypeValue);
                mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
                return true;
            } else if (preference == mScreenshotDelay) {
                int mScreenshotDelayValue = Integer.parseInt(newValue.toString());
                Settings.System.putInt(resolver, Settings.System.SCREENSHOT_DELAY,
                        mScreenshotDelayValue);
                updateScreenshotDelaySummary(mScreenshotDelayValue);
                return true;
            } else if (preference == mMsob) {
                Settings.System.putInt(resolver,
                    Settings.System.MEDIA_SCANNER_ON_BOOT,
                        Integer.valueOf(String.valueOf(newValue)));

                mMsob.setValue(String.valueOf(newValue));
                mMsob.setSummary(mMsob.getEntry());
                return true;
            } else if (preference == mScrollingCachePref) {
                if (newValue != null) {
                    SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String)newValue);
                    return true;
                }
            } else if (preference == mVolumeDialogTimeout) {
                int volDialogTimeout = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.VOLUME_DIALOG_TIMEOUT, volDialogTimeout * 1);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mCustomBootAnimation) {
                openBootAnimationDialog();
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            return true;
        }

        private void updateScreenshotDelaySummary(int screenshotDelay) {
            mScreenshotDelay.setSummary(
                    getResources().getString(R.string.powermenu_screenshot_delay_message, screenshotDelay));
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

        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                    if (data == null) {
                        //Nothing returned by user, probably pressed back button in file manager
                        return;
                    }
                    mBootAnimationPath = data.getData().getPath();
                    openBootAnimationDialog();
                }
            }
        }

        private void openBootAnimationDialog() {
            Log.e(TAG, "boot animation path: " + mBootAnimationPath);
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
                            test.setType("file/*");
                            List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                                    PackageManager.GET_ACTIVITIES);
                            if (!list.isEmpty()) {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                                intent.setType("file/*");
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
            new AbstractAsyncSuCMDProcessor() {
                @Override
                protected void onPostExecute(String result) {
                }
            }.execute("mount -o rw,remount /system",
                    "cp -f /system/media/bootanimation.zip " + BACKUP_PATH + "/bootanimation_backup_" + current + ".zip",
                    "cp -f " + bootAnimationPath + " /system/media/bootanimation.zip",
                    "chmod 644 /system/media/bootanimation.zip",
                    "mount -o ro,remount /system");
        }
    }
}
