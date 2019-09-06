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

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.ContentResolver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.ServiceManager;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.view.WindowManagerGlobal;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Locale;
import android.text.TextUtils;
import android.view.View;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.MasterSwitchPreference;
import com.aicp.extras.utils.Util;

public class Recents extends BaseSettingsFragment implements OnPreferenceChangeListener {

    private static final String PREF_STOCK_RECENTS_CATEGORY = "stock_recents_category";
    private static final String PREF_ALTERNATIVE_RECENTS_CATEGORY = "alternative_recents_category";
    private static final String PREF_SWIPE_UP_ENABLED = "swipe_up_enabled_warning";
    private static final String RECENTS_COMPONENT_TYPE = "recents_component";
    private static final String RECENTS_TYPE = "recents_layout_style";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
    private static final int RECENTS_COMPONENT_OREO = 1;

    private ListPreference mRecentsComponentType;
    private ListPreference mRecentsClearAllLocation;
    private SwitchPreference mRecentsClearAll;
    private ListPreference mRecentsType;
    private PreferenceCategory oreoRecentsCat;

    private PreferenceCategory mStockRecentsCategory;
    private PreferenceCategory mAlternativeRecentsCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.recents;
    }

    /*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ContentResolver resolver = getActivity().getContentResolver();
        final PreferenceScreen prefSet = getPreferenceScreen();

        /**
         * Nice clean code start
         * /

        mStockRecentsCategory = (PreferenceCategory) findPreference(PREF_STOCK_RECENTS_CATEGORY);
        mAlternativeRecentsCategory =
                (PreferenceCategory) findPreference(PREF_ALTERNATIVE_RECENTS_CATEGORY);

        // Alternative recents en-/disabling
        Preference.OnPreferenceChangeListener alternativeRecentsChangeListener =
                new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateDependencies(preference, (Boolean) newValue);
                return true;
            }
        };
        for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
            Preference preference = mAlternativeRecentsCategory.getPreference(i);
            if (preference instanceof MasterSwitchPreference) {
                preference.setOnPreferenceChangeListener(alternativeRecentsChangeListener);
            }
        }
        updateDependencies();

        // Warning for alternative recents when swipe up home navigation is enabled,
        // which controls quickstep (launcher) recents.
        final int swipeUpDefaultValue = getActivity().getResources()
                .getBoolean(com.android.internal.R.bool.config_swipe_up_gesture_default) ? 1: 0;
        final int swipeUpEnabled = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, swipeUpDefaultValue);
        if (swipeUpEnabled == 1) {
            for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
                Preference preference = mAlternativeRecentsCategory.getPreference(i);
                if (PREF_SWIPE_UP_ENABLED.equals(preference.getKey())) {
                    // We want to have that one enabled
                    continue;
                }
                preference.setEnabled(false);
            }
        } else {
            mAlternativeRecentsCategory.removePreference(findPreference(PREF_SWIPE_UP_ENABLED));
        }

        /*
         * Nice clean code end
         * /


        /**
         * Probably better done in xml - code start
         * /

        // recents component type
        mRecentsComponentType = (ListPreference) findPreference(RECENTS_COMPONENT_TYPE);
        int type = Settings.System.getInt(resolver,
                Settings.System.RECENTS_COMPONENT, 0);
        mRecentsComponentType.setValue(String.valueOf(type));
        mRecentsComponentType.setSummary(mRecentsComponentType.getEntry());
        mRecentsComponentType.setOnPreferenceChangeListener(this);

        // clear all recents
        mRecentsClearAllLocation = (ListPreference) findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

        // oreo recents type
        mRecentsType = (ListPreference) findPreference(RECENTS_TYPE);
        int style = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_LAYOUT_STYLE, 0, UserHandle.USER_CURRENT);
        mRecentsType.setValue(String.valueOf(style));
        mRecentsType.setSummary(mRecentsType.getEntry());
        mRecentsType.setOnPreferenceChangeListener(this);

        oreoRecentsCat = (PreferenceCategory)findPreference("recents_ui_oreo_recents_category");
        oreoRecentsCat.setEnabled(type == RECENTS_COMPONENT_OREO);

        /**
         * Most likely too much spagetti - code end
         * /
    }


    private void updateDependencies() {
        updateDependencies(null, null);
    }

    private void updateDependencies(Preference updatedPreference, Boolean newValue) {
        // Disable stock recents category if alternative enabled
        boolean alternativeRecentsEnabled = newValue != null && newValue;
        if (!alternativeRecentsEnabled) {
            for (int i = 0; i < mAlternativeRecentsCategory.getPreferenceCount(); i++) {
                Preference preference = mAlternativeRecentsCategory.getPreference(i);
                if (preference == updatedPreference) {
                    // Already used newValue
                    continue;
                }
                if (preference instanceof MasterSwitchPreference
                        && ((MasterSwitchPreference) preference).isChecked()) {
                    alternativeRecentsEnabled = true;
                    break;
                }
            }
        }
        mStockRecentsCategory.setEnabled(!alternativeRecentsEnabled);
    }
    */


    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        /*
         * Ugly code start
         */
        /*
        if (preference == mRecentsComponentType) {
            int type = Integer.valueOf((String) objValue);
            int index = mRecentsComponentType.findIndexOfValue((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.RECENTS_COMPONENT, type);
            mRecentsComponentType.setSummary(mRecentsComponentType.getEntries()[index]);
            oreoRecentsCat.setEnabled(type == RECENTS_COMPONENT_OREO);
            if (type == RECENTS_COMPONENT_OREO) { // Disable swipe up gesture, if oreo type selected
               Settings.Secure.putInt(getActivity().getContentResolver(),
                    Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED, 0);
            }
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) objValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        } else if (preference == mRecentsType) {
            int style = Integer.valueOf((String) objValue);
            int index = mRecentsType.findIndexOfValue((String) objValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_LAYOUT_STYLE, style, UserHandle.USER_CURRENT);
            mRecentsType.setSummary(mRecentsType.getEntries()[index]);
            Util.showSystemUiRestartDialog(getContext());
            return true;
        }
        */
        /*
         * Ugly code end
         */
        return false;
    }

}
