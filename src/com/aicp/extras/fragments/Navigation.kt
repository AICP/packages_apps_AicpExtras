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

import android.content.ContentResolver
import android.os.Bundle
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

class Navigation : BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener {

    companion object {
        private const val TAG = "AENavigation"
        private const val DEBUG = false
    }

    /*
    // preference keys
    private const val KEY_BUTTON_BRIGHTNESS = "button_brightness"
    private const val KEY_BUTTON_TIMEOUT = "button_backlight_timeout"
    private const val KEY_HWKEY_DISABLE = "hardware_keys_disable"
    private const val KEY_NAVIGATION_BAR_ENABLED = "navigation_bar_show_new"
    private const val KEY_SWAP_HW_NAVIGATION_KEYS = "swap_navigation_keys"

    private const val CATEGORY_BUTTON_BACKLIGHT_OPTIONS = "button_backlight_options_category"
    private const val CATEGORY_HWKEYS = "hardware_keys"
    private const val CATEGORY_WAKEKEYS = "wake_keys"
    private const val PREFSCREEN_HWBUTTON_SETTINGS = "hw_button_settings"
    private const val CATEGORY_GESTURE_NAV_TWEAKS = "gesture_nav_tweaks_category"

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    const val KEY_MASK_HOME = 0x01
    const val KEY_MASK_BACK = 0x02
    const val KEY_MASK_MENU = 0x04
    const val KEY_MASK_ASSIST = 0x08
    const val KEY_MASK_APP_SWITCH = 0x10
    const val KEY_MASK_CAMERA = 0x20

    private var mButtonTimoutBar: SeekBarPreferenceCham? = null
    private var mManualButtonBrightness: SeekBarPreferenceCham? = null

    private var mHwKeyDisable: SwitchPreference? = null
    private var mSwapHWNavKeys: SwitchPreference? = null
    private var mButtonBacklightCategory: PreferenceCategory? = null
    private var mHwKeysCategory: PreferenceCategory? = null
    private var mWakeKeysCategory: PreferenceCategory? = null
    private var mHwButtonSettingsScreen: PreferenceScreen? = null
    private var mGestureTweaksCategory: PreferenceCategory? = null

    private var mNavigationBar: SystemSettingMasterSwitchPreference? = null
    private var mIsNavSwitchingMode = false
    private var mHwKeysSupported = false
    private var mNeedsNavbar = false
    private var mNavigationBarEnabled = false
    private var mWakeInitialized = false
    private var isGestureNavigation = false

    private var mHandler: Handler? = null
    */

    override fun getPreferenceResource(): Int {
        return R.xml.navigation
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefScreen = preferenceScreen
        val resolver = contentResolver

        // Original Java code retained in comments
    }
    */

    override fun onPreferenceChange(
        preference: Preference,
        objValue: Any?
    ): Boolean {

        val resolver: ContentResolver = requireContext().contentResolver        

        /*
        if (preference == mNavigationBar) {
            val value = objValue as Boolean
            mNavigationBarEnabled = value
            updateHardwareCategories(!value && mHwKeysSupported)
            setKeysDisabled(value)
            initializeWakeCategory()

            if (mIsNavSwitchingMode) {
                return false
            }

            mIsNavSwitchingMode = true
            mHandler?.postDelayed({
                mIsNavSwitchingMode = false
            }, 1500)

            return true

        } else if (preference == mHwKeyDisable) {
            val value = objValue as Boolean
            setKeysDisabled(value)
            enableHardwareItems(!value)
            return true

        } else if (preference == mButtonTimoutBar) {
            val buttonTimeout = 1000 * (objValue as Int)
            Settings.System.putInt(
                resolver,
                Settings.System.BUTTON_BACKLIGHT_TIMEOUT,
                buttonTimeout
            )
            return true

        } else if (preference == mManualButtonBrightness) {
            val buttonBrightness = objValue as Int
            Settings.System.putFloat(
                resolver,
                Settings.System.BUTTON_BRIGHTNESS,
                buttonBrightness / 100f
            )
            return true
        }
        */

        return false
    }
}
