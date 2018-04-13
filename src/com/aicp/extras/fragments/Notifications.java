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
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

public class Notifications extends BaseSettingsFragment implements
        OnPreferenceChangeListener {

    private static final String KEY_HEADSUP_NOTIFICATIONS =
                "heads_up_notifications_enabled";
    private static final String KEY_TICKER_NOTIFICATIONS =
                "status_bar_show_ticker";
    private static final String KEY_TICKER_ANIMATION_MODE =
                "status_bar_ticker_animation_mode";

    private SwitchPreference mHeadsUpNotifications;
    private ListPreference mTickerAnimationMode;
    private ListPreference mTickerNotifications;

    @Override
    protected int getPreferenceResource() {
        return R.xml.notifications;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // Headsup Notifications
        mHeadsUpNotifications = (SwitchPreference)
                            prefSet.findPreference(KEY_HEADSUP_NOTIFICATIONS);
        mHeadsUpNotifications.setOnPreferenceChangeListener(this);
        // Statusbar Ticker Notifications
        mTickerNotifications = (ListPreference)
                            prefSet.findPreference(KEY_TICKER_NOTIFICATIONS);
        mTickerNotifications.setOnPreferenceChangeListener(this);
        // Statusbar Ticker Notifications Mode
        mTickerAnimationMode = (ListPreference)
                            prefSet.findPreference(KEY_TICKER_ANIMATION_MODE);

        boolean headsupNotificationsState = Settings.Global.getInt(resolver,
            Settings.Global.HEADS_UP_NOTIFICATIONS_ENABLED, 0) != 0;
        updateDependencies(headsupNotificationsState);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      ContentResolver resolver = getActivity().getContentResolver();

      if (preference == mHeadsUpNotifications) {
          boolean value = (Boolean) newValue;
          updateDependencies(value);
          return true;
      } else if (preference == mTickerNotifications) {
          int val = Integer.parseInt((String) newValue);
          mTickerAnimationMode.setEnabled(val != 0 ? true : false);
          return true;
      }
      return false;
    }

    private void updateDependencies(boolean state) {
        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();

        boolean headsupEnabled = state;
        if (headsupEnabled) {
            Settings.System.putInt(resolver,
                      Settings.System.STATUS_BAR_SHOW_TICKER, 0);
            mTickerNotifications.setValueIndex(0);
            mTickerNotifications.setSummary(res.getString(
                                R.string.ticker_mode_disabled_summary));
        } else {
            mTickerNotifications.setSummary(res.getString(
                                R.string.ticker_mode_summary));
        }
        mTickerNotifications.setEnabled(!headsupEnabled);
        mTickerAnimationMode.setEnabled(!headsupEnabled &&
                  Settings.System.getInt(resolver,
                              Settings.System.STATUS_BAR_SHOW_TICKER, 0) != 0);
    }
}
