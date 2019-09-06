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
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

public class OtherUi extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = OtherUi.class.getSimpleName();

    private static final String SHOW_CPU_INFO_KEY = "show_cpu_info";

    private static final String KEY_DOZE_ON_CHARGE = "doze_on_charge";

    private SwitchPreference mShowCpuInfo;

    @Override
    protected int getPreferenceResource() {
        return R.xml.other_ui;
    }

    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShowCpuInfo = (SwitchPreference) findPreference(SHOW_CPU_INFO_KEY);
        mShowCpuInfo.setChecked(Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, 0) == 1);
        mShowCpuInfo.setOnPreferenceChangeListener(this);

        boolean dozeAlwaysOnDisplayAvailable = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_dozeAlwaysOnDisplayAvailable);
        Preference DozeOnCharge = findPreference(KEY_DOZE_ON_CHARGE);

        if (!dozeAlwaysOnDisplayAvailable){
            DozeOnCharge.getParent().removePreference(DozeOnCharge);
        }
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
    */

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        /*
        if (preference == mShowCpuInfo) {
            writeCpuInfoOptions((Boolean) newValue);
            return true;
        }
        */
        return false;
    }
}
