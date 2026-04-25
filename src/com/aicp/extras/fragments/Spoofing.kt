/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-FileCopyrightText: 2026 AICP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aicp.extras.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.content.SharedPreferences
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
import com.aicp.extras.utils.Util
import com.aicp.gear.preference.SecureSettingMasterSwitchPreference

class Spoofing : BaseSettingsFragment(), DialogInterface.OnClickListener, DialogInterface.OnDismissListener, Preference.OnPreferenceChangeListener {

    companion object {
        const val TAG = "Spoofing"
        private const val KEYBOX_DATA_KEY = "keybox_data_setting"
        private const val PIF_DATA_KEY = "pif_data_setting"
    }

    private lateinit var mEnableSpoofing: SecureSettingMasterSwitchPreference
    private lateinit var mKeyboxFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var mPifFilePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var mPrefs: SharedPreferences
    private var mKeyboxDataPreference: KeyboxDataPreference? = null
    private var mOkDialog: Dialog? = null
    private var mOkClicked: Boolean = false
    private var mPifDataPreference: PifDataPreference? = null

    override fun getPreferenceResource(): Int = R.xml.spoofing

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.spoofing, rootKey))

        val prefScreen: PreferenceScreen? = preferenceScreen
        val res: Resources = resources
        mPrefs = requireActivity().getSharedPreferences(requireActivity().packageName + "_preferences", Context.MODE_PRIVATE)
        val prefSet = preferenceScreen

        mEnableSpoofing = prefSet.findPreference<SecureSettingMasterSwitchPreference>("spoofing")!!

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

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return when (preference) {
            mEnableSpoofing -> {
                if (mEnableSpoofing.isChecked) {
                    mOkClicked = false
                    mOkDialog?.dismiss()
                    mOkDialog = AlertDialog.Builder(requireActivity())
                        .setMessage(R.string.spoofing_warning)
                        .setTitle(R.string.spoofing_warning_title)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .setOnDismissListener(this)
                        .show()
                } else {
                    mPrefs.edit().putBoolean("spoofing", false).apply()
                }
                true
            }
            else -> super.onPreferenceTreeClick(preference)
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mOkClicked = true
            mPrefs.edit().putBoolean("spoofing", true).apply()
        } else {
            mOkClicked = false
            mEnableSpoofing.isChecked = false
            mPrefs.edit().putBoolean("spoofing", false).apply()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (!mOkClicked) {
            mEnableSpoofing.isChecked = false
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mKeyboxDataPreference = findPreference(KEYBOX_DATA_KEY)
        mPifDataPreference = findPreference(PIF_DATA_KEY)

        mKeyboxDataPreference?.setFilePickerLauncher(mKeyboxFilePickerLauncher)
        mPifDataPreference?.setFilePickerLauncher(mPifFilePickerLauncher)
    }
}
