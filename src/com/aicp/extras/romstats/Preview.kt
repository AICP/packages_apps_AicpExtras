/*
 * Copyright (C) 2012 The CyanogenMod Project
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
package com.aicp.extras.romstats

import android.os.Bundle
import androidx.preference.PreferenceScreen
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

class Preview : BaseSettingsFragment() {

    companion object {
        private const val UNIQUE_ID = "preview_id"
        private const val DEVICE = "preview_device"
        private const val VERSION = "preview_version"
        private const val BUILDTYPE = "preview_buildtype"
        private const val COUNTRY = "preview_country"
        private const val CARRIER = "preview_carrier"
        private const val ROMNAME = "preview_romname"
        private const val ROMVERSION = "preview_romversion"
    }

    override fun getPreferenceResource(): Int {
        return R.xml.preview_data
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefSet = preferenceScreen
        val context = requireActivity().applicationContext

        prefSet.findPreference<androidx.preference.Preference>(UNIQUE_ID)?.summary = Utilities.getUniqueID(context)
        prefSet.findPreference<androidx.preference.Preference>(DEVICE)?.summary = Utilities.getDevice()
        prefSet.findPreference<androidx.preference.Preference>(VERSION)?.summary = Utilities.getModVersion()
        prefSet.findPreference<androidx.preference.Preference>(BUILDTYPE)?.summary = Utilities.getBuildType()
        prefSet.findPreference<androidx.preference.Preference>(COUNTRY)?.summary = Utilities.getCountryCode(context)
        prefSet.findPreference<androidx.preference.Preference>(CARRIER)?.summary = Utilities.getCarrier(context)
        prefSet.findPreference<androidx.preference.Preference>(ROMNAME)?.summary = Utilities.getRomName()
        prefSet.findPreference<androidx.preference.Preference>(ROMVERSION)?.summary = Utilities.getRomVersion()
    }
}

