/*
 * Copyright (C) 2018 AICP
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
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v14.preference.SwitchPreference;

import com.aicp.extras.R;

import com.android.internal.utils.du.ActionConstants;
import com.android.internal.utils.du.DUActionUtils;

import com.aicp.gear.preference.SeekBarPreferenceCham;

public class HwKeys extends ActionFragment implements Preference.OnPreferenceChangeListener {

    private static final String HWKEY_DISABLE = "hardware_keys_disable";

    // category keys
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_POWER = "power_key";
    private static final String KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new";
    private static final String KEY_BUTTON_TIMEOUT = "button_timeout";
    private static final String KEY_BUTON_BACKLIGHT_OPTIONS = "button_backlight_options_category";
    private static final String KEY_ACCIDENTAL_TOUCH = "anbi_enabled";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    private SwitchPreference mHwKeyDisable;

    private SeekBarPreferenceCham mButtonTimoutBar;
    private SeekBarPreferenceCham mManualButtonBrightness;
    private PreferenceCategory mButtonBackLightCategory;
    private SwitchPreference mAccidentalTouch;

    @Override
    protected int getPreferenceResource() {
        return R.xml.hw_keys;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getContentResolver();

        mManualButtonBrightness = (SeekBarPreferenceCham) findPreference(
                KEY_BUTTON_MANUAL_BRIGHTNESS_NEW);
        final int customButtonBrightness = getResources().getInteger(
                com.android.internal.R.integer.config_button_brightness_default);
        final int currentBrightness = Settings.System.getInt(resolver,
                Settings.System.CUSTOM_BUTTON_BRIGHTNESS, customButtonBrightness);
        PowerManager pm = (PowerManager)getActivity().getSystemService(Context.POWER_SERVICE);
        mManualButtonBrightness.setMax(pm.getMaximumScreenBrightnessSetting());
        mManualButtonBrightness.setValue(currentBrightness);
        mManualButtonBrightness.setDefaultValue(customButtonBrightness);
        mManualButtonBrightness.setOnPreferenceChangeListener(this);

        mButtonTimoutBar = (SeekBarPreferenceCham) findPreference(KEY_BUTTON_TIMEOUT);
        int currentTimeout = Settings.System.getInt(resolver,
                Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0);
        mButtonTimoutBar.setValue(currentTimeout);
        mButtonTimoutBar.setOnPreferenceChangeListener(this);

        // Prevent accidental touch to hw keys.
        mAccidentalTouch = (SwitchPreference) findPreference(KEY_ACCIDENTAL_TOUCH);
        int deviceHardwareKeys = getActivity().getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        if (deviceHardwareKeys == 0 || deviceHardwareKeys == 64) {
            mAccidentalTouch.getParent().removePreference(mAccidentalTouch);
        }

        final boolean enableBacklightOptions = getResources().getBoolean(
                com.android.internal.R.bool.config_button_brightness_support);

        mButtonBackLightCategory = (PreferenceCategory) findPreference(KEY_BUTON_BACKLIGHT_OPTIONS);

        if (!enableBacklightOptions) {
            mButtonBackLightCategory.getParent().removePreference(mButtonBackLightCategory);
        }

        final boolean needsNavbar = DUActionUtils.hasNavbarByDefault(getActivity());
        int keysDisabled = 0;
        mHwKeyDisable = (SwitchPreference) findPreference(HWKEY_DISABLE);
        if (!needsNavbar) {
            keysDisabled = Settings.Secure.getIntForUser(getContentResolver(),
                    Settings.Secure.HARDWARE_KEYS_DISABLE, 0,
                    UserHandle.USER_CURRENT);
            mHwKeyDisable.setChecked(keysDisabled != 0);
            mHwKeyDisable.setOnPreferenceChangeListener(this);
        } else {
            mHwKeyDisable.getParent().removePreference(mHwKeyDisable);
        }

        // bits for hardware keys present on device
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        final int deviceWakeKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);

        // read bits for present hardware keys
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        final boolean showHomeWake = (deviceWakeKeys & KEY_MASK_HOME) != 0;
        final boolean showBackWake = (deviceWakeKeys & KEY_MASK_BACK) != 0;
        final boolean showMenuWake = (deviceWakeKeys & KEY_MASK_MENU) != 0;
        final boolean showAssistWake = (deviceWakeKeys & KEY_MASK_ASSIST) != 0;
        final boolean showAppSwitchWake = (deviceWakeKeys & KEY_MASK_APP_SWITCH) != 0;

        // load categories and init/remove preferences based on device
        // configuration
        final PreferenceCategory backCategory = (PreferenceCategory)
                findPreference(CATEGORY_BACK);
        final PreferenceCategory homeCategory = (PreferenceCategory)
                findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory = (PreferenceCategory)
                findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory = (PreferenceCategory)
                findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory = (PreferenceCategory)
                findPreference(CATEGORY_APPSWITCH);

        // back key
        if (hasBackKey) {
            if (!showBackWake) {
                backCategory.removePreference(findPreference(Settings.System.BACK_WAKE_SCREEN));
            }
        } else {
            backCategory.getParent().removePreference(backCategory);
        }

        // home key
        if (hasHomeKey) {
            if (!showHomeWake) {
                homeCategory.removePreference(findPreference(Settings.System.HOME_WAKE_SCREEN));
            }
        } else {
            homeCategory.getParent().removePreference(homeCategory);
        }

        // App switch key (recents)
        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) {
                appSwitchCategory.removePreference(findPreference(
                        Settings.System.APP_SWITCH_WAKE_SCREEN));
            }
        } else {
            appSwitchCategory.getParent().removePreference(appSwitchCategory);
        }

        // menu key
        if (hasMenuKey) {
            if (!showMenuWake) {
                menuCategory.removePreference(findPreference(Settings.System.MENU_WAKE_SCREEN));
            }
        } else {
            menuCategory.getParent().removePreference(menuCategory);
        }

        // search/assist key
        if (hasAssistKey) {
            if (!showAssistWake) {
                assistCategory.removePreference(findPreference(Settings.System.ASSIST_WAKE_SCREEN));
            }
        } else {
            assistCategory.getParent().removePreference(assistCategory);
        }

        // let super know we can load ActionPreferences
        onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.HWKEYS));

        // load preferences first
        setActionPreferencesEnabled(keysDisabled == 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mHwKeyDisable) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.HARDWARE_KEYS_DISABLE,
                    value ? 1 : 0);
            setActionPreferencesEnabled(!value);
            return true;
        } else if (preference == mButtonTimoutBar) {
            int buttonTimeout = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout);
            return true;
        } else if (preference == mManualButtonBrightness) {
            int buttonBrightness = (Integer) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_BRIGHTNESS, buttonBrightness);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean usesExtendedActionsList() {
        return true;
    }
}
