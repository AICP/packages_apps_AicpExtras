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
import com.aicp.gear.preference.SystemSettingMasterSwitchPreference

import com.aicp.gear.preference.SystemSettingIntListPreference
import com.aicp.gear.preference.SystemSettingListPreference
import com.aicp.gear.preference.SystemSettingSwitchPreference


class StatusBar : BaseSettingsFragment(), Preference.OnPreferenceChangeListener {

    private var mQuickPulldown: ListPreference? = null

    override fun getPreferenceResource(): Int {
        return R.xml.status_bar
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resolver: ContentResolver = requireActivity().contentResolver

    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        val resolver: ContentResolver = requireActivity().contentResolver
        return false
    }

}
