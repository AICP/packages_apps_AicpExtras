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
import android.provider.Settings;

import com.aicp.extras.utils.PackageListAdapter;
import com.aicp.extras.R;

import java.util.HashMap;

public class AppCircleBarAppChooser extends ChooserActivityBlacklist {

    private static final String KEY_SIDEBAR_APPLIST = "add_appcirclebar_packages";

    @Override
    protected int getPreferenceResource() {
        return R.xml.circlebar_appchooser;
    }

    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeAllPreferences();
    }

    protected void initializeAllPreferences() {
        mBlacklistPackageStore = Settings.System.WHITELIST_APP_CIRCLE_BAR;
        mPackageManager = getActivity().getPackageManager();
        mPackageAdapter = new PackageListAdapter(getActivity());
        mBlacklistPrefList = getPreferenceScreen();
        mBlacklistPrefList.setOrderingAsAdded(false);
        mBlacklistPackages = new HashMap<String, Package>();
        mAddBlacklistPref = findPreference(KEY_SIDEBAR_APPLIST);
        mAddBlacklistPref.setOnPreferenceClickListener(this);
    }
    */
}
