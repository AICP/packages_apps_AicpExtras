/*
 * Copyright (C) 2015 The CyanogenMod Project
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

package com.aicp.extras.romstats;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import com.aicp.extras.R;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class StatsUploadJobService extends JobService {

    private static final String TAG = StatsUploadJobService.class.getSimpleName();
    private static final boolean DEBUG = false;

    public static final String KEY_JOB_TYPE = "job_type";
    public static final int JOB_TYPE_AICP= 1;

    public static final String KEY_UNIQUE_ID = "device_hash";
    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_VERSION = "device_version";
    public static final String KEY_BUILDTYPE = "device_buildtype";
    public static final String KEY_COUNTRY = "device_country";
    public static final String KEY_CARRIER = "device_carrier";
    public static final String KEY_CARRIER_ID = "device_carrier_id";
    public static final String KEY_ROM_NAME = "rom_name";
    public static final String KEY_ROM_VERSION = "rom_version";
    public static final String KEY_SIGN_CERT = "sign_cert";
    public static final String KEY_TIMESTAMP = "timeStamp";

    List<JobParameters> mFinishedJobs = new LinkedList<>();
    ArrayMap<JobParameters, StatsUploadTask> mCurrentJobs = new ArrayMap<>();

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        if (DEBUG)
            Log.d(TAG, "onStartJob() called with " + "jobParameters = [" + jobParameters + "]");
        final StatsUploadTask uploadTask = new StatsUploadTask(jobParameters);
        mCurrentJobs.put(jobParameters, uploadTask);
        uploadTask.execute((Void) null);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (DEBUG)
            Log.d(TAG, "onStopJob() called with " + "jobParameters = [" + jobParameters + "]");

        final StatsUploadTask cancelledJob = mCurrentJobs.remove(jobParameters);

        // if we can't remove from finished jobs, that means it's not finished!
        final boolean jobSuccessfullyFinished = mFinishedJobs.remove(jobParameters);

        if (!jobSuccessfullyFinished) {
            // cancel the ongoing background task
            if (cancelledJob != null) {
                cancelledJob.cancel(true);
            }
        }

        return !jobSuccessfullyFinished;
    }

    private class StatsUploadTask extends AsyncTask<Void, Void, Boolean> {

        private JobParameters mJobParams;

        public StatsUploadTask(JobParameters jobParams) {
            this.mJobParams = jobParams;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            PersistableBundle extras = mJobParams.getExtras();

            String deviceId = extras.getString(KEY_UNIQUE_ID);
            String deviceName = extras.getString(KEY_DEVICE_NAME);
            String deviceVersion = extras.getString(KEY_VERSION);
            String deviceBuildType = extras.getString(KEY_BUILDTYPE);
            String deviceCountry = extras.getString(KEY_COUNTRY);
            String deviceCarrier = extras.getString(KEY_CARRIER);
            String deviceCarrierId = extras.getString(KEY_CARRIER_ID);
            String romName = extras.getString(KEY_ROM_NAME);
            String romVersion = extras.getString(KEY_ROM_VERSION);
            String romStatsSignCert = extras.getString(KEY_SIGN_CERT);
            //long timeStamp = extras.getLong(KEY_TIMESTAMP);

            boolean success = false;
            if (!isCancelled()) {
                int jobType = extras.getInt(KEY_JOB_TYPE, -1);

                switch (jobType) {
                    case JOB_TYPE_AICP:
                        try {
                            success = uploadToAicp(deviceId, deviceName, deviceVersion,
                                    deviceBuildType, deviceCountry, deviceCarrier,
                                    deviceCarrierId, romName, romVersion, romStatsSignCert);
                        } catch (IOException e) {
                            Log.e(TAG, "Could not upload stats checkin to aicp server", e);
                            success = false;
                        }
                        break;
                }
            }

            if (success) {
                AnonymousStats.removeJob(StatsUploadJobService.this, mJobParams.getJobId());
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                mFinishedJobs.add(mJobParams);
            }
            if (DEBUG)
                Log.d(TAG, "job id " + mJobParams.getJobId() + ", has finished with success="
                        + success);
            jobFinished(mJobParams, !success);
        }
    }


    private boolean uploadToAicp(String deviceId, String deviceName, String deviceVersion,
                               String deviceBuildType, String deviceCountry, String deviceCarrier,
                               String deviceCarrierId, String romName, String romVersion, String romStatsSignCert)
            throws IOException {

        final Uri uri = Uri.parse(Utilities.getStatsUrl()).buildUpon()
                .appendQueryParameter("device_hash", deviceId)
                .appendQueryParameter("device_name", deviceName)
                .appendQueryParameter("device_version", deviceVersion)
                .appendQueryParameter("device_buildtype", deviceBuildType)
                .appendQueryParameter("device_country", deviceCountry)
                .appendQueryParameter("device_carrier", deviceCarrier)
                .appendQueryParameter("device_carrier_id", deviceCarrierId)
                .appendQueryParameter("rom_name", romName)
                .appendQueryParameter("rom_version", romVersion)
                .appendQueryParameter("sign_cert", romStatsSignCert)
                .build();
        URL url = new URL(uri.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            final int responseCode = urlConnection.getResponseCode();
            final boolean success = responseCode == HttpURLConnection.HTTP_OK;
            if (DEBUG) Log.d(TAG, "aicp server response code=" + responseCode + " suceess=" + success);
            if (!success) {
                Log.w(TAG, "failed sending, server returned: " + getResponse(urlConnection,
                        !success));
            }
            return success;
        } finally {
            urlConnection.disconnect();
        }

    }

    private String getResponse(HttpURLConnection httpUrlConnection, boolean errorStream)
            throws IOException {
        InputStream responseStream = new BufferedInputStream(errorStream
                ? httpUrlConnection.getErrorStream()
                : httpUrlConnection.getInputStream());

        BufferedReader responseStreamReader = new BufferedReader(
                new InputStreamReader(responseStream));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = responseStreamReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        responseStreamReader.close();
        responseStream.close();

        return stringBuilder.toString();
    }

}
