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
import android.os.UserHandle;
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
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

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

        private static final String TAG = "VariousShit";

        private static final String KEY_CAMERA_SOUNDS = "camera_sounds";
        private static final String HEADSET_CONNECT_PLAYER = "headset_connect_player";
        private static final String PROP_CAMERA_SOUND = "persist.sys.camera-sound";
        private static final String SCREENSHOT_TYPE = "screenshot_type";
        private static final String SCREENSHOT_DELAY = "screenshot_delay";
        private static final String SCREENSHOT_SUMMARY = "Delay is set to ";
        private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
        private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
        private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
        private static final String SCROLLINGCACHE_DEFAULT = "1";
        private static final String KEY_VOLUME_DIALOG_TIMEOUT = "volume_dialog_timeout";
        private static final String LOCKCLOCK_START_SETTINGS = "lockclock_settings";
        private static final String LOCKCLOCK_PACKAGE_NAME = "com.cyanogenmod.lockclock";
        private static Intent INTENT_LOCKCLOCK_SETTINGS = new Intent(Intent.ACTION_MAIN)
                .setClassName(LOCKCLOCK_PACKAGE_NAME, LOCKCLOCK_PACKAGE_NAME + ".preference.Preferences");
        private static final String PREF_SUSPEND_ACTIONS = "suspend_actions";

        private SwitchPreference mCameraSounds;
        private ListPreference mLaunchPlayerHeadsetConnection;
        private ListPreference mMsob;
        private ListPreference mScreenshotType;
        private SeekBarPreferenceCham mScreenshotDelay;
        private ListPreference mScrollingCachePref;
        private SeekBarPreferenceCham mVolumeDialogTimeout;
        private Preference mLockClockSettings;
        private Preference mSuspendActions;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.various_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();
            PackageManager pm = getActivity().getPackageManager();

            mMsob = (ListPreference) findPreference(PREF_MEDIA_SCANNER_ON_BOOT);
            mMsob.setValue(String.valueOf(Settings.System.getInt(resolver,
                    Settings.System.MEDIA_SCANNER_ON_BOOT, 0)));
            mMsob.setSummary(mMsob.getEntry());
            mMsob.setOnPreferenceChangeListener(this);

            mCameraSounds = (SwitchPreference) findPreference(KEY_CAMERA_SOUNDS);
            mCameraSounds.setChecked(SystemProperties.getBoolean(PROP_CAMERA_SOUND, true));
            mCameraSounds.setOnPreferenceChangeListener(this);

            mScreenshotType = (ListPreference) findPreference(SCREENSHOT_TYPE);
            int mScreenshotTypeValue = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_TYPE, 0);
            mScreenshotType.setValue(String.valueOf(mScreenshotTypeValue));
            mScreenshotType.setSummary(mScreenshotType.getEntry());
            mScreenshotType.setOnPreferenceChangeListener(this);

            mScreenshotDelay = (SeekBarPreferenceCham) findPreference(SCREENSHOT_DELAY);
            int ssDelay = Settings.System.getInt(resolver,
                    Settings.System.SCREENSHOT_DELAY, 1000);
            mScreenshotDelay.setValue(ssDelay / 1);
            mScreenshotDelay.setOnPreferenceChangeListener(this);

            mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
            mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                    SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
            mScrollingCachePref.setOnPreferenceChangeListener(this);

            // Volume dialog timeout seekbar
            mVolumeDialogTimeout = (SeekBarPreferenceCham) findPreference(KEY_VOLUME_DIALOG_TIMEOUT);
            int volumeDialogTimeout = Settings.System.getInt(resolver,
                    Settings.System.VOLUME_DIALOG_TIMEOUT, 3000);
            mVolumeDialogTimeout.setValue(volumeDialogTimeout / 1);
            mVolumeDialogTimeout.setOnPreferenceChangeListener(this);

            mLockClockSettings = (Preference) prefSet.findPreference(LOCKCLOCK_START_SETTINGS);
            if (!Helpers.isPackageInstalled(LOCKCLOCK_PACKAGE_NAME, pm)) {
                prefSet.removePreference(mLockClockSettings);
            }

            mSuspendActions = (Preference) prefSet.findPreference(PREF_SUSPEND_ACTIONS);

            mLaunchPlayerHeadsetConnection = (ListPreference) findPreference(HEADSET_CONNECT_PLAYER);
            int mLaunchPlayerHeadsetConnectionValue = Settings.System.getIntForUser(resolver,
                    Settings.System.HEADSET_CONNECT_PLAYER, 0, UserHandle.USER_CURRENT);
            mLaunchPlayerHeadsetConnection.setValue(Integer.toString(mLaunchPlayerHeadsetConnectionValue));
            mLaunchPlayerHeadsetConnection.setSummary(mLaunchPlayerHeadsetConnection.getEntry());
            mLaunchPlayerHeadsetConnection.setOnPreferenceChangeListener(this);
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
            } else if (preference == mScreenshotDelay) {
                int screenshotDelay = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.SCREENSHOT_DELAY, screenshotDelay * 1);
                return true;
            } else if (preference == mMsob) {
                Settings.System.putInt(resolver,
                    Settings.System.MEDIA_SCANNER_ON_BOOT,
                        Integer.valueOf(String.valueOf(newValue)));

                mMsob.setValue(String.valueOf(newValue));
                mMsob.setSummary(mMsob.getEntry());
                return true;
            } else if (preference == mScrollingCachePref) {
                if (newValue != null) {
                    SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String)newValue);
                    return true;
                }
            } else if (preference == mVolumeDialogTimeout) {
                int volDialogTimeout = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.VOLUME_DIALOG_TIMEOUT, volDialogTimeout * 1);
                return true;
            } else if (preference == mLaunchPlayerHeadsetConnection) {
                int mLaunchPlayerHeadsetConnectionValue = Integer.valueOf((String) newValue);
                int index = mLaunchPlayerHeadsetConnection.findIndexOfValue((String) newValue);
                mLaunchPlayerHeadsetConnection.setSummary(
                        mLaunchPlayerHeadsetConnection.getEntries()[index]);
                Settings.System.putIntForUser(resolver, Settings.System.HEADSET_CONNECT_PLAYER,
                        mLaunchPlayerHeadsetConnectionValue, UserHandle.USER_CURRENT);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mLockClockSettings) {
                startActivity(INTENT_LOCKCLOCK_SETTINGS);
            } else if (preference == mSuspendActions) {
                Intent intent = new Intent(getActivity(), SuspendActions.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return true;
        }
    }
}
