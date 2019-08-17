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
import android.graphics.drawable.AdaptiveIconDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import androidx.preference.Preference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

import com.aicp.gear.util.ThemeOverlayHelper;

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
        findPreference(Settings.System.THEMING_CORNERS).setOnPreferenceChangeListener(this);
        findPreference(Settings.System.THEMING_SYSTEM_ICONS_STYLE)
                .setOnPreferenceChangeListener(this);
        findPreference(AdaptiveIconDrawable.MASK_SETTING_PROP).setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (Settings.System.THEMING_BASE.equals(preference.getKey()) ||
                Settings.System.THEMING_CORNERS.equals(preference.getKey()) ||
                Settings.System.THEMING_SYSTEM_ICONS_STYLE.equals(preference.getKey())) {
            if (ThemeOverlayHelper.doesThemeChangeRequireSystemUIRestart(getActivity(),
                        preference.getKey(), null, Integer.parseInt((String) newValue))) {
                postRestartSystemUi();
            }
            return true;
        } else if (AdaptiveIconDrawable.MASK_SETTING_PROP.equals(preference.getKey())) {
            Util.showRebootDialog(getActivity(), getString(R.string.icon_shape_changed_title),
                    getString(R.string.icon_shape_changed_message), true);
            return true;
        } else {
            return false;
        }
    }

    private void postRestartSystemUi() {
        // Keep a context even if activity gets closed
        final Context appContext = getActivity().getApplicationContext();
        mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Util.restartSystemUi(appContext);
                }
        }, 200);
    }

}
