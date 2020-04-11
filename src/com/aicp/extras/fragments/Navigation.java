/*
 * Copyright (C) 2017 The Dirty Unicorns Project
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

import android.os.Bundle;
import android.os.Handler;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.SystemSettingMasterSwitchPreference;

import com.android.internal.util.hwkeys.ActionUtils;

public class Navigation extends BaseSettingsFragment implements
            Preference.OnPreferenceChangeListener {

    private static final String KEY_KILLAPP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String KEY_SWAP_HW_NAVIGATION_KEYS = "swap_navigation_keys";
    private static final String KEY_NAVIGATION_BAR_ENABLED = "navigation_bar_show_new";

    private SystemSettingMasterSwitchPreference mNavigationBar;
    private boolean mIsNavSwitchingMode = false;

    private Handler mHandler;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefScreen = getPreferenceScreen();

        final boolean needsNavbar = ActionUtils.hasNavbarByDefault(getActivity());
        final boolean hwkeysSupported = ActionUtils.isHWKeysSupported(getActivity());
        SwitchPreference longPressBackToKill = (SwitchPreference) findPreference(KEY_KILLAPP_LONGPRESS_BACK);
        SwitchPreference swapHWNavKeys = (SwitchPreference) findPreference(KEY_SWAP_HW_NAVIGATION_KEYS);

        if (needsNavbar || !hwkeysSupported) {
            if (swapHWNavKeys != null) swapHWNavKeys.getParent().removePreference(swapHWNavKeys);
            if (longPressBackToKill != null) longPressBackToKill.getParent().removePreference(longPressBackToKill);
        }

        mNavigationBar = (SystemSettingMasterSwitchPreference) findPreference(KEY_NAVIGATION_BAR_ENABLED);
        mNavigationBar.setOnPreferenceChangeListener(this);
        mHandler = new Handler();
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mNavigationBar) {
            boolean value = (Boolean) objValue;
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);
            return true;
        }
        return false;
    }
}
