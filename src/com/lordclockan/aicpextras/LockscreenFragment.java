package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.android.internal.util.aicp.AicpUtils;
import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockscreenFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {
        }

        private static final String PREF_KEYGUARD_TORCH = "keyguard_toggle_torch";
        private static final String PREF_LOCK_SCREEN_HIDE_AMPM = "lock_screen_hide_ampm";

        private SwitchPreference mKeyguardTorch;
        private FingerprintManager mFingerprintManager;
        private SwitchPreference mFingerprintVib;
        private SwitchPreference mHideAmPm;
        private PreferenceCategory mMiscCategory;

        static final int DEFAULT = 0xffffffff;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.lockscreen_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mMiscCategory = (PreferenceCategory) prefSet.findPreference("lockscreen_misc_category");

            // Keyguard Torch
            mKeyguardTorch = (SwitchPreference) findPreference(PREF_KEYGUARD_TORCH);
            if (!AicpUtils.deviceSupportsFlashLight(getActivity())) {
                mMiscCategory.removePreference(mKeyguardTorch);
            }

            // Fingerprint vibration
            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            mFingerprintVib = (SwitchPreference) findPreference("fingerprint_success_vib");
            if (!mFingerprintManager.isHardwareDetected()) {
                mMiscCategory.removePreference(mFingerprintVib);
            }

            // Hide AM/PM
            mHideAmPm = (SwitchPreference) findPreference(PREF_LOCK_SCREEN_HIDE_AMPM);
            mHideAmPm.setOnPreferenceChangeListener(this);
            if (DateFormat.is24HourFormat(getActivity())) {
                mMiscCategory.removePreference(mHideAmPm);
            } else {
                mHideAmPm.setChecked((Settings.System.getInt(resolver,
                        Settings.System.LOCK_SCREEN_HIDE_AMPM, 0) == 1));
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if  (preference == mHideAmPm) {
                boolean checked = ((SwitchPreference) preference).isChecked();
                Settings.System.putInt(resolver,
                        Settings.System.LOCK_SCREEN_HIDE_AMPM, checked ? 1:0);
                Helpers.showSystemUIrestartDialog(getActivity());
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
