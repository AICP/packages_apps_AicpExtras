/*
 * Copyright (C) 2017 The Dirty Unicorns Project
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

import android.content.ContentResolver;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.aicp.gear.preference.SystemSettingListPreference;
import com.aicp.gear.preference.SystemSettingSwitchPreference;

public class NavigationGestureSettings extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_LONG_BACK_SWIPE_TIMEOUT = "long_back_swipe_timeout";
    private static final String KEY_BACK_SWIPE_EXTENDED = "back_swipe_extended";
    private static final String KEY_LEFT_SWIPE_ACTIONS = "left_long_back_swipe_action";
    private static final String KEY_RIGHT_SWIPE_ACTIONS = "right_long_back_swipe_action";
    private static final String KEY_LEFT_SWIPE_APP_ACTION = "left_swipe_app_action";
    private static final String KEY_RIGHT_SWIPE_APP_ACTION = "right_swipe_app_action";
    private static final String KEY_LEFT_VERTICAL_SWIPE_ACTIONS = "left_vertical_back_swipe_action";
    private static final String KEY_RIGHT_VERTICAL_SWIPE_ACTIONS = "right_vertical_back_swipe_action";
    private static final String KEY_LEFT_VERTICAL_SWIPE_APP_ACTION = "left_vertical_swipe_app_action";
    private static final String KEY_RIGHT_VERTICAL_SWIPE_APP_ACTION = "right_vertical_swipe_app_action";
    private static final String KEY_CATEGORY_LEFT_VERTICAL_SWIPE = "left_vertical_swipe";
    private static final String KEY_CATEGORY_RIGHT_VERTICAL_SWIPE = "right_vertical_swipe";

    private int leftSwipeActions;
    private int rightSwipeActions;

    private SystemSettingListPreference mLeftSwipeActions;
    private SystemSettingListPreference mRightSwipeActions;
    private SystemSettingListPreference mLeftVerticalSwipeActions;
    private SystemSettingListPreference mRightVerticalSwipeActions;

    private Preference mLeftSwipeAppSelection;
    private Preference mRightSwipeAppSelection;
    private Preference mLeftVerticalSwipeAppSelection;
    private Preference mRightVerticalSwipeAppSelection;

    private SystemSettingListPreference mTimeout;
    private SystemSettingSwitchPreference mExtendedSwipe;

    private PreferenceCategory leftVerticalSwipeCategory;
    private PreferenceCategory rightVerticalSwipeCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation_gestures;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final ContentResolver resolver = getActivity().getContentResolver();
        leftSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mLeftSwipeActions = (SystemSettingListPreference) findPreference(KEY_LEFT_SWIPE_ACTIONS);
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());
        mLeftSwipeActions.setOnPreferenceChangeListener(this);
        rightSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mRightSwipeActions = (SystemSettingListPreference) findPreference(KEY_RIGHT_SWIPE_ACTIONS);
        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());
        mRightSwipeActions.setOnPreferenceChangeListener(this);
        mLeftSwipeAppSelection = (Preference) findPreference(KEY_LEFT_SWIPE_APP_ACTION);
        boolean isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mLeftSwipeAppSelection.setEnabled(isAppSelection);
        mRightSwipeAppSelection = (Preference) findPreference(KEY_RIGHT_SWIPE_APP_ACTION);
        isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mRightSwipeAppSelection.setEnabled(isAppSelection);
        customAppCheck();

        leftVerticalSwipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_LEFT_VERTICAL_SWIPE);
        rightVerticalSwipeCategory = (PreferenceCategory) findPreference(KEY_CATEGORY_RIGHT_VERTICAL_SWIPE);

        mTimeout = (SystemSettingListPreference) findPreference(KEY_LONG_BACK_SWIPE_TIMEOUT);
        mExtendedSwipe = (SystemSettingSwitchPreference) findPreference(KEY_BACK_SWIPE_EXTENDED);

        int leftVerticalSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mLeftVerticalSwipeActions = (SystemSettingListPreference) findPreference(KEY_LEFT_VERTICAL_SWIPE_ACTIONS);
        mLeftVerticalSwipeActions.setValue(Integer.toString(leftVerticalSwipeActions));
        mLeftVerticalSwipeActions.setSummary(mLeftVerticalSwipeActions.getEntry());
        mLeftVerticalSwipeActions.setOnPreferenceChangeListener(this);
        int rightVerticalSwipeActions = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        mRightVerticalSwipeActions = (SystemSettingListPreference) findPreference(KEY_RIGHT_VERTICAL_SWIPE_ACTIONS);
        mRightVerticalSwipeActions.setValue(Integer.toString(rightVerticalSwipeActions));
        mRightVerticalSwipeActions.setSummary(mRightVerticalSwipeActions.getEntry());
        mRightVerticalSwipeActions.setOnPreferenceChangeListener(this);
        mLeftVerticalSwipeAppSelection = (Preference) findPreference(KEY_LEFT_VERTICAL_SWIPE_APP_ACTION);
        isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mLeftVerticalSwipeAppSelection.setEnabled(isAppSelection);
        mRightVerticalSwipeAppSelection = (Preference) findPreference(KEY_RIGHT_VERTICAL_SWIPE_APP_ACTION);
        isAppSelection = Settings.System.getIntForUser(resolver,
                Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, 0, UserHandle.USER_CURRENT) == 5/*action_app_action*/;
        mRightVerticalSwipeAppSelection.setEnabled(isAppSelection);

        boolean extendedSwipe = Settings.System.getIntForUser(resolver,
            Settings.System.BACK_SWIPE_EXTENDED, 0,
            UserHandle.USER_CURRENT) != 0;
        mExtendedSwipe.setChecked(extendedSwipe);
        mExtendedSwipe.setOnPreferenceChangeListener(this);
        mTimeout.setEnabled(!mExtendedSwipe.isChecked());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLeftSwipeActions) {
            int leftSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, leftSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mLeftSwipeActions.findIndexOfValue((String) newValue);
            mLeftSwipeActions.setSummary(
                    mLeftSwipeActions.getEntries()[index]);
            mLeftSwipeAppSelection.setEnabled(leftSwipeActions == 5);
            customAppCheck();
            return true;
        } else if (preference == mRightSwipeActions) {
            int rightSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, rightSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mRightSwipeActions.findIndexOfValue((String) newValue);
            mRightSwipeActions.setSummary(
                    mRightSwipeActions.getEntries()[index]);
            mRightSwipeAppSelection.setEnabled(rightSwipeActions == 5);
            customAppCheck();
            return true;
        } else if (preference == mExtendedSwipe) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            mExtendedSwipe.setChecked(enabled);
            mTimeout.setEnabled(!enabled);
        } else if (preference == mLeftVerticalSwipeActions) {
            int leftVerticalSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, leftVerticalSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mLeftVerticalSwipeActions.findIndexOfValue((String) newValue);
            mLeftVerticalSwipeActions.setSummary(
                    mLeftVerticalSwipeActions.getEntries()[index]);
            mLeftVerticalSwipeAppSelection.setEnabled(leftVerticalSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        } else if (preference == mRightVerticalSwipeActions) {
            int rightVerticalSwipeActions = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, rightVerticalSwipeActions,
                    UserHandle.USER_CURRENT);
            int index = mRightVerticalSwipeActions.findIndexOfValue((String) newValue);
            mRightVerticalSwipeActions.setSummary(
                    mRightVerticalSwipeActions.getEntries()[index]);
            mRightVerticalSwipeAppSelection.setEnabled(rightVerticalSwipeActions == 5);
            actionPreferenceReload();
            customAppCheck();
            return true;
        }
        return false;
    }

    /* Helper for reloading both short and long gesture as they might change on
       package uninstallation */
    private void actionPreferenceReload() {
        int leftSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int rightSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_LONG_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int leftVerticalSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.LEFT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);
        int rightVerticalSwipeActions = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.RIGHT_VERTICAL_BACK_SWIPE_ACTION, 0,
                UserHandle.USER_CURRENT);

        // Reload the action preferences
        mLeftSwipeActions.setValue(Integer.toString(leftSwipeActions));
        mLeftSwipeActions.setSummary(mLeftSwipeActions.getEntry());
        mRightSwipeActions.setValue(Integer.toString(rightSwipeActions));
        mRightSwipeActions.setSummary(mRightSwipeActions.getEntry());
        mLeftSwipeAppSelection.setEnabled(mLeftSwipeActions.getEntryValues()
                [leftSwipeActions].equals("5"));
        mRightSwipeAppSelection.setEnabled(mRightSwipeActions.getEntryValues()
                [rightSwipeActions].equals("5"));
        mLeftVerticalSwipeAppSelection.setEnabled(mLeftVerticalSwipeActions.getEntryValues()
                [leftVerticalSwipeActions].equals("5"));
        mRightVerticalSwipeAppSelection.setEnabled(mRightVerticalSwipeActions.getEntryValues()
                [rightVerticalSwipeActions].equals("5"));
    }

    private void customAppCheck() {
        mLeftSwipeAppSelection.setSummary(Settings.System.getStringForUser(getContentResolver(),
                String.valueOf(Settings.System.LEFT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
        mRightSwipeAppSelection.setSummary(Settings.System.getStringForUser(getContentResolver(),
                String.valueOf(Settings.System.RIGHT_LONG_BACK_SWIPE_APP_FR_ACTION), UserHandle.USER_CURRENT));
    }
}
