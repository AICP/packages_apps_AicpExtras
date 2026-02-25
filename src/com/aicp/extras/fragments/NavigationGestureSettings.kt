/*
 * Copyright (C) 2017 The Dirty Unicorns Project
 * Copyright (C) 2026 AICP
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
package com.aicp.extras.fragments

import android.os.Bundle
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

class NavigationGestureSettings :
    BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener {

    /*
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
    */

    override fun getPreferenceResource(): Int {
        return R.xml.navigation_gestures
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Original Java logic intentionally kept commented out
    }
    */

    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?
    ): Boolean {

        /*
        if (preference == mLeftSwipeActions) {
            // original implementation
            return true
        } else if (preference == mRightSwipeActions) {
            return true
        } else if (preference == mExtendedSwipe) {
        } else if (preference == mLeftVerticalSwipeActions) {
            return true
        } else if (preference == mRightVerticalSwipeActions) {
            return true
        }
        */

        return false
    }

    /*
    Helper for reloading both short and long gesture as they might change on
    package uninstallation
    */

    /*
    private void actionPreferenceReload() {
        // original implementation
    }

    private void customAppCheck() {
        // original implementation
    }
    */
}
