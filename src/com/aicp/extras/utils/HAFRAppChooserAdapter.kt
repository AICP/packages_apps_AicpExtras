/*
 * Copyright (C) 2026 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.aicp.extras.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.aicp.extras.R
import java.util.Collections
import java.util.LinkedList
import java.util.Locale

abstract class HAFRAppChooserAdapter(
    private val context: Context
) : BaseAdapter(), Filterable {

    private val handler = Handler(Looper.getMainLooper())
    private val packageManager: PackageManager = context.packageManager
    private val layoutInflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    protected var installedAppInfo: List<PackageInfo> =
        packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)

    protected var installedApps: MutableList<AppItem> = LinkedList()
    protected var temporaryList: List<PackageInfo> = installedAppInfo

    private var isUpdating = false
    private var hasLauncherFilter = false

    @Synchronized
    fun update() {
        onStartUpdate()

        Thread {
            isUpdating = true
            val temp: MutableList<AppItem> = LinkedList()

            for (info in temporaryList) {
                val item = AppItem().apply {
                    val appInfo = info.applicationInfo ?: continue
                    title = appInfo.loadLabel(packageManager)
                    icon = appInfo.loadIcon(packageManager)
                    packageName = info.packageName
                }

                val index = Collections.binarySearch(temp, item)
                val isLauncherApp =
                    packageManager.getLaunchIntentForPackage(info.packageName) != null

                if (!hasLauncherFilter || isLauncherApp) {
                    if (index < 0) {
                        temp.add(-index - 1, item)
                    } else {
                        temp.add(index + 1, item)
                    }
                }
            }

            handler.post {
                installedApps = temp
                notifyDataSetChanged()
                isUpdating = false
                onFinishUpdate()
            }
        }.start()
    }

    abstract fun onStartUpdate()
    abstract fun onFinishUpdate()

    override fun getCount(): Int = installedApps.size

    override fun getItem(position: Int): AppItem {
        return when {
            position >= installedApps.size -> installedApps[installedApps.size - 1]
            position < 0 -> installedApps[0]
            else -> installedApps[position]
        }
    }

    override fun getItemId(position: Int): Long {
        return if (position < 0 || position >= installedApps.size) {
            -1
        } else {
            getItem(position).hashCode().toLong()
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView != null) {
            view = convertView
            holder = view.tag as ViewHolder
        } else {
            view = layoutInflater.inflate(R.layout.view_app_list, parent, false)
            holder = ViewHolder().apply {
                name = view.findViewById(android.R.id.title)
                icon = view.findViewById(android.R.id.icon)
                pkg = view.findViewById(android.R.id.message)
            }
            view.tag = holder
        }

        val appInfo = getItem(position)
        holder.name?.text = appInfo.title
        holder.pkg?.text = appInfo.packageName
        holder.icon?.setImageDrawable(appInfo.icon)

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                // No UI update here (original behavior)
            }

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                if (constraint.isNullOrEmpty()) {
                    temporaryList = installedAppInfo
                    return FilterResults()
                }

                val filteredList = ArrayList<PackageInfo>()
                val filterText = constraint.toString().lowercase(Locale.ENGLISH)

                for (data in installedAppInfo) {
                    try {
                        val appInfo = data.applicationInfo ?: continue

                        val label = appInfo
                            .loadLabel(packageManager)
                            .toString()
                            .lowercase(Locale.ENGLISH)

                        if (label.contains(filterText) ||
                            data.packageName.lowercase(Locale.ENGLISH).contains(filterText)
                        ) {
                            filteredList.add(data)
                        }
                    } catch (_: Exception) {
                    }
                }

                temporaryList = filteredList
                return FilterResults()
            }
        }
    }

    class AppItem : Comparable<AppItem> {
        var title: CharSequence? = null
        var packageName: String? = null
        var icon: Drawable? = null

        override fun compareTo(other: AppItem): Int {
            return title.toString().compareTo(other.title.toString())
        }
    }

    private class ViewHolder {
        var name: TextView? = null
        var icon: ImageView? = null
        var pkg: TextView? = null
    }

    fun setLauncherFilter(enabled: Boolean) {
        hasLauncherFilter = enabled
    }
}
