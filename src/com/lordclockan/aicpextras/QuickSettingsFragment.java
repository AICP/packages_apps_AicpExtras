package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
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

import net.margaritov.preference.colorpicker.ColorPickerPreference;

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

        private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
        private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
        private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
        private static final String PREF_ROWS_PORTRAIT = "qs_rows_portrait";
        private static final String PREF_ROWS_LANDSCAPE = "qs_rows_landscape";
        private static final String PREF_COLUMNS_PORTRAIT = "qs_columns_portrait";
        private static final String PREF_COLUMNS_LANDSCAPE = "qs_columns_landscape";
        private static final String PREF_QS_DATA_ADVANCED = "qs_data_advanced";
        private static final String CATEGORY_WEATHER = "weather_category";
        private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";

        private ListPreference mTileAnimationStyle;
        private ListPreference mTileAnimationDuration;
        private ListPreference mTileAnimationInterpolator;
        private SeekBarPreferenceCham mRowsPortrait;
        private SeekBarPreferenceCham mRowsLandscape;
        private SeekBarPreferenceCham mQsColumnsPortrait;
        private SeekBarPreferenceCham mQsColumnsLandscape;
        private SwitchPreference mQsDataAdvanced;
        private PreferenceCategory mWeatherCategory;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.qs_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();
            final ContentResolver resolver = getActivity().getContentResolver();
            final PackageManager pm = getActivity().getPackageManager();

            int defaultValue;

            // QS tile animation
            mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
            int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_STYLE, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            mTileAnimationStyle.setOnPreferenceChangeListener(this);

            mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
            int tileAnimationDuration = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_DURATION, 1500,
                    UserHandle.USER_CURRENT);
            mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
            updateTileAnimationDurationSummary(tileAnimationDuration);
            mTileAnimationDuration.setOnPreferenceChangeListener(this);

            mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
            int tileAnimationInterpolator = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

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

            mWeatherCategory = (PreferenceCategory) prefSet.findPreference(CATEGORY_WEATHER);
            if (mWeatherCategory != null && (!Helpers.isPackageInstalled(WEATHER_SERVICE_PACKAGE, pm))) {
                prefSet.removePreference(mWeatherCategory);
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
            if (preference == mTileAnimationStyle) {
                int tileAnimationStyle = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_STYLE,
                        tileAnimationStyle, UserHandle.USER_CURRENT);
                updateTileAnimationStyleSummary(tileAnimationStyle);
                updateAnimTileStyle(tileAnimationStyle);
                return true;
            } else if (preference == mTileAnimationDuration) {
                int tileAnimationDuration = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_DURATION,
                        tileAnimationDuration, UserHandle.USER_CURRENT);
                updateTileAnimationDurationSummary(tileAnimationDuration);
                return true;
            } else if (preference == mTileAnimationInterpolator) {
                int tileAnimationInterpolator = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_INTERPOLATOR,
                        tileAnimationInterpolator, UserHandle.USER_CURRENT);
                updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
                return true;
            } else if (preference == mRowsPortrait) {
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
            }
            return false;


        }

        private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
            String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                    .valueOf(tileAnimationStyle))];
            mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
        }

        private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
            String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                    .valueOf(tileAnimationDuration))];
            mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
        }

        private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
            String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                    .valueOf(tileAnimationInterpolator))];
            mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
        }

        private void updateAnimTileStyle(int tileAnimationStyle) {
            if (mTileAnimationDuration != null) {
                if (tileAnimationStyle == 0) {
                    mTileAnimationDuration.setSelectable(false);
                    mTileAnimationInterpolator.setSelectable(false);
                } else {
                    mTileAnimationDuration.setSelectable(true);
                    mTileAnimationInterpolator.setSelectable(true);
                }
            }
        }
    }
}
