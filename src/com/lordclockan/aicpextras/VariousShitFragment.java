package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.View;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;

public class VariousShitFragment extends Fragment {

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

        private static final String KEY_CAMERA_SOUNDS = "camera_sounds";
        private static final String PROP_CAMERA_SOUND = "persist.sys.camera-sound";
        private static final String SCREENSHOT_TYPE = "screenshot_type";
        private static final String USE_ROUND_ICONS = "config_use_round_icon";

        private SwitchPreference mCameraSounds;
        private ListPreference mScreenshotType;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.various_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mCameraSounds = (SwitchPreference) findPreference(KEY_CAMERA_SOUNDS);
            mCameraSounds.setChecked(SystemProperties.getBoolean(PROP_CAMERA_SOUND, true));
            mCameraSounds.setOnPreferenceChangeListener(this);

            mScreenshotType = (ListPreference) findPreference(SCREENSHOT_TYPE);
            int mScreenshotTypeValue = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, 0);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            mScreenshotType.setSummary(mScreenshotType.getEntry());
            mScreenshotType.setOnPreferenceChangeListener(this);

            // Set default value for round icons
            SwitchPreference roundIconsPref = (SwitchPreference) findPreference(USE_ROUND_ICONS);
            roundIconsPref.setChecked(0 !=
                Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.CONFIG_USE_ROUND_ICON,
                        Resources.getSystem().getBoolean(
                                com.android.internal.R.bool.config_useRoundIcon) ? 1 : 0)
            );
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mCameraSounds) {
               if (KEY_CAMERA_SOUNDS.equals(key)) {
                   if ((Boolean) newValue) {
                       SystemProperties.set(PROP_CAMERA_SOUND, "1");
                   } else {
                       SystemProperties.set(PROP_CAMERA_SOUND, "0");
                   }
                }
                return true;
            } else if (preference == mScreenshotType) {
                int mScreenshotTypeValue = Integer.parseInt(((String) newValue).toString());
                mScreenshotType.setSummary(
                        mScreenshotType.getEntries()[mScreenshotTypeValue]);
                Settings.System.putInt(resolver,
                        Settings.System.SCREENSHOT_TYPE, mScreenshotTypeValue);
                mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
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
