/*
 * Copyright (C) 2017 AICP
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

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import android.text.Spannable
import android.text.TextUtils
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout

import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.preference.SystemSettingMasterSwitchPreference

import com.aicp.gear.preference.SystemSettingIntListPreference
import com.aicp.gear.preference.SystemSettingListPreference
import com.aicp.gear.preference.SystemSettingSwitchPreference

//import com.android.internal.util.aicp.AicpUtils

class StatusBar : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {

//    private val SMART_PULLDOWN = "qs_smart_pulldown"
//    private val QUICK_PULLDOWN = "qs_quick_pulldown"
/*    private val KEY_CARRIER_LABEL = "status_bar_show_carrier"
    private val KEY_CUSTOM_CARRIER_LABEL = "custom_carrier_label"
    private val KEY_HIDE_NOTCH = "statusbar_hide_notch"
    private val KEY_ESTIMATE_IN_QQS = "qs_show_battery_estimate"
    private val KEY_BATTERY_PERCENTAGE = "status_bar_show_battery_percent"
    private val KEY_NETWORK_TRAFFIC_STATUSBAR = "network_traffic_state"
*/
//    private var mSmartPulldown: ListPreference? = null
    private var mQuickPulldown: ListPreference? = null
/*    private var mCustomCarrierLabel: Preference? = null
    private var mShowBatteryPercentage: SystemSettingIntListPreference? = null
    private var mShowCarrierLabel: SystemSettingIntListPreference? = null
    private var mShowBatteryInQQS: SystemSettingSwitchPreference? = null

    private var mCustomCarrierLabelText: String? = null
*/

    override fun getPreferenceResource(): Int {
        return R.xml.status_bar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resolver: ContentResolver = requireActivity().contentResolver

/*        // Smart Pulldown
        mSmartPulldown = findPreference(SMART_PULLDOWN) as ListPreference
        val smartPulldown = Settings.System.getInt(resolver, Settings.System.QS_SMART_PULLDOWN, 0)
        updateSmartPulldownSummary(smartPulldown)
        mSmartPulldown?.onPreferenceChangeListener = this

        // Quick Pulldown
        mQuickPulldown = findPreference(QUICK_PULLDOWN) as ListPreference
        mQuickPulldown?.onPreferenceChangeListener = this
        val quickPulldownValue = Settings.System.getIntForUser(
            resolver,
            Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
            0,
            UserHandle.USER_CURRENT
        )
        mQuickPulldown?.value = quickPulldownValue.toString()
        updateQuickPulldownSummary(quickPulldownValue)

        mShowCarrierLabel = findPreference(KEY_CARRIER_LABEL) as SystemSettingIntListPreference
        val showCarrierLabel = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_SHOW_CARRIER, 1)
        val NonNotchEntries = arrayOf(
            resources.getString(R.string.show_carrier_disabled),
            resources.getString(R.string.show_carrier_keyguard),
            resources.getString(R.string.show_carrier_statusbar),
            resources.getString(R.string.show_carrier_enabled)
        )
        val NotchEntries = arrayOf(
            resources.getString(R.string.show_carrier_disabled),
            resources.getString(R.string.show_carrier_keyguard)
        )
        val NonNotchValues = arrayOf("0", "1", "2", "3")
        val NotchValues = arrayOf("0", "1")
        mShowCarrierLabel?.entries = if (AicpUtils.hasNotch(requireActivity())) NotchEntries else NonNotchEntries
        mShowCarrierLabel?.entryValues = if (AicpUtils.hasNotch(requireActivity())) NotchValues else NonNotchValues
        mShowCarrierLabel?.value = showCarrierLabel.toString()
        mShowCarrierLabel?.summary = mShowCarrierLabel?.entry
        mShowCarrierLabel?.onPreferenceChangeListener = this

        mCustomCarrierLabel = findPreference(KEY_CUSTOM_CARRIER_LABEL)
        updateCustomLabelTextSummary()

        val prefNetTrafficStatusBar = findPreference<SystemSettingMasterSwitchPreference>(KEY_NETWORK_TRAFFIC_STATUSBAR)
        if (resources.getBoolean(R.bool.config_haveIntrusiveNotch)) {
            prefNetTrafficStatusBar?.parent?.removePreference(prefNetTrafficStatusBar)
        }

        // Battery Percentage
        mShowBatteryPercentage = findPreference(KEY_BATTERY_PERCENTAGE) as SystemSettingIntListPreference
        mShowBatteryPercentage?.onPreferenceChangeListener = this
        val showBatteryPercentageValue = Settings.System.getIntForUser(
            resolver,
            Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
            0,
            UserHandle.USER_CURRENT
        )
        mShowBatteryPercentage?.value = showBatteryPercentageValue.toString()
        updateShowBatteryPercentageSummary(showBatteryPercentageValue)

        // Battery estimate in Quick QS
        mShowBatteryInQQS = findPreference(KEY_ESTIMATE_IN_QQS) as SystemSettingSwitchPreference
        updateShowBatteryInQQS(showBatteryPercentageValue)

        val displayCutout = resources.getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout)
        if (displayCutout.isEmpty()) {
            val hideNotchPref = findPreference<Preference>(KEY_HIDE_NOTCH)
            hideNotchPref?.parent?.removePreference(hideNotchPref)
        }*/
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val resolver: ContentResolver = requireActivity().contentResolver
/*        if (preference == mSmartPulldown) {
            val value = (newValue as String).toInt()
            updateSmartPulldownSummary(value)
            return true
        } else if (preference == mQuickPulldown) {
            val quickPulldownValue = (newValue as String).toInt()
            Settings.System.putIntForUser(
                resolver,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                quickPulldownValue,
                UserHandle.USER_CURRENT
            )
            updateQuickPulldownSummary(quickPulldownValue)
            return true
        } else if (preference == mShowCarrierLabel) {
            val value = (newValue as String).toInt()
            updateCarrierLabelSummary(value)
            return true
        } else if (preference == mShowBatteryPercentage) {
            val showBatteryPercentageValue = (newValue as String).toInt()
            Settings.System.putIntForUser(
                resolver,
                Settings.System.STATUS_BAR_SHOW_BATTERY_PERCENT,
                showBatteryPercentageValue,
                UserHandle.USER_CURRENT
            )
            updateShowBatteryInQQS(showBatteryPercentageValue)
            return true
        }*/
        return false
    }

    /*
    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        super.onPreferenceTreeClick(preference)
        val resolver: ContentResolver = requireActivity().contentResolver
        if (preference.key == KEY_CUSTOM_CARRIER_LABEL) {
            val alert = AlertDialog.Builder(requireActivity())
            alert.setTitle(R.string.custom_carrier_label_title)
            alert.setMessage(R.string.custom_carrier_label_explain)

            val container = LinearLayout(requireActivity())
            container.orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(55, 20, 55, 20)

            // Set an EditText view to get user input
            val input = EditText(requireActivity())
            input.setText(if (TextUtils.isEmpty(mCustomCarrierLabelText)) "" else mCustomCarrierLabelText)
            input.setSelection(input.text.length)
            input.layoutParams = lp
            input.gravity = Gravity.TOP or Gravity.START
            container.addView(input)
            alert.setView(container)
            alert.setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                val value = (input.text as Spannable).toString().trim()
                Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value)
                updateCustomLabelTextSummary()
                val i = Intent()
                i.action = Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED
                requireActivity().sendBroadcast(i)
            }
            alert.setNegativeButton(getString(android.R.string.cancel), null)
            alert.show()
            return true
        } else {
            return false
        }
    }

    private fun updateCarrierLabelSummary(value: Int) {
        val res: Resources = resources
        mShowCarrierLabel?.summary = when (value) {
            0 -> res.getString(R.string.show_carrier_disabled)
            1 -> res.getString(R.string.show_carrier_keyguard)
            2 -> res.getString(R.string.show_carrier_statusbar)
            3 -> res.getString(R.string.show_carrier_enabled)
            else -> ""
        }
    }

    private fun updateSmartPulldownSummary(value: Int) {
        val res: Resources = resources
        mSmartPulldown?.summary = when (value) {
            0 -> res.getString(R.string.smart_pulldown_off_summary)
            3 -> res.getString(R.string.smart_pulldown_none_summary)
            else -> {
                val type = if (value == 1) res.getString(R.string.smart_pulldown_dismissable)
                           else res.getString(R.string.smart_pulldown_ongoing)
                res.getString(R.string.smart_pulldown_summary, type)
            }
        }
    }

    private fun updateQuickPulldownSummary(value: Int) {
        val res: Resources = resources
        mQuickPulldown?.summary = when (value) {
            0 -> res.getString(R.string.quick_pulldown_off)
            3 -> res.getString(R.string.quick_pulldown_summary_always)
            else -> {
                val direction = if (value == 2) res.getString(R.string.quick_pulldown_left)
                                else res.getString(R.string.quick_pulldown_right)
                res.getString(R.string.quick_pulldown_summary, direction)
            }
        }
    }

    private fun updateCustomLabelTextSummary() {
        mCustomCarrierLabelText = Settings.System.getString(
            requireActivity().contentResolver,
            Settings.System.CUSTOM_CARRIER_LABEL
        )
        mCustomCarrierLabel?.summary = if (TextUtils.isEmpty(mCustomCarrierLabelText)) {
            getString(R.string.custom_carrier_label_notset)
        } else {
            mCustomCarrierLabelText
        }
    }

    private fun updateShowBatteryPercentageSummary(value: Int) {
        val res: Resources = resources
        mShowBatteryPercentage?.summary = when (value) {
            0 -> res.getString(R.string.status_bar_battery_percentage_default)
            1 -> res.getString(R.string.status_bar_battery_percentage_text_inside)
            2 -> res.getString(R.string.status_bar_battery_percentage_text_next)
            else -> ""
        }
    }

    private fun updateShowBatteryInQQS(value: Int) {
        when (value) {
            1 -> mShowBatteryInQQS?.isEnabled = true
            0, 2 -> {
                mShowBatteryInQQS?.isChecked = false
                mShowBatteryInQQS?.isEnabled = false
            }
        }
    }
    */
}
