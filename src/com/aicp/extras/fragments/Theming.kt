/*
 * Copyright (C) 2018-2026 Android Ice Cold Project
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
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

// class Theming : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {
class Theming : BaseSettingsFragment() {

    // private val mHandler = Handler()

    override fun getPreferenceResource(): Int {
        return R.xml.theming
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        Util.requireConfig(
            requireActivity(),
            findPreference(Settings.System.DISPLAY_HIDE_NOTCH),
            com.android.internal.R.bool.config_showHideNotchSettings,
            true,
            false
        )
        */
    }

    /*
    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {

        if (Settings.System.THEMING_BASE == preference.key ||
            Settings.System.THEMING_CORNERS == preference.key ||
            Settings.System.THEMING_SYSTEM_ICONS_STYLE == preference.key) {

            if (ThemeOverlayHelper.doesThemeChangeRequireSystemUIRestart(
                    requireActivity(),
                    preference.key,
                    null,
                    (newValue as String).toInt()
                )
            ) {
                postRestartSystemUi()
            }
            return true

        } else if (AdaptiveIconDrawable.MASK_SETTING_PROP == preference.key) {

            Util.showRebootDialog(
                requireActivity(),
                getString(R.string.icon_shape_changed_title),
                getString(R.string.icon_shape_changed_message),
                true
            )
            return true

        } else {
            return false
        }
    }

    private fun postRestartSystemUi() {
        val appContext = requireActivity().applicationContext
        mHandler.postDelayed({
            Util.restartSystemUi(appContext)
        }, 200)
    }
    */
}
