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

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.LauncherActivity
import com.aicp.extras.R
import com.aicp.extras.utils.Util

class AeSettings : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {

    companion object {
        private const val PREF_THEME = "ae_theme"
        private const val PREF_AE_LAUNCHER = "ae_launcher_enabled"
    }

    private lateinit var mAeLauncherComponent: ComponentName
    private lateinit var mTheme: ListPreference
    private lateinit var mAeLauncher: SwitchPreferenceCompat

    override fun getPreferenceResource(): Int {
        return R.xml.ae_settings
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pm = requireContext().packageManager
        mAeLauncherComponent = ComponentName(requireContext(), LauncherActivity::class.java)

        mTheme = findPreference(PREF_THEME)!!
        mTheme.onPreferenceChangeListener = this

        mAeLauncher = findPreference(PREF_AE_LAUNCHER)!!
        mAeLauncher.isChecked = pm.getComponentEnabledSetting(mAeLauncherComponent) !=
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        mAeLauncher.onPreferenceChangeListener = this
    }

    override fun onResume() {
        super.onResume()
        Util.setSummaryToValue(mTheme)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return when (preference) {
            mTheme -> {
                Util.setSummaryToValue(mTheme, newValue)
                if (mTheme.value != newValue) {
                    activity?.recreate()
                }
                true
            }
            mAeLauncher -> {
                setAeLauncherEnabled(newValue as Boolean)
                true
            }
            else -> false
        }
    }

    private fun setAeLauncherEnabled(enabled: Boolean) {
        val pm = requireContext().packageManager
        pm.setComponentEnabledSetting(
            mAeLauncherComponent,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        Toast.makeText(
            requireContext(),
            R.string.ae_launcher_enabled_update,
            Toast.LENGTH_LONG
        ).show()
    }
}

