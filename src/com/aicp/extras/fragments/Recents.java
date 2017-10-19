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


package com.aicp.extras.fragments;

import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.os.Bundle;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.MasterSwitchPreference;

public class Recents extends BaseSettingsFragment {

    private static final String PREF_STOCK_RECENTS_CATEGORY = "stock_recents_category";
    private static final String PREF_ALTERNATIVE_RECENTS_CATEGORY = "alternative_recents_category";

    private PreferenceCategory mStockRecentsCategory;
    private PreferenceCategory mAlternativeRecentsCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.recents;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mStockRecentsCategory = (PreferenceCategory) findPreference(PREF_STOCK_RECENTS_CATEGORY);
        mAlternativeRecentsCategory =
                (PreferenceCategory) findPreference(PREF_ALTERNATIVE_RECENTS_CATEGORY);

        // Alternative recents en-/disabling
        Preference.OnPreferenceChangeListener alternativeRecentsChangeListener =
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateDependencies((Boolean) newValue ? preference : null);
                return true;
            }
        };
        for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
            Preference preference = mAlternativeRecentsCategory.getPreference(i);
            if (preference instanceof MasterSwitchPreference) {
                preference.setOnPreferenceChangeListener(alternativeRecentsChangeListener);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
            Preference preference = mAlternativeRecentsCategory.getPreference(i);
            if (preference instanceof MasterSwitchPreference) {
                ((MasterSwitchPreference) preference).reloadValue();
            }
        }
        updateDependencies(null);
    }

    private void updateDependencies(Preference enabledAlternativeRecentsPreference) {
        boolean alternativeRecentsEnabled = false;
        for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
            Preference preference = mAlternativeRecentsCategory.getPreference(i);
            if (enabledAlternativeRecentsPreference != null
                    && enabledAlternativeRecentsPreference != preference
                    && preference instanceof MasterSwitchPreference
                    && ((MasterSwitchPreference) preference).isChecked()) {
                // Only one alternative recents at the time!
                ((MasterSwitchPreference) preference).setCheckedPersisting(false);
            } else if (preference instanceof MasterSwitchPreference
                    && ((MasterSwitchPreference) preference).isChecked()) {
                alternativeRecentsEnabled = true;
            }
        }
        mStockRecentsCategory.setEnabled(!alternativeRecentsEnabled);
    }
}
