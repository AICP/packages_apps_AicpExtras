/*
 * Copyright (C) 2010 The Android Open Source Project
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
import androidx.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;

import com.aicp.extras.R;
import com.aicp.extras.BaseSettingsFragment;

public class AppCircleBar extends BaseSettingsFragment implements
        OnPreferenceClickListener {
    private static final String TAG = "AppCircleSidebar";

    @Override
    protected int getPreferenceResource() {
        return R.xml.app_circlebar;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public boolean onPreferenceClick(Preference preference) {
      return true;
    }
}
