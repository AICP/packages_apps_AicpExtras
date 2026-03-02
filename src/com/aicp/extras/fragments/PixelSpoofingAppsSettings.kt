/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aicp.extras.fragments

import com.aicp.extras.R
import package com.aicp.gear.preference.AppListSettingsFragment

class PixelSpoofingAppsSettings : AppListSettingsFragment() {

    override fun getTitleResId(): Int = R.string.pixel_spoofing_apps_title

    override fun excludeSystemApps(): Boolean = false

    override fun getInitialCheckedList(): List<String> {
        val context = context ?: return emptyList()
        val packageList = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.PIXEL_SPOOFING_APPS
        )
        return packageList?.takeIf { it.isNotBlank() }?.split("|") ?: emptyList()
    }

    override fun onListUpdate(packageName: String, isChecked: Boolean) {
        val context = context ?: return
        val current = getInitialCheckedList().toMutableSet()
        if (isChecked) current.add(packageName) else current.remove(packageName)
        Settings.Secure.putString(
            context.contentResolver,
            Settings.Secure.PIXEL_SPOOFING_APPS,
            current.joinToString("|")
        )
    }
}

