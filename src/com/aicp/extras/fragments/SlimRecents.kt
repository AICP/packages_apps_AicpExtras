/*
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
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.preference.MasterSwitchPreference
import java.util.*

class SlimRecents : BaseSettingsFragment(),
    Preference.OnPreferenceChangeListener,
    DialogInterface.OnDismissListener {

    companion object {
        private const val RECENT_PANEL_LEFTY_MODE = "recent_panel_lefty_mode"
        private const val RECENT_ICON_PACK = "slim_icon_pack"

        private val sSupportedActions = arrayOf(
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme"
        )

        private val sSupportedCategories = arrayOf(
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME"
        )
    }

    private lateinit var mRecentPanelLeftyMode: SwitchPreference
    private lateinit var mAppSidebar: MasterSwitchPreference
    private lateinit var mIconPack: Preference

    private var mDialog: AlertDialog? = null
    private var mListView: ListView? = null

    override fun getPreferenceResource(): Int {
        return R.xml.slim_recents
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mRecentPanelLeftyMode =
            findPreference(RECENT_PANEL_LEFTY_MODE)!!
        mRecentPanelLeftyMode.onPreferenceChangeListener = this

        mAppSidebar =
            findPreference(Settings.System.USE_RECENT_APP_SIDEBAR)!!

        mIconPack = findPreference(RECENT_ICON_PACK)!!
    }

    override fun onResume() {
        super.onResume()

        val recentLeftyMode =
            Settings.System.getInt(
                requireContext().contentResolver,
                Settings.System.RECENT_PANEL_GRAVITY,
                Gravity.END
            ) == Gravity.START

        mRecentPanelLeftyMode.isChecked = recentLeftyMode
    }

    override fun onPreferenceChange(
        preference: Preference,
        newValue: Any?
    ): Boolean {

        return if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(
                requireContext().contentResolver,
                Settings.System.RECENT_PANEL_GRAVITY,
                if (newValue as Boolean) Gravity.START else Gravity.END
            )
            true
        } else {
            false
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return if (preference == mIconPack) {
            pickIconPack(requireContext())
            true
        } else {
            super.onPreferenceTreeClick(preference)
        }
    }

    /** Slim Recents Icon Pack Dialog **/
    private fun pickIconPack(context: Context) {
        if (mDialog != null) return

        val supportedPackages = getSupportedPackages(context)
        if (supportedPackages.isEmpty()) {
            Toast.makeText(
                context,
                R.string.no_iconpacks_summary,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.dialog_pick_iconpack_title)
            .setOnDismissListener(this)
            .setNegativeButton(R.string.cancel, null)
            .setView(createDialogView(context, supportedPackages))

        mDialog = builder.show()
    }

    private fun createDialogView(
        context: Context,
        supportedPackages: Map<String, IconPackInfo>
    ): View {

        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_iconpack, null)

        val adapter = IconAdapter(context, supportedPackages)

        mListView = view.findViewById(R.id.iconpack_list)
        mListView!!.adapter = adapter

        mListView!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->

                if (adapter.isCurrentIconPack(position)) return@OnItemClickListener

                val selectedPackage = adapter.getItem(position)

                Settings.System.putString(
                    requireContext().contentResolver,
                    Settings.System.SLIM_RECENTS_ICON_PACK,
                    selectedPackage
                )

                mDialog?.dismiss()
            }

        return view
    }

    override fun onDismiss(dialog: DialogInterface?) {
        mDialog = null
    }

    private class IconAdapter(
        ctx: Context,
        supportedPackages: Map<String, IconPackInfo>
    ) : BaseAdapter() {

        private val mSupportedPackages: ArrayList<IconPackInfo>
        private val mLayoutInflater: LayoutInflater =
            LayoutInflater.from(ctx)

        private val mCurrentIconPack: String? =
            Settings.System.getString(
                ctx.contentResolver,
                Settings.System.SLIM_RECENTS_ICON_PACK
            )

        private var mCurrentIconPackPosition = -1

        init {
            mSupportedPackages =
                ArrayList(supportedPackages.values)

            mSupportedPackages.sortWith { lhs, rhs ->
                lhs.label.toString()
                    .compareTo(rhs.label.toString(), true)
            }

            val res: Resources = ctx.resources
            val defaultLabel =
                res.getString(R.string.default_iconpack_title)
            val icon =
                res.getDrawable(android.R.drawable.sym_def_app_icon)

            mSupportedPackages.add(
                0,
                IconPackInfo(defaultLabel, icon, "")
            )
        }

        override fun getCount(): Int = mSupportedPackages.size

        override fun getItem(position: Int): String =
            mSupportedPackages[position].packageName

        override fun getItemId(position: Int): Long = 0

        fun isCurrentIconPack(position: Int): Boolean =
            mCurrentIconPackPosition == position

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup?
        ): View {

            val view = convertView
                ?: mLayoutInflater.inflate(
                    R.layout.iconpack_view_radio,
                    parent,
                    false
                )

            val info = mSupportedPackages[position]

            val txtView = view.findViewById<TextView>(R.id.title)
            txtView.text = info.label

            val imgView = view.findViewById<ImageView>(R.id.icon)
            imgView.setImageDrawable(info.icon)

            val radioButton =
                view.findViewById<RadioButton>(R.id.radio)

            val isCurrent =
                info.packageName == mCurrentIconPack

            radioButton.isChecked = isCurrent

            if (isCurrent) {
                mCurrentIconPackPosition = position
            }

            return view
        }
    }

    private fun getSupportedPackages(
        context: Context
    ): Map<String, IconPackInfo> {

        val packages = HashMap<String, IconPackInfo>()
        val packageManager = context.packageManager

        var intent = Intent()

        for (action in sSupportedActions) {
            intent.action = action
            for (r in packageManager.queryIntentActivities(intent, 0)) {
                val info = IconPackInfo(r, packageManager)
                packages[r.activityInfo.packageName] = info
            }
        }

        intent = Intent(Intent.ACTION_MAIN)

        for (category in sSupportedCategories) {
            intent.addCategory(category)

            for (r in packageManager.queryIntentActivities(intent, 0)) {
                val info = IconPackInfo(r, packageManager)
                packages[r.activityInfo.packageName] = info
            }

            intent.removeCategory(category)
        }

        return packages
    }

    private class IconPackInfo {
        var packageName: String = ""
        var label: CharSequence = ""
        var icon: Drawable? = null

        constructor(r: ResolveInfo, packageManager: PackageManager) {
            packageName = r.activityInfo.packageName
            icon = r.loadIcon(packageManager)
            label = r.loadLabel(packageManager)
        }

        constructor()

        constructor(
            label: String,
            icon: Drawable,
            packageName: String
        ) {
            this.label = label
            this.icon = icon
            this.packageName = packageName
        }
    }
}
