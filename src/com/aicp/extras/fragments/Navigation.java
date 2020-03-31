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
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.android.internal.util.hwkeys.ActionUtils;

public class Navigation extends BaseSettingsFragment {

    private static final String KEY_KILLAPP_LONGPRESS_BACK = "kill_app_longpress_back";

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
        if (needsNavbar || !hwkeysSupported) {
            prefScreen.removePreference(longPressBackToKill);
        }
    }
}
