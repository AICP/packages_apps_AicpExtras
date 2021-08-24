/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

package com.aicp.extras.fragments;

import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.SystemSettingMasterSwitchPreference;
import com.aicp.gear.preference.SecureSettingListPreference;
import com.aicp.gear.preference.SecureSettingSwitchPreference;
import com.android.settingslib.widget.LayoutPreference;

import java.time.format.DateTimeFormatter;
import java.time.LocalTime;

public class SleepMode extends BaseSettingsFragment implements
    Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = "SleepMode";

    private static final String MODE_KEY = "sleep_mode_auto_mode";
    private static final String SINCE_PREF_KEY = "sleep_mode_auto_since";
    private static final String TILL_PREF_KEY = "sleep_mode_auto_till";
    private static final String KEY_SLEEP_BUTTON = "sleep_mode_button";
    private static final String TOGGLES_CATEGORY_KEY = "sleep_mode_toggles";

    private PreferenceCategory mToggles;
    private SecureSettingListPreference mModePref;
    private Preference mSincePref;
    private Preference mTillPref;
    private Button mTurnOnButton;
    private Button mTurnOffButton;
    private Context mContext;
    private Handler mHandler;
    private ContentResolver mContentResolver;
    private boolean mIsNavSwitchingMode = false;

    private final View.OnClickListener mButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mTurnOnButton || v == mTurnOffButton) {
                boolean enabled = Settings.Secure.getIntForUser(mContentResolver,
                        Settings.Secure.SLEEP_MODE_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
                enableSleepMode(!enabled);
                updateStateInternal();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsNavSwitchingMode = false;
                    }
                }, 1500);
            }
        }
    };

    @Override
    protected int getPreferenceResource() {
        return R.xml.sleep_mode_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        mHandler = new Handler();
        mContentResolver = getActivity().getContentResolver();

        SettingsObserver settingsObserver = new SettingsObserver(new Handler());
        settingsObserver.observe();

        mSincePref = findPreference(SINCE_PREF_KEY);
        mSincePref.setOnPreferenceClickListener(this);
        mTillPref = findPreference(TILL_PREF_KEY);
        mTillPref.setOnPreferenceClickListener(this);

        int mode = Settings.Secure.getIntForUser(mContentResolver,
                MODE_KEY, 0, UserHandle.USER_CURRENT);
        mModePref = (SecureSettingListPreference) findPreference(MODE_KEY);
        mModePref.setOnPreferenceChangeListener(this);

        mToggles = findPreference(TOGGLES_CATEGORY_KEY);

        LayoutPreference preference = findPreference(KEY_SLEEP_BUTTON);
        mTurnOnButton = preference.findViewById(R.id.sleep_mode_on_button);
        mTurnOnButton.setOnClickListener(mButtonListener);
        mTurnOffButton = preference.findViewById(R.id.sleep_mode_off_button);
        mTurnOffButton.setOnClickListener(mButtonListener);

        updateTimeEnablement(mode);
        updateTimeSummary(mode);
        updateStateInternal();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mModePref) {
            int value = Integer.parseInt((String) newValue);
            updateTimeEnablement(value);
            updateTimeSummary(value);
            updateStateInternal();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mSincePref || preference == mTillPref) {
            String[] times = getCustomTimeSetting();
            boolean isSince = preference == mSincePref;
            int hour, minute;
            TimePickerDialog.OnTimeSetListener listener = (view, hourOfDay, minute1) -> {
                updateTimeSetting(isSince, hourOfDay, minute1);
            };
            if (isSince) {
                String[] sinceValues = times[0].split(":", 0);
                hour = Integer.parseInt(sinceValues[0]);
                minute = Integer.parseInt(sinceValues[1]);
            } else {
                String[] tillValues = times[1].split(":", 0);
                hour = Integer.parseInt(tillValues[0]);
                minute = Integer.parseInt(tillValues[1]);
            }
            TimePickerDialog dialog = new TimePickerDialog(mContext, listener,
                    hour, minute, DateFormat.is24HourFormat(mContext));
            dialog.show();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private String[] getCustomTimeSetting() {
        String value = Settings.Secure.getStringForUser(mContentResolver,
                Settings.Secure.SLEEP_MODE_AUTO_TIME, UserHandle.USER_CURRENT);
        if (value == null || value.equals("")) value = "20:00,07:00";
        return value.split(",", 0);
    }

    private void updateTimeEnablement(int mode) {
        mSincePref.setVisible(mode == 2 || mode == 4);
        mTillPref.setVisible(mode == 2 || mode == 3);
    }

    private void updateTimeSummary(int mode) {
        updateTimeSummary(getCustomTimeSetting(), mode);
    }

    private void updateTimeSummary(String[] times, int mode) {
        if (mode == 0) {
            mSincePref.setSummary("-");
            mTillPref.setSummary("-");
            return;
        }

        if (mode == 1) {
            mSincePref.setSummary(R.string.sleep_mode_schedule_sunset);
            mTillPref.setSummary(R.string.sleep_mode_schedule_sunrise);
            return;
        }

        String outputFormat = DateFormat.is24HourFormat(mContext) ? "HH:mm" : "h:mm a";
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime sinceDT = LocalTime.parse(times[0], formatter);
        LocalTime tillDT = LocalTime.parse(times[1], formatter);

        if (mode == 3) {
            mSincePref.setSummary(R.string.sleep_mode_schedule_sunset);
            mTillPref.setSummary(tillDT.format(outputFormatter));
        } else if (mode == 4) {
            mTillPref.setSummary(R.string.sleep_mode_schedule_sunrise);
            mSincePref.setSummary(sinceDT.format(outputFormatter));
        } else {
            mSincePref.setSummary(sinceDT.format(outputFormatter));
            mTillPref.setSummary(tillDT.format(outputFormatter));
        }
    }

    private void updateTimeSetting(boolean since, int hour, int minute) {
        String[] times = getCustomTimeSetting();
        String nHour = "";
        String nMinute = "";
        if (hour < 10) nHour += "0";
        if (minute < 10) nMinute += "0";
        nHour += String.valueOf(hour);
        nMinute += String.valueOf(minute);
        times[since ? 0 : 1] = nHour + ":" + nMinute;
        Settings.Secure.putStringForUser(mContentResolver,
                Settings.Secure.SLEEP_MODE_AUTO_TIME,
                times[0] + "," + times[1], UserHandle.USER_CURRENT);
        updateTimeSummary(times, Integer.parseInt(mModePref.getValue()));
    }

    private void updateStateInternal() {
        if (mTurnOnButton == null || mTurnOffButton == null) {
            return;
        }

        int mode = Settings.Secure.getIntForUser(mContentResolver,
                MODE_KEY, 0, UserHandle.USER_CURRENT);
        boolean isActivated = Settings.Secure.getIntForUser(mContentResolver,
                Settings.Secure.SLEEP_MODE_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
        String timeValue = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                Settings.Secure.SLEEP_MODE_AUTO_TIME, UserHandle.USER_CURRENT);
        if (timeValue == null || timeValue.equals("")) timeValue = "20:00,07:00";
        String[] time = timeValue.split(",", 0);
        String outputFormat = DateFormat.is24HourFormat(mContext) ? "HH:mm" : "h:mm a";
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime sinceValue = LocalTime.parse(time[0], formatter);
        LocalTime tillValue = LocalTime.parse(time[1], formatter);

        String buttonText;

        switch (mode) {
            default:
            case 0:
                buttonText = mContext.getString(isActivated ? R.string.night_display_activation_off_manual
                        : R.string.night_display_activation_on_manual);
                break;
            case 1:
                buttonText = mContext.getString(isActivated ? R.string.night_display_activation_off_twilight
                        : R.string.night_display_activation_on_twilight);
                break;
            case 2:
                if (isActivated) {
                    buttonText = mContext.getString(R.string.night_display_activation_off_custom, sinceValue.format(outputFormatter));
                } else {
                    buttonText = mContext.getString(R.string.night_display_activation_on_custom, tillValue.format(outputFormatter));
                }
                break;
            case 3:
                if (isActivated) {
                    buttonText = mContext.getString(R.string.night_display_activation_off_twilight);
                } else {
                    buttonText = mContext.getString(R.string.night_display_activation_on_custom, tillValue.format(outputFormatter));
                }
                break;
            case 4:
                if (isActivated) {
                    buttonText = mContext.getString(R.string.night_display_activation_off_custom, sinceValue.format(outputFormatter));
                } else {
                    buttonText = mContext.getString(R.string.night_display_activation_on_twilight);
                }
                break;
        }

        if (isActivated) {
            mTurnOnButton.setVisibility(View.GONE);
            mTurnOffButton.setVisibility(View.VISIBLE);
            mTurnOffButton.setText(buttonText);
            mToggles.setEnabled(false);
        } else {
            mTurnOnButton.setVisibility(View.VISIBLE);
            mTurnOffButton.setVisibility(View.GONE);
            mTurnOnButton.setText(buttonText);
            mToggles.setEnabled(true);
        }
    }

    private void enableSleepMode(boolean enable) {
        if (mIsNavSwitchingMode) return;
        mIsNavSwitchingMode = true;
        Settings.Secure.putIntForUser(mContext.getContentResolver(),
                Settings.Secure.SLEEP_MODE_ENABLED, enable ? 1 : 0, UserHandle.USER_CURRENT);
    }

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContentResolver;
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SLEEP_MODE_ENABLED), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.Secure.getUriFor(
                    Settings.Secure.SLEEP_MODE_AUTO_MODE), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateStateInternal();
        }
    }
}
