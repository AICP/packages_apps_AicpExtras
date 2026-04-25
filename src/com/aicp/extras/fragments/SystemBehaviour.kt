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

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.SystemProperties
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.gear.preference.SecureSettingMasterSwitchPreference

class SystemBehaviour : BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener {

    companion object {
        private const val KEY_ENABLE_BLURS = "enable_blurs_on_windows"
        private const val SF_PROP_REQUIRED_FOR_BLUR =
            "ro.surface_flinger.supports_background_blur"
    }

    private lateinit var mEnableSpoofing: SecureSettingMasterSwitchPreference
    private var enableBlurPref: SwitchPreferenceCompat? = null

    override fun getPreferenceResource(): Int =
        R.xml.system_behaviour

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        enableBlurPref = findPreference(KEY_ENABLE_BLURS)

        enableBlurPref?.let { pref ->

            if (pref.isChecked) {
                setWindowBlur(true)
            }

            pref.onPreferenceChangeListener = this

            val blurPropValue =
                SystemProperties.getInt(SF_PROP_REQUIRED_FOR_BLUR, 0)
            val blurSupported = blurPropValue == 1

            if (!blurSupported) {
                pref.isEnabled = false
            }
        }
        mEnableSpoofing = findPreference("spoofing")!!
        mEnableSpoofing.onPreferenceChangeListener = this

        updateSpoofingAccess(mEnableSpoofing.isChecked)
    }

    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?
    ): Boolean {

        if (preference == enableBlurPref) {
            val blurEnabled = !(newValue as Boolean)

            setWindowBlur(blurEnabled)
            return true
        }
        if (preference == mEnableSpoofing) {
            val shouldEnable = newValue as Boolean
            if (shouldEnable) {
                showSpoofingWarningDialog()
                return false
            } else {
                return true
            }
        }
        return true
    }

    private fun updateSpoofingAccess(enabled: Boolean) {
        val spoofingPref = findPreference<Preference>("spoofing")
        spoofingPref?.isEnabled = enabled
    }

    private fun showSpoofingWarningDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle(R.string.spoofing_warning_title)
            .setMessage(R.string.spoofing_warning)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                mEnableSpoofing.isChecked = true
                updateSpoofingAccess(true)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setWindowBlur(disable: Boolean) {
        val context = context ?: return

        try {
            Settings.Global.putInt(
                context.contentResolver,
                "disable_window_blurs",
                if (disable) 1 else 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
