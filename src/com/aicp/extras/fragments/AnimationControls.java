/*
 * Copyright (C) 2014 AOKP
 *
 * Modified by crDroid Android
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.android.internal.util.aicp.AwesomeAnimationHelper;

import java.util.Arrays;

public class AnimationControls extends BaseSettingsFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String ACTIVITY_OPEN = "activity_open";
    private static final String ACTIVITY_CLOSE = "activity_close";
    private static final String TASK_OPEN = "task_open";
    private static final String TASK_OPEN_BEHIND = "task_open_behind";
    private static final String TASK_CLOSE = "task_close";
    private static final String TASK_MOVE_TO_FRONT = "task_move_to_front";
    private static final String TASK_MOVE_TO_BACK = "task_move_to_back";
//    private static final String ANIMATION_NO_OVERRIDE = "animation_no_override";
    private static final String WALLPAPER_OPEN = "wallpaper_open";
    private static final String WALLPAPER_CLOSE = "wallpaper_close";
    private static final String WALLPAPER_INTRA_OPEN = "wallpaper_intra_open";
    private static final String WALLPAPER_INTRA_CLOSE = "wallpaper_intra_close";

    ListPreference mActivityOpenPref;
    ListPreference mActivityClosePref;
    ListPreference mTaskOpenPref;
    ListPreference mTaskOpenBehindPref;
    ListPreference mTaskClosePref;
    ListPreference mTaskMoveToFrontPref;
    ListPreference mTaskMoveToBackPref;
    ListPreference mWallpaperOpen;
    ListPreference mWallpaperClose;
    ListPreference mWallpaperIntraOpen;
    ListPreference mWallpaperIntraClose;
//    SwitchPreference mAnimNoOverride;

    private int[] mAnimations;
    private String[] mAnimationsStrings;
    private String[] mAnimationsNum;

    @Override
    protected int getPreferenceResource() {
        return R.xml.aokp_animation_controls;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefs = getPreferenceScreen();
        mAnimations = AwesomeAnimationHelper.getAnimationsList();
        int animqty = mAnimations.length;
        mAnimationsStrings = new String[animqty];
        mAnimationsNum = new String[animqty];
        for (int i = 0; i < animqty; i++) {
            mAnimationsStrings[i] = AwesomeAnimationHelper.getProperName(getActivity(), mAnimations[i]);
            mAnimationsNum[i] = String.valueOf(mAnimations[i]);
        }

        //mAnimNoOverride = (SwitchPreference) findPreference(ANIMATION_NO_OVERRIDE);
        //mAnimNoOverride.setChecked(Settings.Global.getBoolean(mContentRes,
        //        Settings.Global.ANIMATION_CONTROLS_NO_OVERRIDE, false));

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

        mTaskOpenBehindPref = (ListPreference) findPreference(TASK_OPEN_BEHIND);
        mTaskOpenBehindPref.setOnPreferenceChangeListener(this);
        mTaskOpenBehindPref.setSummary(getProperSummary(mTaskOpenBehindPref));
        mTaskOpenBehindPref.setEntries(mAnimationsStrings);
        mTaskOpenBehindPref.setEntryValues(mAnimationsNum);
    }

    //@Override
    //public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
    //                                     Preference preference) {
    //   if (preference == mAnimNoOverride) {
    //        Settings.Global.putBoolean(mContentRes,
    //                Settings.Global.ANIMATION_CONTROLS_NO_OVERRIDE,
    //                    mAnimNoOverride.isChecked());
    //        return true;
    //    }
    //    return false;
    //}

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        int val = Integer.parseInt((String) newValue);
        int settingIndex = getPreferenceIndex(preference);
        if (settingIndex >= 0) {
            Settings.Global.putInt(resolver, Settings.Global.ACTIVITY_ANIMATION_CONTROLS[settingIndex], val);
            preference.setSummary(mAnimationsStrings[val]);
            return true;
        }
        return false;
    }

    private int getPreferenceIndex(Preference preference) {
        int animationIndex;
        switch(preference.getKey()) {
            case ACTIVITY_OPEN:
                animationIndex = 0;
                break;
            case ACTIVITY_CLOSE:
                animationIndex = 1;
                break;
            case TASK_OPEN:
                animationIndex = 2;
                break;
            case TASK_CLOSE:
                animationIndex = 3;
                break;
            case TASK_MOVE_TO_FRONT:
                animationIndex = 4;
                break;
            case TASK_MOVE_TO_BACK:
                animationIndex = 5;
                break;
            case WALLPAPER_OPEN:
                animationIndex = 6;
                break;
            case WALLPAPER_CLOSE:
                animationIndex = 7;
                break;
            case WALLPAPER_INTRA_OPEN:
                animationIndex = 8;
                break;
            case WALLPAPER_INTRA_CLOSE:
                animationIndex = 9;
                break;
            case TASK_OPEN_BEHIND:
                animationIndex = 10;
                break;
            default:
                animationIndex = -1;
        }
        return animationIndex;
    }

    private String getProperSummary(Preference preference) {
        String mString = Settings.Global.ACTIVITY_ANIMATION_CONTROLS[getPreferenceIndex(preference)];
        int mNum = Settings.Global.getInt(getActivity().getContentResolver(), mString, 0);
        return mAnimationsStrings[mNum];
    }
}
