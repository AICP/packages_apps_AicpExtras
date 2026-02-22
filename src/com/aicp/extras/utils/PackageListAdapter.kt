/*
 * Copyright (C) 2012-2014 The CyanogenMod Project
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
package com.aicp.extras.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.aicp.extras.R
import java.util.*

class PackageListAdapter(context: Context) : BaseAdapter(), Runnable {

    private val mPm: PackageManager = context.packageManager
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val mInstalledPackages = LinkedList<PackageItem>()

    companion object {
        // Packages which don't have launcher icons, but which we want to show nevertheless
        private val PACKAGE_WHITELIST = arrayOf(
            "android",                          // system server
            "com.android.systemui",             // system UI
            "com.android.providers.downloads"   // download provider
        )
    }

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val item = msg.obj as PackageItem
            val index = Collections.binarySearch(mInstalledPackages, item)
            if (index < 0) {
                mInstalledPackages.add(-index - 1, item)
            } else {
                mInstalledPackages[index].activityTitles.addAll(item.activityTitles)
            }
            notifyDataSetChanged()
        }
    }

    data class PackageItem(
        val packageName: String,
        val title: CharSequence,
        val icon: Drawable,
        val activityTitles: TreeSet<CharSequence> = TreeSet()
    ) : Comparable<PackageItem> {
        override fun compareTo(other: PackageItem): Int {
            val cmp = title.toString().compareTo(other.title.toString(), ignoreCase = true)
            return if (cmp != 0) cmp else packageName.compareTo(other.packageName)
        }
    }

    private class ViewHolder {
        lateinit var title: TextView
        lateinit var summary: TextView
        lateinit var icon: ImageView
    }

    init {
        reloadList()
    }

    override fun getCount(): Int = synchronized(mInstalledPackages) { mInstalledPackages.size }

    override fun getItem(position: Int): PackageItem = synchronized(mInstalledPackages) { mInstalledPackages[position] }

    override fun getItemId(position: Int): Long = synchronized(mInstalledPackages) {
        mInstalledPackages[position].packageName.hashCode().toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView != null) {
            view = convertView
            holder = view.tag as ViewHolder
        } else {
            view = mInflater.inflate(R.layout.applist_preference_icon, parent, false)
            holder = ViewHolder()
            holder.title = view.findViewById(com.android.internal.R.id.title)
            holder.summary = view.findViewById(com.android.internal.R.id.summary)
            holder.icon = view.findViewById(R.id.icon)
            view.tag = holder
        }

        val applicationInfo = getItem(position)
        holder.title.text = applicationInfo.title
        holder.icon.setImageDrawable(applicationInfo.icon)

        var needSummary = applicationInfo.activityTitles.isNotEmpty()
        if (applicationInfo.activityTitles.size == 1) {
            if (TextUtils.equals(applicationInfo.title, applicationInfo.activityTitles.first())) {
                needSummary = false
            }
        }

        if (needSummary) {
            holder.summary.text = TextUtils.join(", ", applicationInfo.activityTitles)
            holder.summary.visibility = View.VISIBLE
        } else {
            holder.summary.visibility = View.GONE
        }

        return view
    }

    private fun reloadList() {
        mInstalledPackages.clear()
        Thread(this).start()
    }

    override fun run() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val installedAppsInfo = mPm.queryIntentActivities(mainIntent, 0)

        installedAppsInfo.forEach { info ->
            val appInfo = info.activityInfo.applicationInfo
            val item = PackageItem(
                packageName = appInfo.packageName,
                title = appInfo.loadLabel(mPm),
                icon = appInfo.loadIcon(mPm)
            )
            item.activityTitles.add(info.loadLabel(mPm))
            mHandler.obtainMessage(0, item).sendToTarget()
        }

        PACKAGE_WHITELIST.forEach { pkgName ->
            try {
                val appInfo = mPm.getApplicationInfo(pkgName, 0)
                val item = PackageItem(
                    packageName = appInfo.packageName,
                    title = appInfo.loadLabel(mPm),
                    icon = appInfo.loadIcon(mPm)
                )
                mHandler.obtainMessage(0, item).sendToTarget()
            } catch (_: PackageManager.NameNotFoundException) {
                // ignore missing whitelist packages
            }
        }
    }
}
