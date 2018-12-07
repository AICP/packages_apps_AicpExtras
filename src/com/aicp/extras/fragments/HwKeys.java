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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.provider.Settings;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.android.internal.util.aicp.DeviceUtils;
import com.android.internal.utils.ActionUtils;

public class HwKeys extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {
    // category keys
    private static final String CATEGORY_POWER = "power_key";
    private static final String CATEGORY_VOLUME = "volume_keys";
    private static final String CATEGORY_BACKLIGHT = "button_lights_key";

    private static final String KEY_TORCH_LONG_PRESS_POWER_GESTURE =
            "torch_long_press_power_gesture";
    private static final String KEY_TORCH_LONG_PRESS_POWER_TIMEOUT =
            "torch_long_press_power_timeout";
    private static final String KEY_VOLUME_ROCKER_WAKE_SCREEN =
            "volrocker_wake_screen";

    // Masks for checking presence of hardware keys.
    // Must match values in core/res/res/values/config.xml
    private static final int KEY_MASK_HOME = 0x01;
    private static final int KEY_MASK_BACK = 0x02;
    private static final int KEY_MASK_MENU = 0x04;
    private static final int KEY_MASK_ASSIST = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;
    private static final int KEY_MASK_CAMERA = 0x20;
    private static final int KEY_MASK_VOLUME = 0x40;

    private static final int BUTTON_BRIGHTNESS_DEFAULT = 180;

    private SwitchPreference mTorchLongPressPowerGesture;
    private ListPreference mTorchLongPressPowerTimeout;

    private boolean mNavbarVisible = false;
    private int mButtonBrightness;
    private int mDeviceHardwareKeys;
    private int mDeviceHardwareWakeKeys;

    @Override
    protected int getPreferenceResource() {
        return R.xml.hw_keys;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getContentResolver();
        PreferenceScreen prefScreen = getPreferenceScreen();

        mDeviceHardwareKeys = getActivity().getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);
        mDeviceHardwareWakeKeys = getActivity().getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareWakeKeys);
        final boolean hasPowerKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER);

        // Read HW Device buttons.
        final boolean hasHome = (mDeviceHardwareKeys & KEY_MASK_HOME) != 0;
        final boolean hasAppSwitch = (mDeviceHardwareKeys & KEY_MASK_APP_SWITCH) != 0;
        final boolean hasBack = (mDeviceHardwareKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenu = (mDeviceHardwareKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssist = (mDeviceHardwareKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasCamera = (mDeviceHardwareKeys & KEY_MASK_CAMERA) != 0;

        final boolean hasVolumeRockerKey = (mDeviceHardwareWakeKeys & KEY_MASK_VOLUME) != 0;

        final PreferenceCategory powerCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_POWER);
        final PreferenceCategory volumeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_VOLUME);
        final PreferenceCategory backlightCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACKLIGHT);

        // Long press power while display is off to activate torchlight
        mTorchLongPressPowerGesture =
                (SwitchPreference) findPreference(KEY_TORCH_LONG_PRESS_POWER_GESTURE);
        final int torchLongPressPowerTimeout = Settings.System.getInt(resolver,
                Settings.System.TORCH_LONG_PRESS_POWER_TIMEOUT, 0);
        mTorchLongPressPowerTimeout = initList(KEY_TORCH_LONG_PRESS_POWER_TIMEOUT,
                torchLongPressPowerTimeout);

        if (hasPowerKey) {
            if (!DeviceUtils.deviceSupportsFlashLight(getActivity())) {
                powerCategory.removePreference(mTorchLongPressPowerGesture);
                powerCategory.removePreference(mTorchLongPressPowerTimeout);
            }
        } else {
            prefScreen.removePreference(powerCategory);
        }

        if (!hasVolumeRockerKey) {
            prefScreen.removePreference(volumeCategory);
        }

        mNavbarVisible = Settings.Secure.getIntForUser(resolver,
                        Settings.Secure.NAVIGATION_BAR_VISIBLE,
                        ActionUtils.hasNavbarByDefault(getActivity()) ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        mButtonBrightness =  Settings.System.getInt(resolver,
                        Settings.System.BUTTON_BRIGHTNESS, BUTTON_BRIGHTNESS_DEFAULT);
        final boolean buttonBrightnessEnabled = Settings.System.getIntForUser(resolver,
                Settings.System.BUTTON_BRIGHTNESS_ENABLED, 0, UserHandle.USER_CURRENT) == 1;

        if (hasNavigationBar()) {  // we have navbar, turn lights off, remove lights category
            Settings.System.putInt(resolver,
                    Settings.System.BUTTON_BRIGHTNESS_ENABLED, 0);
            prefScreen.removePreference(backlightCategory);
        } else {
            if (mDeviceHardwareKeys != 0) {  // dont have navbar but have hwkeys, turn lights on
                Settings.System.putInt(resolver,
                        Settings.System.BUTTON_BRIGHTNESS_ENABLED, 1);
            } else { // we have no navbar, no hwkeys, only gesture navigation possibly, no lights
                Settings.System.putInt(resolver,
                        Settings.System.BUTTON_BRIGHTNESS_ENABLED, 0);
                prefScreen.removePreference(backlightCategory);
            }
        }
    }

    private ListPreference initList(String key, int value) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(ListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        Settings.System.putInt(getContentResolver(), setting, Integer.valueOf(value));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTorchLongPressPowerTimeout) {
            handleListChange(mTorchLongPressPowerTimeout, newValue,
                    Settings.System.TORCH_LONG_PRESS_POWER_TIMEOUT);
            return true;
        }
        return false;
    }

    public boolean hasNavigationBar() {
        return mNavbarVisible;
    }

}
