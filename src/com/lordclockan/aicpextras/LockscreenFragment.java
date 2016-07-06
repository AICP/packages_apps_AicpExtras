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
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.lordclockan.R;
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

        public static final int IMAGE_PICK = 1;
        public static final int SET_KEYGUARD_WALLPAPER = 2;

        private static final String KEY_LOCKSCREEN_BLUR_RADIUS = "lockscreen_blur_radius";
        private static final String PREF_LOCKSCREEN_WEATHER = "lockscreen_weather";
        private static final String KEY_WALLPAPER_SET = "lockscreen_wallpaper_set";
        private static final String KEY_WALLPAPER_CLEAR = "lockscreen_wallpaper_clear";
        private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
        private static final String LOCKSCREEN_OWNER_INFO_COLOR = "lockscreen_owner_info_color";
        private static final String LOCKSCREEN_ALARM_COLOR = "lockscreen_alarm_color";
        private static final String LOCKSCREEN_CLOCK_COLOR = "lockscreen_clock_color";
        private static final String LOCKSCREEN_CLOCK_DATE_COLOR = "lockscreen_clock_date_color";
        private static final String LOCKSCREEN_COLORS_RESET = "lockscreen_colors_reset";
        private static final String PREF_LOCKSCREEN_CLOCK_FONT_SIZE = "lockscreen_clock_font_size";
        private static final String PREF_LOCKSCREEN_DATE_FONT_SIZE = "lockscreen_date_font_size";
        private static final String PREF_LOCKSCREEN_ALPHA = "lockscreen_alpha";
        private static final String PREF_LOCKSCREEN_SECURITY_ALPHA = "lockscreen_security_alpha";

        private SeekBarPreferenceCham mBlurRadius;
        private Preference mLockscreenWeather;
        private Preference mSetWallpaper;
        private Preference mClearWallpaper;
        private ListPreference mLockClockFonts;
        private ColorPickerPreference mLockscreenOwnerInfoColorPicker;
        private ColorPickerPreference mLockscreenAlarmColorPicker;
        private ColorPickerPreference mLockscreenClockColorPicker;
        private ColorPickerPreference mLockscreenClockDateColorPicker;
        private Preference mLockscreenColorsReset;
        private SeekBarPreferenceCham mLockClockFontSize;
        private SeekBarPreferenceCham mLockDateFontSize;
        private SeekBarPreferenceCham mLsAlpha;
        private SeekBarPreferenceCham mLsSecurityAlpha;
        private FingerprintManager mFingerprintManager;
        private SwitchPreference mFingerprintVib;

        static final int DEFAULT = 0xffffffff;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.lockscreen_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mBlurRadius = (SeekBarPreferenceCham) findPreference(KEY_LOCKSCREEN_BLUR_RADIUS);
            mBlurRadius.setValue(Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_BLUR_RADIUS, 14));
            mBlurRadius.setOnPreferenceChangeListener(this);
            if(!getResources().getBoolean(com.android.internal.R.bool.config_use_stackblur)) {
                prefSet.removePreference(mBlurRadius);
            }

            mLockscreenWeather = prefSet.findPreference(PREF_LOCKSCREEN_WEATHER);

            mSetWallpaper = (Preference) findPreference(KEY_WALLPAPER_SET);
            mClearWallpaper = (Preference) findPreference(KEY_WALLPAPER_CLEAR);

            mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
            mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.LOCK_CLOCK_FONTS, 4)));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            mLockClockFonts.setOnPreferenceChangeListener(this);

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

            // Lockscreen clock font size
            mLockClockFontSize =
                    (SeekBarPreferenceCham) findPreference(PREF_LOCKSCREEN_CLOCK_FONT_SIZE);
            int lockClockFontSize = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_CLOCK_FONT_SIZE, 88);
            mLockClockFontSize.setValue(lockClockFontSize / 1);
            mLockClockFontSize.setOnPreferenceChangeListener(this);

            // Lockscreen date font size
            mLockDateFontSize =
                    (SeekBarPreferenceCham) findPreference(PREF_LOCKSCREEN_DATE_FONT_SIZE);
            int lockDateFontSize = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_DATE_FONT_SIZE, 14);
            mLockDateFontSize.setValue(lockDateFontSize / 1);
            mLockDateFontSize.setOnPreferenceChangeListener(this);

            // LS alpha
            mLsAlpha =
                    (SeekBarPreferenceCham) findPreference(PREF_LOCKSCREEN_ALPHA);
            float alpha = Settings.System.getFloat(resolver,
                    Settings.System.LOCKSCREEN_ALPHA, 0.45f);
            mLsAlpha.setValue((int)(100 * alpha));
            mLsAlpha.setOnPreferenceChangeListener(this);

            mLsSecurityAlpha =
                    (SeekBarPreferenceCham) findPreference(PREF_LOCKSCREEN_SECURITY_ALPHA);
            float alpha2 = Settings.System.getFloat(resolver,
                    Settings.System.LOCKSCREEN_SECURITY_ALPHA, 0.75f);
            mLsSecurityAlpha.setValue((int)(100 * alpha2));
            mLsSecurityAlpha.setOnPreferenceChangeListener(this);

            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            mFingerprintVib = (SwitchPreference) prefSet.findPreference("fingerprint_success_vib");
            if (!mFingerprintManager.isHardwareDetected()){
                prefSet.removePreference(mFingerprintVib);
            }

            mLockscreenColorsReset = (Preference) findPreference(LOCKSCREEN_COLORS_RESET);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBlurRadius) {
                int width = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.LOCKSCREEN_BLUR_RADIUS, width);
                return true;
            } else if (preference == mLockClockFonts) {
                Settings.System.putInt(resolver, Settings.System.LOCK_CLOCK_FONTS,
                        Integer.valueOf((String) newValue));
                mLockClockFonts.setValue(String.valueOf(newValue));
                mLockClockFonts.setSummary(mLockClockFonts.getEntry());
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
            } else if (preference == mLockClockFontSize) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.LOCKSCREEN_CLOCK_FONT_SIZE, val * 1);
                return true;
            } else if (preference == mLockDateFontSize) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.LOCKSCREEN_DATE_FONT_SIZE, val * 1);
                return true;
            } else if (preference == mLsAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putFloat(resolver,
                        Settings.System.LOCKSCREEN_ALPHA, alpha / 100.0f);
                return true;
            } else if (preference == mLsSecurityAlpha) {
                int alpha2 = (Integer) newValue;
                Settings.System.putFloat(resolver,
                        Settings.System.LOCKSCREEN_SECURITY_ALPHA, alpha2 / 100.0f);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mLockscreenWeather) {
                Intent intent = new Intent(getActivity(), Weather.class);
                getActivity().startActivity(intent);
            } else if (preference == mSetWallpaper) {
                setKeyguardWallpaper();
                return true;
            } else if (preference == mClearWallpaper) {
                clearKeyguardWallpaper();
                Toast.makeText(getView().getContext(), getString(R.string.reset_lockscreen_wallpaper),
                        Toast.LENGTH_LONG).show();
                return true;
            } else if (preference == mLockscreenColorsReset) {
                resetToDefault();
                return true;
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == IMAGE_PICK && resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    Intent intent = new Intent();
                    intent.setClassName("com.android.wallpapercropper",
                            "com.android.wallpapercropper.WallpaperCropActivity");
                    intent.putExtra("keyguardMode", "1");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }

        private void setKeyguardWallpaper() {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_PICK);
        }

        private void clearKeyguardWallpaper() {
            WallpaperManager wallpaperManager = null;
            wallpaperManager = WallpaperManager.getInstance(getActivity());
            wallpaperManager.clearKeyguardWallpaper();
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
