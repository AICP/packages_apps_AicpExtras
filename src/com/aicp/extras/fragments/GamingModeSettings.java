/*
 * Copyright (C) 2020 The exTHmUI Open Source Project
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

import com.android.internal.logging.nano.MetricsProto;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreference;
import androidx.preference.PreferenceScreen;

import com.aicp.extras.R;
import com.aicp.extras.BaseSettingsFragment;
import com.aicp.gear.preference.AicpPreferenceFragment;

import java.util.ArrayList;

import com.aicp.extras.preference.PackageListPreference;
import com.aicp.gear.preference.SystemSettingSeekBarPreference;

public class GamingModeSettings extends BaseSettingsFragment implements OnPreferenceChangeListener {

    private PackageListPreference mGamingPrefList;
    private SwitchPreference mUseMenuSwitch;
    private SwitchPreference mShowFPS;
    private Preference mDanmaku;
    private Preference mQapps;
    private SystemSettingSeekBarPreference mOpacity;

    private boolean performance_supported;

    @Override
    protected int getPreferenceResource() {
        return R.xml.settings_gaming;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final PreferenceScreen prefScreen = getPreferenceScreen();

        final PreferenceCategory perfCat = (PreferenceCategory) prefScreen
                .findPreference("performance_category");

        performance_supported = getResources().getBoolean(
                    com.android.internal.R.bool.config_gamingmode_performance);

        if (!performance_supported) {
            prefScreen.removePreference(perfCat);
        }

        mUseMenuSwitch = (SwitchPreference) findPreference("gaming_mode_use_overlay_menu");
        mShowFPS = (SwitchPreference) findPreference("gaming_mode_fps_info");
        mDanmaku = (Preference) findPreference("gaming_mode_notification_danmaku");
        mQapps = (Preference) findPreference("gaming_mode_quick_start_apps");
        mOpacity = (SystemSettingSeekBarPreference) findPreference("gaming_mode_menu_opacity");

        boolean fpsEnabled = Settings.System.getInt(getContentResolver(),
                            Settings.System.GAMING_MODE_SHOW_FPSINFO, 0) == 1;

        mShowFPS.setChecked(fpsEnabled);
        mShowFPS.setOnPreferenceChangeListener(this);

        boolean menuEnabled = Settings.System.getInt(getContentResolver(),
                            Settings.System.GAMING_MODE_USE_OVERLAY_MENU, 1) == 1;
        mUseMenuSwitch.setChecked(menuEnabled);
        mUseMenuSwitch.setOnPreferenceChangeListener(this);

        mDanmaku.setEnabled(menuEnabled);
        mQapps.setEnabled(menuEnabled);
        mOpacity.setEnabled(menuEnabled);

        mGamingPrefList = (PackageListPreference) findPreference("gaming_mode_app_list");
        mGamingPrefList.setRemovedListKey(Settings.System.GAMING_MODE_REMOVED_APP_LIST);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mUseMenuSwitch) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.GAMING_MODE_USE_OVERLAY_MENU,
                    value ? 1 : 0);
            mDanmaku.setEnabled(value);
            mQapps.setEnabled(value);
            mOpacity.setEnabled(value);
            return true;
        } else if (preference == mShowFPS) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.GAMING_MODE_SHOW_FPSINFO,
                    value ? 1 : 0);
            return true;
        }
        return false;
    }

}
