/*
 * Copyright (C) 2018 Android Ice Cold Project
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

package com.aicp.extras.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.preference.PreferenceDataStore;
import android.util.Log;

import com.aicp.extras.R;

import com.aicp.gear.preference.GlobalSettingsStore;
import com.aicp.gear.preference.SystemSettingsStore;
import com.aicp.gear.preference.SecureSettingsStore;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class to handle mutual exclusive master switch preferences, reloadValue calls etc.
 */
public class MasterSwitchPreferenceDependencyHandler {

    private static final String TAG =
            MasterSwitchPreferenceDependencyHandler.class.getSimpleName();

    private HashMap<Integer,ArrayList<PreferenceControl>> mPreferences;
    private SystemSettingsStore mSystemSetingsStore;
    private SecureSettingsStore mSecureSetingsStore;
    private GlobalSettingsStore mGlobalSettingsStore;

    public MasterSwitchPreferenceDependencyHandler(Context context) {
        mPreferences = new HashMap();
        mSystemSetingsStore = new SystemSettingsStore(context.getContentResolver());
        mSecureSetingsStore = new SecureSettingsStore(context.getContentResolver());
        mGlobalSettingsStore = new GlobalSettingsStore(context.getContentResolver());
    }

    private static interface PreferenceControl {
        boolean isChecked();
        void setChecked(boolean checked);
        int getGroupId();
        String getKey();
        void reload();
        boolean isSystemSetting();
        boolean isSecureSetting();
        boolean isGlobalSetting();
    }

    private static class MasterSwitchPreferenceControl implements PreferenceControl {
        private MasterSwitchPreference mPreference;
        public MasterSwitchPreferenceControl(MasterSwitchPreference preference) {
            mPreference = preference;
        }
        @Override
        public boolean isChecked() {
            return mPreference.isChecked();
        }
        @Override
        public void setChecked(boolean checked) {
            mPreference.setCheckedPersisting(checked);
        }
        @Override
        public int getGroupId() {
            return mPreference.getThereCanBeOnlyOneGroupId();
        }
        @Override
        public String getKey() {
            return mPreference.getKey();
        }
        @Override
        public void reload() {
            mPreference.reloadValue();
        }
        @Override
        public boolean isSystemSetting() {
            return mPreference.getPreferenceDataStore() instanceof SystemSettingsStore;
        }
        @Override
        public boolean isSecureSetting() {
            return mPreference.getPreferenceDataStore() instanceof SecureSettingsStore;
        }
        @Override
        public boolean isGlobalSetting() {
            return mPreference.getPreferenceDataStore() instanceof GlobalSettingsStore;
        }
    }

    private static class PreferenceStoreControl implements PreferenceControl {
        private String mKey;
        private int mGroup;
        private boolean mDefaultValue;
        private PreferenceDataStore mPreferenceStore;
        public PreferenceStoreControl(String key, int group, boolean defaultValue,
                PreferenceDataStore preferenceStore) {
            mKey = key;
            mGroup = group;
            mDefaultValue = defaultValue;
            mPreferenceStore = preferenceStore;
        }
        @Override
        public boolean isChecked() {
            return mPreferenceStore.getBoolean(mKey, mDefaultValue);
        }
        @Override
        public void setChecked(boolean checked) {
            mPreferenceStore.putBoolean(mKey, checked);
        }
        @Override
        public int getGroupId() {
            return mGroup;
        }
        @Override
        public String getKey() {
            return mKey;
        }
        @Override
        public void reload() {}
        @Override
        public boolean isSystemSetting() {
            return mPreferenceStore instanceof SystemSettingsStore;
        }
        @Override
        public boolean isSecureSetting() {
            return mPreferenceStore instanceof SecureSettingsStore;
        }
        @Override
        public boolean isGlobalSetting() {
            return mPreferenceStore instanceof GlobalSettingsStore;
        }
    }

    public void addPreferences(MasterSwitchPreference... preferences) {
        for (MasterSwitchPreference pref: preferences) {
            pref.setDependencyHandler(this);
            addPreferences(new MasterSwitchPreferenceControl(pref));
        }
    }

    public void addSystemSettingPreferences(int group, String... keys) {
        for (String key: keys) {
            addPreferences(new PreferenceStoreControl(key, group, false, mSystemSetingsStore));
        }
    }

    public void addSecureSettingPreferences(int group, String... keys) {
        for (String key: keys) {
            addPreferences(new PreferenceStoreControl(key, group, false, mSecureSetingsStore));
        }
    }

    public void addGlobalSettingPreferences(int group, String... keys) {
        for (String key: keys) {
            addPreferences(new PreferenceStoreControl(key, group, false, mGlobalSettingsStore));
        }
    }

    private void addPreferences(PreferenceControl... preferences) {
        for (PreferenceControl pref: preferences) {
            int groupId = pref.getGroupId();
            // Even add those for groupId 0 (no group) for reload on resume
            ArrayList<PreferenceControl> groupList;
            if (!mPreferences.containsKey(groupId)) {
                groupList = new ArrayList();
                mPreferences.put(groupId, groupList);
            } else {
                groupList = mPreferences.get(groupId);
            }
            if (!groupList.contains(pref)) {
                groupList.add(pref);
            }
        }
    }

    public boolean isAnotherEnabled(int groupId, String preferenceKey) {
        if (groupId == 0) {
            // group id 0 is no group id -> no restriction
            return true;
        }
        ArrayList<PreferenceControl> groupList = mPreferences.get(groupId);
        if (groupList == null) {
            Log.e(TAG, "inconsistent master switch dependency handler: canDisable called for " +
                    "unregistered group " + groupId + " (" + preferenceKey + ")");
            return true;
        }
        // Check if at least one other is enabled
        for (PreferenceControl pref: groupList) {
            if (preferenceKey.equals(pref.getKey())) {
                continue;
            }
            if (pref.isChecked()) {
                // Another switch is enabled -> all fine!
                return true;
            }
        }
        return false;
    }

    public void onEnablePref(int groupId, String preferenceKey) {
        if (groupId == 0) {
            // group id 0 is no group id -> no restriction
            return;
        }
        ArrayList<PreferenceControl> groupList = mPreferences.get(groupId);
        if (groupList == null) {
            Log.e(TAG, "inconsistent master switch dependency handler: onEnablePref called for " +
                    "unregistered group " + groupId + " (" + preferenceKey + ")");
            return;
        }
        // Disable others of same group
        for (PreferenceControl pref: groupList) {
            if (preferenceKey.equals(pref.getKey())) {
                continue;
            }
            pref.setChecked(false);
        }
    }

    public void onResume() {
        for (ArrayList<PreferenceControl> group: mPreferences.values()) {
            for (PreferenceControl pref: group) {
                pref.reload();
            }
        }
    }

    public String[] getSystemSettingsForGroup(int groupId) {
        if (groupId == 0) {
            // group id 0 is no group id -> no restrictions
            return new String[0];
        }
        ArrayList<PreferenceControl> groupList = mPreferences.get(groupId);
        if (groupList == null) {
            Log.e(TAG, "inconsistent master switch dependency handler: getSystemSettingForGroup " +
                    "called for unregistered group " + groupId);
            return new String[0];
        }
        ArrayList<String> result = new ArrayList();
        for (PreferenceControl pref: groupList) {
            if (pref.isSystemSetting()) {
                result.add(pref.getKey());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public String[] getSecureSettingsForGroup(int groupId) {
        if (groupId == 0) {
            // group id 0 is no group id -> no restrictions
            return new String[0];
        }
        ArrayList<PreferenceControl> groupList = mPreferences.get(groupId);
        if (groupList == null) {
            Log.e(TAG, "inconsistent master switch dependency handler: getSecureSettingForGroup " +
                    "called for unregistered group " + groupId);
            return new String[0];
        }
        ArrayList<String> result = new ArrayList();
        for (PreferenceControl pref: groupList) {
            if (pref.isSecureSetting()) {
                result.add(pref.getKey());
            }
        }
        return result.toArray(new String[result.size()]);
    }

    public String[] getGlobalSettingsForGroup(int groupId) {
        if (groupId == 0) {
            // group id 0 is no group id -> no restrictions
            return new String[0];
        }
        ArrayList<PreferenceControl> groupList = mPreferences.get(groupId);
        if (groupList == null) {
            Log.e(TAG, "inconsistent master switch dependency handler: getGlobalSettingForGroup " +
                    "called for unregistered group " + groupId);
            return new String[0];
        }
        ArrayList<String> result = new ArrayList();
        for (PreferenceControl pref: groupList) {
            if (pref.isGlobalSetting()) {
                result.add(pref.getKey());
            }
        }
        return result.toArray(new String[result.size()]);
    }


    public static void showConfirmDisableDialog(final Context context,
                                     final DialogInterface.OnClickListener continueButtonListener,
                                     final DialogInterface.OnClickListener cancelButtonListener) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.switch_disable_warning_title)
                .setMessage(R.string.switch_disable_warning_message)
                .setPositiveButton(R.string.switch_disable_warning_continue,
                        continueButtonListener)
                .setNegativeButton(R.string.switch_disable_warning_cancel,
                        cancelButtonListener)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (cancelButtonListener != null) {
                                // Cancelling equals negative selection
                                cancelButtonListener.onClick(dialog,
                                        DialogInterface.BUTTON_NEGATIVE);
                            }
                        }
                })
                .show();
    }
}
