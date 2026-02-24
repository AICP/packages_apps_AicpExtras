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

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.ArraySet
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreferenceCompat
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.DateFormat
import java.util.*

class AnonymousStats : BaseSettingsFragment(), DialogInterface.OnClickListener, DialogInterface.OnDismissListener, Preference.OnPreferenceChangeListener {

    companion object {
        private const val PREF_VIEW_STATS = "pref_view_stats"
        private const val PREF_LAST_REPORT_ON = "pref_last_report_on"
        private const val PREF_REPORT_INTERVAL = "pref_reporting_interval"
        const val KEY_JOB_QUEUE = "pref_job_queue"
        const val QUEUE_MAX_THRESHOLD = 1000

        @JvmStatic
        fun getPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(Utilities.SETTINGS_PREF_NAME, 0)
        }

        @JvmStatic
        fun getJobQueue(context: Context): MutableSet<String> {
            return getPreferences(context).getStringSet(KEY_JOB_QUEUE, mutableSetOf()) ?: mutableSetOf()
        }

        @JvmStatic
        fun clearJobQueue(context: Context) {
            getPreferences(context).edit().remove(KEY_JOB_QUEUE).commit()
        }

        @JvmStatic
        fun addJob(context: Context, jobId: Int) {
            val jobQueue = getJobQueue(context)
            jobQueue.add(jobId.toString())
            getPreferences(context).edit().putStringSet(KEY_JOB_QUEUE, jobQueue).commit()
        }

        @JvmStatic
        fun removeJob(context: Context, jobId: Int) {
            val jobQueue = getJobQueue(context)
            jobQueue.remove(jobId.toString())
            getPreferences(context).edit().putStringSet(KEY_JOB_QUEUE, jobQueue).commit()
        }

        @JvmStatic
        fun getNextJobId(context: Context): Int {
            val currentQueue = getJobQueue(context)
            if (currentQueue.isEmpty()) {
                return 1
            } else if (currentQueue.size >= QUEUE_MAX_THRESHOLD) {
                return -1
            } else {
                var i = 1
                while (currentQueue.contains(i.toString())) {
                    i++
                }
                return i
            }
        }
    }

    // Rest des Codes bleibt unverändert
    private lateinit var mEnableReporting: SwitchPreferenceCompat
    private lateinit var mPersistentOptout: SwitchPreferenceCompat
    private lateinit var mViewStats: Preference
    private var mOkDialog: Dialog? = null
    private var mOkClicked: Boolean = false
    private lateinit var mPrefs: SharedPreferences

    override fun getPreferenceResource(): Int {
        return R.xml.anonymous_stats
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mPrefs = getPreferences(requireActivity())
        val prefSet = preferenceScreen

        mEnableReporting = prefSet.findPreference(Const.ANONYMOUS_OPT_IN)!!
        mPersistentOptout = prefSet.findPreference(Const.ANONYMOUS_OPT_OUT_PERSIST)!!
        mViewStats = prefSet.findPreference(PREF_VIEW_STATS)!!

        val firstBoot = mPrefs.getBoolean(Const.ANONYMOUS_FIRST_BOOT, true)
        if (mEnableReporting.isChecked && firstBoot) {
            Log.d(Const.TAG, "First app start, set params and report immediately")
            mPrefs.edit().putBoolean(Const.ANONYMOUS_FIRST_BOOT, false).apply()
            mPrefs.edit().putLong(Const.ANONYMOUS_LAST_CHECKED, 1).apply()
            ReportingServiceManager.launchService(requireActivity(), false)
        }

        var mPrefHolder: Preference?
        val lastCheck = mPrefs.getLong(Const.ANONYMOUS_LAST_CHECKED, 0)
        if (lastCheck > 1) {
            val lastCheckStr = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(lastCheck))
            val lastCheckTitle = getString(R.string.last_report_on) + ": " + lastCheckStr

            mPrefHolder = prefSet.findPreference(PREF_LAST_REPORT_ON)
            mPrefHolder?.title = lastCheckTitle

            val nextCheck = mPrefs.getLong(Const.ANONYMOUS_NEXT_ALARM, 0)
            if (nextCheck > 0) {
                val nextAlarmStr = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(nextCheck))
                val nextAlarmSummary = getString(R.string.next_report_on) + ": " + nextAlarmStr
                mPrefHolder?.summary = nextAlarmSummary
            }
        } else {
            mPrefHolder = prefSet.findPreference(PREF_LAST_REPORT_ON)
            (mPrefHolder?.parent as? PreferenceScreen)?.removePreference(mPrefHolder)
        }

        mPrefHolder = prefSet.findPreference(PREF_REPORT_INTERVAL)
        val tFrame = Utilities.getTimeFrame().toInt()
        mPrefHolder?.summary = resources.getQuantityString(R.plurals.reporting_interval_days, tFrame, tFrame)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference) {
            mEnableReporting -> {
                if (mEnableReporting.isChecked) {
                    mOkClicked = false
                    mOkDialog?.dismiss()
                    mOkDialog = AlertDialog.Builder(requireActivity())
                        .setMessage(getString(R.string.anonymous_statistics_warning))
                        .setTitle(R.string.anonymous_statistics_warning_title)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNeutralButton(getString(R.string.anonymous_learn_more), this)
                        .setNegativeButton(android.R.string.no, this)
                        .show()
                    mOkDialog?.setOnDismissListener(this)
                } else {
                    mPrefs.edit().putBoolean(Const.ANONYMOUS_OPT_IN, false).apply()
                }
                true
            }
            mPersistentOptout -> {
                if (mPersistentOptout.isChecked) {
                    try {
                        val sdCard = Environment.getExternalStorageDirectory()
                        val dir = File(sdCard.absolutePath + "/.AICPROMStats")
                        dir.mkdirs()
                        val cookieFile = File(dir, "optout")

                        FileOutputStream(cookieFile).use { optOutCookie ->
                            OutputStreamWriter(optOutCookie).use { oStream ->
                                oStream.write("true")
                            }
                        }
                        Log.d(Const.TAG, "Persistent Opt-Out cookie written successfully")
                    } catch (e: IOException) {
                        Log.e(Const.TAG, "Unable to write persistent optout cookie", e)
                    }
                } else {
                    try {
                        val sdCard = Environment.getExternalStorageDirectory()
                        val dir = File(sdCard.absolutePath + "/.AICPROMStats")
                        val cookieFile = File(dir, "optout")
                        cookieFile.delete()
                        Log.d(Const.TAG, "Persistent Opt-Out cookie removed successfully")
                    } catch (e: Exception) {
                        Log.w(Const.TAG, "Unable to write persistent optout cookie", e)
                    }
                }
                true
            }
            mViewStats -> {
                val uri = Uri.parse(Utilities.getStatsUrl())
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        return false
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!mOkClicked) {
            mEnableReporting.isChecked = false
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        mPrefs.edit().putBoolean(Const.ANONYMOUS_FIRST_BOOT, false).apply()
        Log.d(Const.TAG, "No more First Boot.")
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                mOkClicked = true
                mPrefs.edit().putBoolean(Const.ANONYMOUS_OPT_IN, true).apply()
                Log.d(Const.TAG, "User opted in")

                mPersistentOptout.isChecked = false
                try {
                    val sdCard = Environment.getExternalStorageDirectory()
                    val dir = File(sdCard.absolutePath + "/.AICPROMStats")
                    val cookieFile = File(dir, "optout")
                    cookieFile.delete()
                    Log.d(Const.TAG, "Persistent Opt-Out cookie removed successfully")
                } catch (e: Exception) {
                    Log.w(Const.TAG, "Unable to write persistent optout cookie", e)
                }
                ReportingServiceManager.launchService(requireActivity(), true)
            }
            DialogInterface.BUTTON_NEGATIVE -> {
                mEnableReporting.isChecked = false
            }
            else -> {
                val uri = Uri.parse("https://aicp-rom.com/")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }
}

