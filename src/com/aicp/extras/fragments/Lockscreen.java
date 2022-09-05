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

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

//import com.android.internal.util.aicp.AicpUtils;
import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;
import com.aicp.gear.util.AicpContextConstants;

public class Lockscreen {/* extends BaseSettingsFragment {
/*
    private static final String FP_SUCCESS_VIBRATION = "fingerprint_success_vib";
    private static final String KEY_AOD_SCHEDULE = "always_on_display_schedule";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";
    private static final String KEY_LOCKSCREEN_BLUR = "lockscreen_blur";

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
*/
/*     @Override
    protected int getPreferenceResource() {
        return R.xml.lockscreen;
    } */
/*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context mContext = getContext();
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        WallpaperManager manager = WallpaperManager.getInstance(mContext);

        try {
            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        } catch (Exception e) {
            //ignore
        }
        // Fingerprint vibration
        mFingerprintVib = (SwitchPreference) prefSet.findPreference(FP_SUCCESS_VIBRATION);
        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()){
            mFingerprintVib.getParent().removePreference(mFingerprintVib);
        }

        Util.requireConfig(getActivity(), findPreference(KEY_AOD_SCHEDULE),
                com.android.internal.R.bool.config_dozeAlwaysOnDisplayAvailable, true, false);

        // Lockscreen blur
        Preference lockscreenBlur = (Preference) findPreference(KEY_LOCKSCREEN_BLUR);
        ParcelFileDescriptor pfd = manager.getWallpaperFile(WallpaperManager.FLAG_LOCK);
        if (!AicpUtils.supportsBlur() || pfd != null) {
            lockscreenBlur.setEnabled(false);
            lockscreenBlur.setSummary(getResources().getString(R.string.lockscreen_blur_disabled));
        }

        // FOD category
        PreferenceCategory fodIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        PackageManager packageManager = getContext().getPackageManager();
        boolean supportsFod = packageManager.hasSystemFeature(AicpContextConstants.Features.FOD);

        if (fodIconPickerCategory != null && !supportsFod) {
            fodIconPickerCategory.getParent().removePreference(fodIconPickerCategory);
        }

    }*/
}
