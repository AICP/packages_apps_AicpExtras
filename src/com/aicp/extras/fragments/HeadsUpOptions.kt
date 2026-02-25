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

    /*
    private val KEY_HEADSUP_BLACKLIST_PACKAGES = "add_headsup_blacklist_packages"
    private val KEY_HEADSUP_PREFERENCE_GROUP = "headsup_activity_blacklist"
    private val DIALOG_BLACKLIST_APPS = 1

    protected lateinit var mPackageAdapter: PackageListAdapter
    protected lateinit var mPackageManager: PackageManager
    protected var mBlacklistPrefList: PreferenceGroup? = null
    protected var mAddBlacklistPref: Preference? = null
    protected var mBlacklistPackageList: String? = null
    protected var mBlacklistPackageStore: String? = null
    protected lateinit var mBlacklistPackages: MutableMap<String, Package>

    protected var mDialog: AlertDialog? = null
    protected var mListView: ListView? = null
    */

    override fun getPreferenceResource(): Int {
        return R.xml.heads_up_options
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAllPreferences()
    }

    protected fun showDialog(dialogId: Int) {
        when (dialogId) {
            DIALOG_BLACKLIST_APPS -> {
                val alertDialog = AlertDialog.Builder(requireActivity())
                val list = ListView(requireActivity())
                list.adapter = mPackageAdapter
                alertDialog.setTitle(R.string.profile_choose_app)
                alertDialog.setView(list)
                val dialog: Dialog = alertDialog.create()
                list.setOnItemClickListener { parent, view, position, id ->
                    val info = parent.getItemAtPosition(position) as PackageItem
                    addCustomApplicationPref(info.packageName, mBlacklistPackages)
                    dialog.cancel()
                }
                dialog.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCustomApplicationPrefs()
    }

    protected fun initializeAllPreferences() {
        mBlacklistPackageStore = Settings.System.HEADS_UP_BLACKLIST_VALUES
        mPackageManager = requireActivity().packageManager
        mPackageAdapter = PackageListAdapter(requireActivity())
        mBlacklistPrefList = findPreference(KEY_HEADSUP_PREFERENCE_GROUP) as PreferenceGroup
        mBlacklistPrefList?.isOrderingAsAdded = false
        mBlacklistPackages = HashMap()
        mAddBlacklistPref = findPreference(KEY_HEADSUP_BLACKLIST_PACKAGES)
        mAddBlacklistPref?.setOnPreferenceClickListener(this)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        return if (preference == mAddBlacklistPref) {
            showDialog(DIALOG_BLACKLIST_APPS)
            true
        } else {
            val builder = AlertDialog.Builder(requireActivity())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                //.setIconAttribute(android.R.attr.alertDialogIcon)
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                    removeApplicationPref(preference.key, mBlacklistPackages)
                }
                .setNegativeButton(android.R.string.cancel, null)
            builder.show()
            true
        }
    }
*/
    /*
     * Application class
     */
    /*
    protected class Package(val name: String) {
        override fun toString(): String {
            return name
        }

        companion object {
            fun fromString(value: String?): Package? {
                if (TextUtils.isEmpty(value)) return null
                return try {
                    Package(value!!)
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
    }

    protected fun refreshCustomApplicationPrefs() {
        if (!parsePackageList()) return

        mBlacklistPrefList?.removeAll()
        for (pkg in mBlacklistPackages.values) {
            try {
                val pref = createPreferenceFromInfo(pkg)
                mBlacklistPrefList?.addPreference(pref)
            } catch (e: PackageManager.NameNotFoundException) {
                // Do nothing
            }
        }
        mAddBlacklistPref?.order = 0
        mBlacklistPrefList?.addPreference(mAddBlacklistPref)
    }

    protected fun addCustomApplicationPref(packageName: String, map: MutableMap<String, Package>) {
        var pkg = map[packageName]
        if (pkg == null) {
            pkg = Package(packageName)
            map[packageName] = pkg
            savePackageList(false, map)
            refreshCustomApplicationPrefs()
        }
    }

    protected fun createPreferenceFromInfo(pkg: Package): Preference {
        val info: PackageInfo = mPackageManager.getPackageInfo(pkg.name, PackageManager.GET_META_DATA)
        val pref = AppListPreference(requireActivity())
        pref.key = pkg.name
        pref.title = info.applicationInfo.loadLabel(mPackageManager)
        pref.icon = info.applicationInfo.loadIcon(mPackageManager)
        pref.isPersistent = false
        pref.setOnPreferenceClickListener(this)
        return pref
    }

    protected fun removeApplicationPref(packageName: String, map: MutableMap<String, Package>) {
        if (map.remove(packageName) != null) {
            savePackageList(false, map)
            refreshCustomApplicationPrefs()
        }
    }

    protected fun parsePackageList(): Boolean {
        var parsed = false
        val blacklistString = Settings.System.getString(requireActivity().contentResolver, mBlacklistPackageStore)
        if (DEBUG) Log.v(TAG, "blacklistString: $blacklistString")

        if (!TextUtils.equals(mBlacklistPackageList, blacklistString)) {
            mBlacklistPackageList = blacklistString
            mBlacklistPackages.clear()
            parseAndAddToMap(blacklistString, mBlacklistPackages)
            parsed = true
        }
        return parsed
    }

    protected fun parseAndAddToMap(baseString: String?, map: MutableMap<String, Package>) {
        if (baseString == null) return
        val array = TextUtils.split(baseString, "\\|")
        if (DEBUG) Log.v(TAG, "baseString: $baseString")

        for (item in array) {
            if (TextUtils.isEmpty(item)) continue
            val pkg = Package.fromString(item)
            if (pkg != null) map[pkg.name] = pkg
        }
    }

    protected fun savePackageList(preferencesUpdated: Boolean, map: MutableMap<String, Package>) {
        val settings = ArrayList<String>()
        for (app in map.values) {
            settings.add(app.toString())
        }
        val value = TextUtils.join("|", settings)
        if (preferencesUpdated) mBlacklistPackageList = value
        if (DEBUG) Log.v(TAG, "blackStringSaved: $value")
        Settings.System.putString(requireActivity().contentResolver, mBlacklistPackageStore, value)
    }
    */
}
