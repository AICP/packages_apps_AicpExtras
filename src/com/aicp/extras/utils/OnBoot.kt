/*
 * Copyright (C) 2026 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.aicp.extras.utils

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.SELinux
import android.util.Log
import android.widget.Toast
import com.aicp.extras.Constants
import com.aicp.extras.R

class OnBoot : BroadcastReceiver() {

    private var settingsContext: Context? = null
    private var setupRunning = false
    private var mContext: Context? = null

    companion object {
        private const val TAG = "SettingsOnBoot"
    }

    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val procInfos = activityManager.runningAppProcesses ?: emptyList<ActivityManager.RunningAppProcessInfo>()

        setupRunning = procInfos.any { it.processName == "com.google.android.setupwizard" }

        if (!setupRunning) {
            try {
                settingsContext = context.createPackageContext("com.android.settings", 0)
            } catch (e: Exception) {
                Log.e(TAG, "Package not found", e)
            }

            val sharedPreferences: SharedPreferences = context.getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)

            if (sharedPreferences.contains(Constants.PREF_SELINUX_MODE)) {
                val currentIsSelinuxEnforcing = SELinux.isSELinuxEnforced()
                val isSelinuxEnforcing = sharedPreferences.getBoolean(Constants.PREF_SELINUX_MODE, currentIsSelinuxEnforcing)

                if (isSelinuxEnforcing && !currentIsSelinuxEnforcing) {
                    try {
                        SuShell.runWithSuCheck("setenforce 1")
                        showToast(context.getString(R.string.selinux_enforcing_toast_title), context)
                    } catch (e: SuShell.SuDeniedException) {
                        showToast(context.getString(R.string.cannot_get_su), context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else if (!isSelinuxEnforcing && currentIsSelinuxEnforcing) {
                    try {
                        SuShell.runWithSuCheck("setenforce 0")
                        showToast(context.getString(R.string.selinux_permissive_toast_title), context)
                    } catch (e: SuShell.SuDeniedException) {
                        showToast(context.getString(R.string.cannot_get_su), context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun showToast(toastString: String, context: Context) {
        Toast.makeText(context, toastString, Toast.LENGTH_SHORT).show()
    }
}
