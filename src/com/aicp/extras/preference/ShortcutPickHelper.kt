/**
     Copyright (C) 2011 The CyanogenMod Project
     Copyright (C) 2026 Android Ice Cold Project
     SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.extras.preference

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.ActivityInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.aicp.extras.R
import java.net.URISyntaxException
import java.util.*

class ShortcutPickHelper(
    private val mParent: FragmentActivity,
    private val mListener: OnPickListener
) {

    private var mAlertDialog: AlertDialog? = null
    private val mPackageManager: PackageManager = mParent.packageManager
    private var lastFragmentId = 0

    interface OnPickListener {
        fun shortcutPicked(uri: String?, friendlyName: String?, isApplication: Boolean)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == FragmentActivity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_PICK_APPLICATION -> completeSetCustomApp(data)
                REQUEST_CREATE_SHORTCUT -> completeSetCustomShortcut(data)
                REQUEST_PICK_SHORTCUT -> processShortcut(
                    data,
                    REQUEST_PICK_APPLICATION,
                    REQUEST_CREATE_SHORTCUT
                )
            }
        }
    }

    fun pickShortcut(
        names: Array<String>?,
        icons: Array<ShortcutIconResource>?,
        fragmentId: Int
    ) {

        val bundle = Bundle()

        val shortcutNames = ArrayList<String>()
        names?.forEach { shortcutNames.add(it) }
        shortcutNames.add(mParent.getString(R.string.profile_applist_title))
        shortcutNames.add(mParent.getString(R.string.picker_activities))

        bundle.putStringArrayList(Intent.EXTRA_SHORTCUT_NAME, shortcutNames)

        val shortcutIcons = ArrayList<ShortcutIconResource>()
        icons?.forEach { shortcutIcons.add(it) }

        shortcutIcons.add(
            ShortcutIconResource.fromContext(
                mParent,
                android.R.drawable.sym_def_app_icon
            )
        )

        shortcutIcons.add(
            ShortcutIconResource.fromContext(
                mParent,
                R.drawable.activities_icon
            )
        )

        bundle.putParcelableArrayList(
            Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
            shortcutIcons
        )

        val pickIntent = Intent(Intent.ACTION_PICK_ACTIVITY)

        pickIntent.putExtra(
            Intent.EXTRA_INTENT,
            Intent(Intent.ACTION_CREATE_SHORTCUT)
        )

        pickIntent.putExtra(
            Intent.EXTRA_TITLE,
            mParent.getText(R.string.select_custom_app_title)
        )

        pickIntent.putExtras(bundle)

        lastFragmentId = fragmentId

        startFragmentOrActivity(pickIntent, REQUEST_PICK_SHORTCUT)
    }

    private fun startFragmentOrActivity(
        pickIntent: Intent,
        requestCode: Int
    ) {

        if (lastFragmentId == 0) {
            mParent.startActivityForResult(pickIntent, requestCode)
        } else {

            val frag: Fragment? =
                mParent.supportFragmentManager.findFragmentById(lastFragmentId)

            frag?.let {
                mParent.startActivityFromFragment(it, pickIntent, requestCode)
            }
        }
    }

    private fun processShortcut(
        intent: Intent,
        requestCodeApplication: Int,
        requestCodeShortcut: Int
    ) {

        val applicationName =
            mParent.getString(R.string.profile_applist_title)

        val application2name =
            mParent.getString(R.string.picker_activities)

        val shortcutName =
            intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

        if (applicationName == shortcutName) {

            val mainIntent = Intent(Intent.ACTION_MAIN, null)
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

            val pickIntent = Intent(Intent.ACTION_PICK_ACTIVITY)
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent)

            startFragmentOrActivity(pickIntent, requestCodeApplication)

        } else if (application2name == shortcutName) {

            val pInfos =
                mPackageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)

            val appListView = ExpandableListView(mParent)

            val appAdapter =
                AppExpandableAdapter(pInfos, mParent)

            appListView.setAdapter(appAdapter)

            appListView.setOnChildClickListener { parent, _, groupPosition, childPosition, _ ->

                val group =
                    parent.expandableListAdapter
                        .getGroup(groupPosition) as AppExpandableAdapter.GroupInfo

                val pkgName = group.info.packageName

                val activities = group.info.activities

                val actName =
                    activities?.get(childPosition)?.name ?: return@setOnChildClickListener false

                val shortIntent = Intent(Intent.ACTION_MAIN)
                shortIntent.setClassName(pkgName, actName)

                completeSetCustomApp(shortIntent)

                mAlertDialog?.dismiss()

                true
            }

            val builder = AlertDialog.Builder(mParent)

            builder.setView(appListView)

            mAlertDialog = builder.create()

            mAlertDialog?.setTitle(
                mParent.getString(R.string.select_custom_activity_title)
            )

            mAlertDialog?.show()

            mAlertDialog?.setOnCancelListener {
                mListener.shortcutPicked(null, null, false)
            }

        } else {
            startFragmentOrActivity(intent, requestCodeShortcut)
        }
    }

    inner class AppExpandableAdapter(
        pInfos: List<PackageInfo>,
        context: Context
    ) : BaseExpandableListAdapter() {

        val allList = ArrayList<GroupInfo>()
        val groupPadding: Int

        inner class GroupInfo(
            val label: String,
            val info: PackageInfo
        )

        init {

            for (pkg in pInfos) {

                val label =
                    pkg.applicationInfo?.loadLabel(mPackageManager)?.toString()
                        ?: pkg.packageName

                allList.add(GroupInfo(label, pkg))
            }

            allList.sortBy { it.label.lowercase(Locale.getDefault()) }

            groupPadding =
                context.resources.getDimensionPixelSize(
                    R.dimen.shortcut_picker_left_padding
                )
        }

        override fun getChild(
            groupPosition: Int,
            childPosition: Int
        ): Any {

            val activities = allList[groupPosition].info.activities

            return activities?.get(childPosition)?.name ?: ""
        }

        override fun getChildId(
            groupPosition: Int,
            childPosition: Int
        ): Long = childPosition.toLong()

        override fun getChildrenCount(groupPosition: Int): Int {

            val activities = allList[groupPosition].info.activities

            return activities?.size ?: 0
        }

        override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup
        ): View {

            val view =
                convertView ?: View.inflate(
                    mParent,
                    android.R.layout.simple_list_item_1,
                    null
                ).apply {
                    setPadding(groupPadding, 0, 0, 0)
                }

            val textView =
                view.findViewById<TextView>(android.R.id.text1)

            val pkg = allList[groupPosition].info.packageName

            val name = getChild(groupPosition, childPosition)
                .toString()
                .replaceFirst("$pkg.", "")

            textView.text = name

            return view
        }

        override fun getGroup(groupPosition: Int): Any =
            allList[groupPosition]

        override fun getGroupCount(): Int =
            allList.size

        override fun getGroupId(groupPosition: Int): Long =
            groupPosition.toLong()

        override fun getGroupView(
            groupPosition: Int,
            isExpanded: Boolean,
            convertView: View?,
            parent: ViewGroup
        ): View {

            val view =
                convertView ?: View.inflate(
                    mParent,
                    android.R.layout.simple_list_item_1,
                    null
                ).apply {
                    setPadding(70, 0, 0, 0)
                }

            val textView =
                view.findViewById<TextView>(android.R.id.text1)

            val group = getGroup(groupPosition) as GroupInfo

            textView.text = group.label

            return view
        }

        override fun isChildSelectable(
            groupPosition: Int,
            childPosition: Int
        ) = true

        override fun hasStableIds() = true
    }

    private fun completeSetCustomApp(data: Intent) {
        mListener.shortcutPicked(
            data.toUri(0),
            getFriendlyActivityName(data, false),
            true
        )
    }

    private fun completeSetCustomShortcut(data: Intent) {

        val intent =
            data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
                ?: return

        intent.putExtra(
            Intent.EXTRA_SHORTCUT_NAME,
            data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
        )

        var appUri = intent.toUri(Intent.URI_INTENT_SCHEME)

        appUri = appUri.replace(
            "com.android.contacts.action.QUICK_CONTACT",
            "android.intent.action.VIEW"
        )

        mListener.shortcutPicked(
            appUri,
            getFriendlyShortcutName(intent),
            false
        )
    }

    private fun getFriendlyActivityName(
        intent: Intent,
        labelOnly: Boolean
    ): String? {

        val ai: ActivityInfo? =
            intent.resolveActivityInfo(
                mPackageManager,
                PackageManager.GET_ACTIVITIES
            )

        var friendlyName: String? = null

        if (ai != null) {

            friendlyName =
                ai.loadLabel(mPackageManager)?.toString()

            if (friendlyName == null && !labelOnly) {
                friendlyName = ai.name
            }
        }

        return friendlyName ?: if (!labelOnly) intent.toUri(0) else null
    }

    private fun getFriendlyShortcutName(intent: Intent): String? {

        val activityName = getFriendlyActivityName(intent, true)

        val name =
            intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

        return if (activityName != null && name != null) {
            "$activityName: $name"
        } else name ?: intent.toUri(0)
    }

    fun getFriendlyNameForUri(uri: String?): String? {

        if (uri == null) return null

        return try {

            val intent = Intent.parseUri(uri, 0)

            if (Intent.ACTION_MAIN == intent.action) {
                getFriendlyActivityName(intent, false)
            } else {
                getFriendlyShortcutName(intent)
            }

        } catch (_: URISyntaxException) {
            uri
        }
    }

    companion object {

        private const val REQUEST_PICK_SHORTCUT = 100
        private const val REQUEST_PICK_APPLICATION = 101
        private const val REQUEST_CREATE_SHORTCUT = 102
    }
}
