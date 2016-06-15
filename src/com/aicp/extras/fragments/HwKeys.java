/*
 * Copyright (C) 2018 AICP
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
import android.support.v7.preference.PreferenceCategory;

import com.aicp.extras.R;

import com.android.internal.utils.du.ActionConstants;

public class HwKeys extends ActionFragment {

    // category keys
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_POWER = "power_key";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    @Override
    protected int getPreferenceResource() {
        return R.xml.hw_keys;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // bits for hardware keys present on device
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        // read bits for present hardware keys
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        // load categories and init/remove preferences based on device
        // configuration
        final PreferenceCategory backCategory = (PreferenceCategory)
                findPreference(CATEGORY_BACK);
        final PreferenceCategory homeCategory = (PreferenceCategory)
                findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory = (PreferenceCategory)
                findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory = (PreferenceCategory)
                findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory = (PreferenceCategory)
                findPreference(CATEGORY_APPSWITCH);

        // back key
        if (!hasBackKey) {
            backCategory.getParent().removePreference(backCategory);
        }

        // home key
        if (!hasHomeKey) {
            homeCategory.getParent().removePreference(homeCategory);
        }

        // App switch key (recents)
        if (!hasAppSwitchKey) {
            appSwitchCategory.getParent().removePreference(appSwitchCategory);
        }

        // menu key
        if (!hasMenuKey) {
            menuCategory.getParent().removePreference(menuCategory);
        }

        // search/assist key
        if (!hasAssistKey) {
            assistCategory.getParent().removePreference(assistCategory);
        }

        // let super know we can load ActionPreferences
        onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.HWKEYS));
    }

    @Override
    protected boolean usesExtendedActionsList() {
        return true;
    }
}
