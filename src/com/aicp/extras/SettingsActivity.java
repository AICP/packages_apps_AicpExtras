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

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import com.aicp.extras.fragments.Dashboard;
import com.aicp.extras.preference.MasterSwitchPreference;

public class SettingsActivity extends BaseActivity {

    private Fragment mFragment;

    // String extra containing the fragment class
    private static final String EXTRA_FRAGMENT_CLASS =
            "com.aicp.extras.extra.preference_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String fragmentClass = getIntent().getStringExtra(EXTRA_FRAGMENT_CLASS);
        mFragment = getNewFragment(fragmentClass);
        getFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (mFragment instanceof android.preference.PreferenceFragment) {
                android.preference.PreferenceScreen preferenceScreen =
                        ((android.preference.PreferenceFragment) mFragment).getPreferenceScreen();
                if (preferenceScreen != null) {
                    actionBar.setTitle(preferenceScreen.getTitle());
                }
            } else if (mFragment instanceof android.support.v14.preference.PreferenceFragment) {
                android.support.v7.preference.PreferenceScreen preferenceScreen =
                        ((android.support.v14.preference.PreferenceFragment) mFragment)
                                .getPreferenceScreen();
                if (preferenceScreen != null) {
                    actionBar.setTitle(preferenceScreen.getTitle());
                }
            }
        }
    }

    public boolean onPreferenceClick(android.preference.Preference preference) {
        if (preference instanceof android.preference.PreferenceScreen) {
            String fragmentClass = preference.getFragment();
            if (fragmentClass != null) {
                startActivity(new Intent(this, SubSettingsActivity.class)
                        .putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass));
                return true;
            }
        }
        return false;
    }

    public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
        if (preference instanceof android.support.v7.preference.PreferenceScreen
                || preference instanceof MasterSwitchPreference) {
            String fragmentClass = preference.getFragment();
            if (fragmentClass != null) {
                startActivity(new Intent(this, SubSettingsActivity.class)
                        .putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass));
                return true;
            }
        }
        return false;
    }

    private Fragment getNewFragment(String fragmentClass) {
        if (fragmentClass != null) {
            try {
                return (Fragment) Class.forName(fragmentClass).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                    ClassCastException e) {
                e.printStackTrace();
            }
        }
        return getDefaultFragment();
    }

    protected Fragment getDefaultFragment() {
        return new Dashboard();
    }
}
