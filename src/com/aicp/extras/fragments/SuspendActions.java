/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserManager;
import androidx.preference.Preference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.SystemSettingMasterSwitchPreference;
import com.aicp.gear.preference.SystemSettingSwitchPreference;

public class SuspendActions extends BaseSettingsFragment
           implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SuspendActions";

    private static final String SCREEN_STATE_TOGGLES_GPS = "screen_state_gps";
    private static final String SCREEN_STATE_TOGGLES_MOBILE_DATA = "screen_state_mobile_data";
    private static final String SCREEN_STATE_TOGGLES_TWOG = "screen_state_twog";
    private static final String SCREEN_STATE_TOGGLES_THREEG = "screen_state_threeg";

    private Context mContext;

    private SystemSettingMasterSwitchPreference mEnableScreenStateTogglesTwoG;
    private SystemSettingMasterSwitchPreference mEnableScreenStateTogglesThreeG;
    private SystemSettingMasterSwitchPreference mEnableScreenStateTogglesMobileData;
    private SystemSettingSwitchPreference mEnableScreenStateTogglesGps;

    @Override
    protected int getPreferenceResource() {
        return R.xml.suspend_actions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();

        mEnableScreenStateTogglesTwoG = (SystemSettingMasterSwitchPreference) findPreference(
                SCREEN_STATE_TOGGLES_TWOG);
        mEnableScreenStateTogglesTwoG.setOnPreferenceChangeListener(this);
        mEnableScreenStateTogglesThreeG = (SystemSettingMasterSwitchPreference) findPreference(
                SCREEN_STATE_TOGGLES_THREEG);
        mEnableScreenStateTogglesThreeG.setOnPreferenceChangeListener(this);
        mEnableScreenStateTogglesMobileData = (SystemSettingMasterSwitchPreference) findPreference(
                SCREEN_STATE_TOGGLES_MOBILE_DATA);
        mEnableScreenStateTogglesMobileData.setOnPreferenceChangeListener(this);
        mEnableScreenStateTogglesGps = (SystemSettingSwitchPreference) findPreference(
                SCREEN_STATE_TOGGLES_GPS);
        mEnableScreenStateTogglesGps.setOnPreferenceChangeListener(this);

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (!cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
            mEnableScreenStateTogglesTwoG.getParent().removePreference(mEnableScreenStateTogglesTwoG);
            mEnableScreenStateTogglesThreeG.getParent().removePreference(mEnableScreenStateTogglesThreeG);
            mEnableScreenStateTogglesMobileData.getParent().removePreference(mEnableScreenStateTogglesMobileData);
        }

        // Only enable these controls if this user is allowed to change location
        // sharing settings.
        final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        boolean isLocationChangeAllowed = !um.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION);

        // TODO: check if gps is available on this device?
        if (!isLocationChangeAllowed) {
            mEnableScreenStateTogglesGps.getParent().removePreference(mEnableScreenStateTogglesGps);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
        mContext.sendBroadcast(intent);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
