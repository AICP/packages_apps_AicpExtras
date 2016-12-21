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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class ReportingServiceManager extends BroadcastReceiver {

	private static final long MILLIS_PER_HOUR = 60L * 60L * 1000L;
	private static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
	private static final long UPDATE_INTERVAL = 1L * MILLIS_PER_DAY;

	public static final String ACTION_LAUNCH_SERVICE =
					"com.android.settings.action.TRIGGER_REPORT_METRICS";
	public static final String EXTRA_FORCE = "force";

	// UPDATE_INTERVAL days is set in the build.prop file
	// private static final long UPDATE_INTERVAL = 1L * MILLIS_PER_DAY;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			Log.d(Const.TAG, "[onReceive] BOOT_COMPLETED");

			Utilities.checkIconVisibility(context);
			if (Utilities.persistentOptOut(context)) {
				return;
			}

			setAlarm(context, 0);
		} else if (intent.getAction().equals(ACTION_LAUNCH_SERVICE)){
			Log.d(Const.TAG, "[onReceive] CONNECTIVITY_CHANGE");
      launchService(context, intent.getBooleanExtra(EXTRA_FORCE, false));
		}
	}

	public static void setAlarm(Context context, long millisFromNow) {
		SharedPreferences prefs = AnonymousStats.getPreferences(context);

        //prefs.edit().putBoolean(AnonymousStats.ANONYMOUS_ALARM_SET, false).apply();
		//boolean firstBoot = prefs.getBoolean(AnonymousStats.ANONYMOUS_FIRST_BOOT, true);

		// get ANONYMOUS_OPT_IN pref, defaults to true (new behavior)
		boolean optedIn = prefs.getBoolean(Const.ANONYMOUS_OPT_IN, true);

		// If we want the old behavior, re-read OPT_IN but default to false
		if (Utilities.getReportingMode() == Const.ROMSTATS_REPORTING_MODE_OLD) {
			optedIn = prefs.getBoolean(Const.ANONYMOUS_OPT_IN, false);
			Log.d(Const.TAG, "[setAlarm] AskFirstBoot, optIn=" + optedIn);
		}

		if (!optedIn) {
			return;
		}

		if (millisFromNow <= 0) {
			long lastSynced = prefs.getLong(Const.ANONYMOUS_LAST_CHECKED, 0);
			if (lastSynced == 0) {
				// never synced, so let's fake out that the last sync was just now.
				// this will allow the user tFrame time to opt out before it will start
				// sending up anonymous stats.
				lastSynced = System.currentTimeMillis();
				prefs.edit().putLong(Const.ANONYMOUS_LAST_CHECKED, lastSynced).apply();
				Log.d(Const.TAG, "[setAlarm] Set alarm for first sync.");
			}
			millisFromNow = (lastSynced + UPDATE_INTERVAL) - System.currentTimeMillis();
		}

		Intent intent = new Intent(ACTION_LAUNCH_SERVICE);
		intent.setClass(context, ReportingServiceManager.class);

		long nextAlarm = System.currentTimeMillis() + millisFromNow;

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarm, PendingIntent.getBroadcast(context, 0, intent, 0));
		Log.d(Const.TAG, "[setAlarm] Next sync attempt in : " + millisFromNow / MILLIS_PER_HOUR + " hours");

		prefs.edit().putLong(Const.ANONYMOUS_NEXT_ALARM, nextAlarm).apply();
	}

  public static void launchService(Context context, boolean force) {
		SharedPreferences prefs = AnonymousStats.getPreferences(context);

		boolean optedIn = prefs.getBoolean(Const.ANONYMOUS_OPT_IN, true);
		if (!optedIn) {
			return;
		}

		boolean firstBoot = prefs.getBoolean(Const.ANONYMOUS_FIRST_BOOT, true);
		if (firstBoot && Utilities.getReportingMode() == Const.ROMSTATS_REPORTING_MODE_OLD) {
			Log.d(Const.TAG, "[launchService] MODE=1 & firstBoot -> prompt user");

			// promptUser is called through a service because it cannot be called from a BroadcastReceiver
			Intent intent = new Intent();
			intent.setClass(context, ReportingService.class);
			intent.putExtra("promptUser", true);
			context.startService(intent);
			return;
		}

		long lastSynced = prefs.getLong(Const.ANONYMOUS_LAST_CHECKED, 0);
		if (!force) {
				if (lastSynced == 0) {
						setAlarm(context, 0);
						return;
				}
				long timeElapsed = System.currentTimeMillis() - lastSynced;
				if (timeElapsed < UPDATE_INTERVAL) {
						long timeLeft = UPDATE_INTERVAL - timeElapsed;
						Log.d(ReportingService.TAG, "Waiting for next sync : "
										+ timeLeft / MILLIS_PER_HOUR + " hours");
						return;
				}
		}

		String lastReportedVersion = prefs.getString(Const.ANONYMOUS_LAST_REPORT_VERSION, null);
		if (!Utilities.getRomVersionHash().equals(lastReportedVersion)) {
			// if rom version has changed since last reporting, do an immediate reporting
			lastSynced = 1;
		}

		Intent intent = new Intent();
		intent.setClass(context, ReportingService.class);
		context.startService(intent);
	}
}
