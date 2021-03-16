/*
 * Copyright (C) 2021 Yet Another AOSP Project
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
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

import com.aicp.gear.preference.SecureSettingListPreference;

public class AODSchedule extends BaseSettingsFragment implements
        Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    static final int MODE_DISABLED = 0;
    static final int MODE_NIGHT = 1;
    static final int MODE_TIME = 2;
    static final int MODE_MIXED_SUNSET = 3;
    static final int MODE_MIXED_SUNRISE = 4;

    private static final String MODE_KEY = "doze_always_on_auto_mode";
    private static final String FROM_PREF_KEY = "doze_always_on_auto_from";
    private static final String TILL_PREF_KEY = "doze_always_on_auto_to";

    private SecureSettingListPreference mModePref;
    private Preference mFromPref;
    private Preference mTillPref;

    @Override
    protected int getPreferenceResource() {
        return R.xml.always_on_display_schedule;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PreferenceScreen screen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mFromPref = findPreference(FROM_PREF_KEY);
        mFromPref.setOnPreferenceClickListener(this);
        mTillPref = findPreference(TILL_PREF_KEY);
        mTillPref.setOnPreferenceClickListener(this);

        int mode = Settings.Secure.getIntForUser(resolver,
                MODE_KEY, MODE_DISABLED, UserHandle.USER_CURRENT);
        mModePref = (SecureSettingListPreference) findPreference(MODE_KEY);
        mModePref.setOnPreferenceChangeListener(this);

        updateTimeEnablement(mode);
        updateTimeSummary(mode);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mModePref) {
            int value = Integer.valueOf((String) objValue);
            updateTimeEnablement(value);
            updateTimeSummary(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String[] times = getCustomTimeSetting();
        boolean isFrom = preference == mFromPref;
        int hour, minute; hour = minute = 0;
        TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                updateTimeSetting(isFrom, hourOfDay, minute);
            }
        };
        if (isFrom) {
            String[] fromValues = times[0].split(":", 0);
            hour = Integer.parseInt(fromValues[0]);
            minute = Integer.parseInt(fromValues[1]);
        } else {
            String[] tillValues = times[1].split(":", 0);
            hour = Integer.parseInt(tillValues[0]);
            minute = Integer.parseInt(tillValues[1]);
        }
        TimePickerDialog dialog = new TimePickerDialog(getContext(), listener,
                hour, minute, DateFormat.is24HourFormat(getContext()));
        dialog.show();
        return true;
    }

    private String[] getCustomTimeSetting() {
        String value = Settings.Secure.getStringForUser(getActivity().getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON_AUTO_TIME, UserHandle.USER_CURRENT);
        if (value == null || value.isEmpty()) value = "20:00,07:00";
        return value.split(",", 0);
    }

    private void updateTimeEnablement(int mode) {
        mFromPref.setEnabled(mode == MODE_TIME || mode == MODE_MIXED_SUNRISE);
        mTillPref.setEnabled(mode == MODE_TIME || mode == MODE_MIXED_SUNSET);
    }

    private void updateTimeSummary(int mode) {
        updateTimeSummary(getCustomTimeSetting(), mode);
    }

    private void updateTimeSummary(String[] times, int mode) {
        if (mode == MODE_DISABLED) {
            mFromPref.setSummary("-");
            mTillPref.setSummary("-");
            return;
        }
        if (mode == MODE_NIGHT) {
            mFromPref.setSummary(R.string.always_on_display_schedule_sunset);
            mTillPref.setSummary(R.string.always_on_display_schedule_sunrise);
            return;
        }
        if (mode == MODE_MIXED_SUNSET) {
            mFromPref.setSummary(R.string.always_on_display_schedule_sunset);
        } else if (mode == MODE_MIXED_SUNRISE) {
            mTillPref.setSummary(R.string.always_on_display_schedule_sunrise);
        }
        if (DateFormat.is24HourFormat(getContext())) {
            if (mode != MODE_MIXED_SUNSET) mFromPref.setSummary(times[0]);
            if (mode != MODE_MIXED_SUNRISE) mTillPref.setSummary(times[1]);
            return;
        }
        String[] fromValues = times[0].split(":", 0);
        String[] tillValues = times[1].split(":", 0);
        int fromHour = Integer.parseInt(fromValues[0]);
        int tillHour = Integer.parseInt(tillValues[0]);
        String fromSummary = "";
        String tillSummary = "";
        if (fromHour > 12) {
            fromHour -= 12;
            fromSummary += String.valueOf(fromHour) + ":" + fromValues[1] + " PM";
        } else {
            fromSummary = times[0].substring(1) + " AM";
        }
        if (tillHour > 12) {
            tillHour -= 12;
            tillSummary += String.valueOf(tillHour) + ":" + tillValues[1] + " PM";
        } else {
            tillSummary = times[0].substring(1) + " AM";
        }
        if (mode != MODE_MIXED_SUNSET) mFromPref.setSummary(fromSummary);
        if (mode != MODE_MIXED_SUNRISE) mTillPref.setSummary(tillSummary);
    }

    private void updateTimeSetting(boolean isFrom, int hour, int minute) {
        String[] times = getCustomTimeSetting();
        String nHour = "";
        String nMinute = "";
        if (hour < 10) nHour += "0";
        if (minute < 10) nMinute += "0";
        nHour += String.valueOf(hour);
        nMinute += String.valueOf(minute);
        times[isFrom ? 0 : 1] = nHour + ":" + nMinute;
        Settings.Secure.putStringForUser(getActivity().getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON_AUTO_TIME,
                times[0] + "," + times[1], UserHandle.USER_CURRENT);
        updateTimeSummary(times, Integer.parseInt(mModePref.getValue()));
    }
}
