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

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.aicp.gear.preference.SystemSettingIntListPreference;
import com.aicp.gear.preference.SystemSettingSeekBarPreference;

public class Lockscreen extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String FP_SUCCESS_VIBRATION = "fingerprint_success_vib";
    private static final String PREF_LS_CLOCK_SELECTION = "lockscreen_clock_selection";
    private static final String PREF_LS_CLOCK_FONTSIZE = "lockclock_font_size";

    private static final int CLOCK_MAXSIZE = 108;
    private static final int TEXT_CLOCK_MAXSIZE = 64;

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SystemSettingIntListPreference mLockClockStylePref;
    private SystemSettingSeekBarPreference mLockClockFontPref;

    @Override
    protected int getPreferenceResource() {
        return R.xml.lockscreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        try {
            mFingerprintManager = (FingerprintManager) getActivity().
                                getSystemService(Context.FINGERPRINT_SERVICE);
        } catch (Exception e) {
            //ignore
        }
        // Fingerprint vibration
        mFingerprintVib = (SwitchPreference) prefSet.findPreference(FP_SUCCESS_VIBRATION);

        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()){
            mFingerprintVib.getParent().removePreference(mFingerprintVib);
        }

        // Lockscreen clock font size
        mLockClockFontPref = (SystemSettingSeekBarPreference)
                              prefSet.findPreference(PREF_LS_CLOCK_FONTSIZE);

        // Lockscreen clock selection
        mLockClockStylePref = (SystemSettingIntListPreference)
                              prefSet.findPreference(PREF_LS_CLOCK_SELECTION);
        int lockClockStyle = Settings.System.getIntForUser(resolver,
                Settings.System.LOCKSCREEN_CLOCK_SELECTION, 0, UserHandle.USER_CURRENT);
        updateFontSizeMax(lockClockStyle);
        mLockClockStylePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLockClockStylePref) {
            int value = Integer.parseInt((String) newValue);
            updateFontSizeMax(value);
            return true;
        }
        return false;
    }

    private void updateFontSizeMax(int clockStyle){
        if (clockStyle == 14) {
            mLockClockFontPref.setMax(TEXT_CLOCK_MAXSIZE);
        } else {
            mLockClockFontPref.setMax(CLOCK_MAXSIZE);
        }
    }
}
