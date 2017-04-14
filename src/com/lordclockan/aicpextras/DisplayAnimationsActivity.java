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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
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
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class DisplayAnimationsActivity extends Fragment {

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

        private static final String TAG = "AnimSettings";

        private static final int REQUEST_PICK_BOOT_ANIMATION = 201;

        private static final String POWER_MENU_ANIMATIONS = "power_menu_animations";
        private static final String KEY_TOAST_ANIMATION = "toast_animation";
        private static final String TOAST_ICON_COLOR = "toast_icon_color";
        private static final String TOAST_TEXT_COLOR = "toast_text_color";

        private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
        private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
        private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
        private static final String PREF_CUSTOM_BOOTANIM = "custom_bootanimation";
        private static final String BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip";
        private static final String BACKUP_PATH = new File(Environment
                .getExternalStorageDirectory(), "/AICP_ota").getAbsolutePath();

        private ColorPickerPreference mIconColor;
        private ColorPickerPreference mTextColor;
        private ListPreference mPowerMenuAnimations;
        private ListPreference mTileAnimationStyle;
        private ListPreference mTileAnimationDuration;
        private ListPreference mTileAnimationInterpolator;
        private ListPreference mToastAnimation;
        private Preference mCustomBootAnimation;
        private ImageView mView;
        private TextView mError;
        private AlertDialog mCustomBootAnimationDialog;
        private AnimationDrawable mAnimationPart1;
        private AnimationDrawable mAnimationPart2;
        private String mErrormsg;
        private String mBootAnimationPath;

        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.display_anim_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mContext = getActivity();

            // Power Menu Animations
            mPowerMenuAnimations = (ListPreference) prefSet.findPreference(POWER_MENU_ANIMATIONS);
            mPowerMenuAnimations.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.POWER_MENU_ANIMATIONS, 0)));
            mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            mPowerMenuAnimations.setOnPreferenceChangeListener(this);

            // Toast Animations
            mToastAnimation = (ListPreference) prefSet.findPreference(KEY_TOAST_ANIMATION);
            mToastAnimation.setSummary(mToastAnimation.getEntry());
            int CurrentToastAnimation = Settings.System.getInt(
                    resolver, Settings.System.TOAST_ANIMATION, 1);
            mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
            mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
            mToastAnimation.setOnPreferenceChangeListener(this);

            int intColor = 0xffffffff;
            String hexColor = String.format("#%08x", (0xffffffff & 0xffffffff));

            // Toast icon color
            mIconColor = (ColorPickerPreference) findPreference(TOAST_ICON_COLOR);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.TOAST_ICON_COLOR, 0xffffffff);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mIconColor.setNewPreviewColor(intColor);
            mIconColor.setSummary(hexColor);
            mIconColor.setOnPreferenceChangeListener(this);

            // Toast text color
            mTextColor = (ColorPickerPreference) findPreference(TOAST_TEXT_COLOR);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.TOAST_TEXT_COLOR, 0xffffffff);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setNewPreviewColor(intColor);
            mTextColor.setSummary(hexColor);
            mTextColor.setOnPreferenceChangeListener(this);

            // QS tile animation
            mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
            int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_STYLE, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            mTileAnimationStyle.setOnPreferenceChangeListener(this);

            mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
            int tileAnimationDuration = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_DURATION, 1500,
                    UserHandle.USER_CURRENT);
            mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
            updateTileAnimationDurationSummary(tileAnimationDuration);
            mTileAnimationDuration.setOnPreferenceChangeListener(this);

            mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
            int tileAnimationInterpolator = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

            // Custom bootanimation
            mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM);

            resetBootAnimation();

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mPowerMenuAnimations) {
                Settings.System.putInt(resolver, Settings.System.POWER_MENU_ANIMATIONS,
                        Integer.parseInt((String) newValue));
                mPowerMenuAnimations.setValue(String.valueOf(newValue));
                mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
                return true;
            } else if (preference == mToastAnimation) {
                int index = mToastAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.TOAST_ANIMATION, index);
                mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
                Toast.makeText(getActivity(), mToastAnimation.getEntries()[index],
                        Toast.LENGTH_SHORT).show();
                return true;
            }  else if (preference == mIconColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                       .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                       Settings.System.TOAST_ICON_COLOR, intHex);
                Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                       Toast.LENGTH_SHORT).show();
                return true;
            } else if (preference == mTextColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                      .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                      Settings.System.TOAST_TEXT_COLOR, intHex);
                Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                      Toast.LENGTH_SHORT).show();
                return true;
            } else if (preference == mTileAnimationStyle) {
                int tileAnimationStyle = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_STYLE,
                        tileAnimationStyle, UserHandle.USER_CURRENT);
                updateTileAnimationStyleSummary(tileAnimationStyle);
                updateAnimTileStyle(tileAnimationStyle);
            } else if (preference == mTileAnimationDuration) {
                int tileAnimationDuration = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_DURATION,
                        tileAnimationDuration, UserHandle.USER_CURRENT);
                updateTileAnimationDurationSummary(tileAnimationDuration);
                return true;
            } else if (preference == mTileAnimationInterpolator) {
                int tileAnimationInterpolator = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_INTERPOLATOR,
                        tileAnimationInterpolator, UserHandle.USER_CURRENT);
                updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
                return true;
            }
            return false;
        }

        private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
            String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                    .valueOf(tileAnimationStyle))];
            mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
        }

        private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
            String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                    .valueOf(tileAnimationDuration))];
            mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
        }

        private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
            String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                    .valueOf(tileAnimationInterpolator))];
            mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
        }

        private void updateAnimTileStyle(int tileAnimationStyle) {
            if (mTileAnimationDuration != null) {
                if (tileAnimationStyle == 0) {
                    mTileAnimationDuration.setSelectable(false);
                    mTileAnimationInterpolator.setSelectable(false);
                } else {
                    mTileAnimationDuration.setSelectable(true);
                    mTileAnimationInterpolator.setSelectable(true);
                }
            }
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
                    // We want to know the path for copying it later as root; data might contain
                    // an URI that's not a path though, so save it locally to work around that
                    mBootAnimationPath = BACKUP_PATH + File.separator + "tmpbootanim.zip";

                    InputStream inputStream = null;
                    FileOutputStream outputStream = null;

                    try {
                        inputStream = getActivity().getContentResolver().openInputStream(data.getData());
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
                    openBootAnimationDialog();
                }
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
    }
}
