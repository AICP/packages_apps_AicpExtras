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

import android.content.ContentResolver;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.aicp.extras.R;
import com.aicp.extras.BaseSettingsFragment;

public class StatusBarBatterySettings extends BaseSettingsFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "StatusBarBatterySettings";

    private static final String STATUSBAR_BATTERY_STYLE = "statusbar_battery_style";
    private static final String FORCE_BATTERY_PERCENTAGE = "keyguard_qsheader_show_battery_percent";

    private ListPreference mBatteryStyle;
    private SwitchPreference mForceShowQSHeaderPercent;
    private boolean mForceShowPercent;

    @Override
    protected int getPreferenceResource() {
        return R.xml.status_bar_battery_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        mBatteryStyle = (ListPreference) findPreference(STATUSBAR_BATTERY_STYLE);
        mBatteryStyle.setOnPreferenceChangeListener(this);
        mBatteryStyle.setValue(Integer.toString(Settings.Secure.getInt(resolver,
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0)));
        mBatteryStyle.setSummary(mBatteryStyle.getEntry());

        mForceShowQSHeaderPercent = (SwitchPreference) findPreference(FORCE_BATTERY_PERCENTAGE);
        mForceShowQSHeaderPercent.setOnPreferenceChangeListener(this);
        int forceShowQSHeaderPercent = Settings.System.getInt(resolver,
            Settings.System.QS_HEADER_BATTERY_PERCENT, 0);
        mForceShowQSHeaderPercent.setChecked(forceShowQSHeaderPercent != 0);

        mForceShowPercent = Settings.System.getInt(resolver,
            Settings.System.SHOW_BATTERY_PERCENT,0) != 0;

        updateDependencies(Integer.parseInt((String) mBatteryStyle.getValue()));
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      ContentResolver resolver = getActivity().getContentResolver();
      if (preference == mBatteryStyle) {
          int val = Integer.parseInt((String) newValue);
          int index = mBatteryStyle.findIndexOfValue((String) newValue);
          Settings.Secure.putInt(resolver,
                  Settings.Secure.STATUS_BAR_BATTERY_STYLE, val);
          mBatteryStyle.setSummary(mBatteryStyle.getEntries()[index]);
          updateDependencies(val);
          return true;
      } else if (preference == mForceShowQSHeaderPercent) {
          boolean value = (Boolean) newValue;
          Settings.System.putInt(resolver, Settings.System.QS_HEADER_BATTERY_PERCENT,
                  value ? 1 : 0);
          return true;
      }
      return false;
    }

    private void updateDependencies(int index) {
        if (mForceShowPercent) {
              if (index != 5) {
                  mForceShowQSHeaderPercent.setEnabled(false);
              } else {
                  mForceShowQSHeaderPercent.setEnabled(true);
              }
        } else {
              if (index == 2 || index == 3) {
                  mForceShowQSHeaderPercent.setEnabled(false);
              } else {
                  mForceShowQSHeaderPercent.setEnabled(true);
              }
        }
    }
}
