/*
 * Copyright (C) 2017-2020 AICP
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

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;
import com.aicp.gear.preference.SystemSettingIntListPreference;
import com.aicp.gear.preference.SystemSettingSeekBarPreference;
import com.aicp.gear.preference.SystemSettingSwitchPreference;
import com.android.internal.util.aicp.DeviceUtils;

public class Notifications extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String ALERT_SLIDER_PREF = "alert_slider_notifications";
    private static final String KEY_RINGTONE_FOCUS = "ringtone_focus_mode";
    private static final String CAT_NOTIFICATION_FLASHLIGHT = "notification_flash";
    private static final String PREF_FLASHLIGHT_ON_CALL = "flashlight_on_call";
    private static final String PREF_FLASHLIGHT_ON_CALL_WAITING = "flashlight_on_call_waiting";
    private static final String PREF_FLASHLIGHT_ON_CALL_IGNORE_DND = "flashlight_on_call_ignore_dnd";
    private static final String PREF_FLASHLIGHT_ON_CALL_RATE = "flashlight_on_call_rate";
    private static final String PREF_NOTIFICATION_HEADER = "notification_headers";
    private static final String PREF_BATTERY_LIGHT = "battery_light_enabled";

    private SwitchPreference mFlashOnCallWaiting;
    private SwitchPreference mFlashOnCallIgnoreDND;
    private SystemSettingIntListPreference mFlashOnCall;
    private SystemSettingSeekBarPreference mFlashOnCallRate;
    private SystemSettingSwitchPreference mNotificationHeader;

    @Override
    protected int getPreferenceResource() {
        return R.xml.notifications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        Util.requireFullStatusbar(getActivity(),
                findPreference(Settings.System.STATUS_BAR_SHOW_TICKER));
*/
        mFlashOnCallWaiting = (SwitchPreference) getPreferenceScreen().findPreference(PREF_FLASHLIGHT_ON_CALL_WAITING);
        mFlashOnCallIgnoreDND = (SwitchPreference) getPreferenceScreen().findPreference(PREF_FLASHLIGHT_ON_CALL_IGNORE_DND);
        mFlashOnCallRate = (SystemSettingSeekBarPreference) getPreferenceScreen().findPreference(PREF_FLASHLIGHT_ON_CALL_RATE);

        mFlashOnCall = (SystemSettingIntListPreference) getPreferenceScreen().findPreference(PREF_FLASHLIGHT_ON_CALL);
//        mNotificationHeader = (SystemSettingSwitchPreference) getPreferenceScreen().findPreference(PREF_NOTIFICATION_HEADER);

        boolean optionEnabled = Settings.System.getInt(getContentResolver(),
                Settings.System.FLASHLIGHT_ON_CALL, 0) != 0;
        updateDependencies(optionEnabled);
        mFlashOnCall.setOnPreferenceChangeListener(this);

        if (!DeviceUtils.deviceSupportsFlashLight(getActivity())) {
            getPreferenceScreen().removePreference(findPreference(
                    CAT_NOTIFICATION_FLASHLIGHT));
        }
/*
        Util.requireConfig(getActivity(), findPreference(PREF_BATTERY_LIGHT),
                com.android.internal.R.bool.config_intrusiveNotificationLed, true, false);
*/
        Util.requireConfig(getActivity(), findPreference(ALERT_SLIDER_PREF),
                com.android.internal.R.bool.config_hasAlertSlider, true, false);
        Util.requireConfig(getActivity(), findPreference(KEY_RINGTONE_FOCUS),
                com.android.internal.R.bool.config_deviceRingtoneFocusMode, true, false);
/*
        mNotificationHeader.setOnPreferenceChangeListener(this);*/
    }

    private void updateDependencies(boolean enabled) {
        mFlashOnCallWaiting.setEnabled(enabled);
        mFlashOnCallIgnoreDND.setEnabled(enabled);
        mFlashOnCallRate.setEnabled(enabled);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContentResolver();
        if (preference == mFlashOnCall) {
            int value = Integer.parseInt((String) newValue);
            updateDependencies(value != 0);
            return true;
        }/* else if (preference == mNotificationHeader) {
            Util.showSystemUiRestartDialog(getActivity());
            return true;
        }*/
        return false;
    }
}
