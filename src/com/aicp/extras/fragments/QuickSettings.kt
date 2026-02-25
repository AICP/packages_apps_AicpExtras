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

import android.content.ContentResolver
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import android.text.Spannable
import android.text.TextUtils
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

class QuickSettings : BaseSettingsFragment(), OnPreferenceChangeListener {

    companion object {
        private const val KEY_CUSTOM_FOOTER_TEXT = "custom_footer_text"
    }

    private lateinit var customFooterTextPref: Preference
    private var customFooterText: String? = null

    override fun getPreferenceResource(): Int = R.xml.quick_settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        customFooterTextPref = findPreference(KEY_CUSTOM_FOOTER_TEXT)!!
        updateCustomFooterTextSummary()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val resolver: ContentResolver = requireContext().contentResolver
        // Hier könntest du weitere Preference-Changes abfangen, z.B. QS Columns Auto
        return false
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val resolver: ContentResolver = requireContext().contentResolver

        if (preference.key == KEY_CUSTOM_FOOTER_TEXT) {

            val alert = android.app.AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.footer_text_label_title)
            alert.setMessage(R.string.footer_text_label_explain)

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
                setText(if (customFooterText.isNullOrEmpty()) "" else customFooterText)
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
                    Settings.System.QS_FOOTER_TEXT_STRING,
                    value,
                    UserHandle.USER_CURRENT
                )
                updateCustomFooterTextSummary()
            }

            alert.setNegativeButton(android.R.string.cancel, null)
            alert.show()

            return true
        }

        return super.onPreferenceTreeClick(preference)
    }

    private fun updateCustomFooterTextSummary() {
        val resolver: ContentResolver = requireContext().contentResolver
        customFooterText = Settings.System.getStringForUser(
            resolver,
            Settings.System.QS_FOOTER_TEXT_STRING,
            UserHandle.USER_CURRENT
        )

        customFooterTextPref.summary = if (customFooterText.isNullOrEmpty()) {
            getString(R.string.footer_text_default)
        } else {
            customFooterText
        }
    }
}

