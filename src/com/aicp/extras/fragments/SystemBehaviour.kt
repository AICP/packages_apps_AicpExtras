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
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemProperties
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.Util
import com.aicp.gear.preference.SecureSettingMasterSwitchPreference


class SystemBehaviour : BaseSettingsFragment(),
    DialogInterface.OnClickListener, DialogInterface.OnDismissListener, Preference.OnPreferenceChangeListener {

    companion object {
        private const val KEY_ENABLE_BLURS = "enable_blurs_on_windows"
        private const val SF_PROP_REQUIRED_FOR_BLUR =
            "ro.surface_flinger.supports_background_blur"
    }

    private lateinit var mEnableSpoofing: SecureSettingMasterSwitchPreference
    private lateinit var mPrefs: SharedPreferences
    private var enableBlurPref: SwitchPreferenceCompat? = null
    private var mOkDialog: Dialog? = null
    private var mOkClicked: Boolean = false

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
        mPrefs = requireActivity().getSharedPreferences(requireActivity().packageName + "_preferences", Context.MODE_PRIVATE)
        val prefSet = preferenceScreen

        mEnableSpoofing = prefSet.findPreference<SecureSettingMasterSwitchPreference>("spoofing")!!
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

        return false
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
}

