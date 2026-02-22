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

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log

class ReportingServiceManager : BroadcastReceiver() {

    companion object {
        private const val DEBUG = false
        private const val MILLIS_PER_HOUR = 60L * 60L * 1000L
        private const val MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR

        const val ACTION_LAUNCH_SERVICE = "com.android.settings.action.TRIGGER_REPORT_METRICS"
        const val EXTRA_FORCE = "force"

        @JvmStatic
        fun setAlarm(context: Context, millisFromNow: Long) {
            val prefs = AnonymousStats.getPreferences(context)
            var optedIn = prefs.getBoolean(
                Const.ANONYMOUS_OPT_IN,
                Utilities.getReportingMode() != Const.ROMSTATS_REPORTING_MODE_OLD
            )
            if (DEBUG) Log.d(Const.TAG, "[setAlarm] optedIn=$optedIn")

            val firstBoot = prefs.getBoolean(Const.ANONYMOUS_FIRST_BOOT, true)
            if (firstBoot && Utilities.getReportingMode() == Const.ROMSTATS_REPORTING_MODE_OLD) {
                Log.d(Const.TAG, "[setAlarm] MODE=1 & firstBoot -> prompt user")
                getUserResponse(context, true)
                optedIn = prefs.getBoolean(Const.ANONYMOUS_OPT_IN, false)
            }

            if (!optedIn) return

            val UPDATE_INTERVAL = Utilities.getTimeFrame().toLong() * MILLIS_PER_DAY
            var millis = millisFromNow
            if (millis <= 0) {
                var lastSynced = prefs.getLong(Const.ANONYMOUS_LAST_CHECKED, 0)
                if (lastSynced == 0L) {
                    lastSynced = System.currentTimeMillis()
                    prefs.edit().putLong(Const.ANONYMOUS_LAST_CHECKED, lastSynced).apply()
                }
                millis = (lastSynced + UPDATE_INTERVAL) - System.currentTimeMillis()
            }

            val intent = Intent(ACTION_LAUNCH_SERVICE).apply {
                setClass(context, ReportingServiceManager::class.java)
                putExtra("promptUser", false)
            }

            val nextAlarm = System.currentTimeMillis() + millis
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                nextAlarm,
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            )

            prefs.edit().putLong(Const.ANONYMOUS_NEXT_ALARM, nextAlarm).apply()
            if (DEBUG) Log.d(Const.TAG, "[setAlarm] Next sync in ${millis / MILLIS_PER_HOUR} hours")
        }

        @JvmStatic
        fun launchService(context: Context, force: Boolean) {
            val prefs = AnonymousStats.getPreferences(context)
            val optedIn = prefs.getBoolean(
                Const.ANONYMOUS_OPT_IN,
                Utilities.getReportingMode() == Const.ROMSTATS_REPORTING_MODE_OLD
            )
            if (DEBUG) Log.d(Const.TAG, "[launchService] optedIn=$optedIn")
            if (!optedIn) return

            val lastSynced = prefs.getLong(Const.ANONYMOUS_LAST_CHECKED, 0)
            if (!force && lastSynced != 0L) {
                val timeElapsed = System.currentTimeMillis() - lastSynced
                val UPDATE_INTERVAL = Utilities.getTimeFrame().toLong() * MILLIS_PER_DAY
                if (timeElapsed < UPDATE_INTERVAL) return
            }

            val intent = Intent(context, ReportingService::class.java).apply {
                putExtra("promptUser", false)
            }
            context.startService(intent)
        }

        private fun getUserResponse(context: Context, promptUser: Boolean) {
            val intent = Intent(context, ReportingService::class.java).apply {
                putExtra("promptUser", promptUser)
            }
            context.startService(intent)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(Const.TAG, "[onReceive] BOOT_COMPLETED")
                Utilities.checkIconVisibility(context)
                if (Utilities.persistentOptOut(context)) return
                setAlarm(context, 0)
            }
            ACTION_LAUNCH_SERVICE -> {
                Log.d(Const.TAG, "[onReceive] CONNECTIVITY_CHANGE")
                launchService(context, intent.getBooleanExtra(EXTRA_FORCE, false))
            }
        }
    }
}
