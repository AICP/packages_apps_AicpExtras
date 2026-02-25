/*
 * Copyright (C) 2017 The ABC rom
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
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.PackageListAdapter
import com.aicp.extras.utils.PackageListAdapter.PackageItem

class SlimRecentsBlacklist : BaseSettingsFragment(),
    Preference.OnPreferenceClickListener {

    companion object {
        private const val TAG = "SlimRecentsBlacklist"
        private const val DIALOG_BLACKLIST_APPS = 1
    }

    private lateinit var mPackageAdapter: PackageListAdapter
    private lateinit var mPackageManager: PackageManager
    private lateinit var mBlacklistPrefList: PreferenceGroup
    private lateinit var mAddBlacklistPref: Preference
    private var mBlacklistPackageList: String? = null
    private lateinit var mBlacklistPackages: MutableMap<String, BlacklistPackage>

    override fun getPreferenceResource(): Int {
        return R.xml.slim_recents_blacklist
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAllPreferences()
    }

    private fun showDialog(dialogId: Int) {
        when (dialogId) {
            DIALOG_BLACKLIST_APPS -> {
                val activity = requireActivity()
                val alertDialog = AlertDialog.Builder(activity)
                val list = ListView(activity)

                list.adapter = mPackageAdapter
                alertDialog.setTitle(R.string.profile_choose_app)
                alertDialog.setView(list)

                val dialog = alertDialog.create()

                list.onItemClickListener =
                    AdapterView.OnItemClickListener { parent, _, position, _ ->
                        val info =
                            parent.getItemAtPosition(position) as PackageItem
                        addCustomApplicationPref(
                            info.packageName,
                            mBlacklistPackages
                        )
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

    private fun initializeAllPreferences() {
        val activity = requireActivity()

        mPackageManager = activity.packageManager
        mPackageAdapter = PackageListAdapter(activity)

        mBlacklistPrefList = preferenceScreen
        mBlacklistPrefList.isOrderingAsAdded = false

        mBlacklistPackages = HashMap()

        mAddBlacklistPref = findPreference("add_blacklist_packages")!!
        mAddBlacklistPref.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (preference == mAddBlacklistPref) {
            showDialog(DIALOG_BLACKLIST_APPS)
        } else {
            AlertDialog.Builder(requireActivity())
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    removeApplicationPref(
                        preference.key,
                        mBlacklistPackages
                    )
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        return true
    }

    /**
     * Application class
     */
    private class BlacklistPackage(var name: String) {

        override fun toString(): String {
            return name
        }

        companion object {
            fun fromString(value: String?): BlacklistPackage? {
                if (TextUtils.isEmpty(value)) {
                    return null
                }
                return try {
                    BlacklistPackage(value!!)
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
    }

    private fun refreshCustomApplicationPrefs() {
        if (!parsePackageList()) return

        mBlacklistPrefList.removeAll()

        for (pkg in mBlacklistPackages.values) {
            try {
                val pref = createPreferenceFromInfo(pkg)
                mBlacklistPrefList.addPreference(pref)
            } catch (_: PackageManager.NameNotFoundException) {
            }
        }

        mAddBlacklistPref.order = 0
        mBlacklistPrefList.addPreference(mAddBlacklistPref)
    }

    private fun addCustomApplicationPref(
        packageName: String,
        map: MutableMap<String, BlacklistPackage>
    ) {
        var pkg = map[packageName]
        if (pkg == null) {
            pkg = BlacklistPackage(packageName)
            map[packageName] = pkg
            savePackageList(false, map)
            refreshCustomApplicationPrefs()
        }
    }

    @Throws(PackageManager.NameNotFoundException::class)
    private fun createPreferenceFromInfo(
        pkg: BlacklistPackage
    ): Preference {

        val info: PackageInfo =
            mPackageManager.getPackageInfo(
                pkg.name,
                PackageManager.GET_META_DATA
            )

        val pref = Preference(requireActivity())
        pref.key = pkg.name
        val appInfo = info.applicationInfo
        if (appInfo != null) {
            pref.title = appInfo.loadLabel(mPackageManager)
            pref.icon = appInfo.loadIcon(mPackageManager)
        }        
        pref.isPersistent = false
        pref.onPreferenceClickListener = this

        return pref
    }

    private fun removeApplicationPref(
        packageName: String,
        map: MutableMap<String, BlacklistPackage>
    ) {
        if (map.remove(packageName) != null) {
            savePackageList(false, map)
            refreshCustomApplicationPrefs()
        }
    }

    private fun parsePackageList(): Boolean {
        var parsed = false

        val blacklistString = Settings.System.getString(
            requireActivity().contentResolver,
            Settings.System.SLIM_RECENTS_BLACKLIST_VALUES
        )

        if (!TextUtils.equals(mBlacklistPackageList, blacklistString)) {
            mBlacklistPackageList = blacklistString
            mBlacklistPackages.clear()
            parseAndAddToMap(blacklistString, mBlacklistPackages)
            parsed = true
        }

        return parsed
    }

    private fun parseAndAddToMap(
        baseString: String?,
        map: MutableMap<String, BlacklistPackage>
    ) {
        if (baseString == null) return

        val array = TextUtils.split(baseString, "\\|")
        for (item in array) {
            if (TextUtils.isEmpty(item)) continue
            val pkg = BlacklistPackage.fromString(item)
            if (pkg != null) {
                map[pkg.name] = pkg
            }
        }
    }

    private fun savePackageList(
        preferencesUpdated: Boolean,
        map: MutableMap<String, BlacklistPackage>
    ) {
        val setting = Settings.System.SLIM_RECENTS_BLACKLIST_VALUES

        val settings = ArrayList<String>()
        for (app in map.values) {
            settings.add(app.toString())
        }

        val value = TextUtils.join("|", settings)

        if (preferencesUpdated) {
            mBlacklistPackageList = value
        }

        Settings.System.putString(
            requireActivity().contentResolver,
            setting,
            value
        )
    }
}
