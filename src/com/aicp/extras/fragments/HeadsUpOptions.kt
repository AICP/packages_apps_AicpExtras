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

import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.PackageListAdapter
import com.aicp.extras.utils.PackageListAdapter.PackageItem
import com.aicp.gear.preference.AppListPreference
import com.android.internal.logging.nano.MetricsProto.MetricsEvent

class HeadsUpOptions : BaseSettingsFragment() /*, Preference.OnPreferenceClickListener*/ {

    private val TAG = "HeadsUpOptions"
    private val DEBUG = false

    override fun getPreferenceResource(): Int {
        return R.xml.heads_up_options
    }

    /*
     * Application class
     */
}
