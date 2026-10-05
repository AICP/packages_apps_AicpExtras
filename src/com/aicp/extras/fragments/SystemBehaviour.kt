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
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemProperties
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.Util
import com.aicp.gear.preference.SecureSettingMasterSwitchPreference
import java.net.InetAddress

class SystemBehaviour : BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener {

    companion object {
        private const val KEY_ENABLE_BLURS = "enable_blurs_on_windows"
        private const val SF_PROP_REQUIRED_FOR_BLUR = "ro.surface_flinger.supports_background_blur"
        private const val SPOOFING_KEY = "spoofing"
        private const val PREF_SYSTEM_APP_REMOVER = "system_app_remover"
        private const val PREF_ADBLOCK = "persist.aicp.hosts_block"
        private const val PREF_SYSTEM_SMART_5G = "smart_5g"
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var mEnableSpoofing: SecureSettingMasterSwitchPreference
    private var enableBlurPref: SwitchPreferenceCompat? = null

    override fun getPreferenceResource(): Int = R.xml.system_behaviour

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        enableBlurPref = findPreference(KEY_ENABLE_BLURS)
        enableBlurPref?.let { pref ->
            if (pref.isChecked) setWindowBlur(true)
            pref.onPreferenceChangeListener = this
            val blurSupported = SystemProperties.getInt(SF_PROP_REQUIRED_FOR_BLUR, 0) == 1
            pref.isEnabled = blurSupported
        }

        mEnableSpoofing = findPreference(SPOOFING_KEY)!!
        mEnableSpoofing.onPreferenceChangeListener = this

        val systemAppRemover = findPreference<Preference>(PREF_SYSTEM_APP_REMOVER)
        if (!Util.hasSu()) {
            systemAppRemover?.isEnabled = false
        }

        val smart5g = findPreference<Preference>(PREF_SYSTEM_SMART_5G)
        if (!Util.is5GSupported(requireContext())) {
            smart5g?.isEnabled = false
        }

        findPreference<Preference>(PREF_ADBLOCK)?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
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
            }

            Settings.Secure.putInt(requireContext().contentResolver, SPOOFING_KEY, 0)
            return true
        }

        if (preference.key == PREF_ADBLOCK) {
            handler.postDelayed({
                InetAddress.clearDnsCache()
            }, 1000)
            return true
        }

        return true
    }

    private fun showSpoofingWarningDialog() {
        val activity = activity ?: return
        AlertDialog.Builder(activity)
            .setTitle(R.string.spoofing_warning_title)
            .setMessage(R.string.spoofing_warning)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                Settings.Secure.putInt(requireContext().contentResolver, SPOOFING_KEY, 1)
                mEnableSpoofing.isChecked = true
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                mEnableSpoofing.isChecked = false
            }
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
