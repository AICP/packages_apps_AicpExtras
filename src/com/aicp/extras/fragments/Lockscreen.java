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
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

public class Lockscreen extends BaseSettingsFragment {

    private static final String FP_SUCCESS_VIBRATION = "fingerprint_success_vib";
    private static final String FP_UNLOCK_KEYSTORE = "fp_unlock_keystore";
    private static final String FP_WAKE_UNLOCK = "fp_wake_and_unlock";

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SwitchPreference mFpKeystore;
    private SwitchPreference mFpWakeAndUnlock;

    @Override
    protected int getPreferenceResource() {
        return R.xml.lockscreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        // Fingerprint vibration
        mFingerprintVib = (SwitchPreference) prefSet.findPreference(FP_SUCCESS_VIBRATION);
        // Fingerprint unlock keystore
        mFpKeystore = (SwitchPreference) prefSet.findPreference(FP_UNLOCK_KEYSTORE);
        // Fingerprint wake and unlock
        mFpWakeAndUnlock = (SwitchPreference) prefSet.findPreference(FP_WAKE_UNLOCK);
        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()){
            mFingerprintVib.getParent().removePreference(mFingerprintVib);
            mFpKeystore.getParent().removePreference(mFpKeystore);
            mFpWakeAndUnlock.getParent().removePreference(mFpWakeAndUnlock);
        }

        boolean configWakeAndUnlockEnabled = getActivity().getResources().getBoolean(
                com.android.internal.R.bool.config_fingerprintWakeAndUnlock);
        if (mFpWakeAndUnlock != null && configWakeAndUnlockEnabled) {
            mFpWakeAndUnlock.getParent().removePreference(mFpWakeAndUnlock);
        }
    }
}
