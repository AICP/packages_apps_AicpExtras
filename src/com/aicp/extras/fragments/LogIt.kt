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
package com.aicp.extras.fragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.SuShell
import com.aicp.extras.utils.Util
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Suppress("DEPRECATION")
class LogIt : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {

    companion object {
        private val TAG = LogIt::class.java.simpleName

        private const val PREF_LOGCAT = "logcat"
        private const val PREF_LOGCAT_RADIO = "logcat_radio"
        private const val PREF_KMSG = "kmseg"
        private const val PREF_DMESG = "dmesg"
        private const val PREF_AICP_LOG_IT = "aicp_log_it_now"
        private const val PREF_SHARE_TYPE = "aicp_log_share_type"

        private val sdCardDirectory = Environment.getExternalStorageDirectory()

        private val logcatFile = File(sdCardDirectory, "aicp_logcat.txt")
        private val logcatRadioFile = File(sdCardDirectory, "aicp_radiolog.txt")
        private val kmsgFile = File(sdCardDirectory, "aicp_kmsg.txt")
        private val dmesgFile = File(sdCardDirectory, "aicp_dmesg.txt")

        private val shareZipFile = File(sdCardDirectory, "aicp_logs.zip")

        private const val HASTE_MAX_LOG_SIZE = 400000
        private const val AICP_HASTE = "https://haste.aicp-rom.com/documents"
    }

    private lateinit var mLogcat: CheckBoxPreference
    private lateinit var mLogcatRadio: CheckBoxPreference
    private lateinit var mKmsg: CheckBoxPreference
    private lateinit var mDmesg: CheckBoxPreference
    private lateinit var mAicpLogIt: Preference
    private lateinit var mShareType: ListPreference

    private var sharingIntentString: String = ""
    private var shareHaste = false
    private var shareZip = true

    override fun getPreferenceResource(): Int = R.xml.log_it

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mLogcat = findPreference(PREF_LOGCAT)!!
        mLogcatRadio = findPreference(PREF_LOGCAT_RADIO)!!
        mKmsg = findPreference(PREF_KMSG)!!
        mDmesg = findPreference(PREF_DMESG)!!
        mAicpLogIt = findPreference(PREF_AICP_LOG_IT)!!
        mShareType = findPreference(PREF_SHARE_TYPE)!!

        mLogcat.onPreferenceChangeListener = this
        mLogcatRadio.onPreferenceChangeListener = this
        mKmsg.onPreferenceChangeListener = this
        mDmesg.onPreferenceChangeListener = this
        mShareType.onPreferenceChangeListener = this

        resetValues()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference) {
            mLogcat, mLogcatRadio, mKmsg, mDmesg -> {
                updateEnabledState(preference as CheckBoxPreference, newValue as Boolean)
                return true
            }
            mShareType -> {
                when (newValue as String) {
                    "0" -> {
                        mShareType.summary = getString(R.string.log_it_share_type_haste)
                        shareHaste = true
                        shareZip = false
                    }
                    "1" -> {
                        mShareType.summary = getString(R.string.log_it_share_type_zip)
                        shareHaste = false
                        shareZip = true
                    }
                    else -> {
                        mShareType.summary = ""
                        shareHaste = false
                        shareZip = false
                    }
                }
                return true
            }
        }
        return false
    }

    private fun updateEnabledState(changedPref: CheckBoxPreference, newValue: Boolean) {
        val logSelectors = arrayOf(mLogcat, mLogcatRadio, mKmsg, mDmesg)
        var enabled = newValue

        if (!enabled) {
            for (pref in logSelectors) {
                if (pref == changedPref) continue
                if (pref.isChecked) {
                    enabled = true
                    break
                }
            }
        }

        mAicpLogIt.isEnabled = enabled
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (preference == mAicpLogIt) {
            CreateLogTask().execute(
                mLogcat.isChecked,
                mLogcatRadio.isChecked,
                mKmsg.isChecked,
                mDmesg.isChecked
            )
            return true
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun logZipDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.log_it_dialog_title)
            .setMessage(R.string.logcat_warning)
            .setPositiveButton(R.string.share_title) { _, _ ->
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.type = "application/zip"
                sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareZipFile))
                try {
                    StrictMode.disableDeathOnFileUriExposure()
                    startActivity(
                        Intent.createChooser(
                            sharingIntent,
                            getString(R.string.log_it_share_via)
                        )
                    )
                } finally {
                    StrictMode.enableDeathOnFileUriExposure()
                }
            }
            .show()
    }

    private fun makeLogcat() {
        SuShell.runWithSuCheck("logcat -d > ${logcatFile.absolutePath}")
    }

    private fun makeLogcatRadio() {
        SuShell.runWithSuCheck("logcat -d -b radio > ${logcatRadioFile.absolutePath}")
    }

    private fun makeKmsg() {
        SuShell.runWithSuCheck("dmesg > ${kmsgFile.absolutePath}")
    }

    private fun makeDmesg() {
        SuShell.runWithSuCheck("dmesg > ${dmesgFile.absolutePath}")
    }

    private fun createShareZip(
        logcat: Boolean,
        logcatRadio: Boolean,
        kmsg: Boolean,
        dmesg: Boolean
    ) {
        ZipOutputStream(BufferedOutputStream(FileOutputStream(shareZipFile))).use { out ->
            if (logcat) writeToZip(logcatFile, out)
            if (logcatRadio) writeToZip(logcatRadioFile, out)
            if (kmsg) writeToZip(kmsgFile, out)
            if (dmesg) writeToZip(dmesgFile, out)
        }
    }

    private fun writeToZip(file: File, out: ZipOutputStream) {
        BufferedInputStream(FileInputStream(file)).use { input ->
            val entry = ZipEntry(file.name)
            out.putNextEntry(entry)
            val buffer = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                out.write(buffer, 0, length)
            }
        }
    }

    private inner class CreateLogTask :
        AsyncTask<Boolean, Void, String?>() {

        private var mException: Exception? = null
        private lateinit var progressDialog: ProgressDialog

        override fun onPreExecute() {
            progressDialog = ProgressDialog(requireActivity())
            progressDialog.setMessage(getString(R.string.log_it_logs_in_progress))
            progressDialog.setCancelable(false)
            progressDialog.isIndeterminate = true
            progressDialog.show()
        }

        override fun doInBackground(vararg params: Boolean?): String? {
            try {
                val logcat = params.getOrNull(0) == true
                val logcatRadio = params.getOrNull(1) == true
                val kmsg = params.getOrNull(2) == true
                val dmesg = params.getOrNull(3) == true

                if (logcat) makeLogcat()
                if (logcatRadio) makeLogcatRadio()
                if (kmsg) makeKmsg()
                if (dmesg) makeDmesg()

                if (shareZip) {
                    createShareZip(logcat, logcatRadio, kmsg, dmesg)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating logs", e)
                mException = e
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            progressDialog.dismiss()

            if (mException is SuShell.SuDeniedException) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.cannot_get_su),
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            if (shareZip) {
                logZipDialog()
            }
        }
    }

    fun resetValues() {
        mLogcat.isChecked = false
        mLogcatRadio.isChecked = false
        mKmsg.isChecked = false
        mDmesg.isChecked = false
        mAicpLogIt.isEnabled = false
        mShareType.value = "1"
        mShareType.summary = mShareType.entry
        shareHaste = false
        shareZip = true
    }
}
