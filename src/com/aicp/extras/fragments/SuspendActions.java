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

import static android.telephony.TelephonyManager.NETWORK_CLASS_2_G;
import static android.telephony.TelephonyManager.NETWORK_CLASS_3_G;
import static android.telephony.TelephonyManager.NETWORK_CLASS_UNKNOWN;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SeekBarPreferenceCham;
import com.aicp.extras.preference.SystemSettingMasterSwitchPreference;

public class SuspendActions extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "SuspendActions";

    private static final String SCREEN_STATE_TOOGLES_TWOG = "screen_state_twog";
    private static final String SCREEN_STATE_TOOGLES_THREEG = "screen_state_threeg";
    private static final String SCREEN_STATE_TOOGLES_GPS = "screen_state_gps";
    private static final String SCREEN_STATE_TOOGLES_MOBILE_DATA = "screen_state_mobile_data";
    private static final String SCREEN_STATE_CATEGORY_LOCATION = "screen_state_toggles_location_key";
    private static final String SCREEN_STATE_CATEGORY_MOBILE_DATA = "screen_state_toggles_mobile_key";

    private Context mContext;

    private SystemSettingMasterSwitchPreference mScreenStateToggleTwoG;
    private SystemSettingMasterSwitchPreference mScreenStateToggleThreeG;
    private SwitchPreference mScreenStateToggleGps;
    private SystemSettingMasterSwitchPreference mScreenStateToggleMobileData;
    private SeekBarPreferenceCham mMinutesOffDelay;
    private SeekBarPreferenceCham mMinutesOnDelay;
    private PreferenceCategory mMobileDataCategory;
    private PreferenceCategory mLocationCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.suspend_actions;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mContext = (Context) getActivity();

/*
        mEnableScreenStateToggles = (SwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_ENABLE);
        mEnableScreenStateToggles.setOnPreferenceChangeListener(this);

        mMinutesOffDelay = (SeekBarPreferenceCham) findPreference(SCREEN_STATE_OFF_DELAY);
        int offd = Settings.System.getInt(resolver,
                Settings.System.SCREEN_STATE_OFF_DELAY, 0);
        mMinutesOffDelay.setValue(offd / 60);
        mMinutesOffDelay.setOnPreferenceChangeListener(this);

        mMinutesOnDelay = (SeekBarPreferenceCham) findPreference(SCREEN_STATE_ON_DELAY);
        int ond = Settings.System.getInt(resolver,
                Settings.System.SCREEN_STATE_ON_DELAY, 0);
        mMinutesOnDelay.setValue(ond / 60);
        mMinutesOnDelay.setOnPreferenceChangeListener(this);
*/
        mMobileDataCategory = (PreferenceCategory) findPreference(
                SCREEN_STATE_CATEGORY_MOBILE_DATA);
        mLocationCategory = (PreferenceCategory) findPreference(
                SCREEN_STATE_CATEGORY_LOCATION);

        mScreenStateToggleTwoG = (SystemSettingMasterSwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_TWOG);
        mScreenStateToggleThreeG = (SystemSettingMasterSwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_THREEG);
        mScreenStateToggleMobileData = (SystemSettingMasterSwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_MOBILE_DATA);

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

        if (!cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) || !isMobileDataEnabled()){
            mMobileDataCategory.getParent().removePreference(mMobileDataCategory);
        } else {
            mScreenStateToggleTwoG.setOnPreferenceChangeListener(this);
            mScreenStateToggleMobileData.setOnPreferenceChangeListener(this);
            switch (tm.getNetworkClass(tm.getNetworkType())) {
              case NETWORK_CLASS_2_G:
              case NETWORK_CLASS_3_G:
              case NETWORK_CLASS_UNKNOWN:
                  mScreenStateToggleThreeG.getParent().removePreference(mScreenStateToggleThreeG);
              default:
                  mScreenStateToggleThreeG.setOnPreferenceChangeListener(this);
            }
        }

        // Only enable these controls if this user is allowed to change location
        // sharing settings.
        final UserManager um = (UserManager) getActivity().getSystemService(Context.USER_SERVICE);
        boolean isLocationChangeAllowed = !um.hasUserRestriction(UserManager.DISALLOW_SHARE_LOCATION);

        // TODO: check if gps is available on this device?
        mScreenStateToggleGps = (SwitchPreference) findPreference(
                SCREEN_STATE_TOOGLES_GPS);

        if (!isLocationChangeAllowed){
            mLocationCategory.getParent().removePreference(mLocationCategory);
        } else {
            mScreenStateToggleGps.setOnPreferenceChangeListener(this);
        }
    }

    private boolean isMobileDataEnabled(){
            TelephonyManager telephonyService = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyService.getDataEnabled();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

        if (preference == mScreenStateToggleTwoG ||
                preference == mScreenStateToggleThreeG ||
                preference == mScreenStateToggleMobileData ||
                preference == mScreenStateToggleGps) {
            Intent intent = new Intent("android.intent.action.SCREEN_STATE_SERVICE_UPDATE");
            mContext.sendBroadcast(intent);
            return true;
/*
        } else if (preference == mMinutesOffDelay) {
            int delay = ((Integer) newValue) * 60;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_STATE_OFF_DELAY, delay);

            return true;
        } else if (preference == mMinutesOnDelay) {
            int delay = ((Integer) newValue) * 60;
            Settings.System.putInt(resolver,
                    Settings.System.SCREEN_STATE_ON_DELAY, delay);

            return true;
            */
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void restartService(){
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.screenstate.ScreenStateService");
        getActivity().stopService(service);
        getActivity().startService(service);
    }
}
