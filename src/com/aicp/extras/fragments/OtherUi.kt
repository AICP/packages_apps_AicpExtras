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

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

class OtherUi : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {

    companion object {
        private const val SHOW_CPU_INFO_KEY = "show_cpu_info"
        private const val KEY_DOZE_ON_CHARGE = "doze_on_charge"
    }

    private lateinit var showCpuInfoPref: SwitchPreferenceCompat

    override fun getPreferenceResource(): Int = R.xml.other_ui

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showCpuInfoPref = findPreference<SwitchPreferenceCompat>(SHOW_CPU_INFO_KEY)!!.apply {
            isChecked = Settings.Global.getInt(
                requireContext().contentResolver,
                Settings.Secure.SHOW_CPU_OVERLAY, 0
            ) == 1
            onPreferenceChangeListener = this@OtherUi
        }

        // Optional: Doze on charge check (auskommentiert)
        /*
        val dozeAlwaysOnAvailable = resources.getBoolean(
            com.android.internal.R.bool.config_dozeAlwaysOnDisplayAvailable
        )
        val dozeOnChargePref = findPreference<Preference>(KEY_DOZE_ON_CHARGE)
        if (!dozeAlwaysOnAvailable) {
            dozeOnChargePref?.parent?.removePreference(dozeOnChargePref)
        }
        */
    }

    private fun writeCpuInfoOptions(enabled: Boolean) {
        val resolver = requireContext().contentResolver
        Settings.Global.putInt(
            resolver,
            Settings.Secure.SHOW_CPU_OVERLAY,
            if (enabled) 1 else 0
        )

        val serviceIntent = Intent().setClassName(
            "com.android.systemui",
            "com.android.systemui.CPUInfoService"
        )

        if (enabled) {
            requireActivity().startService(serviceIntent)
        } else {
            requireActivity().stopService(serviceIntent)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return if (preference == showCpuInfoPref) {
            writeCpuInfoOptions(newValue as Boolean)
            true
        } else {
            false
        }
    }
}

