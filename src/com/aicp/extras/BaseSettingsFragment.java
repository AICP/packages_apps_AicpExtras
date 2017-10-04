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


package com.aicp.extras;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v14.preference.PreferenceFragment;

import com.aicp.extras.utils.Util;

public abstract class BaseSettingsFragment extends PreferenceFragment {

    private static final String TAG = BaseSettingsFragment.class.getSimpleName();

    protected abstract int getPreferenceResource();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(getPreferenceResource());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return Util.onPreferenceTreeClick(this, preference)
                || super.onPreferenceTreeClick(preference);
    }
}
