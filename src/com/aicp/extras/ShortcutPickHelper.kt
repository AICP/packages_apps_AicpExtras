/*
 * Copyright (C) 2011 The CyanogenMod Project
 * Copyright (C) 2026 AICP
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
package com.aicp.extras

import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import java.net.URISyntaxException
import java.util.*

class ShortcutPickHelper(private val mParent: Activity, private val mListener: OnPickListener) {

    interface OnPickListener {
        fun shortcutPicked(uri: String?, friendlyName: String?, isApplication: Boolean)
    }

    private val mPackageManager: PackageManager = mParent.packageManager
    private var mAlertDialog: AlertDialog? = null
    private var lastFragmentId: Int = 0

    companion object {
        private const val REQUEST_PICK_SHORTCUT = 100
        private const val REQUEST_PICK_APPLICATION = 101
        private const val REQUEST_CREATE_SHORTCUT = 102
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_PICK_APPLICATION -> completeSetCustomApp(data)
                REQUEST_CREATE_SHORTCUT -> completeSetCustomShortcut(data)
                REQUEST_PICK_SHORTCUT -> processShortcut(data, REQUEST_PICK_APPLICATION, REQUEST_CREATE_SHORTCUT)
            }
        }
    }

    fun pickShortcut(names: Array<String>?, icons: Array<ShortcutIconResource>?, fragmentId: Int) {
        val bundle = Bundle()

        val shortcutNames = ArrayList<String>()
        if (names != null) {
            shortcutNames.addAll(names.asList())
        }
        shortcutNames.add(mParent.getString(R.string.profile_applist_title))
        shortcutNames.add(mParent.getString(R.string.picker_activities))
        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames)

        val shortcutIcons = ArrayList<ShortcutIconResource>()
        if (icons != null) {
            shortcutIcons.addAll(icons.asList())
        }
        shortcutIcons.add(ShortcutIconResource.fromContext(mParent, android.R.drawable.sym_def_app_icon))
        shortcutIcons.add(ShortcutIconResource.fromContext(mParent, R.drawable.activities_icon))
        bundle.putParcelableArrayList(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, shortcutIcons)

        val pickIntent = Intent(Intent.ACTION_PICK_ACTIVITY).apply {
            putExtra(Intent.EXTRA_INTENT, Intent(Intent.ACTION_CREATE_SHORTCUT))
            putExtra(Intent.EXTRA_TITLE, mParent.getText(R.string.select_custom_app_title))
            putExtras(bundle)
        }
        lastFragmentId = fragmentId
        startFragmentOrActivity(pickIntent, REQUEST_PICK_SHORTCUT)
    }

    private fun startFragmentOrActivity(pickIntent: Intent, requestCode: Int) {
        if (lastFragmentId == 0) {
            mParent.startActivityForResult(pickIntent, requestCode)
        } else {
            val cFrag = mParent.fragmentManager.findFragmentById(lastFragmentId)
            if (cFrag != null) {
                cFrag.startActivityForResult(pickIntent, requestCode)
            }
        }
    }

    private fun processShortcut(intent: Intent, requestCodeApplication: Int, requestCodeShortcut: Int) {
        val applicationName = mParent.getString(R.string.profile_applist_title)
        val application2name = mParent.getString(R.string.picker_activities)
        val shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

        if (applicationName == shortcutName) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val pickIntent = Intent(Intent.ACTION_PICK_ACTIVITY).apply {
                putExtra(Intent.EXTRA_INTENT, mainIntent)
            }
            startFragmentOrActivity(pickIntent, requestCodeApplication)
        } else if (application2name == shortcutName) {
            val pInfos = mPackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            val appListView = ExpandableListView(mParent)
            val appAdapter = AppExpandableAdapter(pInfos, mParent)
            appListView.setAdapter(appAdapter)
            appListView.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
                val shortIntent = Intent(Intent.ACTION_MAIN)
                val pkgName = appAdapter.getGroup(groupPosition).info.packageName
                val activities = appAdapter.getGroup(groupPosition).info.activities ?: return@setOnChildClickListener true
                if (childPosition < activities.size) {
                    val actName = activities[childPosition].name
                    shortIntent.setClassName(pkgName, actName)
                    completeSetCustomApp(shortIntent)
                }
                mAlertDialog?.dismiss()
                true
            }
            val builder = AlertDialog.Builder(mParent)
            builder.setView(appListView)
            mAlertDialog = builder.create()
            mAlertDialog?.setTitle(mParent.getString(R.string.select_custom_activity_title))
            mAlertDialog?.show()
            mAlertDialog?.setOnCancelListener { _ ->
                mListener.shortcutPicked(null, null, false)
            }
        } else {
            startFragmentOrActivity(intent, requestCodeShortcut)
        }
    }

    private fun completeSetCustomApp(data: Intent) {
        mListener.shortcutPicked(data.toUri(0), getFriendlyActivityName(data, false), true)
    }

    private fun completeSetCustomShortcut(data: Intent) {
        val intent = data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)?.apply {
            putExtra(Intent.EXTRA_SHORTCUT_NAME, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME))
        }
        var appUri = intent?.toUri(0)
        appUri = appUri?.replace("com.android.contacts.action.QUICK_CONTACT", "android.intent.action.VIEW")
        mListener.shortcutPicked(appUri, getFriendlyShortcutName(intent), false)
    }

    private fun getFriendlyActivityName(intent: Intent, labelOnly: Boolean): String? {
        val ai = intent.resolveActivityInfo(mPackageManager, PackageManager.GET_ACTIVITIES)
        var friendlyName: String? = null
        if (ai != null) {
            friendlyName = ai.loadLabel(mPackageManager)?.toString()
            if (friendlyName == null && !labelOnly) {
                friendlyName = ai.name
            }
        }
        return if (friendlyName != null || labelOnly) friendlyName else intent.toUri(0)
    }

    private fun getFriendlyShortcutName(intent: Intent?): String? {
        val activityName = getFriendlyActivityName(intent ?: return null, true)
        val name = intent?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

        return if (activityName != null && name != null) {
            "$activityName: $name"
        } else {
            name ?: intent?.toUri(0)
        }
    }

    fun getFriendlyNameForUri(uri: String?): String? {
        if (uri == null) {
            return null
        }

        try {
            val intent = Intent.parseUri(uri, 0)
            return if (Intent.ACTION_MAIN == intent.action) {
                getFriendlyActivityName(intent, false)
            } else {
                getFriendlyShortcutName(intent)
            }
        } catch (e: URISyntaxException) {
            // Ignore
        }

        return uri
    }

    inner class AppExpandableAdapter(private val pInfos: List<PackageInfo>, private val context: Context) : BaseExpandableListAdapter() {

        private val allList: ArrayList<GroupInfo> = ArrayList()
        private val groupPadding: Int = context.resources.getDimensionPixelSize(R.dimen.shortcut_picker_left_padding)

        init {
            for (i in pInfos) {
                val label = i.applicationInfo?.loadLabel(mPackageManager)?.toString() ?: continue
                allList.add(GroupInfo(label, i))
            }
            Collections.sort(allList, LabelCompare())
        }

        override fun getChild(groupPosition: Int, childPosition: Int): String {
            val activities = allList[groupPosition].info.activities ?: return ""
            return if (childPosition < activities.size) activities[childPosition].name else ""
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return allList[groupPosition].info.activities?.size ?: 0
        }

        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: View.inflate(mParent, android.R.layout.simple_list_item_1, null).apply {
                setPadding(groupPadding, 0, 0, 0)
            }
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = getChild(groupPosition, childPosition).replaceFirst(allList[groupPosition].info.packageName + ".", "")
            return view
        }

        override fun getGroup(groupPosition: Int): GroupInfo {
            return allList[groupPosition]
        }

        override fun getGroupCount(): Int {
            return allList.size
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: View.inflate(mParent, android.R.layout.simple_list_item_1, null).apply {
                setPadding(70, 0, 0, 0)
            }
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = getGroup(groupPosition).label.toString()
            return view
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        inner class LabelCompare : Comparator<GroupInfo> {
            override fun compare(item1: GroupInfo, item2: GroupInfo): Int {
                val rank1 = item1.label.lowercase(Locale.getDefault())
                val rank2 = item2.label.lowercase(Locale.getDefault())
                return rank1.compareTo(rank2)
            }
        }

        inner class GroupInfo(val label: String, val info: PackageInfo)
    }
}

