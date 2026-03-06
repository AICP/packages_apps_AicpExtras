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

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.Util
import java.net.InetAddress
import android.telephony.TelephonyManager;
import android.content.Context;

class SystemExtensions : BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener {

    companion object {
        private const val PREF_SYSTEM_APP_REMOVER = "system_app_remover"
        private const val PREF_ADBLOCK = "persist.aicp.hosts_block"
        private const val PREF_SYSTEM_SMART_5G = "smart_5g"
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun getPreferenceResource(): Int =
        R.xml.system_extensions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val systemAppRemover = findPreference<Preference>(PREF_SYSTEM_APP_REMOVER)

        if (!Util.hasSu()) {
            systemAppRemover?.isEnabled = false
        }


        val smart5g = findPreference<Preference>(PREF_SYSTEM_SMART_5G)

        if (!Util.is5GSupported(requireContext())) {
            smart5g?.isEnabled = false
        }

        findPreference<Preference>(PREF_ADBLOCK)
            ?.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?
    ): Boolean {

        return if (preference.key == PREF_ADBLOCK) {

            // Delay, damit die Property persistiert wird,
            // bevor der DNS-Cache geleert wird
            handler.postDelayed({
                InetAddress.clearDnsCache()
            }, 1000)

            true
        } else {
            false
        }
    }
}

