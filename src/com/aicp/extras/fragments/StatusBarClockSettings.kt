/*
 * SPDX-FileCopyrightText: 2018-2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.extras.fragments

import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.gear.preference.SystemSettingListPreference
import java.util.Date

class StatusBarClockSettings : BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener {

    companion object {
        private const val TAG = "StatusBarClockSettings"

        private const val CLOCK_DATE_FORMAT = "status_bar_clock_date_format"
        private const val CLOCK_DATE_POSITION = "status_bar_clock_date_position"
        private const val CLOCK_DATE_DISPLAY = "status_bar_clock_date_display"
        private const val CLOCK_DATE_STYLE = "status_bar_clock_date_style"

        const val CLOCK_DATE_STYLE_LOWERCASE = 1
        const val CLOCK_DATE_STYLE_UPPERCASE = 2
        private const val CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18
    }

    private lateinit var mClockDatePosition: SystemSettingListPreference
    private lateinit var mClockDateFormat: ListPreference
    private lateinit var mClockDateDisplay: SystemSettingListPreference
    private lateinit var mClockDateStyle: SystemSettingListPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.status_bar_clock, rootKey)

        mClockDateDisplay = findPreference<SystemSettingListPreference>(CLOCK_DATE_DISPLAY)!!
        mClockDatePosition = findPreference<SystemSettingListPreference>(CLOCK_DATE_POSITION)!!
        mClockDateStyle = findPreference<SystemSettingListPreference>(CLOCK_DATE_STYLE)!!
        mClockDateFormat = findPreference<ListPreference>(CLOCK_DATE_FORMAT)!!

        mClockDateDisplay.onPreferenceChangeListener = this
        mClockDatePosition.onPreferenceChangeListener = this
        mClockDateStyle.onPreferenceChangeListener = this
        mClockDateFormat.onPreferenceChangeListener = this

        val dateDisplay = Settings.System.getIntForUser(
            requireContext().contentResolver,
            Settings.System.STATUS_BAR_CLOCK_DATE_DISPLAY,
            0,
            UserHandle.USER_CURRENT
        )

        mClockDatePosition.isEnabled = dateDisplay > 0
        mClockDateStyle.isEnabled = dateDisplay > 0
        mClockDateFormat.isEnabled = dateDisplay > 0

        if (mClockDateFormat.value == null) {
            mClockDateFormat.value = "EEE"
        }

        parseClockDateFormats()
    }

    override fun getPreferenceResource(): Int {
        return R.xml.status_bar_clock
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference) {
            mClockDateDisplay -> {
                val valInt = (newValue as String).toInt()
                val enabled = valInt != 0
                mClockDatePosition.isEnabled = enabled
                mClockDateStyle.isEnabled = enabled
                mClockDateFormat.isEnabled = enabled
                return true
            }
            mClockDatePosition, mClockDateStyle -> {
                parseClockDateFormats()
                return true
            }
            mClockDateFormat -> {
                val index = mClockDateFormat.findIndexOfValue(newValue as String)
                if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                    val alert = AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.status_bar_date_string_edittext_title)
                        .setMessage(R.string.status_bar_date_string_edittext_summary)

                    val input = EditText(requireActivity())
                    val oldText = Settings.System.getString(
                        requireActivity().contentResolver,
                        Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT
                    )
                    oldText?.let { input.setText(it) }
                    alert.setView(input)

                    alert.setPositiveButton(R.string.menu_save) { _, _ ->
                        val value = input.text.toString()
                        if (value.isNotEmpty()) {
                            Settings.System.putString(
                                requireActivity().contentResolver,
                                Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT,
                                value
                            )
                        }
                    }

                    alert.setNegativeButton(R.string.cancel) { _, _ -> }
                    alert.create().show()
                } else {
                    Settings.System.putString(
                        requireActivity().contentResolver,
                        Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT,
                        newValue as String
                    )
                }
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        val dateDisplay = Settings.System.getIntForUser(
            requireContext().contentResolver,
            Settings.System.STATUS_BAR_CLOCK_DATE_DISPLAY,
            0,
            UserHandle.USER_CURRENT
        )

        mClockDatePosition.isEnabled = dateDisplay > 0
        mClockDateStyle.isEnabled = dateDisplay > 0
        mClockDateFormat.isEnabled = dateDisplay > 0
        parseClockDateFormats()
    }

    private fun parseClockDateFormats() {
        val dateEntries = resources.getStringArray(R.array.status_bar_date_format_entries_values)
        val parsedDateEntries = Array<CharSequence>(dateEntries.size) { "" }
        val now = Date()
        val lastEntry = dateEntries.size - 1

        val dateFormat = Settings.System.getIntForUser(
            requireActivity().contentResolver,
            Settings.System.STATUS_BAR_CLOCK_DATE_STYLE,
            0,
            UserHandle.USER_CURRENT
        )

        for (i in dateEntries.indices) {
            parsedDateEntries[i] = if (i == lastEntry) {
                dateEntries[i]
            } else {
                val dateString = DateFormat.format(dateEntries[i], now).toString()
                when (dateFormat) {
                    CLOCK_DATE_STYLE_LOWERCASE -> dateString.lowercase()
                    CLOCK_DATE_STYLE_UPPERCASE -> dateString.uppercase()
                    else -> dateString
                }
            }
        }

        mClockDateFormat.entries = parsedDateEntries
    }
}
