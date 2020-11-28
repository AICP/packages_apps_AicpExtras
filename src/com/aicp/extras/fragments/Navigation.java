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

import com.android.internal.util.aicp.AicpUtils;
import com.android.internal.util.aicp.DeviceUtils;
import com.android.internal.util.hwkeys.ActionConstants;
import com.android.internal.util.hwkeys.ActionUtils;

import android.util.Log;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY;

public class Navigation extends BaseSettingsFragment
          implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AENavigation";
    private static final boolean DEBUG = false;

    // preference keys
    private static final String KEY_BUTTON_BRIGHTNESS = "button_brightness";
    private static final String KEY_BUTTON_TIMEOUT = "button_backlight_timeout";
    private static final String KEY_HWKEY_DISABLE = "hardware_keys_disable";
    private static final String KEY_NAVIGATION_BAR_ENABLED = "navigation_bar_show_new";
    private static final String KEY_SWAP_HW_NAVIGATION_KEYS = "swap_navigation_keys";

    private static final String CATEGORY_BUTTON_BACKLIGHT_OPTIONS = "button_backlight_options_category";
    private static final String CATEGORY_HWKEYS = "hardware_keys";
    private static final String CATEGORY_WAKEKEYS = "wake_keys";
    private static final String PREFSCREEN_HWBUTTON_SETTINGS= "hw_button_settings";
//    private static final String CATEGORY_GESTURE_NAV_TWEAKS = "gesture_nav_tweaks_category";

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
    private SwitchPreference mSwapHWNavKeys;
    private PreferenceCategory mButtonBacklightCategory;
    private PreferenceCategory mHwKeysCategory;
    private PreferenceCategory mWakeKeysCategory;
    private PreferenceScreen mHwButtonSettingsScreen;
//    private PreferenceCategory mGestureTweaksCategory;

    private SystemSettingMasterSwitchPreference mNavigationBar;
    private boolean mIsNavSwitchingMode = false;
    private boolean mHwKeysSupported;
    private boolean mNeedsNavbar;
    private boolean mNavigationBarEnabled;
    private boolean mWakeInitialized = false;
//    private boolean isGestureNavigation;

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

        mNavigationBarEnabled = Settings.System.getIntForUser(
                getContentResolver(), Settings.System.FORCE_SHOW_NAVBAR,
                mNeedsNavbar ? 1 : 0, UserHandle.USER_CURRENT) != 0;

        mNavigationBar = (SystemSettingMasterSwitchPreference) findPreference(KEY_NAVIGATION_BAR_ENABLED);
        mNavigationBar.setOnPreferenceChangeListener(this);

        // load categories and init/remove preferences based on device
        // configuration
        mButtonBacklightCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_BUTTON_BACKLIGHT_OPTIONS);
        mHwKeysCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_HWKEYS);
        mWakeKeysCategory = (PreferenceCategory) prefScreen
                .findPreference(CATEGORY_WAKEKEYS);

        mHwKeyDisable = (SwitchPreference) findPreference(KEY_HWKEY_DISABLE);
        mHwKeyDisable.setOnPreferenceChangeListener(this);
        mHwButtonSettingsScreen = (PreferenceScreen) prefScreen
                .findPreference(PREFSCREEN_HWBUTTON_SETTINGS);
        mSwapHWNavKeys = (SwitchPreference) findPreference(KEY_SWAP_HW_NAVIGATION_KEYS);

        mManualButtonBrightness = (SeekBarPreferenceCham) findPreference(
                KEY_BUTTON_BRIGHTNESS);
        mManualButtonBrightness.setOnPreferenceChangeListener(this);
        mButtonTimoutBar = (SeekBarPreferenceCham) findPreference(KEY_BUTTON_TIMEOUT);
        mButtonTimoutBar.setOnPreferenceChangeListener(this);

//        isGestureNavigation = AicpUtils.isThemeEnabled(NAV_BAR_MODE_GESTURAL_OVERLAY);
//        mGestureTweaksCategory = (PreferenceCategory) findPreference(CATEGORY_GESTURE_NAV_TWEAKS);
/*
        if (!isGestureNavigation) {
            mGestureTweaksCategory.getParent().removePreference(mGestureTweaksCategory);
        }*/

        initializeHWKeysCategory();
        initializeWakeCategory();
        initializeButtonBacklightCategory();

        mHandler = new Handler();
    }

    private boolean isKeysDisabled() {
        return Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.HARDWARE_KEYS_DISABLE, mNeedsNavbar ? 1 : 0,
                UserHandle.USER_CURRENT) != 0;
    }

    private void setKeysDisabled(boolean value) {
        Settings.Secure.putInt(getContentResolver(),
            Settings.Secure.HARDWARE_KEYS_DISABLE, value ? 1 : 0);
    }

    private void initializeHWKeysCategory(){
        if (mHwKeysSupported && !mNeedsNavbar && !mNavigationBarEnabled) {
            mHwKeyDisable.setChecked(isKeysDisabled());
            enableHardwareItems(!isKeysDisabled());
            updateCategoryVisibility(mHwKeysCategory, true);
        } else {
            updateCategoryVisibility(mHwKeysCategory, false);
        }
    }

    private void initializeWakeCategory() {
        if (!mHwKeysSupported || isKeysDisabled() || mNavigationBarEnabled) {
            updateCategoryVisibility(mWakeKeysCategory, false);
        } else {
            if (mWakeKeysCategory != null && !mWakeInitialized) {
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

                if (wakePrefRemoved == 6 && mWakeKeysCategory != null) {
                    mWakeKeysCategory.getParent().removePreference(mWakeKeysCategory);
                } else {
                    updateCategoryVisibility(mWakeKeysCategory, true);
                }
                mWakeInitialized = true;
            }
        }
    }

    private void initializeButtonBacklightCategory() {
        final boolean enableBacklightOptions = getResources().getBoolean(
                com.android.internal.R.bool.config_deviceHasVariableButtonBrightness);
        if (mHwKeysSupported && enableBacklightOptions && !mNavigationBarEnabled){
            final float customDefaultButtonBrightness = getResources().getFloat(
                    com.android.internal.R.dimen.config_buttonBrightnessSettingDefaultFloat);
            if (DEBUG) Log.d(TAG, "customDefaultButtonBrightness: " + customDefaultButtonBrightness);
            final float currentBrightness = Settings.System.getFloat(getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, customDefaultButtonBrightness);
            if (DEBUG) Log.d(TAG, "currentBrightness: " + currentBrightness);
            PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            final float buttonBrightnessSettingDefault = pm.getBrightnessConstraint(
                    PowerManager.BRIGHTNESS_CONSTRAINT_TYPE_DEFAULT_BUTTON);
            if (DEBUG) Log.d(TAG, "pm: buttonBrightnessSettingDefault: " + buttonBrightnessSettingDefault);
            final float screenBrightnessSettingMinimum = pm.getBrightnessConstraint(
                    PowerManager.BRIGHTNESS_CONSTRAINT_TYPE_MINIMUM);
            if (DEBUG) Log.d(TAG, "pm: screenBrightnessSettingMinimum: " + screenBrightnessSettingMinimum);
            final float screenBrightnessSettingMaximum = pm.getBrightnessConstraint(
                    PowerManager.BRIGHTNESS_CONSTRAINT_TYPE_MAXIMUM);
            if (DEBUG) Log.d(TAG, "pm: screenBrightnessSettingMaximum: " + screenBrightnessSettingMaximum);

            mManualButtonBrightness.setMax((int)(screenBrightnessSettingMaximum*100));
            mManualButtonBrightness.setMin((int)screenBrightnessSettingMinimum);
            mManualButtonBrightness.setValue((int)(currentBrightness*100));
            mManualButtonBrightness.setDefaultValue((int)(buttonBrightnessSettingDefault*100));

            int currentTimeout = Settings.System.getInt(getContentResolver(),
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0) / 1000;
            mButtonTimoutBar.setValue(currentTimeout);
            updateCategoryVisibility(mButtonBacklightCategory, true);
        } else {
            updateCategoryVisibility(mButtonBacklightCategory, false);
        }
    }

    private void updateCategoryVisibility(PreferenceCategory category, boolean visible) {
        if (category != null) category.setVisible(visible);
    }

    private void updateCategoryState(PreferenceCategory category, boolean enable) {
        if (category != null) category.setEnabled(enable);
    }

    private void updateHardwareCategories(boolean visible) {
        updateCategoryVisibility(mHwKeysCategory, visible);
        updateCategoryVisibility(mWakeKeysCategory, visible);
        updateCategoryVisibility(mButtonBacklightCategory, visible);
    }

    private void enableHardwareItems(boolean enable) {
        updateCategoryState(mWakeKeysCategory, enable);
        updateCategoryState(mButtonBacklightCategory, enable);
        mHwButtonSettingsScreen.setEnabled(enable);
        mSwapHWNavKeys.setEnabled(enable);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getContentResolver();
        if (preference == mNavigationBar) {
            boolean value = (Boolean) objValue;
            mNavigationBarEnabled = value;
            updateHardwareCategories(!value && mHwKeysSupported);
            setKeysDisabled(value);
            initializeWakeCategory();
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
            setKeysDisabled(value);
            enableHardwareItems(!value);
            return true;
        } else if (preference == mButtonTimoutBar) {
            int buttonTimeout = 1000 * (int) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout);
            return true;
        } else if (preference == mManualButtonBrightness) {
            int buttonBrightness = (int) objValue;
            Settings.System.putFloat(resolver,
                    Settings.System.BUTTON_BRIGHTNESS, buttonBrightness/100f);
            return true;
        }
        return false;
    }
}
