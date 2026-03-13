/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * Copyright (C) 2026 AICP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aicp.extras.fragments

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.preference.KeyboxDataPreference
import com.aicp.extras.preference.PifDataPreference

class Spoofing : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {

    companion object {
        const val TAG = "Spoofing"
        private const val KEYBOX_DATA_KEY = "keybox_data_setting"
        private const val PIF_DATA_KEY = "pif_data_setting"
    }

    private lateinit var mKeyboxFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var mPifFilePickerLauncher: ActivityResultLauncher<Intent>
    private var mKeyboxDataPreference: KeyboxDataPreference? = null
    private var mPifDataPreference: PifDataPreference? = null

    override fun getPreferenceResource(): Int = R.xml.spoofing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefScreen: PreferenceScreen? = preferenceScreen
        val res: Resources = resources

        mKeyboxFilePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri: Uri? = result.data?.data
                val pref = findPreference<Preference>(KEYBOX_DATA_KEY)
                if (pref is KeyboxDataPreference) {
                    uri?.let { pref.handleFileSelected(it) }
                }
            }
        }

        mPifFilePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri: Uri? = result.data?.data
                val pref = findPreference<Preference>(PIF_DATA_KEY)
                if (pref is PifDataPreference) {
                    uri?.let { pref.handleFileSelected(it) }
                }
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mKeyboxDataPreference = findPreference(KEYBOX_DATA_KEY)
        mPifDataPreference = findPreference(PIF_DATA_KEY)

        mKeyboxDataPreference?.setFilePickerLauncher(mKeyboxFilePickerLauncher)
        mPifDataPreference?.setFilePickerLauncher(mPifFilePickerLauncher)
    }
}
