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
/*
 * Copyright (C) 2017-2020 AICP
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
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.gear.preference.SecureSettingIntListPreference
import net.margaritov.preference.colorpicker.ColorPickerPreference

class AmbientEdgeLights : BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener {

    companion object {
        private const val PULSE_AMBIENT_LIGHT_COLOR_MODE = "pulse_ambient_light_color_mode"
        private const val PULSE_AMBIENT_LIGHT_COLOR = "pulse_ambient_light_color"
    }

    private lateinit var edgeLightColorPref: ColorPickerPreference
    private lateinit var edgeLightColorModePref: SecureSettingIntListPreference

    override fun getPreferenceResource(): Int {
        return R.xml.ambient_edge_lights
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        edgeLightColorModePref =
            findPreference(PULSE_AMBIENT_LIGHT_COLOR_MODE)!!
        edgeLightColorModePref.onPreferenceChangeListener = this

        edgeLightColorPref =
            findPreference(PULSE_AMBIENT_LIGHT_COLOR)!!
        edgeLightColorPref.onPreferenceChangeListener = this

        val edgeLightColorMode = Settings.Secure.getIntForUser(
            requireActivity().contentResolver,
            Settings.Secure.PULSE_AMBIENT_LIGHT_COLOR_MODE,
            1,
            UserHandle.USER_CURRENT
        )

        updateColorPrefs(edgeLightColorMode)
    }

    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?
    ): Boolean {
        val resolver = requireContext().contentResolver        

        if (preference == edgeLightColorModePref) {
            val edgeLightColorMode = (newValue as String).toInt()
            updateColorPrefs(edgeLightColorMode)
            return true
        }
        return false
    }

    private fun updateColorPrefs(mode: Int) {
        edgeLightColorPref.isEnabled = mode == 2
    }
}

