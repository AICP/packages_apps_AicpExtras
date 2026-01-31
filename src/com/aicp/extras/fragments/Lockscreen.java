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

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

//import com.android.internal.util.aicp.AicpUtils;
import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;
import com.aicp.gear.util.AicpContextConstants;

public class Lockscreen extends BaseSettingsFragment {
/*
    private static final String FP_SUCCESS_VIBRATION = "fingerprint_success_vib";
    private static final String KEY_AOD_SCHEDULE = "always_on_display_schedule";
    private static final String FOD_ICON_PICKER_CATEGORY = "fod_icon_picker";
    private static final String KEY_LOCKSCREEN_BLUR = "lockscreen_blur";
*/
    private static final String KEY_CUSTOM_CARRIER_TEXT = "lockscreen_show_custom_carrier_text";
    private Preference mCustomCarrierTextPref;
    private String mCustomCarrierText;
    /*
    private FingerprintManager mFingerprintManager;
    private SwitchPreference mFingerprintVib;
*/
    @Override
    protected int getPreferenceResource() {
        return R.xml.lockscreen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context mContext = getContext();
        PreferenceScreen prefSet = getPreferenceScreen();
/*        ContentResolver resolver = getActivity().getContentResolver();
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
*/
        mCustomCarrierTextPref = (Preference) findPreference(KEY_CUSTOM_CARRIER_TEXT);
        updateCustomCarrierTextSummary();

    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (KEY_CUSTOM_CARRIER_TEXT.equals(preference.getKey())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            LinearLayout container = new LinearLayout(getActivity());
            container.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(55, 20, 55, 20);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomCarrierText) ? "" : mCustomCarrierText);
            input.setSelection(input.getText().length());
            input.setLayoutParams(lp);
            input.setGravity(android.view.Gravity.TOP| Gravity.START);
            container.addView(input);
            alert.setView(container);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putStringForUser(resolver, Settings.System.LOCKSCREEN_SHOW_CUSTOM_CARRIER_TEXT, value, UserHandle.USER_CURRENT);
                            updateCustomCarrierTextSummary();
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void updateCustomCarrierTextSummary() {
        mCustomCarrierText = Settings.System.getStringForUser(
                getActivity().getContentResolver(), Settings.System.LOCKSCREEN_SHOW_CUSTOM_CARRIER_TEXT, UserHandle.USER_CURRENT);

        if (TextUtils.isEmpty(mCustomCarrierText)) {
            mCustomCarrierTextPref.setSummary(R.string.carrier_text_default);
        } else {
            mCustomCarrierTextPref.setSummary(mCustomCarrierText);
        }
    }
}
