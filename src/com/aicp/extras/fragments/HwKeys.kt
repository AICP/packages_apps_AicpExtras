/*
 * Copyright (C) 2018-2026 AICP
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

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.android.internal.util.aicp.DeviceUtils
//import com.android.internal.util.hwkeys.ActionConstants
//import com.android.internal.util.hwkeys.ActionUtils

class HwKeys : BaseSettingsFragment() /*, Preference.OnPreferenceChangeListener*/ {

    /*
    private val CATEGORY_CAMERA = "camera_key"
    val KEY_MASK_CAMERA = 0x20

    private val CATEGORY_HWKEY = "hardware_keys"
    private val CATEGORY_BACK = "back_key"
    private val CATEGORY_HOME = "home_key"
    private val CATEGORY_MENU = "menu_key"
    private val CATEGORY_ASSIST = "assist_key"
    private val CATEGORY_APPSWITCH = "app_switch_key"
    private val CATEGORY_VOLUME = "volume_keys"

    private val KEY_BUTTON_MANUAL_BRIGHTNESS_NEW = "button_manual_brightness_new"
    private val KEY_BUTTON_TIMEOUT = "button_timeout"
    private val KEY_BUTTON_BACKLIGHT_OPTIONS = "button_backlight_options_category"
    private val KEY_HWKEY_DISABLE = "hardware_keys_disable"

    val KEY_MASK_HOME = 0x01
    val KEY_MASK_BACK = 0x02
    val KEY_MASK_MENU = 0x04
    val KEY_MASK_ASSIST = 0x08
    val KEY_MASK_APP_SWITCH = 0x10
    val KEY_MASK_VOLUME = 0x40

    private var mButtonTimoutBar: SeekBarPreferenceCham? = null
    private var mManualButtonBrightness: SeekBarPreferenceCham? = null
    private var mButtonBackLightCategory: PreferenceCategory? = null

    private var mHwKeyDisable: SwitchPreference? = null
    */

    override fun getPreferenceResource(): Int {
        return R.xml.hw_keys
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bits for hardware keys present on device
        val deviceKeys = resources.getInteger(com.android.internal.R.integer.config_deviceHardwareKeys)
        val deviceWakeKeys = resources.getInteger(com.android.internal.R.integer.config_deviceHardwareWakeKeys)

        val hasCameraKey = (deviceKeys and KEY_MASK_CAMERA) != 0
        val showCameraWake = (deviceWakeKeys and KEY_MASK_CAMERA) != 0
        val cameraCategory = findPreference<PreferenceCategory>(CATEGORY_CAMERA)

        // Camera button
        if (hasCameraKey) {
            if (!showCameraWake) {
                cameraCategory?.removePreference(findPreference(Settings.System.CAMERA_WAKE_SCREEN))
            }
        } else {
            cameraCategory?.parent?.removePreference(cameraCategory)
        }

        val resolver: ContentResolver = contentResolver
        val prefScreen: PreferenceScreen = preferenceScreen

        val needsNavbar = ActionUtils.hasNavbarByDefault(requireActivity())
        val hwkeyCategory = prefScreen.findPreference<PreferenceCategory>(CATEGORY_HWKEY)
        var keysDisabled = 0
        mHwKeyDisable = findPreference(KEY_HWKEY_DISABLE)
        if (!needsNavbar) {
            keysDisabled = Settings.Secure.getIntForUser(
                resolver,
                Settings.Secure.HARDWARE_KEYS_DISABLE,
                0,
                UserHandle.USER_CURRENT
            )
            mHwKeyDisable?.isChecked = keysDisabled != 0
            mHwKeyDisable?.setOnPreferenceChangeListener(this)
        } else {
            prefScreen.removePreference(hwkeyCategory)
        }

        val enableBacklightOptions = resources.getBoolean(com.android.internal.R.bool.config_button_brightness_support)
        mButtonBackLightCategory = findPreference(KEY_BUTTON_BACKLIGHT_OPTIONS)
        mManualButtonBrightness = findPreference(KEY_BUTTON_MANUAL_BRIGHTNESS_NEW)
        val customButtonBrightness = resources.getInteger(com.android.internal.R.integer.config_button_brightness_default)
        val currentBrightness = Settings.System.getInt(resolver, Settings.System.CUSTOM_BUTTON_BRIGHTNESS, customButtonBrightness)
        val pm = requireActivity().getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        mManualButtonBrightness?.max = pm.maximumScreenBrightnessSetting
        mManualButtonBrightness?.value = currentBrightness
        mManualButtonBrightness?.defaultValue = customButtonBrightness
        mManualButtonBrightness?.setOnPreferenceChangeListener(this)

        mButtonTimoutBar = findPreference(KEY_BUTTON_TIMEOUT)
        val currentTimeout = Settings.System.getInt(resolver, Settings.System.BUTTON_BACKLIGHT_TIMEOUT, 0)
        mButtonTimoutBar?.value = currentTimeout
        mButtonTimoutBar?.setOnPreferenceChangeListener(this)

        if (!enableBacklightOptions) {
            mButtonBackLightCategory?.parent?.removePreference(mButtonBackLightCategory)
        }

        // Load hardware key categories
        val hasHomeKey = (deviceKeys and KEY_MASK_HOME) != 0
        val hasBackKey = (deviceKeys and KEY_MASK_BACK) != 0
        val hasMenuKey = (deviceKeys and KEY_MASK_MENU) != 0
        val hasAssistKey = (deviceKeys and KEY_MASK_ASSIST) != 0
        val hasAppSwitchKey = (deviceKeys and KEY_MASK_APP_SWITCH) != 0

        val showHomeWake = (deviceWakeKeys and KEY_MASK_HOME) != 0
        val showBackWake = (deviceWakeKeys and KEY_MASK_BACK) != 0
        val showMenuWake = (deviceWakeKeys and KEY_MASK_MENU) != 0
        val showAssistWake = (deviceWakeKeys and KEY_MASK_ASSIST) != 0
        val showAppSwitchWake = (deviceWakeKeys and KEY_MASK_APP_SWITCH) != 0

        val backCategory = prefScreen.findPreference<PreferenceCategory>(CATEGORY_BACK)
        val homeCategory = prefScreen.findPreference<PreferenceCategory>(CATEGORY_HOME)
        val menuCategory = prefScreen.findPreference<PreferenceCategory>(CATEGORY_MENU)
        val assistCategory = prefScreen.findPreference<PreferenceCategory>(CATEGORY_ASSIST)
        val appSwitchCategory = prefScreen.findPreference<PreferenceCategory>(CATEGORY_APPSWITCH)
        val cameraCategory = findPreference<PreferenceCategory>(CATEGORY_CAMERA)

        if (hasBackKey) {
            if (!showBackWake) backCategory?.removePreference(findPreference(Settings.System.BACK_WAKE_SCREEN))
        } else prefScreen.removePreference(backCategory)

        if (hasHomeKey) {
            if (!showHomeWake) homeCategory?.removePreference(findPreference(Settings.System.HOME_WAKE_SCREEN))
        } else {
            prefScreen.removePreference(homeCategory)
            prefScreen.removePreference(hwkeyCategory)
        }

        if (hasAppSwitchKey) {
            if (!showAppSwitchWake) appSwitchCategory?.removePreference(findPreference(Settings.System.APP_SWITCH_WAKE_SCREEN))
        } else prefScreen.removePreference(appSwitchCategory)

        if (hasMenuKey) {
            if (!showMenuWake) menuCategory?.removePreference(findPreference(Settings.System.MENU_WAKE_SCREEN))
        } else prefScreen.removePreference(menuCategory)

        if (hasAssistKey) {
            if (!showAssistWake) assistCategory?.removePreference(findPreference(Settings.System.ASSIST_WAKE_SCREEN))
        } else prefScreen.removePreference(assistCategory)

        if (hasCameraKey) {
            if (!showCameraWake) cameraCategory?.removePreference(findPreference(Settings.System.CAMERA_WAKE_SCREEN))
        } else cameraCategory?.parent?.removePreference(cameraCategory)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val resolver = contentResolver
        if (preference == mHwKeyDisable) {
            val value = newValue as Boolean
            Settings.Secure.putInt(resolver, Settings.Secure.HARDWARE_KEYS_DISABLE, if (value) 1 else 0)
            return true
        } else if (preference == mButtonTimoutBar) {
            val buttonTimeout = newValue as Int
            Settings.System.putInt(resolver, Settings.System.BUTTON_BACKLIGHT_TIMEOUT, buttonTimeout)
            return true
        } else if (preference == mManualButtonBrightness) {
            val buttonBrightness = newValue as Int
            Settings.System.putInt(resolver, Settings.System.CUSTOM_BUTTON_BRIGHTNESS, buttonBrightness)
            return true
        }
        return false
    }
    */
}
