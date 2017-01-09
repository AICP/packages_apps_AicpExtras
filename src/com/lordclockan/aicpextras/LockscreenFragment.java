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
		private ColorPickerPreference mLockscreenOwnerInfoColorPicker;
        private ColorPickerPreference mLockscreenAlarmColorPicker;
        private ColorPickerPreference mLockscreenClockColorPicker;
        private ColorPickerPreference mLockscreenClockDateColorPicker;
        private Preference mLockscreenColorsReset;

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
            if (DateFormat.is24HourFormat(getActivity())) {
                mMiscCategory.removePreference(mHideAmPm);
            } else {
                mHideAmPm.setOnPreferenceChangeListener(this);
            }

            if (mMiscCategory.getPreferenceCount() == 0) {
                prefSet.removePreference(mMiscCategory);
            }
			
			// LockscreenColors
			
            int intColor;
            String hexColor;

            mLockscreenOwnerInfoColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_OWNER_INFO_COLOR);
            mLockscreenOwnerInfoColorPicker.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, DEFAULT);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mLockscreenOwnerInfoColorPicker.setSummary(hexColor);
            mLockscreenOwnerInfoColorPicker.setNewPreviewColor(intColor);

            mLockscreenAlarmColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_ALARM_COLOR);
            mLockscreenAlarmColorPicker.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.LOCKSCREEN_ALARM_COLOR, DEFAULT);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mLockscreenAlarmColorPicker.setSummary(hexColor);
            mLockscreenAlarmColorPicker.setNewPreviewColor(intColor);

            mLockscreenClockColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_CLOCK_COLOR);
            mLockscreenClockColorPicker.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.LOCKSCREEN_CLOCK_COLOR, DEFAULT);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mLockscreenClockColorPicker.setSummary(hexColor);
            mLockscreenClockColorPicker.setNewPreviewColor(intColor);

            mLockscreenClockDateColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_CLOCK_DATE_COLOR);
            mLockscreenClockDateColorPicker.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                        Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, DEFAULT);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mLockscreenClockDateColorPicker.setSummary(hexColor);
            mLockscreenClockDateColorPicker.setNewPreviewColor(intColor);

            mLockscreenColorsReset = (Preference) findPreference(LOCKSCREEN_COLORS_RESET);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mHideAmPm) {
                Helpers.showSystemUIrestartDialog(getActivity());
                return true;
            } else if (preference == mLockscreenOwnerInfoColorPicker) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, intHex);
                return true;
            } else if (preference == mLockscreenAlarmColorPicker) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.LOCKSCREEN_ALARM_COLOR, intHex);
                return true;
            } else if (preference == mLockscreenClockColorPicker) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.LOCKSCREEN_CLOCK_COLOR, intHex);
                return true;
            } else if (preference == mLockscreenClockDateColorPicker) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, intHex);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mLockscreenColorsReset) {
                resetToDefault();
                return true;
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
        }

        private void resetToDefault() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            alertDialog.setTitle(R.string.lockscreen_colors_reset_title);
            alertDialog.setMessage(R.string.lockscreen_colors_reset_message);
            alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    resetValues();
                }
            });
            alertDialog.setNegativeButton(R.string.cancel, null);
            alertDialog.create().show();
        }

        private void resetValues() {
            ContentResolver resolver = getActivity().getContentResolver();
            Settings.System.putInt(resolver,
                    Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, DEFAULT);
            mLockscreenOwnerInfoColorPicker.setNewPreviewColor(DEFAULT);
            mLockscreenOwnerInfoColorPicker.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.LOCKSCREEN_ALARM_COLOR, DEFAULT);
            mLockscreenAlarmColorPicker.setNewPreviewColor(DEFAULT);
            mLockscreenAlarmColorPicker.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.LOCKSCREEN_CLOCK_COLOR, DEFAULT);
            mLockscreenClockColorPicker.setNewPreviewColor(DEFAULT);
            mLockscreenClockColorPicker.setSummary(R.string.default_string);
            Settings.System.putInt(resolver,
                    Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, DEFAULT);
            mLockscreenClockDateColorPicker.setNewPreviewColor(DEFAULT);
            mLockscreenClockDateColorPicker.setSummary(R.string.default_string);
        }
    }
}
