/*
 * Copyright (C) 2018-2026 Android Ice Cold Project
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
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

// class StatusBarClockSettings : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {
class StatusBarClockSettings : BaseSettingsFragment() {

    companion object {
        private const val TAG = "StatusBarClockSettings"

        private const val CLOCK_DATE_FORMAT = "status_bar_clock_date_format"
        private const val CLOCK_POSITION = "status_bar_clock_position"

        const val CLOCK_DATE_STYLE_LOWERCASE = 1
        const val CLOCK_DATE_STYLE_UPPERCASE = 2
        private const val CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18
    }

    /*
    private lateinit var mClockPosition: SystemSettingIntListPreference
    private lateinit var mClockDateFormat: SystemSettingListPreference
    */

    override fun getPreferenceResource(): Int {
        return R.xml.status_bar_clock
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resolver = requireActivity().contentResolver

        /*
        mClockPosition = findPreference(CLOCK_POSITION)!!

        mClockDateFormat = findPreference(CLOCK_DATE_FORMAT)!!
        mClockDateFormat.onPreferenceChangeListener = this
        if (mClockDateFormat.value == null) {
            mClockDateFormat.value = "EEE"
        }

        parseClockDateFormats()
        */
    }

    /*
    override fun onResume() {
        super.onResume()

        val hasNotch = AicpUtils.hasNotch(requireActivity())
        val notchType = DeviceUtils.getCutoutType(requireActivity())
        Log.v(TAG, "notchType: $notchType")

        if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            if (hasNotch && !(notchType == BOUNDS_POSITION_LEFT || notchType == BOUNDS_POSITION_RIGHT)) {
                mClockPosition.setEntries(R.array.clock_position_entries_notch_rtl)
                mClockPosition.setEntryValues(R.array.clock_position_values_notch_rtl)
            } else {
                mClockPosition.setEntries(R.array.clock_position_entries_rtl)
                mClockPosition.setEntryValues(R.array.clock_position_values_rtl)
            }
        } else if (hasNotch && !(notchType == BOUNDS_POSITION_LEFT || notchType == BOUNDS_POSITION_RIGHT)) {
            mClockPosition.setEntries(R.array.clock_position_entries_notch)
            mClockPosition.setEntryValues(R.array.clock_position_values_notch)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val resolver = requireActivity().contentResolver

        if (preference == mClockDateFormat) {
            val index = mClockDateFormat.findIndexOfValue(newValue as String)

            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                val alert = AlertDialog.Builder(requireActivity())
                alert.setTitle(R.string.clock_date_string_edittext_title)
                alert.setMessage(R.string.clock_date_string_edittext_summary)

                val container = LinearLayout(requireActivity())
                container.orientation = LinearLayout.VERTICAL

                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(55, 20, 55, 20)

                val input = EditText(requireActivity())
                val oldText = Settings.System.getString(
                    resolver,
                    Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT
                )
                if (oldText != null) {
                    input.setText(oldText)
                }

                input.layoutParams = lp
                input.gravity = Gravity.TOP or Gravity.START
                container.addView(input)
                alert.setView(container)

                alert.setPositiveButton(R.string.menu_save) { _, _ ->
                    val value = input.text.toString()
                    if (value.isNotEmpty()) {
                        Settings.System.putString(
                            resolver,
                            Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT,
                            value
                        )
                    }
                }

                alert.setNegativeButton(R.string.menu_cancel) { _, _ -> }

                alert.create().show()
            } else {
                if (newValue != null) {
                    Settings.System.putString(
                        resolver,
                        Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT,
                        newValue as String
                    )
                }
            }
            return true
        }
        return false
    }

    private fun parseClockDateFormats() {
        val dateEntries = resources.getStringArray(
            R.array.clock_date_format_entries_values
        )

        val parsedDateEntries = Array<CharSequence>(dateEntries.size) { "" }
        val now = Date()
        val lastEntry = dateEntries.size - 1

        val dateFormat = Settings.System.getInt(
            requireActivity().contentResolver,
            Settings.System.STATUS_BAR_CLOCK_DATE_STYLE,
            0
        )

        for (i in dateEntries.indices) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i]
            } else {
                val dateString = DateFormat.format(dateEntries[i], now).toString()
                val newDate = when (dateFormat) {
                    CLOCK_DATE_STYLE_LOWERCASE -> dateString.lowercase()
                    CLOCK_DATE_STYLE_UPPERCASE -> dateString.uppercase()
                    else -> dateString
                }
                parsedDateEntries[i] = newDate
            }
        }

        mClockDateFormat.entries = parsedDateEntries
    }
    */
}
