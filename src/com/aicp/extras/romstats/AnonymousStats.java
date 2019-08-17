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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceClickListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import java.util.Set;

public class AnonymousStats extends BaseSettingsFragment implements
		DialogInterface.OnClickListener, DialogInterface.OnDismissListener,
		Preference.OnPreferenceChangeListener {

	private static final String PREF_VIEW_STATS = "pref_view_stats";
	private static final String PREF_LAST_REPORT_ON = "pref_last_report_on";
	private static final String PREF_REPORT_INTERVAL = "pref_reporting_interval";

	/* package */ static final String KEY_JOB_QUEUE = "pref_job_queue";
	/* package */ static final int QUEUE_MAX_THRESHOLD = 1000;

	private SwitchPreference mEnableReporting;
	private SwitchPreference mPersistentOptout;
	private Preference mViewStats;

	private Dialog mOkDialog;
	private boolean mOkClicked;

	private SharedPreferences mPrefs;

	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(Utilities.SETTINGS_PREF_NAME, 0);
	}

    @Override
    protected int getPreferenceResource() {
        return R.xml.anonymous_stats;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPrefs = getPreferences(getActivity());

		PreferenceScreen prefSet = getPreferenceScreen();
		mPrefs = getActivity().getSharedPreferences(Utilities.SETTINGS_PREF_NAME, 0);
		mEnableReporting = (SwitchPreference) prefSet.findPreference(Const.ANONYMOUS_OPT_IN);
		mPersistentOptout = (SwitchPreference) prefSet.findPreference(Const.ANONYMOUS_OPT_OUT_PERSIST);
		mViewStats = (Preference) prefSet.findPreference(PREF_VIEW_STATS);

		boolean firstBoot = mPrefs.getBoolean(Const.ANONYMOUS_FIRST_BOOT, true);
		if (mEnableReporting.isChecked() && firstBoot) {
				Log.d(Const.TAG, "First app start, set params and report immediately");
				mPrefs.edit().putBoolean(Const.ANONYMOUS_FIRST_BOOT, false).apply();
				mPrefs.edit().putLong(Const.ANONYMOUS_LAST_CHECKED, 1).apply();
				ReportingServiceManager.launchService(getActivity(), false);
		}

		Preference mPrefHolder;
		/* Experimental feature 2 */
		Long lastCheck = mPrefs.getLong(Const.ANONYMOUS_LAST_CHECKED, 0);
		if (lastCheck > 1) {
			// show last checkin date
			String lastCheckStr = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new java.util.Date(lastCheck));
			lastCheckStr = getResources().getString(R.string.last_report_on) + ": " + lastCheckStr;

			mPrefHolder = prefSet.findPreference(PREF_LAST_REPORT_ON);
			mPrefHolder.setTitle(lastCheckStr);

			Long nextCheck = mPrefs.getLong(Const.ANONYMOUS_NEXT_ALARM, 0);
			if (nextCheck > 0) {
				String nextAlarmStr = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new java.util.Date(nextCheck));
				nextAlarmStr = getResources().getString(R.string.next_report_on) + ": " + nextAlarmStr;
				mPrefHolder.setSummary(nextAlarmStr);
			}
		} else {
            mPrefHolder = prefSet.findPreference(PREF_LAST_REPORT_ON);
            mPrefHolder.getParent().removePreference(mPrefHolder);
		}

		mPrefHolder = prefSet.findPreference(PREF_REPORT_INTERVAL);
		int tFrame = (int) Utilities.getTimeFrame();
		mPrefHolder.setSummary(getResources().getQuantityString(R.plurals.reporting_interval_days, tFrame, tFrame));
	}

	@Override
	public boolean onPreferenceTreeClick(Preference preference) {
		if (preference == mEnableReporting) {
			if (mEnableReporting.isChecked()) {
				// Display the confirmation dialog
				mOkClicked = false;
				if (mOkDialog != null) {
					mOkDialog.dismiss();
				}
				mOkDialog = new AlertDialog.Builder(getActivity())
						.setMessage(this.getResources().getString(R.string.anonymous_statistics_warning))
						.setTitle(R.string.anonymous_statistics_warning_title)
						.setPositiveButton(android.R.string.yes, this)
						.setNeutralButton(getString(R.string.anonymous_learn_more), this)
						.setNegativeButton(android.R.string.no, this)
						.show();
				mOkDialog.setOnDismissListener(this);
			} else {
				// Disable reporting
				mPrefs.edit().putBoolean(Const.ANONYMOUS_OPT_IN, false).apply();
			}
		} else if (preference == mPersistentOptout) {
			if (mPersistentOptout.isChecked()) {
				try {
					File sdCard = Environment.getExternalStorageDirectory();
					File dir = new File (sdCard.getAbsolutePath() + "/.AICPROMStats");
					dir.mkdirs();
					File cookieFile = new File(dir, "optout");

					FileOutputStream optOutCookie = new FileOutputStream(cookieFile);
					OutputStreamWriter oStream = new OutputStreamWriter(optOutCookie);
		            oStream.write("true");
		            oStream.close();
		            optOutCookie.close();
		            Log.d(Const.TAG, "Persistent Opt-Out cookie written successfully");
				} catch (IOException e) {
					Log.e(Const.TAG, "Unable to write persistent optout cookie", e);
				}
			} else {
				try {
					File sdCard = Environment.getExternalStorageDirectory();
					File dir = new File (sdCard.getAbsolutePath() + "/.AICPROMStats");
					File cookieFile = new File(dir, "optout");
					cookieFile.delete();
					Log.d(Const.TAG, "Persistent Opt-Out cookie removed successfully");
				} catch (Exception e) {
					Log.w(Const.TAG, "Unable to write persistent optout cookie", e);
				}
			}
		} else if (preference == mViewStats) {
			// Display the stats page
			Uri uri = Uri.parse(Utilities.getStatsUrl());
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		} else {
			// If we didn't handle it, let preferences handle it.
			return super.onPreferenceTreeClick(preference);
		}
		return true;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (!mOkClicked) {
			mEnableReporting.setChecked(false);
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		mPrefs.edit().putBoolean(Const.ANONYMOUS_FIRST_BOOT, false).apply();
		Log.d(Const.TAG, "No more First Boot.");
		if (which == DialogInterface.BUTTON_POSITIVE) {
			mOkClicked = true;
			mPrefs.edit().putBoolean(Const.ANONYMOUS_OPT_IN, true).apply();
			Log.d(Const.TAG, "User opted in");

			mPersistentOptout.setChecked(false);
			try {
				File sdCard = Environment.getExternalStorageDirectory();
				File dir = new File (sdCard.getAbsolutePath() + "/.AICPROMStats");
				File cookieFile = new File(dir, "optout");
				cookieFile.delete();
				Log.d(Const.TAG, "Persistent Opt-Out cookie removed successfully");
			} catch (Exception e) {
				Log.w(Const.TAG, "Unable to write persistent optout cookie", e);
			}
			ReportingServiceManager.launchService(getActivity(), true);
		} else if (which == DialogInterface.BUTTON_NEGATIVE) {
			mEnableReporting.setChecked(false);
		} else {
			Uri uri = Uri.parse("http://aicp-rom.com/");
			startActivity(new Intent(Intent.ACTION_VIEW, uri));
		}
	}

	public static Set<String> getJobQueue(Context context) {
	    return getPreferences(context).getStringSet(KEY_JOB_QUEUE, new ArraySet<String>());
	}

	public static void clearJobQueue(Context context) {
	    getPreferences(context)
	            .edit()
	            .remove(KEY_JOB_QUEUE)
	            .commit();
	}

	public static void addJob(Context context, int jobId) {
	    Set<String> jobQueue = getJobQueue(context);
	    jobQueue.add(String.valueOf(jobId));

	    getPreferences(context)
	            .edit()
	            .putStringSet(KEY_JOB_QUEUE, jobQueue)
	            .commit();
	}

	public static void removeJob(Context context, int jobId) {
	    Set<String> jobQueue = getJobQueue(context);
	    jobQueue.remove(String.valueOf(jobId));
	    getPreferences(context)
	            .edit()
	            .putStringSet(KEY_JOB_QUEUE, jobQueue)
	            .commit();
	}

	/**
	 * @param context context to use to get prefs
	 * @return Returns the next unused int in the job queue, up until {@link #QUEUE_MAX_THRESHOLD}
	 * is reached, then it will return -1
	 */
	public static int getNextJobId(Context context) {
	    Set<String> currentQueue = getJobQueue(context);

	    if (currentQueue == null) {
	        return 1;
	    } else if (currentQueue.size() >= QUEUE_MAX_THRESHOLD) {
	        return -1;
	    } else {
	        int i = 1;
	        while (currentQueue.contains(String.valueOf(i))) {
	            i++;
	        }
	        return i;

	    }
	}
}
