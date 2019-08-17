/*
 * Copyright (C) 2017 AICP
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


package com.aicp.extras.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.IActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.aicp.extras.Constants;
import com.aicp.extras.R;
import com.aicp.extras.SettingsActivity;

public abstract class Util {

    private static final String TAG = Util.class.getSimpleName();

    public static final String PROPERTY_DEVICE = "ro.aicp.device";
    public static final String PROPERTY_DEVICE_EXT = "ro.product.device";

    public static boolean onPreferenceTreeClick(PreferenceFragment fragment,
                                                Preference preference) {
        Activity activity = fragment.getActivity();
        if (activity instanceof SettingsActivity) {
            return ((SettingsActivity) activity).onPreferenceClick(preference);
        } else {
            Log.w(TAG, "Activity not instanceof SettingsActivity, ignoring preference click");
            return false;
        }
    }


    public static void setSummaryToValue(ListPreference pref) {
        pref.setSummary(pref.getEntry());
    }

    public static void setSummaryToValue(ListPreference pref, Object newValue) {
        try {
            int index = pref.findIndexOfValue((String) newValue);
            pref.setSummary(pref.getEntries()[index]);
        } catch (Exception e) {
            Log.w(TAG, "setSummaryToValue: " + newValue + " caused exception " + e);
        }
    }

    public static boolean isPackageInstalled(String packageName, PackageManager pm) {
        try {
            String mVersion = pm.getPackageInfo(packageName, 0).versionName;
            return mVersion != null;
        } catch (PackageManager.NameNotFoundException notFound) {
            return false;
        }
    }

    public static boolean isPackageEnabled(String packageName, PackageManager pm) {
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException notFound) {
            return false;
        }
    }

    public static String getDevice(Context context) {
        String device = SystemProperties.get(PROPERTY_DEVICE);
        if (TextUtils.isEmpty(device)) {
            device = SystemProperties.get(PROPERTY_DEVICE_EXT);
            //device = translateDeviceName(context, device);
        }
        return device == null ? "" : device.toLowerCase();
   }

   public static boolean hasVibrator(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        return v.hasVibrator();
   }

   public static String getDownloadLinkForDevice(Context context) {
        return "http://dwnld.aicp-rom.com/?device=" + getDevice(context);
   }

   public static String readStringFromFile(File inputFile) throws IOException {
        FileReader fileReader = new FileReader(inputFile);
        StringBuffer stringBuffer = new StringBuffer();
        int numCharsRead;
        char[] charArray = new char[1024];
        while ((numCharsRead = fileReader.read(charArray)) > 0) {
            stringBuffer.append(charArray, 0, numCharsRead);
        }
        fileReader.close();
        return stringBuffer.toString();
    }

    /**
     * Checks if a specific service is running.
     *
     * @param context     The context to retrieve the activity manager
     * @param serviceName The name of the service
     * @return Whether the service is running or not
     */
    public static boolean isServiceRunning(Context context, String serviceName) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (services != null) {
            for (ActivityManager.RunningServiceInfo info : services) {
                if (info.service != null) {
                    if (info.service.getClassName() != null && info.service.getClassName()
                            .equalsIgnoreCase(serviceName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * This can not reliably detect whether the user has root access,
     * but it can detect some cases when the user hasn't.
     */
    public static boolean hasSu() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[] { "which", "su" });
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return br.readLine() != null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (p != null) p.destroy();
        }
        return false;
    }

    public static void requireRoot(Context context, Preference preference) {
        if (preference == null) return;
        if (showAllPrefs(context)) return;
        if (!hasSu()) {
            preference.getParent().removePreference(preference);
        }
    }

    /**
     * Remove preference on devices with display cutout (notch).
     */
    public static void requireFullStatusbar(Context context, Preference preference) {
        if (preference == null) return;
        if (showAllPrefs(context)) return;
        String displayCutoutPath = context.getResources()
                .getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout);
        if (!TextUtils.isEmpty(displayCutoutPath)) {
            // TODO: we might want to check whether cutout is in statusbar area as well
            preference.getParent().removePreference(preference);
        }
    }

    public static void requireConfig(Context context, Preference preference, int configId,
                                     boolean expectValue, boolean allowBypass) {
        if (preference == null) return;
        if (allowBypass && showAllPrefs(context)) return;
        if (context.getResources().getBoolean(configId) != expectValue) {
            preference.getParent().removePreference(preference);
        }
    }

    private static boolean showAllPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(Constants.PREF_SHOW_DEVICE_HIDDEN_PREFS, false);
    }

    public static void restartSystemUi(Context context) {
        new RestartSystemUiTask(context).execute();
    }

    public static void rebootSystem(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot(null);
    }

    public static void showSystemUiRestartDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.systemui_restart_title)
                .setMessage(R.string.systemui_restart_message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        restartSystemUi(context);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static void showRebootDialog(Context context, String title, String message,
                                        boolean soft) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.reboot_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (soft) {
                                    new SoftRebootTask(context).execute();
                                } else {
                                    rebootSystem(context);
                                }
                            }
                })
                .setNegativeButton(R.string.reboot_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Only close dialog
                            }
                })
                .show();
    }

    private static class RestartSystemUiTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;

        public RestartSystemUiTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ActivityManager am =
                        (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                IActivityManager ams = ActivityManager.getService();
                for (ActivityManager.RunningAppProcessInfo app: am.getRunningAppProcesses()) {
                    if ("com.android.systemui".equals(app.processName)) {
                        ams.killApplicationProcess(app.processName, app.uid);
                        break;
                    }
                }
                //Class ActivityManagerNative = Class.forName("android.app.ActivityManagerNative");
                //Method getDefault = ActivityManagerNative.getDeclaredMethod("getDefault", null);
                //Object amn = getDefault.invoke(null, null);
                //Method killApplicationProcess = amn.getClass().getDeclaredMethod("killApplicationProcess", String.class, int.class);
                //mContext.stopService(new Intent().setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SystemUIService")));
                //am.killBackgroundProcesses("com.android.systemui");
                //for (ActivityManager.RunningAppProcessInfo app : am.getRunningAppProcesses()) {
                //    if ("com.android.systemui".equals(app.processName)) {
                //        killApplicationProcess.invoke(amn, app.processName, app.uid);
                //        break;
                //    }
                //}
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class SoftRebootTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private AlertDialog mDialog;

        public SoftRebootTask(Context context) {
            super();
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.soft_reboot_title)
                .setMessage(R.string.soft_reboot_message)
                .create();
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                final IActivityManager am =
                      ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
                if (am != null) {
                    am.restart();
                }
            } catch (RemoteException e) {
                Log.e(TAG, "failure trying to perform soft reboot", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mDialog.dismiss();
        }
    }
}
