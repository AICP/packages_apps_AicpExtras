/*
 * Copyright (C) 2017-2018 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aicp.extras.preference;

import android.content.ContentResolver;
import android.support.v7.preference.PreferenceDataStore;
import android.widget.Switch;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.widget.SwitchBar;

public abstract class PreferenceDataStoreSwitchBarController
        implements SwitchBar.OnSwitchChangeListener {
    // Why abstract?
    // -> need to add appropriate setting to depHndl in constructor!

    private SwitchBar mSwitchBar;
    private String mKey;
    private PreferenceDataStore mPreferenceDataStore;
    private BaseSettingsFragment mSettingsFragment;
    private MasterSwitchPreferenceDependencyHandler mDependencyHandler;
    private boolean mThereShouldBeOne;

    public PreferenceDataStoreSwitchBarController(PreferenceDataStore preferenceDataStore,
                                            SwitchBar switchBar, String key, boolean defaultValue,
                                            BaseSettingsFragment settingsFragment,
                                            MasterSwitchPreferenceDependencyHandler depHndl,
                                            boolean thereShouldBeOne) {
        mKey = key;
        mPreferenceDataStore = preferenceDataStore;
        mSettingsFragment = settingsFragment;
        mSwitchBar = switchBar;
        mSwitchBar.addOnSwitchChangeListener(this);
        mDependencyHandler = depHndl;
        mThereShouldBeOne = thereShouldBeOne;

        // Init
        boolean initialValue = mPreferenceDataStore.getBoolean(mKey, defaultValue);
        mSwitchBar.setChecked(initialValue);
        if (mSettingsFragment != null) {
            mSettingsFragment.setMasterDependencyState(initialValue);
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        if (isChecked) {
            mDependencyHandler.onEnablePref(-1, mKey);
        } else if (mThereShouldBeOne && !mDependencyHandler.isAnotherEnabled(-1, mKey)) {
            mSwitchBar.setChecked(true);
            // TODO dialog and stuff
            return;
        }
        mPreferenceDataStore.putBoolean(mKey, isChecked);
        if (mSettingsFragment != null) {
            mSettingsFragment.setMasterDependencyState(isChecked);
        }
    }

}
