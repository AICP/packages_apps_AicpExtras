package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

import cyanogenmod.providers.CMSettings;

import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.utils.Utils;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

import com.lordclockan.R;

public class QuickSettingsFragment extends Fragment {

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

        private static final String TAG = QuickSettingsFragment.class.getSimpleName();

        private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
        private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
        private static final String PREF_COLUMNS_PORTRAIT = "qs_columns_portrait";
        private static final String PREF_COLUMNS_LANDSCAPE = "qs_columns_landscape";
        private static final String PREF_QS_DATA_ADVANCED = "qs_data_advanced";
        private static final String PREF_BRIGHTNESS_ICON_POSITION = "brightness_icon_position";
        private static final String FP_SWIPE_QUICK_PULLDOWN = "quick_pulldown_fp";

        private FingerprintManager mFingerprintManager;
        private SeekBarPreferenceCham mRowsPortrait;
        private SeekBarPreferenceCham mRowsLandscape;
        private SeekBarPreferenceCham mQsColumnsPortrait;
        private SeekBarPreferenceCham mQsColumnsLandscape;
        private SwitchPreference mFingerprintSwipe;
        private SwitchPreference mQsDataAdvanced;
        private SwitchPreference mBrightnessIconPosition;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.qs_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();
            final ContentResolver resolver = getActivity().getContentResolver();
            final PackageManager pm = getActivity().getPackageManager();

            int defaultValue;

            mRowsPortrait = (SeekBarPreferenceCham) findPreference(PREF_ROWS_PORTRAIT);
            int rowsPortrait = Settings.Secure.getInt(resolver,
                    Settings.Secure.QS_ROWS_PORTRAIT, 3);
            mRowsPortrait.setValue((int) rowsPortrait);
            mRowsPortrait.setOnPreferenceChangeListener(this);

            defaultValue = getResources().getInteger(com.android.internal.R.integer.config_qs_num_rows_landscape_default);
            mRowsLandscape = (SeekBarPreferenceCham) findPreference(PREF_ROWS_LANDSCAPE);
            int rowsLandscape = Settings.Secure.getInt(resolver,
                    Settings.Secure.QS_ROWS_LANDSCAPE, defaultValue);
            mRowsLandscape.setValue((int) rowsLandscape);
            mRowsLandscape.setOnPreferenceChangeListener(this);

            mQsColumnsPortrait = (SeekBarPreferenceCham) findPreference(PREF_COLUMNS_PORTRAIT);
            int columnsQsPortrait = Settings.Secure.getInt(resolver,
                    Settings.Secure.QS_COLUMNS_PORTRAIT, 5);
            mQsColumnsPortrait.setValue((int) columnsQsPortrait);
            mQsColumnsPortrait.setOnPreferenceChangeListener(this);

            mQsColumnsLandscape = (SeekBarPreferenceCham) findPreference(PREF_COLUMNS_LANDSCAPE);
            int columnsQsLandscape = Settings.Secure.getInt(resolver,
                    Settings.Secure.QS_COLUMNS_LANDSCAPE, 3);
            mQsColumnsLandscape.setValue((int) columnsQsLandscape);
            mQsColumnsLandscape.setOnPreferenceChangeListener(this);

            mQsDataAdvanced = (SwitchPreference) findPreference(PREF_QS_DATA_ADVANCED);
            if (Utils.isWifiOnly(getActivity())) {
                prefSet.removePreference(mQsDataAdvanced);
            }

            mBrightnessIconPosition = (SwitchPreference) findPreference(PREF_BRIGHTNESS_ICON_POSITION);
            mBrightnessIconPosition.setOnPreferenceChangeListener(this);

            // Fingerprint swipe for quick pulldown
            mFingerprintManager = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            mFingerprintSwipe = (SwitchPreference) findPreference(FP_SWIPE_QUICK_PULLDOWN);
            if (!mFingerprintManager.isHardwareDetected()) {
                prefSet.removePreference(mFingerprintSwipe);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            int intValue;
            int index;
            if (preference == mRowsPortrait) {
                intValue = (Integer) newValue;
                Settings.Secure.putInt(resolver,
                        Settings.Secure.QS_ROWS_PORTRAIT, intValue);
                return true;
            } else if (preference == mRowsLandscape) {
                intValue = (Integer) newValue;
                Settings.Secure.putInt(resolver,
                        Settings.Secure.QS_ROWS_LANDSCAPE, intValue);
                return true;
            } else if (preference == mQsColumnsPortrait) {
                intValue = (Integer) newValue;
                Settings.Secure.putInt(resolver,
                        Settings.Secure.QS_COLUMNS_PORTRAIT, intValue);
                return true;
            } else if (preference == mQsColumnsLandscape) {
                intValue = (Integer) newValue;
                Settings.Secure.putInt(resolver,
                        Settings.Secure.QS_COLUMNS_LANDSCAPE, intValue);
                return true;
            } else if (preference == mBrightnessIconPosition) {
                Helpers.showSystemUIrestartDialog(getActivity());
                return true;
            }
            return false;
        }
    }
}
