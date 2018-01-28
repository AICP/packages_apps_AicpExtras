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
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.utils.Util;
import com.aicp.extras.R;

public class OtherUi extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String SHOW_CPU_INFO_KEY = "show_cpu_info";
    private static final String KEY_ACCIDENTAL_TOUCH = "anbi_enabled";
    private static final String KEY_VIBRATE_ON_CHARGE_STATE_CHANGED = "vibration_on_charge_state_changed";
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "1";

    private SwitchPreference mAccidentalTouch;
    private SwitchPreference mShowCpuInfo;
    private SwitchPreference mVibrateOnPlug;
    private ListPreference mScrollingCachePref;

    @Override
    protected int getPreferenceResource() {
        return R.xml.other_ui;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShowCpuInfo = (SwitchPreference) findPreference(SHOW_CPU_INFO_KEY);
        mShowCpuInfo.setChecked(Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, 0) == 1);
        mShowCpuInfo.setOnPreferenceChangeListener(this);

        mVibrateOnPlug =
                (SwitchPreference) findPreference(KEY_VIBRATE_ON_CHARGE_STATE_CHANGED);

        if(!Util.hasVibrator(getActivity())){
            mVibrateOnPlug.getParent().removePreference(mVibrateOnPlug);
        }

        // Prevent accidental touch to hw keys.
        mAccidentalTouch = (SwitchPreference) findPreference(KEY_ACCIDENTAL_TOUCH);
        int deviceHardwareKeys = getActivity().getResources().getInteger(
                org.lineageos.platform.internal.R.integer.config_deviceHardwareKeys);
        if (deviceHardwareKeys == 0 || deviceHardwareKeys == 64) {
            mAccidentalTouch.getParent().removePreference(mAccidentalTouch);
        }

        mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setOnPreferenceChangeListener(this);
    }

    private void writeCpuInfoOptions(boolean value) {
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, value ? 1 : 0);
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.CPUInfoService");
        if (value) {
            getActivity().startService(service);
        } else {
            getActivity().stopService(service);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mShowCpuInfo) {
            writeCpuInfoOptions((Boolean) newValue);
            return true;
        } else if (preference == mScrollingCachePref) {
            if (newValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String) newValue);
                return true;
            }
        }
        return false;
    }
}
