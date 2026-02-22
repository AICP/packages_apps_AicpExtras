/*
 * Copyright (C) 2012 The CyanogenMod Project
 * Copyright (C) 2026 AICP
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
package com.aicp.extras.romstats

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.PersistableBundle
import android.preference.PreferenceActivity
import android.util.Log
import com.aicp.extras.R
import com.aicp.extras.romstats.AnonymousStats
import com.aicp.extras.romstats.StatsUploadJobService

class ReportingService : IntentService(ReportingService::class.java.simpleName) {

    companion object {
        const val DEBUG = false
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "notification_romstats"

        const val AE_SETTINGSACTIVITY = "com.aicp.extras.SettingsActivity"
        const val SETTINGS_PACKAGE_NAME = "com.aicp.extras"
        const val ROMSTATS_SETTINGS = "com.aicp.extras.romstats.AnonymousStats"
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return

        val js = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        var canReport = true

        if (intent.getBooleanExtra("promptUser", false)) {
            if (DEBUG) Log.d(Const.TAG, "Prompting user for opt-in.")
            promptUser()
            canReport = false
        }

        val romStatsUrl = Utilities.getStatsUrl()
        if (romStatsUrl.isNullOrEmpty()) {
            Log.e(Const.TAG, "This ROM is not configured for ROM Statistics.")
            canReport = false
        }

        if (canReport) {
            if (DEBUG) Log.d(Const.TAG, "User has opted in -- reporting.")

            if (AnonymousStats.getNextJobId(this) == -1) {
                AnonymousStats.clearJobQueue(this)

                val allPendingJobs = js.allPendingJobs
                if (allPendingJobs.size + 1 >= AnonymousStats.QUEUE_MAX_THRESHOLD) {
                    js.cancelAll()
                } else {
                    for (job in allPendingJobs) {
                        AnonymousStats.addJob(this, job.id)
                    }
                }
            }

            val aicpJobId = AnonymousStats.getNextJobId(this)
            AnonymousStats.addJob(this, aicpJobId)

            if (DEBUG) Log.d(Const.TAG, "scheduling jobs id: $aicpJobId")

            val deviceId = Utilities.getUniqueID(applicationContext)
            val deviceName = Utilities.getDevice()
            val deviceVersion = Utilities.getModVersion()
            val deviceBuildType = Utilities.getBuildType()
            val deviceCountry = Utilities.getCountryCode(applicationContext)
            val deviceCarrier = Utilities.getCarrier(applicationContext)
            val deviceCarrierId = Utilities.getCarrierId(applicationContext)
            val romName = Utilities.getRomName()
            val romVersion = Utilities.getRomVersion()
            val romStatsSignCert = Utilities.getSigningCert(applicationContext)

            if (DEBUG) {
                Log.d(Const.TAG, "SERVICE: Report URL=$romStatsUrl")
                Log.d(Const.TAG, "SERVICE: Device ID=$deviceId")
                Log.d(Const.TAG, "SERVICE: Device Name=$deviceName")
                Log.d(Const.TAG, "SERVICE: Device BuildType=$deviceBuildType")
                Log.d(Const.TAG, "SERVICE: Device Version=$deviceVersion")
                Log.d(Const.TAG, "SERVICE: Country=$deviceCountry")
                Log.d(Const.TAG, "SERVICE: Carrier=$deviceCarrier")
                Log.d(Const.TAG, "SERVICE: Carrier ID=$deviceCarrierId")
                Log.d(Const.TAG, "SERVICE: ROM Version=$romVersion")
                Log.d(Const.TAG, "SERVICE: ROM Name=$romName")
                Log.d(Const.TAG, "SERVICE: Sign Cert=$romStatsSignCert")
            }

            val aicpBundle = PersistableBundle().apply {
                putString(StatsUploadJobService.KEY_DEVICE_NAME, deviceName)
                putString(StatsUploadJobService.KEY_UNIQUE_ID, deviceId)
                putString(StatsUploadJobService.KEY_VERSION, deviceVersion)
                putString(StatsUploadJobService.KEY_BUILDTYPE, deviceBuildType)
                putString(StatsUploadJobService.KEY_COUNTRY, deviceCountry)
                putString(StatsUploadJobService.KEY_CARRIER, deviceCarrier)
                putString(StatsUploadJobService.KEY_CARRIER_ID, deviceCarrierId)
                putString(StatsUploadJobService.KEY_ROM_NAME, romName)
                putString(StatsUploadJobService.KEY_ROM_VERSION, romVersion)
                putString(StatsUploadJobService.KEY_SIGN_CERT, romStatsSignCert)
                putString(StatsUploadJobService.KEY_STATS_URL, romStatsUrl)
                putInt(StatsUploadJobService.KEY_JOB_TYPE, StatsUploadJobService.JOB_TYPE_AICP)
            }

            js.schedule(
                JobInfo.Builder(aicpJobId, ComponentName(packageName, StatsUploadJobService::class.java.name))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setMinimumLatency(1000)
                    .setExtras(aicpBundle)
                    .setPersisted(true)
                    .build()
            )

            val prefs: SharedPreferences = AnonymousStats.getPreferences(this)
            prefs.edit().putLong(Const.ANONYMOUS_LAST_CHECKED, System.currentTimeMillis()).apply()
            ReportingServiceManager.setAlarm(this, 0)
        }
    }

    private fun promptUser() {
        val mainActivity = Intent(Intent.ACTION_MAIN).apply {
            setClassName(SETTINGS_PACKAGE_NAME, AE_SETTINGSACTIVITY)
            putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ROMSTATS_SETTINGS)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            mainActivity,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_romstats_name),
            NotificationManager.IMPORTANCE_LOW
        )

        val builder = Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_desc))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.notification_aicp_stats)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val notif = builder.build()
        notif.flags = notif.flags or Notification.FLAG_AUTO_CANCEL
        notif.priority = Notification.PRIORITY_HIGH
        notificationManager.notify(NOTIFICATION_ID, notif)
    }
}
