/*
 * Copyright (C) 2018 Android Ice Cold Project
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
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

public class Theming extends BaseSettingsFragment implements Preference.OnPreferenceChangeListener {

    private Handler mHandler = new Handler();

    @Override
    protected int getPreferenceResource() {
        return R.xml.theming;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findPreference(Settings.System.THEMING_BASE).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Settings.System.THEMING_BASE.equals(preference.getKey())) {
            // If notifications are themed (both previously or as a result),
            // we need to restart SystemUI for changes to have effect
            if (hasDarkNotifications(Integer.parseInt((String) newValue)) ||
                    hasDarkNotifications(Settings.System.getInt(getActivity().getContentResolver(),
                            Settings.System.THEMING_BASE, 0))) {
                // Keep a context even if activity gets closed
                final Context appContext = getActivity().getApplicationContext();
                mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Util.restartSystemUi(appContext);
                        }
                });
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean hasDarkNotifications(int baseThemePref) {
            return baseThemePref >= 3 && baseThemePref <= 6;
    }

}
