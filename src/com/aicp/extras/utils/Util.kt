/*
 * Copyright (C) 2017-2026 AICP
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
package com.aicp.extras.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.PowerManager
import android.os.ServiceManager
import android.os.SystemProperties
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.aicp.extras.Constants
import com.aicp.extras.R
import com.aicp.extras.SettingsActivity
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object Util {

    private val TAG = Util::class.java.simpleName

    private const val PROPERTY_DEVICE = "ro.aicp.device"
    private const val PROPERTY_DEVICE_EXT = "ro.product.device"

    @JvmStatic
    fun onPreferenceTreeClick(fragment: PreferenceFragmentCompat, preference: Preference): Boolean {
        val activity = fragment.activity
        return if (activity is SettingsActivity) {
            activity.onPreferenceClick(preference)
        } else {
            Log.w(TAG, "Activity not instanceof SettingsActivity, ignoring preference click")
            false
        }
    }

    @JvmStatic
    fun setSummaryToValue(pref: ListPreference) {
        pref.summary = pref.entry
    }

    @JvmStatic
    fun setSummaryToValue(pref: ListPreference, newValue: Any?) {
        try {
            val index = pref.findIndexOfValue(newValue?.toString())
            pref.summary = pref.entries[index]
        } catch (e: Exception) {
            Log.w(TAG, "setSummaryToValue: $newValue caused exception $e")
        }
    }

    @JvmStatic
    fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0).versionName != null
        } catch (notFound: PackageManager.NameNotFoundException) {
            false
        }
    }

    @JvmStatic
    fun isPackageEnabled(packageName: String, pm: PackageManager): Boolean {
        return try {
            val ai: ApplicationInfo = pm.getApplicationInfo(packageName, 0)
            ai.enabled
        } catch (notFound: PackageManager.NameNotFoundException) {
            false
        }
    }

    @JvmStatic
    fun getDevice(context: Context): String {
        var device = SystemProperties.get(PROPERTY_DEVICE)
        if (TextUtils.isEmpty(device)) {
            device = SystemProperties.get(PROPERTY_DEVICE_EXT)
        }
        return device?.lowercase() ?: ""
    }

    @JvmStatic
    fun getDownloadLinkForDevice(context: Context): String {
        return "https://dwnld.aicp-rom.com/?device=${getDevice(context)}"
    }

    @JvmStatic
    fun hasVibrator(context: Context): Boolean {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        return v.hasVibrator()
    }

    @JvmStatic
    fun readStringFromFile(inputFile: File): String {
        return inputFile.readText()
    }

    @JvmStatic
    fun hasSu(): Boolean {
        var process: java.lang.Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val br = BufferedReader(InputStreamReader(process.inputStream))
            br.readLine() != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            process?.destroy()
        }
    }

    @JvmStatic
    fun requireRoot(context: Context, preference: Preference?) {
        preference ?: return
        if (showAllPrefs(context)) return
        (preference.parent as? androidx.preference.PreferenceGroup)?.removePreference(preference)
    }

    @JvmStatic
    fun requireFullStatusbar(context: Context, preference: Preference?) {
        preference ?: return
        if (showAllPrefs(context)) return
        val displayCutoutPath = context.resources.getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout)
        if (!TextUtils.isEmpty(displayCutoutPath)) {
            (preference.parent as? androidx.preference.PreferenceGroup)?.removePreference(preference)
        }
    }

    @JvmStatic
    fun requireConfig(context: Context, preference: Preference?, configId: Int, expectValue: Boolean, allowBypass: Boolean) {
        preference ?: return
        if (allowBypass && showAllPrefs(context)) return
        if (context.resources.getBoolean(configId) != expectValue) {
            (preference.parent as? androidx.preference.PreferenceGroup)?.removePreference(preference)
        }
    }

    @JvmStatic
    fun requireProp(context: Context, preference: Preference?, property: String, defaultValue: Boolean, expectValue: Boolean) {
        preference ?: return
        if (SystemProperties.getBoolean(property, defaultValue) != expectValue) {
            (preference.parent as? androidx.preference.PreferenceGroup)?.removePreference(preference)
        }
    }

    @JvmStatic
    fun require3Nav(context: Context, preference: Preference?) {
        preference ?: return
        val navMode = context.resources.getInteger(com.android.internal.R.integer.config_navBarInteractionMode)
        val NAV_BAR_MODE_3BUTTON = 0 // Definiere die Konstante hier passend
        if (navMode != NAV_BAR_MODE_3BUTTON) {
            preference.isEnabled = false
            preference.summary = context.getString(R.string.alternative_recents_swipe_up_enabled_warning_summary)
        }
    }

    private fun showAllPrefs(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(Constants.PREF_SHOW_DEVICE_HIDDEN_PREFS, false)
    }

    @JvmStatic
    fun restartSystemUi(context: Context) {
        RestartSystemUiTask(context).execute()
    }

    @JvmStatic
    fun rebootSystem(context: Context) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.reboot(null)
    }

    @JvmStatic
    fun showSystemUiRestartDialog(context: Context) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(R.string.systemui_restart_title)
            .setMessage(R.string.systemui_restart_message)
            .setPositiveButton(R.string.ok) { _, _ -> restartSystemUi(context) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    @JvmStatic
    fun showRebootDialog(context: Context, title: String, message: String, soft: Boolean) {
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.reboot_dialog_ok) { _, _ ->
                if (soft) SoftRebootTask(context).execute()
                else rebootSystem(context)
            }
            .setNegativeButton(R.string.reboot_dialog_cancel, null)
            .show()
    }

    private class RestartSystemUiTask(val context: Context) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            try {
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val ams = ActivityManager.getService()
                for (app in am.runningAppProcesses) {
                    if ("com.android.systemui" == app.processName) {
                        ams.killApplicationProcess(app.processName, app.uid)
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }

    private class SoftRebootTask(val context: Context) : AsyncTask<Void, Void, Void>() {
        private var dialog: androidx.appcompat.app.AlertDialog? = null

        override fun onPreExecute() {
            dialog = androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(R.string.soft_reboot_title)
                .setMessage(R.string.soft_reboot_message)
                .create()
            dialog?.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            try {
                val serviceManagerClass = Class.forName("android.os.ServiceManager")
                val getService = serviceManagerClass.getMethod("getService", String::class.java)
                val binder = getService.invoke(null, "activity")

                val iActivityManagerClass = Class.forName("android.app.IActivityManager\$Stub")
                val asInterface = iActivityManagerClass.getMethod("asInterface", android.os.IBinder::class.java)
                val am = asInterface.invoke(null, binder)

                val restartMethod = am.javaClass.getMethod("restart")
                restartMethod.invoke(am)

            } catch (e: Exception) {
                Log.e(TAG, "Failure trying to perform soft reboot", e)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            dialog?.dismiss()
        }
    }
}
