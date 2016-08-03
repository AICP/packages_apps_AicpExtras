/*
 * Copyright (C) 2014 AOKP
 *
 * Modified by crDroid Android
 * Modified by AICP 2015
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

package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.util.aicp.AwesomeAnimationHelper;
import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.AnimBarPreference;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class AnimationControls extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new AnimationControlsFragment()).commit();
    }

    public static class AnimationControlsFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String ACTIVITY_OPEN = "activity_open";
        private static final String ACTIVITY_CLOSE = "activity_close";
        private static final String TASK_OPEN = "task_open";
        private static final String TASK_CLOSE = "task_close";
        private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
        private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
        private static final String ANIMATION_DURATION = "animation_duration";
        private static final String WALLPAPER_OPEN = "wallpaper_open";
        private static final String WALLPAPER_CLOSE = "wallpaper_close";
        private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
        private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";
        private static final String TASK_OPEN_BEHIND = "task_open_behind";
        private static final String ANIMATION_CONTROLS_NO_OVERRIDE = "animation_controls_no_override";
        private static final String ANIMATION_CONTROLS_EXIT_ONLY = "animation_controls_exit_only";
        private static final String ANIMATION_CONTROLS_REVERSE_EXIT = "animation_controls_reverse_exit";

        ListPreference mActivityOpenPref;
        ListPreference mActivityClosePref;
        ListPreference mTaskOpenPref;
        ListPreference mTaskClosePref;
        ListPreference mTaskMoveToFrontPref;
        ListPreference mTaskMoveToBackPref;
        ListPreference mWallpaperOpen;
        ListPreference mWallpaperClose;
        ListPreference mWallpaperIntraOpen;
        ListPreference mWallpaperIntraClose;
        ListPreference mTaskOpenBehind;
        AnimBarPreference mAnimationDuration;
        SwitchPreference mAnimNoOverride;
        SwitchPreference mAnimExitOnly;
        SwitchPreference mAnimReverseOnly;

        private int[] mAnimations;
        private String[] mAnimationsStrings;
        private String[] mAnimationsNum;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.aokp_animation_controls);

            PreferenceScreen prefs = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mAnimations = AwesomeAnimationHelper.getAnimationsList();
            int animqty = mAnimations.length;
            mAnimationsStrings = new String[animqty];
            mAnimationsNum = new String[animqty];
            for (int i = 0; i < animqty; i++) {
                mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(getContext(), mAnimations[i]);
                mAnimationsNum[i] = String.valueOf(mAnimations[i]);
            }

            mActivityOpenPref = (ListPreference) findPreference(ACTIVITY_OPEN);
            mActivityOpenPref.setOnPreferenceChangeListener(this);
            mActivityOpenPref.setSummary(getProperSummary(mActivityOpenPref));
            mActivityOpenPref.setEntries(mAnimationsStrings);
            mActivityOpenPref.setEntryValues(mAnimationsNum);

            mActivityClosePref = (ListPreference) findPreference(ACTIVITY_CLOSE);
            mActivityClosePref.setOnPreferenceChangeListener(this);
            mActivityClosePref.setSummary(getProperSummary(mActivityClosePref));
            mActivityClosePref.setEntries(mAnimationsStrings);
            mActivityClosePref.setEntryValues(mAnimationsNum);

            mTaskOpenPref = (ListPreference) findPreference(TASK_OPEN);
            mTaskOpenPref.setOnPreferenceChangeListener(this);
            mTaskOpenPref.setSummary(getProperSummary(mTaskOpenPref));
            mTaskOpenPref.setEntries(mAnimationsStrings);
            mTaskOpenPref.setEntryValues(mAnimationsNum);

            mTaskClosePref = (ListPreference) findPreference(TASK_CLOSE);
            mTaskClosePref.setOnPreferenceChangeListener(this);
            mTaskClosePref.setSummary(getProperSummary(mTaskClosePref));
            mTaskClosePref.setEntries(mAnimationsStrings);
            mTaskClosePref.setEntryValues(mAnimationsNum);

            mTaskMoveToFrontPref = (ListPreference) findPreference(TASK_MOVE_TO_FRONT);
            mTaskMoveToFrontPref.setOnPreferenceChangeListener(this);
            mTaskMoveToFrontPref.setSummary(getProperSummary(mTaskMoveToFrontPref));
            mTaskMoveToFrontPref.setEntries(mAnimationsStrings);
            mTaskMoveToFrontPref.setEntryValues(mAnimationsNum);

            mTaskMoveToBackPref = (ListPreference) findPreference(TASK_MOVE_TO_BACK);
            mTaskMoveToBackPref.setOnPreferenceChangeListener(this);
            mTaskMoveToBackPref.setSummary(getProperSummary(mTaskMoveToBackPref));
            mTaskMoveToBackPref.setEntries(mAnimationsStrings);
            mTaskMoveToBackPref.setEntryValues(mAnimationsNum);

            mWallpaperOpen = (ListPreference) findPreference(WALLPAPER_OPEN);
            mWallpaperOpen.setOnPreferenceChangeListener(this);
            mWallpaperOpen.setSummary(getProperSummary(mWallpaperOpen));
            mWallpaperOpen.setEntries(mAnimationsStrings);
            mWallpaperOpen.setEntryValues(mAnimationsNum);

            mWallpaperClose = (ListPreference) findPreference(WALLPAPER_CLOSE);
            mWallpaperClose.setOnPreferenceChangeListener(this);
            mWallpaperClose.setSummary(getProperSummary(mWallpaperClose));
            mWallpaperClose.setEntries(mAnimationsStrings);
            mWallpaperClose.setEntryValues(mAnimationsNum);

            mWallpaperIntraOpen = (ListPreference) findPreference(WALLPAPER_INTRA_OPEN);
            mWallpaperIntraOpen.setOnPreferenceChangeListener(this);
            mWallpaperIntraOpen.setSummary(getProperSummary(mWallpaperIntraOpen));
            mWallpaperIntraOpen.setEntries(mAnimationsStrings);
            mWallpaperIntraOpen.setEntryValues(mAnimationsNum);

            mWallpaperIntraClose = (ListPreference) findPreference(WALLPAPER_INTRA_CLOSE);
            mWallpaperIntraClose.setOnPreferenceChangeListener(this);
            mWallpaperIntraClose.setSummary(getProperSummary(mWallpaperIntraClose));
            mWallpaperIntraClose.setEntries(mAnimationsStrings);
            mWallpaperIntraClose.setEntryValues(mAnimationsNum);

            mTaskOpenBehind = (ListPreference) findPreference(TASK_OPEN_BEHIND);
            mTaskOpenBehind.setOnPreferenceChangeListener(this);
            mTaskOpenBehind.setSummary(getProperSummary(mTaskOpenBehind));
            mTaskOpenBehind.setEntries(mAnimationsStrings);
            mTaskOpenBehind.setEntryValues(mAnimationsNum);

            int defaultDuration = Settings.System.getInt(resolver,
                    Settings.System.ANIMATION_CONTROLS_DURATION, 0);
            mAnimationDuration = (AnimBarPreference) findPreference(ANIMATION_DURATION);
            mAnimationDuration.setInitValue((int) (defaultDuration));
            mAnimationDuration.setOnPreferenceChangeListener(this);

            mAnimNoOverride = (SwitchPreference) prefs.findPreference(ANIMATION_CONTROLS_NO_OVERRIDE);
            mAnimNoOverride.setChecked((Settings.System.getInt(resolver,
                    Settings.System.ANIMATION_CONTROLS_NO_OVERRIDE, 0) == 1));
            mAnimNoOverride.setOnPreferenceChangeListener(this);

            mAnimExitOnly = (SwitchPreference) prefs.findPreference(ANIMATION_CONTROLS_EXIT_ONLY);
            mAnimExitOnly.setChecked((Settings.System.getInt(resolver,
                    Settings.System.ANIMATION_CONTROLS_EXIT_ONLY, 1) == 1));
            mAnimExitOnly.setOnPreferenceChangeListener(this);

            mAnimReverseOnly = (SwitchPreference) prefs.findPreference(ANIMATION_CONTROLS_REVERSE_EXIT);
            mAnimReverseOnly.setChecked((Settings.System.getInt(resolver,
                    Settings.System.ANIMATION_CONTROLS_REVERSE_EXIT, 0) == 1));
            mAnimReverseOnly.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            boolean result = false;
            if (preference == mActivityOpenPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[0], val);
            } else if (preference == mActivityClosePref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[1], val);
            } else if (preference == mTaskOpenPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[2], val);
            } else if (preference == mTaskClosePref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[3], val);
            } else if (preference == mTaskMoveToFrontPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[4], val);
            } else if (preference == mTaskMoveToBackPref) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[5], val);
            } else if (preference == mWallpaperOpen) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[6], val);
            } else if (preference == mWallpaperClose) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[7], val);
            } else if (preference == mWallpaperIntraOpen) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[8], val);
            } else if (preference == mWallpaperIntraClose) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[9], val);
            } else if (preference == mTaskOpenBehind) {
                int val = Integer.parseInt((String) newValue);
                result = Settings.System.putInt(resolver,
                        Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], val);
            } else if (preference == mAnimationDuration) {
                int val = Integer.parseInt((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.ANIMATION_CONTROLS_DURATION, val);
            } else if (preference == mAnimNoOverride) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.ANIMATION_CONTROLS_NO_OVERRIDE,
                        value ? 1 : 0);
                return true;
            } else if (preference == mAnimExitOnly) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.ANIMATION_CONTROLS_EXIT_ONLY,
                        value ? 1 : 0);
                return true;
            } else if (preference == mAnimReverseOnly) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver, Settings.System.ANIMATION_CONTROLS_REVERSE_EXIT,
                        value ? 1 : 0);
                return true;
            }
            preference.setSummary(getProperSummary(preference));
            return result;
        }

        private String getProperSummary(Preference preference) {
            String mString = "";
            if (preference == mActivityOpenPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[0];
            } else if (preference == mActivityClosePref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[1];
            } else if (preference == mTaskOpenPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[2];
            } else if (preference == mTaskClosePref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[3];
            } else if (preference == mTaskMoveToFrontPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[4];
            } else if (preference == mTaskMoveToBackPref) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[5];
            } else if (preference == mWallpaperOpen) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[6];
            } else if (preference == mWallpaperClose) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[7];
            } else if (preference == mWallpaperIntraOpen) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[8];
            } else if (preference == mWallpaperIntraClose) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[9];
            } else if (preference == mTaskOpenBehind) {
                mString = Settings.System.ACTIVITY_ANIMATION_CONTROLS[10];
            }

            int mNum = Settings.System.getInt(getActivity().getContentResolver(), mString, 0);
            return mAnimationsStrings[mNum];
        }
    }
}
