/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-FileCopyrightText: Android Ice Cold Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aicp.extras.preference

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.provider.Settings
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.aicp.extras.R
import java.util.function.Consumer

class StatusBarVolteStylePreference(context: Context, attrs: AttributeSet) :
    Preference(context, attrs) {

    private var mWindowBackgroundDrawable: Drawable? = null
    private var mDecorBackgroundDrawable: Drawable? = null
    private var mBlurEnabledListener: Consumer<Boolean>? = null
    private var mDialog: AlertDialog? = null

    private val mEntries: Array<String>
    private val mEntryValues: Array<String>
    private val mPm = context.packageManager

    init {
        mEntries = context.resources.getStringArray(R.array.volte_icon_style_array)
        mEntryValues = context.resources.getStringArray(R.array.volte_icon_style_values)
    }

    private fun getThemeIconColor(): Int {
        val tv = TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.colorControlNormal, tv, true)) {
            return if (tv.resourceId != 0) context.getColor(tv.resourceId) else tv.data
        }
        if (context.theme.resolveAttribute(android.R.attr.textColorPrimary, tv, true)) {
            return if (tv.resourceId != 0) context.getColor(tv.resourceId) else tv.data
        }
        return 0xff000000.toInt()
    }

    private fun getVolteDrawableForStyle(styleIndex: Int): Drawable? {
        if (styleIndex < 0 || styleIndex >= SYSTEMUI_LOGO_DRAWABLES.size) return null
        return try {
            val sysUiRes = mPm.getResourcesForApplication(SYSTEMUI_PACKAGE)
            val name = SYSTEMUI_LOGO_DRAWABLES[styleIndex]
            val id = sysUiRes.getIdentifier(name, "drawable", SYSTEMUI_PACKAGE)
            if (id != 0) {
                var d = sysUiRes.getDrawable(id, context.theme)
                d = d?.mutate()
                d?.setTintList(ColorStateList.valueOf(getThemeIconColor()))
                d
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun getCurrentValue(): Int {
        return Settings.System.getIntForUser(
            context.contentResolver,
            SETTING_KEY,
            0,
            UserHandle.USER_CURRENT
        )
    }

    private fun updateSummary() {
        val value = getCurrentValue()
        val index = indexOfValue(value.toString())
        summary = if (index >= 0) mEntries[index] else mEntries[0]
    }

    private fun indexOfValue(value: String): Int {
        for (i in mEntryValues.indices) {
            if (mEntryValues[i] == value) {
                return i
            }
        }
        return -1
    }

    private fun clearDialogSolidBackgrounds(root: View) {
        val listView = root.findViewById<View>(R.id.logo_style_list)
        val ourContentRoot =
            if (listView != null && listView.parent is View && (listView.parent as View).parent is View)
                (listView.parent as View).parent as View
            else null

        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                clearOpaqueBackgroundsRecursive(root.getChildAt(i), ourContentRoot)
            }
        }
    }

    private fun clearOpaqueBackgroundsRecursive(view: View, excludeSubtree: View?) {
        if (view === excludeSubtree) return
        view.setBackgroundResource(android.R.color.transparent)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                clearOpaqueBackgroundsRecursive(view.getChildAt(i), excludeSubtree)
            }
        }
    }

    override fun onAttachedToHierarchy(pm: androidx.preference.PreferenceManager) {
        super.onAttachedToHierarchy(pm)
        updateSummary()
    }

    override fun onClick() {

        val view = View.inflate(context, R.layout.dialog_statusbar_logo_style, null)
        val listView: ListView = view.findViewById(R.id.logo_style_list)

        val currentValue = getCurrentValue()
        var selectedIndex = indexOfValue(currentValue.toString())
        if (selectedIndex < 0) selectedIndex = 0

        listView.adapter =
            VolteStyleAdapter(context, mEntries, mEntryValues, this, selectedIndex)

        listView.setOnItemClickListener { _, _, position, _ ->
            val value = mEntryValues[position]

            Settings.System.putIntForUser(
                context.contentResolver,
                SETTING_KEY,
                value.toInt(),
                UserHandle.USER_CURRENT
            )

            summary = mEntries[position]
            mDialog?.dismiss()
        }

        val builder = AlertDialog.Builder(context, R.style.LogoStyleDialogTheme)
        builder.setTitle(title)
        builder.setView(view)
        builder.setNegativeButton(android.R.string.cancel, null)

        mDialog = builder.create()

        val window = mDialog?.window
        if (window != null) {

            val density = context.resources.displayMetrics.density
            val maxWidthPx = (320 * density + 0.5f).toInt()
            val screenWidth = context.resources.displayMetrics.widthPixels

            val lp = window.attributes
            lp.width = minOf(maxWidthPx, (screenWidth * 0.85f).toInt())
            window.attributes = lp

            setupWindowBlur(window)
        }

        mDialog?.setOnShowListener { dialog ->

            val tv = TypedValue()
            var accent = 0

            if (context.theme.resolveAttribute(android.R.attr.colorAccent, tv, true)) {
                accent = if (tv.resourceId != 0) context.getColor(tv.resourceId) else tv.data
            }

            if (accent != 0) {

                val negativeButton =
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE)
                negativeButton?.setTextColor(accent)

                val titleId = context.resources.getIdentifier("alertTitle", "id", "android")

                val titleView =
                    if (titleId != 0)
                        dialog.window?.decorView?.findViewById<TextView>(titleId)
                    else null

                titleView?.setTextColor(accent)
            }

            val w = (dialog as AlertDialog).window
            if (w != null) {
                clearDialogSolidBackgrounds(w.decorView)
            }
        }

        mDialog?.setOnDismissListener {

            val listener = mBlurEnabledListener
            val dialog = mDialog

            if (listener != null && dialog != null) {
                val w = dialog.window
               if (w != null) {
                   w.windowManager.removeCrossWindowBlurEnabledListener(listener)
               }
            }
            mBlurEnabledListener = null
            mWindowBackgroundDrawable = null
            mDecorBackgroundDrawable = null
            mDialog = null
        }

        mDialog?.show()
    }

    private fun setupWindowBlur(window: Window?) {

        if (window == null) return

        mWindowBackgroundDrawable =
            context.getDrawable(R.drawable.dialog_logo_style_window_background)?.mutate()

        window.setBackgroundDrawable(mWindowBackgroundDrawable)

        mDecorBackgroundDrawable =
            context.getDrawable(R.drawable.dialog_logo_style_window_background)?.mutate()

        val decor = window.decorView
        decor?.let {
            it.background = mDecorBackgroundDrawable
            it.clipToOutline = true
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        window.setBackgroundBlurRadius(BACKGROUND_BLUR_RADIUS)
        window.setDimAmount(DIM_AMOUNT_WITH_BLUR)

        mWindowBackgroundDrawable?.alpha = WINDOW_BG_ALPHA_WITH_BLUR
        mDecorBackgroundDrawable?.alpha = WINDOW_BG_ALPHA_WITH_BLUR

        mBlurEnabledListener = Consumer { enabled ->
            updateWindowForBlur(enabled)
        }

        mBlurEnabledListener?.let {
            window.windowManager.addCrossWindowBlurEnabledListener(it)
        }

        val enabled = window.windowManager.isCrossWindowBlurEnabled
        updateWindowForBlur(enabled)
    }

    private fun updateWindowForBlur(blursEnabled: Boolean) {

        val dialog = mDialog ?: return
        val window = dialog.window ?: return

        val alpha =
            if (blursEnabled && BACKGROUND_BLUR_RADIUS > 0)
                WINDOW_BG_ALPHA_WITH_BLUR
            else
                WINDOW_BG_ALPHA_NO_BLUR

        mWindowBackgroundDrawable?.alpha = alpha
        mDecorBackgroundDrawable?.alpha = alpha

        window.setDimAmount(
            if (blursEnabled && BACKGROUND_BLUR_RADIUS > 0)
                DIM_AMOUNT_WITH_BLUR
            else
                DIM_AMOUNT_NO_BLUR
        )

        window.setBackgroundBlurRadius(BACKGROUND_BLUR_RADIUS)
        window.attributes = window.attributes
    }

    private class VolteStyleAdapter(
        context: Context,
        private val mEntries: Array<String>,
        private val mEntryValues: Array<String>,
        private val mPreference: StatusBarVolteStylePreference,
        private val mSelectedIndex: Int
    ) : android.widget.BaseAdapter() {

        private val mInflater = LayoutInflater.from(context)
        private val mSelectedBackground =
            context.getDrawable(R.drawable.logo_style_item_selected)

        override fun getCount(): Int = mEntries.size

        override fun getItem(position: Int): Any = mEntries[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            var view = convertView

            if (view == null) {
                view = mInflater.inflate(R.layout.m3_logo_style_list_item, parent, false)
            }

            view!!.background =
                if (position == mSelectedIndex) mSelectedBackground else null

            val text: TextView = view.findViewById(android.R.id.text1)
            val icon: android.widget.ImageView = view.findViewById(R.id.logo_preview)

            text.text = mEntries[position]

            val styleIndex =
                if (position < mEntryValues.size) mEntryValues[position].toInt() else 0

            val d = mPreference.getVolteDrawableForStyle(styleIndex)

            icon.setImageDrawable(d)
            icon.visibility = if (d != null) View.VISIBLE else View.GONE

            return view
        }
    }

    companion object {

        private const val BACKGROUND_BLUR_RADIUS = 80
        private const val WINDOW_BG_ALPHA_WITH_BLUR = 105
        private const val WINDOW_BG_ALPHA_NO_BLUR = 255

        private const val DIM_AMOUNT_WITH_BLUR = 0.1f
        private const val DIM_AMOUNT_NO_BLUR = 0.4f

        private const val SETTING_KEY = "volte_icon_style"
        private const val SYSTEMUI_PACKAGE = "com.android.systemui"

        private val SYSTEMUI_LOGO_DRAWABLES = arrayOf(
            "ic_volte",
            "ic_volte1",
            "ic_volte2",
            "ic_volte3",
            "ic_volte_hd",
            "ic_volte_hd2",
            "ic_volte_miui",
            "ic_volte_emui",
            "ic_volte_margaritov",
            "ic_volte_margaritov2",
            "ic_volte_vivo",
            "ic_volte_aris",
            "ic_volte_beast",
            "ic_volte_ios",
            "ic_volte_lr",
            "ic_volte_realme",
            "ic_volte_typeA",
            "ic_volte_typeB",
            "ic_volte_typeC",
            "ic_volte_typeD",
            "ic_volte_typeE",
            "ic_volte_vcircle",
            "ic_volte_vimeo",
            "ic_volte_volit",
            "ic_volte_zirco"
        )
    }
}
