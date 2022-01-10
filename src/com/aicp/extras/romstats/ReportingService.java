/*
 * Copyright (C) 2012 The CyanogenMod Project
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

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import java.util.List;

import com.aicp.extras.R;
/*
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
*/

public class ReportingService extends IntentService {
    private static final boolean DEBUG = false;

    private static final int NOTIFICATION_ID = 1;

    private static final String CHANNEL_ID = "notification_romstats";

    private static final String AE_SETTINGSACTIVITY = "com.aicp.extras.SettingsActivity";
    private static final String SETTINGS_PACKAGE_NAME = "com.aicp.extras";
    private static final String ROMSTATS_SETTINGS = "com.aicp.extras.romstats.AnonymousStats";

    public ReportingService() {
        super(ReportingService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        JobScheduler js = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean canReport = true;
        if (intent.getBooleanExtra("promptUser", false)) {
        	Log.d(Const.TAG, "Prompting user for opt-in.");
            promptUser();
            canReport = false;
        }

        String RomStatsUrl = Utilities.getStatsUrl();
        if (RomStatsUrl == null || RomStatsUrl.isEmpty()) {
        	Log.e(Const.TAG, "This ROM is not configured for ROM Statistics.");
        	canReport = false;
        }

        if (canReport) {
		        Log.d(Const.TAG, "User has opted in -- reporting.");
		        if (AnonymousStats.getNextJobId(this) == -1) {
		            // if we've filled up to the threshold, we may have some stale job queue ids, purge them
		            // then re-add what hasn't executed yet
		            AnonymousStats.clearJobQueue(this);

		            final List<JobInfo> allPendingJobs = js.getAllPendingJobs();

		            // add one extra job to the size for what we will schedule below so we *always*
		            // have room.
		            if (js.getAllPendingJobs().size() + 1 >= AnonymousStats.QUEUE_MAX_THRESHOLD) {
		                // there are still as many actual pending jobs as our threshold allows.
		                // since we are past the threshold we will be losing data if we don't schedule
		                // another job here, so just clear out all the old data and start fresh
		                js.cancelAll();
		            } else {
		                for (JobInfo pendingJob : allPendingJobs) {
		                    AnonymousStats.addJob(this, pendingJob.getId());
		                }
		            }
		        }
		        int aicpJobId;
		        AnonymousStats.addJob(this, aicpJobId = AnonymousStats.getNextJobId(this));

		        if (DEBUG) Log.d(Const.TAG, "scheduling jobs id: " + aicpJobId);

		        String deviceId = Utilities.getUniqueID(getApplicationContext());
		        String deviceName = Utilities.getDevice();
		        String deviceVersion = Utilities.getModVersion();
		        String deviceBuildType = Utilities.getBuildType();
		        String deviceCountry = Utilities.getCountryCode(getApplicationContext());
		        String deviceCarrier = Utilities.getCarrier(getApplicationContext());
		        String deviceCarrierId = Utilities.getCarrierId(getApplicationContext());
		        String romName = Utilities.getRomName();
		        String romVersion = Utilities.getRomVersion();
		        String romStatsSignCert = Utilities.getSigningCert(getApplicationContext());
		        String romStatsUrl = Utilities.getStatsUrl();

		        if (DEBUG) {
		            Log.d(Const.TAG, "SERVICE: Report URL=" + romStatsUrl);
		            Log.d(Const.TAG, "SERVICE: Device ID=" + deviceId);
		            Log.d(Const.TAG, "SERVICE: Device Name=" + deviceName);
		            Log.d(Const.TAG, "SERVICE: Device BuildType=" + deviceBuildType);
		            Log.d(Const.TAG, "SERVICE: Device Version=" + deviceVersion);
		            Log.d(Const.TAG, "SERVICE: Country=" + deviceCountry);
		            Log.d(Const.TAG, "SERVICE: Carrier=" + deviceCarrier);
		            Log.d(Const.TAG, "SERVICE: Carrier ID=" + deviceCarrierId);
		            Log.d(Const.TAG, "SERVICE: ROM Version=" + romVersion);
		            Log.d(Const.TAG, "SERVICE: ROM Name=" + romName);
		            Log.d(Const.TAG, "SERVICE: Sign Cert=" + romStatsSignCert);
		        }
		        PersistableBundle aicpBundle = new PersistableBundle();
		        aicpBundle.putString(StatsUploadJobService.KEY_DEVICE_NAME, deviceName);
		        aicpBundle.putString(StatsUploadJobService.KEY_UNIQUE_ID, deviceId);
		        aicpBundle.putString(StatsUploadJobService.KEY_VERSION, deviceVersion);
		        aicpBundle.putString(StatsUploadJobService.KEY_BUILDTYPE, deviceBuildType);
		        aicpBundle.putString(StatsUploadJobService.KEY_COUNTRY, deviceCountry);
		        aicpBundle.putString(StatsUploadJobService.KEY_CARRIER, deviceCarrier);
		        aicpBundle.putString(StatsUploadJobService.KEY_CARRIER_ID, deviceCarrierId);
		        aicpBundle.putString(StatsUploadJobService.KEY_ROM_NAME, romName);
		        aicpBundle.putString(StatsUploadJobService.KEY_ROM_VERSION, romVersion);
		        aicpBundle.putString(StatsUploadJobService.KEY_SIGN_CERT, romStatsSignCert);
		        aicpBundle.putString(StatsUploadJobService.KEY_STATS_URL, romStatsUrl);
		        //aicpBundle.putLong(StatsUploadJobService.KEY_TIMESTAMP, System.currentTimeMillis());

		        // set job type
		        aicpBundle.putInt(StatsUploadJobService.KEY_JOB_TYPE,
		                StatsUploadJobService.JOB_TYPE_AICP);

		        // schedule aicp stats upload
		        js.schedule(new JobInfo.Builder(aicpJobId, new ComponentName(getPackageName(),
		                StatsUploadJobService.class.getName()))
		                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
		                .setMinimumLatency(1000)
		                .setExtras(aicpBundle)
		                .setPersisted(true)
		                .build());

		        // reschedule
		        final SharedPreferences prefs = AnonymousStats.getPreferences(this);
		        prefs.edit().putLong(Const.ANONYMOUS_LAST_CHECKED,
		                System.currentTimeMillis()).apply();
		        ReportingServiceManager.setAlarm(this, 0);
        }
	  }

    private void promptUser() {
        Intent mainActivity = new Intent(Intent.ACTION_MAIN);
        mainActivity.setClassName(SETTINGS_PACKAGE_NAME, AE_SETTINGSACTIVITY);
        mainActivity.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, ROMSTATS_SETTINGS);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, mainActivity, PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                getApplicationContext().getString(R.string.notification_romstats_name),
                NotificationManager.IMPORTANCE_LOW);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_desc))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.notification_aicp_stats);

        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        Notification notif = builder.build();
        notif.flags    |= Notification.FLAG_AUTO_CANCEL;
        notif.priority  = Notification.PRIORITY_HIGH;

        notificationManager.notify(NOTIFICATION_ID, notif);
    }
}
