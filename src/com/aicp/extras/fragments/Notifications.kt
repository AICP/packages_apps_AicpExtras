/*
 * Copyright (C) 2017-2026 AICP
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

class Notifications : BaseSettingsFragment() /*, Preference.OnPreferenceChangeListener*/ {

    /*
    companion object {
        private const val ALERT_SLIDER_PREF = "alert_slider_notifications"
        private const val KEY_RINGTONE_FOCUS = "ringtone_focus_mode"
        private const val CAT_NOTIFICATION_FLASHLIGHT = "notification_flash"
        private const val PREF_FLASHLIGHT_ON_CALL = "flashlight_on_call"
        private const val PREF_FLASHLIGHT_ON_CALL_WAITING = "flashlight_on_call_waiting"
        private const val PREF_FLASHLIGHT_ON_CALL_IGNORE_DND = "flashlight_on_call_ignore_dnd"
        private const val PREF_FLASHLIGHT_ON_CALL_RATE = "flashlight_on_call_rate"
        private const val PREF_NOTIFICATION_HEADER = "notification_headers"
        private const val PREF_BATTERY_LIGHT = "battery_light_enabled"
    }

    private lateinit var flashOnCallWaiting: SwitchPreference
    private lateinit var flashOnCallIgnoreDND: SwitchPreference
    private lateinit var flashOnCall: SystemSettingIntListPreference
    private lateinit var flashOnCallRate: SystemSettingSeekBarPreference
    private lateinit var notificationHeader: SystemSettingSwitchPreference
    private lateinit var resolver: ContentResolver
    */

    override fun getPreferenceResource(): Int = R.xml.notifications

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resolver = requireContext().contentResolver

        // Beispiel für spätere Initialisierung:
        // flashOnCallWaiting = findPreference(PREF_FLASHLIGHT_ON_CALL_WAITING)!!
        // flashOnCall.setOnPreferenceChangeListener(this)
        // updateDependencies(Settings.System.getInt(resolver, Settings.System.FLASHLIGHT_ON_CALL, 0) != 0)
    }

    private fun updateDependencies(enabled: Boolean) {
        flashOnCallWaiting.isEnabled = enabled
        flashOnCallIgnoreDND.isEnabled = enabled
        flashOnCallRate.isEnabled = enabled
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return when (preference) {
            flashOnCall -> {
                val value = (newValue as String).toInt()
                updateDependencies(value != 0)
                true
            }
            notificationHeader -> {
                Util.showSystemUiRestartDialog(requireActivity())
                true
            }
            else -> false
        }
    }
    */
}

