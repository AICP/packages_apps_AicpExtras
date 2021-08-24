/*
 * Copyright (C) 2017 AICP
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import android.text.format.DateFormat;
import android.util.Log;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.Constants;
import com.aicp.extras.R;
import com.aicp.extras.utils.SuShell;
import com.aicp.extras.utils.SuTask;
import com.aicp.extras.utils.Util;

import java.time.format.DateTimeFormatter;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.android.settingslib.development.SystemPropPoker;

public class SystemBehaviour extends BaseSettingsFragment
         implements Preference.OnPreferenceChangeListener {
    private static final String TAG = SystemBehaviour.class.getSimpleName();

    private static final String KEY_SMART_PIXELS = "smart_pixels_enable";
    private static final String KEY_ENABLE_BLURS = "enable_blurs_on_windows";
    private static final String KEY_SLEEP_MODE = "sleep_mode";
    private static final String DISABLE_BLURS_SYSPROP = "persist.sys.sf.disable_blurs";
    private static final String SF_PROP_REQUIRED_FOR_BLUR = "ro.surface_flinger.supports_background_blur";

/*
    private static final String KEY_AUDIO_PANEL_POSITION = "volume_panel_on_left";
    private static final String KEY_BARS = "bars_settings";
*/
    private static final String SELINUX_CATEGORY = "selinux";

    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;

    private SwitchPreference mEnableBlurPref;

    private Preference mSleepMode;

    @Override
    protected int getPreferenceResource() {
        return R.xml.system_behaviour;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SELinux
        Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
        mSelinuxMode = (SwitchPreference) findPreference(Constants.PREF_SELINUX_MODE);
        mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
        mSelinuxMode.setOnPreferenceChangeListener(this);
        mSelinuxPersistence =
                (SwitchPreference) findPreference(Constants.PREF_SELINUX_PERSISTENCE);
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxPersistence.setChecked(getContext()
                .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
                .contains(Constants.PREF_SELINUX_MODE));
        Util.requireRoot(getActivity(), selinuxCategory);
/*
        Util.requireConfig(getActivity(), findPreference(KEY_BARS),
                com.android.internal.R.bool.config_haveHigherAspectRatioScreen, true, false);
*/
        Util.requireConfig(getActivity(), findPreference(KEY_SMART_PIXELS),
                com.android.internal.R.bool.config_enableSmartPixels, true, false);

        mEnableBlurPref = (SwitchPreference) findPreference(KEY_ENABLE_BLURS);
        mEnableBlurPref.setChecked(!SystemProperties.getBoolean(
                DISABLE_BLURS_SYSPROP, false /* default */));
        mEnableBlurPref.setOnPreferenceChangeListener(this);
        Util.requireProp(getActivity(), mEnableBlurPref, SF_PROP_REQUIRED_FOR_BLUR, false /* default */, true);

        mSleepMode = findPreference(KEY_SLEEP_MODE);
        updateSleepModeSummary();
    }

    private void updateSleepModeSummary() {
        if (mSleepMode == null) return;
        boolean enabled = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.SLEEP_MODE_ENABLED, 0, UserHandle.USER_CURRENT) == 1;
        int mode = Settings.Secure.getIntForUser(getActivity().getContentResolver(),
                Settings.Secure.SLEEP_MODE_AUTO_MODE, 0, UserHandle.USER_CURRENT);
        String timeValue = Settings.Secure.getStringForUser(getActivity().getContentResolver(),
                Settings.Secure.SLEEP_MODE_AUTO_TIME, UserHandle.USER_CURRENT);
        if (timeValue == null || timeValue.equals("")) timeValue = "20:00,07:00";
        String[] time = timeValue.split(",", 0);
        String outputFormat = DateFormat.is24HourFormat(getContext()) ? "HH:mm" : "h:mm a";
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(outputFormat);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime sinceValue = LocalTime.parse(time[0], formatter);
        LocalTime tillValue = LocalTime.parse(time[1], formatter);
        String detail;
        switch (mode) {
            default:
            case 0:
                detail = getActivity().getString(enabled
                        ? R.string.night_display_summary_on_auto_mode_never
                        : R.string.night_display_summary_off_auto_mode_never);
                break;
            case 1:
                detail = getActivity().getString(enabled
                        ? R.string.night_display_summary_on_auto_mode_twilight
                        : R.string.night_display_summary_off_auto_mode_twilight);
                break;
            case 2:
                if (enabled) {
                    detail = getActivity().getString(R.string.night_display_summary_on_auto_mode_custom, tillValue.format(outputFormatter));
                } else {
                    detail = getActivity().getString(R.string.night_display_summary_off_auto_mode_custom, sinceValue.format(outputFormatter));
                }
                break;
            case 3:
                if (enabled) {
                    detail = getActivity().getString(R.string.night_display_summary_on_auto_mode_custom, tillValue.format(outputFormatter));
                } else {
                    detail = getActivity().getString(R.string.night_display_summary_off_auto_mode_twilight);
                }
                break;
            case 4:
                if (enabled) {
                    detail = getActivity().getString(R.string.night_display_summary_on_auto_mode_twilight);
                } else {
                    detail = getActivity().getString(R.string.night_display_summary_off_auto_mode_custom, sinceValue.format(outputFormatter));
                }
                break;
        }
        String summary = getActivity().getString(enabled
                ? R.string.night_display_summary_on
                : R.string.night_display_summary_off, detail);
        mSleepMode.setSummary(summary);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSleepModeSummary();
    }

    @Override
    public void onPause() {
        super.onPause();
        updateSleepModeSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSelinuxMode) {
            if ((Boolean) newValue) {
                new SwitchSelinuxTask(getActivity()).execute(true);
                setSelinuxEnabled(true, mSelinuxPersistence.isChecked());
            } else {
                new SwitchSelinuxTask(getActivity()).execute(false);
                setSelinuxEnabled(false, mSelinuxPersistence.isChecked());
            }
            return true;
        } else if (preference == mSelinuxPersistence) {
            setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) newValue);
            return true;
        } else if (preference == mEnableBlurPref) {
            final boolean isDisabled = !(Boolean) newValue;
            SystemProperties.set(DISABLE_BLURS_SYSPROP, isDisabled ? "1" : "0");
            SystemPropPoker.getInstance().poke();
            return true;
        }
        return false;
    }

    private void setSelinuxEnabled(boolean status, boolean persistent) {
        SharedPreferences.Editor editor = getContext()
                .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
        if (persistent) {
            editor.putBoolean(Constants.PREF_SELINUX_MODE, status);
        } else {
            editor.remove(Constants.PREF_SELINUX_MODE);
        }
        editor.apply();
        mSelinuxMode.setChecked(status);
    }

    private class SwitchSelinuxTask extends SuTask<Boolean> {
        public SwitchSelinuxTask(Context context) {
            super(context);
        }
        @Override
        protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
            if (params.length != 1) {
                Log.e(TAG, "SwitchSelinuxTask: invalid params count");
                return;
            }
            if (params[0]) {
                SuShell.runWithSuCheck("setenforce 1");
            } else {
                SuShell.runWithSuCheck("setenforce 0");
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                // Did not work, so restore actual value
                setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
            }
        }
    }
}
