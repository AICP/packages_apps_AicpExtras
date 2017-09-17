package com.aicp.extras.utils;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SELinux;
import android.util.Log;
import android.widget.Toast;

import com.aicp.extras.Constants;
import com.aicp.extras.R;

import java.io.IOException;
import java.util.List;

public class OnBoot extends BroadcastReceiver {

    private Context settingsContext = null;
    private static final String TAG = "SettingsOnBoot";
    private boolean mSetupRunning = false;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos =
                activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++) {
            if(procInfos.get(i).processName.equals("com.google.android.setupwizard")) {
                mSetupRunning = true;
            }
        }

        if(!mSetupRunning) {
            try {
                settingsContext = context.createPackageContext("com.android.settings", 0);
            } catch (Exception e) {
                Log.e(TAG, "Package not found", e);
            }
            SharedPreferences sharedpreferences = context.getSharedPreferences("selinux_pref",
                    Context.MODE_PRIVATE);
            if (sharedpreferences.contains(Constants.PREF_SELINUX_MODE)) {
                boolean currentIsSelinuxEnforcing = SELinux.isSELinuxEnforced();
                boolean isSelinuxEnforcing =
                        sharedpreferences.getBoolean(Constants.PREF_SELINUX_MODE,
                                currentIsSelinuxEnforcing);
                if (isSelinuxEnforcing) {
                    if (!currentIsSelinuxEnforcing) {
                        try {
                            SuShell.runWithSuCheck("setenforce 1");
                            showToast(context.getString(R.string.selinux_enforcing_toast_title),
                                    context);
                        } catch (SuShell.SuDeniedException e) {
                            showToast(context.getString(R.string.cannot_get_su), context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (currentIsSelinuxEnforcing) {
                        try {
                            SuShell.runWithSuCheck("setenforce 0");
                            showToast(context.getString(R.string.selinux_permissive_toast_title),
                                    context);
                        } catch (SuShell.SuDeniedException e) {
                            showToast(context.getString(R.string.cannot_get_su), context);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private void showToast(String toastString, Context context) {
        Toast.makeText(context, toastString, Toast.LENGTH_SHORT)
                .show();
    }

}
