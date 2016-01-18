package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
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
import com.lordclockan.aicpextras.utils.Utils;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

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
        private static final String KEYGUARD_TOGGLE_TORCH = "keyguard_toggle_torch";

        private SeekBarPreferenceCham mBlurRadius;
        private Preference mLockscreenWeather;
        private Preference mSetWallpaper;
        private Preference mClearWallpaper;
        private ListPreference mLockClockFonts;
        private SwitchPreference mKeyguardTorch;

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

            mLockscreenWeather = prefSet.findPreference(PREF_LOCKSCREEN_WEATHER);

            mSetWallpaper = (Preference) findPreference(KEY_WALLPAPER_SET);
            mClearWallpaper = (Preference) findPreference(KEY_WALLPAPER_CLEAR);

            mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
            mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.LOCK_CLOCK_FONTS, 0)));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            mLockClockFonts.setOnPreferenceChangeListener(this);

            mKeyguardTorch = (SwitchPreference) findPreference(KEYGUARD_TOGGLE_TORCH);
            if (!Utils.isWifiOnly(getActivity())) {
                prefSet.removePreference(mKeyguardTorch);
            } else {
                mKeyguardTorch.setChecked((Settings.System.getInt(resolver,
                        Settings.System.KEYGUARD_TOGGLE_TORCH, 0) == 1));
            }
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
            } else if (preference == mKeyguardTorch) {
                Settings.System.putInt(resolver,
                        Settings.System.KEYGUARD_TOGGLE_TORCH,
                        (Boolean) newValue ? 1 : 0);
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
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        private void toast(String text) {
            Toast toast = Toast.makeText(getView().getContext(), text,
                    Toast.LENGTH_SHORT);
            toast.show();
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
    }
}
