package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.widget.NumberPickerPreference;

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

        private static final int MIN_DELAY_VALUE = 1;
        private static final int MAX_DELAY_VALUE = 30;

        private static final String CONFIG_USE_ROUND_ICON = "config_use_round_icon";
        private static final String KEY_CAMERA_SOUNDS = "camera_sounds";
        private static final String PROP_CAMERA_SOUND = "persist.sys.camera-sound";
        private static final String SCREENSHOT_TYPE = "screenshot_type";
        private static final String SCREENSHOT_DELAY = "screenshot_delay";
        private static final String SCREENSHOT_SUMMARY = "Delay is set to ";

        private SwitchPreference mRoundIcon;
        private SwitchPreference mCameraSounds;
        private ListPreference mScreenshotType;
        private NumberPickerPreference mScreenshotDelay;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.various_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mRoundIcon = (SwitchPreference) findPreference(CONFIG_USE_ROUND_ICON);
            mRoundIcon.setChecked(
                    !new File(android.content.pm.PackageParser.DISABLE_CONFIG_ROUND_ICONS_FILE)
                            .exists()
            );
            mRoundIcon.setOnPreferenceChangeListener(this);

            mCameraSounds = (SwitchPreference) findPreference(KEY_CAMERA_SOUNDS);
            mCameraSounds.setChecked(SystemProperties.getBoolean(PROP_CAMERA_SOUND, true));
            mCameraSounds.setOnPreferenceChangeListener(this);

            mScreenshotType = (ListPreference) findPreference(SCREENSHOT_TYPE);
            int mScreenshotTypeValue = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, 0);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            mScreenshotType.setSummary(mScreenshotType.getEntry());
            mScreenshotType.setOnPreferenceChangeListener(this);

            mScreenshotDelay = (NumberPickerPreference) findPreference(SCREENSHOT_DELAY);
            mScreenshotDelay.setOnPreferenceChangeListener(this);
            mScreenshotDelay.setMinValue(MIN_DELAY_VALUE);
            mScreenshotDelay.setMaxValue(MAX_DELAY_VALUE);
            int ssDelay = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_DELAY, 1);
            mScreenshotDelay.setCurrentValue(ssDelay);
            updateScreenshotDelaySummary(ssDelay);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mRoundIcon) {
                File f = new File(android.content.pm.PackageParser.DISABLE_CONFIG_ROUND_ICONS_FILE);
                if ((Boolean) newValue) {
                    if (!f.delete()) {
                        mRoundIcon.setChecked(false);
                        Log.e("Various Shit", "Unknown problem while trying to delete " +
                                android.content.pm.PackageParser.DISABLE_CONFIG_ROUND_ICONS_FILE);
                    }
                } else {
                    try {
                        if (!f.createNewFile()) {
                            mRoundIcon.setChecked(true);
                            Log.e("Various Shit", "Unknown problem while trying to create " +
                                    android.content.pm.PackageParser.DISABLE_CONFIG_ROUND_ICONS_FILE);
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                        mRoundIcon.setChecked(true);
                    }
                }
            } else if (preference == mCameraSounds) {
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
            } else if (preference == mScreenshotDelay) {
                int mScreenshotDelayValue = Integer.parseInt(newValue.toString());
                Settings.System.putInt(resolver, Settings.System.SCREENSHOT_DELAY,
                        mScreenshotDelayValue);
                updateScreenshotDelaySummary(mScreenshotDelayValue);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void updateScreenshotDelaySummary(int screenshotDelay) {
            mScreenshotDelay.setSummary(
                    getResources().getString(R.string.powermenu_screenshot_delay_message, screenshotDelay));
        }
    }
}
