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
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;
import com.aicp.gear.preference.SystemSettingListPreference;
import com.aicp.gear.preference.SystemSettingSeekBarPreference;
import com.aicp.gear.util.AicpContextConstants;

public class Lockscreen extends BaseSettingsFragment
        implements OnPreferenceChangeListener {

    private static final String FP_SUCCESS_VIBRATION = "fingerprint_success_vib";
    private static final String LOCKSCREEN_MEDIA_BLUR = "lockscreen_media_blur";
    private static final String LS_ALBUM_ART_FILTER = "lockscreen_album_art_filter";
    //private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";

    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
    private SystemSettingSeekBarPreference mLockscreenMediaBlur;
    private SystemSettingListPreference mLsAlbumArtFilter;

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
            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
        } catch (Exception e) {
            //ignore
        }
        // Fingerprint vibration
        mFingerprintVib = (SwitchPreference) prefSet.findPreference(FP_SUCCESS_VIBRATION);
        if (mFingerprintManager == null || !mFingerprintManager.isHardwareDetected()){
            mFingerprintVib.getParent().removePreference(mFingerprintVib);
        }

        mLockscreenMediaBlur = (SystemSettingSeekBarPreference) prefSet.findPreference(LOCKSCREEN_MEDIA_BLUR);
        mLockscreenMediaBlur.setOnPreferenceChangeListener(this);

        mLsAlbumArtFilter = (SystemSettingListPreference) prefSet.findPreference(LS_ALBUM_ART_FILTER);
        mLsAlbumArtFilter.setOnPreferenceChangeListener(this);

        // FOD category
/*        PreferenceCategory fodIconPickerCategory = (PreferenceCategory) findPreference(FOD_ICON_PICKER_CATEGORY);
        PackageManager packageManager = getContext().getPackageManager();
        boolean supportsFod = packageManager.hasSystemFeature(AicpContextConstants.Features.FOD);

        if (fodIconPickerCategory != null && !supportsFod) {
            fodIconPickerCategory.getParent().removePreference(fodIconPickerCategory);
        }*/
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLsAlbumArtFilter) {
            Boolean isBlurValue = (Integer) mLsAlbumArtFilter.findIndexOfValue((String) newValue) > 2;
            mLockscreenMediaBlur.setEnabled(isBlurValue);
            Util.showSystemUiRestartDialog(getActivity());
            return true;
        } else if (preference == mLockscreenMediaBlur) {
            // Util.showSystemUiRestartDialog(getActivity());
            return true;
        }
        return false;
    }
}
