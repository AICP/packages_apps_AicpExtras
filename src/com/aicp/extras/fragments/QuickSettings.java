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
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;


import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SystemSettingSeekBarPreference;

public class QuickSettings extends BaseSettingsFragment
    implements OnPreferenceChangeListener {

    private static final String QS_QUICKBAR_COLUMNS_AUTO = "qs_quickbar_columns_auto";
    private static final String QS_QUICKBAR_COLUMNS_COUNT = "qs_quickbar_columns";

    private SwitchPreference mQQSColsAuto;
    private SystemSettingSeekBarPreference mQQSColsCount;

    @Override
    protected int getPreferenceResource() {
        return R.xml.quick_settings;
    }

    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        mQQSColsAuto = (SwitchPreference) findPreference(QS_QUICKBAR_COLUMNS_AUTO);
        mQQSColsCount = (SystemSettingSeekBarPreference) findPreference(QS_QUICKBAR_COLUMNS_COUNT);

        boolean qqsColsAutoEnabled = Settings.System.getInt(resolver,
                Settings.System.AICP_QS_QUICKBAR_COLUMNS, 6) == -1;
        mQQSColsAuto.setChecked(qqsColsAutoEnabled);
        mQQSColsCount.setEnabled(!qqsColsAutoEnabled);
        mQQSColsAuto.setOnPreferenceChangeListener(this);
    }
    */

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        /*
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQQSColsAuto) {
            Boolean qqsColsAutoEnabled = (Boolean) newValue;
            mQQSColsCount.setEnabled(!qqsColsAutoEnabled);
            if (qqsColsAutoEnabled){
              Settings.System.putInt(resolver,
                      Settings.System.AICP_QS_QUICKBAR_COLUMNS, -1);
            }
            return true;
        }
        */
        return false;
    }
}
