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
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.preference.SystemSettingMasterSwitchPreference;
import com.aicp.gear.preference.SeekBarPreferenceCham;

import com.android.internal.util.aicp.DeviceUtils;
import com.android.internal.util.hwkeys.ActionConstants;
import com.android.internal.util.hwkeys.ActionUtils;

public class Navigation extends BaseSettingsFragment implements
            Preference.OnPreferenceChangeListener {

    private static final String KEY_KILLAPP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String KEY_SWAP_HW_NAVIGATION_KEYS = "swap_navigation_keys";
    private static final String KEY_NAVIGATION_BAR_ENABLED = "navigation_bar_show_new";
    // preference keys
    private static final String KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new";
    private static final String KEY_BUTTON_TIMEOUT = "button_timeout";
    private static final String KEY_BUTTON_BACKLIGHT_OPTIONS = "button_backlight_options_category";
    private static final String KEY_HWKEY_DISABLE = "hardware_keys_disable";

    private static final String CATEGORY_HWKEY = "hardware_keys";
    private static final String CATEGORY_WAKE = "wake_keys";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;

    private SeekBarPreferenceCham mButtonTimoutBar;
    private SeekBarPreferenceCham mManualButtonBrightness;

    private SwitchPreference mHwKeyDisable;
    private SwitchPreference mLongPressBackToKill;
    private SwitchPreference mSwapHWNavKeys;
    private PreferenceCategory mHwKeyCategory;
    private PreferenceCategory mWakeKeysCategory;

    private SystemSettingMasterSwitchPreference mNavigationBar;
    private boolean mIsNavSwitchingMode = false;
    private boolean mHwKeysSupported;
    private boolean mNeedsNavbar;

    private Handler mHandler;

    @Override
    protected int getPreferenceResource() {
        return R.xml.navigation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();

        mNeedsNavbar = ActionUtils.hasNavbarByDefault(getActivity());
        mHwKeysSupported = ActionUtils.isHWKeysSupported(getActivity());

        mHwKeyCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_HWKEY);
        mLongPressBackToKill = (SwitchPreference) findPreference(KEY_KILLAPP_LONGPRESS_BACK);
        mSwapHWNavKeys = (SwitchPreference) findPreference(KEY_SWAP_HW_NAVIGATION_KEYS);
        mHwKeyDisable = (SwitchPreference) findPreference(KEY_HWKEY_DISABLE);
        mHwKeyDisable.setOnPreferenceChangeListener(this);

        final boolean navigationBarEnabled = Settings.System.getIntForUser(
                getContentResolver(), Settings.System.FORCE_SHOW_NAVBAR,
                mNeedsNavbar ? 1 : 0, UserHandle.USER_CURRENT) != 0;

        mManualButtonBrightness = (SeekBarPreferenceCham) findPreference(
                KEY_BUTTON_MANUAL_BRIGHTNESS_NEW);
        mManualButtonBrightness.setOnPreferenceChangeListener(this);
        mButtonTimoutBar = (SeekBarPreferenceCham) findPreference(KEY_BUTTON_TIMEOUT);
        mButtonTimoutBar.setOnPreferenceChangeListener(this);

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
        final boolean hasCameraKey = (deviceKeys & KEY_MASK_CAMERA) != 0;

        final boolean showHomeWake = (deviceWakeKeys & KEY_MASK_HOME) != 0;
        final boolean showBackWake = (deviceWakeKeys & KEY_MASK_BACK) != 0;
        final boolean showMenuWake = (deviceWakeKeys & KEY_MASK_MENU) != 0;
        final boolean showAssistWake = (deviceWakeKeys & KEY_MASK_ASSIST) != 0;
        final boolean showAppSwitchWake = (deviceWakeKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean showCameraWake = (deviceWakeKeys & KEY_MASK_CAMERA) != 0;

        // load categories and init/remove preferences based on device
        // configuration
        mWakeKeysCategory = (PreferenceCategory) prefScreen.findPreference(CATEGORY_WAKE);

        if (!mHwKeysSupported && keysDisabled()) {
            updateWakeVisibility(false);
        } else {
            int wakePrefRemoved = 0;
            // back key
            if (!hasBackKey || !showBackWake ) {
                mWakeKeysCategory.removePreference(findPreference(Settings.System.BACK_WAKE_SCREEN));
                wakePrefRemoved += 1;
            }

            // home key
            if (!hasHomeKey || !showHomeWake) {
                mWakeKeysCategory.removePreference(findPreference(Settings.System.HOME_WAKE_SCREEN));
                wakePrefRemoved += 1;
            }

            // App switch key (recents)
            if (!hasAppSwitchKey || !showAppSwitchWake) {
                mWakeKeysCategory.removePreference(findPreference(
                            Settings.System.APP_SWITCH_WAKE_SCREEN));
                wakePrefRemoved += 1;
            }

            // menu key
            if (!hasMenuKey || !showMenuWake) {
                mWakeKeysCategory.removePreference(findPreference(Settings.System.MENU_WAKE_SCREEN));
                wakePrefRemoved += 1;
            }

            // search/assist key
            if (!hasAssistKey || !showAssistWake) {
                mWakeKeysCategory.removePreference(findPreference(Settings.System.ASSIST_WAKE_SCREEN));
                wakePrefRemoved += 1;
            }

            // camera key
            if (!hasCameraKey || !showCameraWake) {
                mWakeKeysCategory.removePreference(findPreference(Settings.System.CAMERA_WAKE_SCREEN));
                wakePrefRemoved += 1;
            }

            if (wakePrefRemoved == 6) {
                mWakeKeysCategory.getParent().removePreference(mWakeKeysCategory);
            } else {
                mWakeKeysCategory.setVisible(true);
            }
        }
        if (navigationBarEnabled) {
            updateHWKeysVisibility(false);
            updateDependents(false);
        } else if (!keysDisabled()) {
            updateHWKeysVisibility(true);
            updateDependents(true);
        } else {
            updateDependents(false);
        }

        mNavigationBar = (SystemSettingMasterSwitchPreference) findPreference(KEY_NAVIGATION_BAR_ENABLED);
        mNavigationBar.setOnPreferenceChangeListener(this);
        mHandler = new Handler();
    }

    private void updateDependents(boolean enabled) {
        updateWakeVisibility(enabled);
        updateButtonBacklight(enabled);
        mLongPressBackToKill.setEnabled(enabled);
        mSwapHWNavKeys.setEnabled(enabled);
    }

    private void updateHWKeysVisibility(boolean enabled) {
        if (mHwKeysSupported && (!mNeedsNavbar || enabled)) {
            mHwKeyDisable.setChecked(keysDisabled());
            mHwKeyCategory.setVisible(true);
        } else {
            mHwKeyCategory.setVisible(false);
        }
    }

    private boolean keysDisabled() {
        boolean areKeysDisabled = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.HARDWARE_KEYS_DISABLE, mNeedsNavbar ? 1 : 0,
                UserHandle.USER_CURRENT) != 0;
        return areKeysDisabled;
    }

    private void updateWakeVisibility(boolean visible) {
        if (mWakeKeysCategory != null) mWakeKeysCategory.setVisible(visible && !keysDisabled());
    }

    private void updateButtonBacklight(boolean enabled) {
        final PreferenceCategory buttonBackLightCategory = (PreferenceCategory)
                  findPreference(KEY_BUTTON_BACKLIGHT_OPTIONS);
        final boolean enableBacklightOptions = getResources().getBoolean(
                com.android.internal.R.bool.config_button_brightness_support);
        if (mHwKeysSupported && enableBacklightOptions && enabled){
            final int customButtonBrightness = getResources().getInteger(
                    com.android.internal.R.integer.config_button_brightness_default);
            final int currentBrightness = Settings.System.getInt(getContentResolver(),
                    Settings.System.CUSTOM_BUTTON_BRIGHTNESS, customButtonBrightness);
            PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            mManualButtonBrightness.setMax(pm.getMaximumScreenBrightnessSetting());
            mManualButtonBrightness.setValue(currentBrightness);
            mManualButtonBrightness.setDefaultValue(customButtonBrightness);

            int currentTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0);
            mButtonTimoutBar.setValue(currentTimeout);
            buttonBackLightCategory.setVisible(true);
        } else {
            buttonBackLightCategory.setVisible(false);
        }
    }
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getContentResolver();
        if (preference == mNavigationBar) {
            boolean value = (Boolean) objValue;
            updateHWKeysVisibility(!value);
            updateDependents(!value);
/*            updateButtonBacklight(!value);
            updateWakeVisibility(!value);*/
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);
            return true;
        } else if (preference == mHwKeyDisable) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(resolver,
                    Settings.Secure.HARDWARE_KEYS_DISABLE, value ? 1 : 0);
            updateDependents(!value);
            return true;
        } else if (preference == mButtonTimoutBar) {
            int buttonTimeout = (Integer) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout);
            return true;
        } else if (preference == mManualButtonBrightness) {
            int buttonBrightness = (Integer) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.CUSTOM_BUTTON_BRIGHTNESS, buttonBrightness);
            return true;
        }
        return false;
    }
}
