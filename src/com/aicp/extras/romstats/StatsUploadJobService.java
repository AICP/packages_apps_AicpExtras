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
import androidx.collection.ArrayMap;
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
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class StatsUploadJobService extends JobService {

//    private static final String TAG = StatsUploadJobService.class.getSimpleName();
    private static final String TAG = Const.TAG;
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
    public static final String KEY_STATS_URL = "rom_stats_url";
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
            String romStatsUrl = extras.getString(KEY_STATS_URL);
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
                                    deviceCarrierId, romName, romVersion,
                                    romStatsSignCert, romStatsUrl);
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
                               String deviceCarrierId, String romName, String romVersion,
                               String romStatsSignCert, String romStatsUrl)
            throws IOException {

            HttpClient httpClient;
            if (Const.SKIP_CERTIFICATE_CHECK) {
                try {
                    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    trustStore.load(null, null);

                    SSLSocketFactory socketFactory = new InsecureSSLSocketFactory(trustStore);
                    socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                    HttpParams params = new BasicHttpParams();
                    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                    HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                    SchemeRegistry schReg = new SchemeRegistry();
                    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                    schReg.register(new Scheme("https", socketFactory, 443));

                    ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, schReg);

                    httpClient = new DefaultHttpClient(ccm, params);
                } catch (KeyStoreException|IOException|NoSuchAlgorithmException
                        |CertificateException|KeyManagementException|UnrecoverableKeyException e) {
                    Log.w(Const.TAG, "Could not set up custom truststore", e);
                    httpClient = new DefaultHttpClient();
                }
            } else {
                httpClient = new DefaultHttpClient();
            }
            HttpPost httpPost = new HttpPost(romStatsUrl + "submit");
            boolean success = false;

            try {
                List<NameValuePair> kv = new ArrayList<NameValuePair>(5);
                kv.add(new BasicNameValuePair("device_hash", deviceId));
                kv.add(new BasicNameValuePair("device_name", deviceName));
                kv.add(new BasicNameValuePair("device_version", deviceVersion));
                kv.add(new BasicNameValuePair("device_buildtype", deviceBuildType));
                kv.add(new BasicNameValuePair("device_country", deviceCountry));
                kv.add(new BasicNameValuePair("device_carrier", deviceCarrier));
                kv.add(new BasicNameValuePair("device_carrier_id", deviceCarrierId));
                kv.add(new BasicNameValuePair("rom_name", romName));
                kv.add(new BasicNameValuePair("rom_version", romVersion));
                kv.add(new BasicNameValuePair("sign_cert", romStatsSignCert));

                httpPost.setEntity(new UrlEncodedFormEntity(kv));
                HttpResponse response = httpClient.execute(httpPost);

                if (DEBUG) Log.d(Const.TAG, "RESULT: code=" + response.getStatusLine().getStatusCode());
                if (DEBUG) Log.d(Const.TAG, "RESULT: message=" + EntityUtils.toString(response.getEntity()));

                success = true;
            } catch (IOException e) {
                Log.w(Const.TAG, "Could not upload stats checkin", e);
            }
            return success;
    }

    private class InsecureSSLSocketFactory extends SSLSocketFactory{
        private SSLContext sslContext = SSLContext.getInstance("TLS");
        public InsecureSSLSocketFactory(KeyStore trustStore) throws NoSuchAlgorithmException,
                KeyManagementException, KeyStoreException, UnrecoverableKeyException{
            super(trustStore);

            TrustManager trustManager = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)
                        throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[]{trustManager}, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException{
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException{
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
