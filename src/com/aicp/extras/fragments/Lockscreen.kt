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
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import android.text.Spannable
import android.text.TextUtils
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

class Lockscreen : BaseSettingsFragment() {

    companion object {
        private const val KEY_CUSTOM_CARRIER_TEXT = "lockscreen_show_custom_carrier_text"
    }

    private lateinit var customCarrierTextPref: Preference
    private var customCarrierText: String? = null

    override fun getPreferenceResource(): Int = R.xml.lockscreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefScreen: PreferenceScreen? = preferenceScreen

        customCarrierTextPref = findPreference(KEY_CUSTOM_CARRIER_TEXT)!!
        updateCustomCarrierTextSummary()
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val resolver: ContentResolver = requireContext().contentResolver

        if (preference.key == KEY_CUSTOM_CARRIER_TEXT) {

            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.custom_carrier_label_title)
            alert.setMessage(R.string.custom_carrier_label_explain)

            val container = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
            }

            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(55, 20, 55, 20)
            }

            val input = EditText(requireContext()).apply {
                setText(if (customCarrierText.isNullOrEmpty()) "" else customCarrierText)
                setSelection(text.length)
                layoutParams = lp
                gravity = Gravity.TOP or Gravity.START
            }

            container.addView(input)
            alert.setView(container)

            alert.setPositiveButton(android.R.string.ok) { _, _ ->
                val value = (input.text as Spannable).toString().trim()
                Settings.System.putStringForUser(
                    resolver,
                    Settings.System.LOCKSCREEN_SHOW_CUSTOM_CARRIER_TEXT,
                    value,
                    UserHandle.USER_CURRENT
                )
                updateCustomCarrierTextSummary()
            }

            alert.setNegativeButton(android.R.string.cancel, null)
            alert.show()

            return true
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun updateCustomCarrierTextSummary() {
        val resolver: ContentResolver = requireContext().contentResolver
        customCarrierText = Settings.System.getStringForUser(
            resolver,
            Settings.System.LOCKSCREEN_SHOW_CUSTOM_CARRIER_TEXT,
            UserHandle.USER_CURRENT
        )

        customCarrierTextPref.summary = if (customCarrierText.isNullOrEmpty()) {
            getString(R.string.carrier_text_default)
        } else {
            customCarrierText
        }
    }
}

