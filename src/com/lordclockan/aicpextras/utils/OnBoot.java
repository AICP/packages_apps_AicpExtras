package com.lordclockan.aicpextras.util;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.lordclockan.R;
import com.lordclockan.aicpextras.SuShell;

import java.io.IOException;
import java.util.List;

public class OnBoot extends BroadcastReceiver {

    Context settingsContext = null;
    private static final String TAG = "SettingsOnBoot";
    private boolean mSetupRunning = false;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
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
            SharedPreferences sharedpreferences = context.getSharedPreferences("selinux_pref", Context.MODE_PRIVATE);
            if (sharedpreferences.contains("selinux")) {
                boolean isSelinuxEnforcing = sharedpreferences.getBoolean("selinux", true);
                if (isSelinuxEnforcing) {
                    if (SuShell.runWithSu("getenforce").contains("Permissive")) {
                        SuShell.runWithSu("setenforce 1");
                    }
                } else {
                    if (SuShell.runWithSu("getenforce").contains("Enforcing")) {
                        SuShell.runWithSu("setenforce 0");
                        showToast(context.getString(R.string.selinux_permissive_toast_title), context);
                    }
                }
            }
        }
    }

    private void showToast(String toastString, Context context) {
        Toast.makeText(context, toastString, Toast.LENGTH_SHORT)
                .show();
    }

    private void setSelinuxEnabled(boolean status) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
        editor.putBoolean("selinux", status);
        editor.apply();
    }
}
