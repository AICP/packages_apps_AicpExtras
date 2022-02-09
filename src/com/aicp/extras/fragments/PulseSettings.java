/*
 * Copyright (C) 2015 The Dirty Unicorns Project
 * Copyright (C) 2019-2020 AICP
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

import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import android.provider.Settings;
import android.util.TypedValue;
import android.widget.CompoundButton;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.preference.SecureSettingMasterSwitchPreference;
import com.aicp.extras.R;
import com.aicp.gear.preference.SecureSettingColorPickerPreference;
import com.aicp.gear.preference.SecureSettingIntListPreference;
import com.aicp.gear.preference.SecureSettingSeekBarPreference;
import com.aicp.gear.preference.SecureSettingSwitchPreference;

public class PulseSettings extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = PulseSettings.class.getSimpleName();

    private static final String CATEGORY_PULSE_FADING_BARS = "pulse_fading_bars_category";
    private static final String CATEGORY_PULSE_2 = "pulse_2";
    private static final String CATEGORY_PULSE_COLOR = "pulse_color_category";
    private static final String CATEGORY_PULSE_RENDER_MODE = "pulse_render_mode_category";

    private static final String KEY_RENDER_MODE_FADING_LINES = "pulse_render_mode_fading_lines";
    private static final String KEY_RENDER_MODE_FADING_BLOCKS = "pulse_render_mode_fading_blocks";

    private static final String PULSE_RENDER_MODE_KEY = "navbar_pulse_render_style";
    private static final String PULSE_COLOR_MODE_KEY = "navbar_pulse_color_type";
    private static final String PULSE_COLOR_MODE_CHOOSER_KEY = "navbar_pulse_color_user";
    private static final String PULSE_COLOR_MODE_LAVA_SPEED_KEY = "navbar_pulse_lavalamp_speed";

    private static final String KEY_NAVBAR_PULSE = "navbar_pulse_enabled";
    private static final String KEY_LOCKSCREEN_PULSE = "lockscreen_pulse_enabled";
    private static final String KEY_AMBIENTDISPLAY_PULSE = "ambient_pulse_enabled";

    private static final int COLOR_TYPE_ACCENT = 0;
    private static final int COLOR_TYPE_USER = 1;
    private static final int COLOR_TYPE_LAVALAMP = 2;
    private static final int COLOR_TYPE_AUTO = 3;

    private static final int RENDER_STYLE_FADING_BARS = 0;
    private static final int RENDER_STYLE_SOLID_LINES = 1;

    private SecureSettingColorPickerPreference mColorPickerPref;
    private SecureSettingIntListPreference mRenderMode;
    private SecureSettingIntListPreference mColorModePref;
    private SecureSettingMasterSwitchPreference mRenderModeBlocks;
    private SecureSettingMasterSwitchPreference mRenderModeLines;
    private SecureSettingSeekBarPreference mLavaSpeedPref;
    private SwitchPreference mNavBarPulse;
    private SwitchPreference mLockscreenPulse;
    private SwitchPreference mAmbientDisplayPulse;
    private PreferenceCategory mPulseColorCategory;
    private PreferenceCategory mPulseRenderModeCategory;

    @Override
    protected int getPreferenceResource() {
        return R.xml.pulse_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mColorModePref = (SecureSettingIntListPreference) findPreference(PULSE_COLOR_MODE_KEY);
        mLavaSpeedPref = (SecureSettingSeekBarPreference) findPreference(PULSE_COLOR_MODE_LAVA_SPEED_KEY);
        mColorPickerPref = (SecureSettingColorPickerPreference) findPreference(PULSE_COLOR_MODE_CHOOSER_KEY);
        mColorModePref.setOnPreferenceChangeListener(this);
        int colorMode = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_COLOR_TYPE, COLOR_TYPE_ACCENT, UserHandle.USER_CURRENT);
        updateColorPrefs(colorMode);

        int renderMode = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_RENDER_STYLE_URI, RENDER_STYLE_SOLID_LINES, UserHandle.USER_CURRENT);
        mRenderModeBlocks = (SecureSettingMasterSwitchPreference) findPreference(KEY_RENDER_MODE_FADING_BLOCKS);
        mRenderModeBlocks.setChecked(renderMode == 0);
        mRenderModeBlocks.setOnPreferenceChangeListener(this);
        mRenderModeLines = (SecureSettingMasterSwitchPreference) findPreference(KEY_RENDER_MODE_FADING_LINES);
        mRenderModeLines.setChecked(renderMode != 0);
        mRenderModeLines.setOnPreferenceChangeListener(this);

        mPulseColorCategory = (PreferenceCategory) findPreference(CATEGORY_PULSE_COLOR);
        mPulseRenderModeCategory = (PreferenceCategory) findPreference(CATEGORY_PULSE_RENDER_MODE);

        mNavBarPulse = (SwitchPreference) findPreference(KEY_NAVBAR_PULSE);
        mNavBarPulse.setOnPreferenceClickListener(this);
        mLockscreenPulse = (SwitchPreference) findPreference(KEY_LOCKSCREEN_PULSE);
        mLockscreenPulse.setOnPreferenceClickListener(this);
        mAmbientDisplayPulse = (SwitchPreference) findPreference(KEY_AMBIENTDISPLAY_PULSE);
        mAmbientDisplayPulse.setOnPreferenceClickListener(this);
        updateDependentCategories();
    }

    private void updateDependentCategories() {
        int pulseLocations = 0;
        if (!mNavBarPulse.isChecked()) pulseLocations += 1;
        if (!mLockscreenPulse.isChecked()) pulseLocations += 1;
        if (!mAmbientDisplayPulse.isChecked()) pulseLocations += 1;
        mPulseColorCategory.setEnabled(pulseLocations < 3 ? true : false);
        mPulseRenderModeCategory.setEnabled(pulseLocations < 3 ? true : false);
    }

    private void updateColorPrefs(int val) {
        switch (val) {
            case COLOR_TYPE_ACCENT:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(false);
                break;
            case COLOR_TYPE_USER:
                mColorPickerPref.setEnabled(true);
                mLavaSpeedPref.setEnabled(false);
                break;
            case COLOR_TYPE_LAVALAMP:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(true);
                break;
            case COLOR_TYPE_AUTO:
                mColorPickerPref.setEnabled(false);
                mLavaSpeedPref.setEnabled(false);
                break;
        }
    }

    private void updateRenderMode(){
        int renderMode = 1;
        if (!mRenderModeLines.isChecked() && mRenderModeBlocks.isChecked()) {
            renderMode = 0;
        }
        Settings.Secure.putIntForUser(getContentResolver(),
              Settings.Secure.PULSE_RENDER_STYLE_URI, renderMode,
              UserHandle.USER_CURRENT);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(mRenderModeLines) || preference.equals(mRenderModeBlocks)) {
            updateRenderMode();
            return true;
        } else if (preference.equals(mColorModePref)) {
            updateColorPrefs(Integer.valueOf(String.valueOf(newValue)));
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.equals(mNavBarPulse) ||
                  preference.equals(mLockscreenPulse) ||
                  preference.equals(mAmbientDisplayPulse)) {
            updateDependentCategories();
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.PULSE_ENABLED, 0, UserHandle.USER_CURRENT) != 0) {
            int colorMode = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.PULSE_COLOR_TYPE, COLOR_TYPE_ACCENT, UserHandle.USER_CURRENT);
            updateColorPrefs(colorMode);
            updateDependentCategories();
        }
    }
}
