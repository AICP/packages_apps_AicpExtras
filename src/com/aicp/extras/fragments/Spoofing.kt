/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-FileCopyrightText: 2026 AICP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aicp.extras.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.preference.KeyboxDataPreference
import com.aicp.extras.preference.PifDataPreference

class Spoofing : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {

    companion object {
        private const val KEYBOX_DATA_KEY = "keybox_data_setting"
        private const val PIF_DATA_KEY = "pif_data_setting"
    }

    private lateinit var mKeyboxFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var mPifFilePickerLauncher: ActivityResultLauncher<Intent>
    private var mKeyboxDataPreference: KeyboxDataPreference? = null
    private var mPifDataPreference: PifDataPreference? = null

    override fun getPreferenceResource(): Int = R.xml.spoofing

    override fun onCreate(savedInstanceState: Bundle?) {
        mKeyboxFilePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.data?.let { uri ->
                    (findPreference(KEYBOX_DATA_KEY) as? KeyboxDataPreference)?.handleFileSelected(uri)
                }
            }
        }

        mPifFilePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                result.data?.data?.let { uri ->
                    (findPreference(PIF_DATA_KEY) as? PifDataPreference)?.handleFileSelected(uri)
                }
            }
        }
        super.onCreate(savedInstanceState)

        val isSpoofingEnabled = Settings.Secure.getInt(
            requireContext().contentResolver,
            "spoofing", 0
        ) == 1

        if (!isSpoofingEnabled) {
            requireActivity().finish()
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mKeyboxDataPreference = findPreference(KEYBOX_DATA_KEY)
        mPifDataPreference = findPreference(PIF_DATA_KEY)

        mKeyboxDataPreference?.setFilePickerLauncher(mKeyboxFilePickerLauncher)
        mPifDataPreference?.setFilePickerLauncher(mPifFilePickerLauncher)
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean = false
}
