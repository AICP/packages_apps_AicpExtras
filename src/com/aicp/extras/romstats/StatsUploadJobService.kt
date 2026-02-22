/*
 * Copyright (C) 2015 The CyanogenMod Project
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

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.AsyncTask
import android.os.PersistableBundle
import android.util.Log
import androidx.collection.ArrayMap
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.util.LinkedList

class StatsUploadJobService : JobService() {

    private val TAG = Const.TAG
    private val DEBUG = false

    companion object {
        const val KEY_JOB_TYPE = "job_type"
        const val JOB_TYPE_AICP = 1

        const val KEY_UNIQUE_ID = "device_hash"
        const val KEY_DEVICE_NAME = "device_name"
        const val KEY_VERSION = "device_version"
        const val KEY_BUILDTYPE = "device_buildtype"
        const val KEY_COUNTRY = "device_country"
        const val KEY_CARRIER = "device_carrier"
        const val KEY_CARRIER_ID = "device_carrier_id"
        const val KEY_ROM_NAME = "rom_name"
        const val KEY_ROM_VERSION = "rom_version"
        const val KEY_STATS_URL = "rom_stats_url"
        const val KEY_SIGN_CERT = "sign_cert"
        const val KEY_TIMESTAMP = "timeStamp"
    }

    private val mFinishedJobs = LinkedList<JobParameters>()
    private val mCurrentJobs = ArrayMap<JobParameters, StatsUploadTask>()

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        if (DEBUG) Log.d(TAG, "onStartJob() called with $jobParameters")
        val uploadTask = StatsUploadTask(jobParameters)
        mCurrentJobs[jobParameters] = uploadTask
        uploadTask.execute()
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        if (DEBUG) Log.d(TAG, "onStopJob() called with $jobParameters")
        val cancelledJob = mCurrentJobs.remove(jobParameters)
        val jobSuccessfullyFinished = mFinishedJobs.remove(jobParameters)
        if (jobSuccessfullyFinished != true) cancelledJob?.cancel(true)
        return jobSuccessfullyFinished != true
    }

    private inner class StatsUploadTask(private val mJobParams: JobParameters) :
        AsyncTask<Void, Void, Boolean>() {

        override fun doInBackground(vararg params: Void?): Boolean {
            val extras: PersistableBundle = mJobParams.extras

            val deviceId = extras.getString(KEY_UNIQUE_ID)
            val deviceName = extras.getString(KEY_DEVICE_NAME)
            val deviceVersion = extras.getString(KEY_VERSION)
            val deviceBuildType = extras.getString(KEY_BUILDTYPE)
            val deviceCountry = extras.getString(KEY_COUNTRY)
            val deviceCarrier = extras.getString(KEY_CARRIER)
            val deviceCarrierId = extras.getString(KEY_CARRIER_ID)
            val romName = extras.getString(KEY_ROM_NAME)
            val romVersion = extras.getString(KEY_ROM_VERSION)
            val romStatsUrl = extras.getString(KEY_STATS_URL)
            val romStatsSignCert = extras.getString(KEY_SIGN_CERT)

            var success = false
            if (!isCancelled) {
                when (extras.getInt(KEY_JOB_TYPE, -1)) {
                    JOB_TYPE_AICP -> {
                        try {
                            success = uploadToAicp(
                                deviceId, deviceName, deviceVersion,
                                deviceBuildType, deviceCountry, deviceCarrier,
                                deviceCarrierId, romName, romVersion,
                                romStatsSignCert, romStatsUrl
                            )
                        } catch (e: IOException) {
                            Log.e(TAG, "Could not upload stats checkin", e)
                        }
                    }
                }
            }

            if (success) {
                AnonymousStats.removeJob(this@StatsUploadJobService, mJobParams.jobId)
            }

            return success
        }

        override fun onPostExecute(success: Boolean) {
            if (success) mFinishedJobs.add(mJobParams)
            if (DEBUG) Log.d(TAG, "job id ${mJobParams.jobId} finished: $success")
            jobFinished(mJobParams, !success)
        }
    }

    private fun uploadToAicp(
        deviceId: String?, deviceName: String?, deviceVersion: String?,
        deviceBuildType: String?, deviceCountry: String?, deviceCarrier: String?,
        deviceCarrierId: String?, romName: String?, romVersion: String?,
        romStatsSignCert: String?, romStatsUrl: String?
    ): Boolean {
        if (romStatsUrl == null) return false
        val httpClient: HttpClient = DefaultHttpClient()
        val httpPost = HttpPost("$romStatsUrl/submit")
        var success = false
        try {
            val kv = ArrayList<BasicNameValuePair>()
            kv.add(BasicNameValuePair("device_hash", deviceId))
            kv.add(BasicNameValuePair("device_name", deviceName))
            kv.add(BasicNameValuePair("device_version", deviceVersion))
            kv.add(BasicNameValuePair("device_buildtype", deviceBuildType))
            kv.add(BasicNameValuePair("device_country", deviceCountry))
            kv.add(BasicNameValuePair("device_carrier", deviceCarrier))
            kv.add(BasicNameValuePair("device_carrier_id", deviceCarrierId))
            kv.add(BasicNameValuePair("rom_name", romName))
            kv.add(BasicNameValuePair("rom_version", romVersion))
            kv.add(BasicNameValuePair("sign_cert", romStatsSignCert))

            httpPost.entity = UrlEncodedFormEntity(kv)
            val response: HttpResponse = httpClient.execute(httpPost)
            if (DEBUG) {
                Log.d(TAG, "RESULT: code=${response.statusLine.statusCode}, message=${EntityUtils.toString(response.entity)}")
            }
            success = true
        } catch (e: IOException) {
            Log.w(TAG, "Could not upload stats checkin", e)
        }
        return success
    }
}
