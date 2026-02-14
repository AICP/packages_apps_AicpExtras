/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.extras.preference

import android.content.Context
import android.graphics.Color
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
import androidx.core.graphics.ColorUtils
import androidx.preference.Preference
import com.aicp.extras.R
import java.util.function.Consumer

class StatusBarClockChipStylePreference(
    context: Context,
    attrs: AttributeSet
) : Preference(context, attrs) {

    companion object {
        private const val BACKGROUND_BLUR_RADIUS = 80
        private const val WINDOW_BG_ALPHA_WITH_BLUR = 105
        private const val WINDOW_BG_ALPHA_NO_BLUR = 255
        private const val DIM_AMOUNT_WITH_BLUR = 0.1f
        private const val DIM_AMOUNT_NO_BLUR = 0.4f

        private const val SETTING_KEY = "statusbar_clock_chip"
        private const val SYSTEMUI_PACKAGE = "com.android.systemui"

        /** Outline-only chip styles (transparent fill) */
        private val OUTLINE_CHIP_STYLES = setOf(2, 8)

        private val SYSTEMUI_CHIP_DRAWABLES = arrayOf(
            "sb_date_bg1",
            "sb_date_bg2",
            "sb_date_bg3",
            "sb_date_bg4",
            "sb_date_bg5",
            "sb_date_bg6",
            "sb_date_bg7",
            "sb_date_bg8",
            "sb_date_bg9",
            "sb_date_bg10",
            "sb_date_bg11",
            "sb_date_bg12",
        )
    }

    private var mWindowBackgroundDrawable: Drawable? = null
    private var mDecorBackgroundDrawable: Drawable? = null
    private var mBlurEnabledListener: Consumer<Boolean>? = null
    private var mDialog: AlertDialog? = null

    private val mEntries: Array<String> =
        context.resources.getStringArray(R.array.statusbar_clock_chip_entries)

    private val mEntryValues: Array<String> =
        context.resources.getStringArray(R.array.statusbar_clock_chip_values)

    private val mPm = context.packageManager

    private fun getChipDrawableForStyle(styleIndex: Int): Drawable? {
        if (styleIndex < 1 || styleIndex > SYSTEMUI_CHIP_DRAWABLES.size) return null
        return try {
            val sysUiRes = mPm.getResourcesForApplication(SYSTEMUI_PACKAGE)
            val name = SYSTEMUI_CHIP_DRAWABLES[styleIndex - 1]
            val id = sysUiRes.getIdentifier(name, "drawable", SYSTEMUI_PACKAGE)
            if (id != 0) sysUiRes.getDrawable(id, context.theme) else null
        } catch (e: Exception) {
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

    private fun getTextColorOnAccent(): Int {
        val tv = TypedValue()
        if (!context.theme.resolveAttribute(android.R.attr.colorAccent, tv, true)) {
            return Color.WHITE
        }
        val accent = if (tv.resourceId != 0) context.getColor(tv.resourceId) else tv.data
        val luminance = ColorUtils.calculateLuminance(accent)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    private fun indexOfValue(value: String): Int {
        for (i in mEntryValues.indices) {
            if (mEntryValues[i] == value) return i
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

        listView.adapter = ChipStyleAdapter(
            context,
            mEntries,
            mEntryValues,
            this,
            selectedIndex
        )

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

                val titleId =
                    context.resources.getIdentifier("alertTitle", "id", "android")

                val titleView =
                    if (titleId != 0)
                        dialog.window?.decorView?.findViewById<TextView>(titleId)
                    else null

                titleView?.setTextColor(accent)
            }

            val w = (dialog as AlertDialog).window
            if (w != null) clearDialogSolidBackgrounds(w.decorView)
        }

        mDialog?.setOnDismissListener {
            val listener = mBlurEnabledListener
            val dialog = mDialog
            if (listener != null && dialog != null) {
                dialog.window?.windowManager
                    ?.removeCrossWindowBlurEnabledListener(listener)
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
        if (decor != null && mDecorBackgroundDrawable != null) {
            decor.background = mDecorBackgroundDrawable
            decor.clipToOutline = true
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setBackgroundBlurRadius(BACKGROUND_BLUR_RADIUS)
        window.setDimAmount(DIM_AMOUNT_WITH_BLUR)

        mWindowBackgroundDrawable?.alpha = WINDOW_BG_ALPHA_WITH_BLUR
        mDecorBackgroundDrawable?.alpha = WINDOW_BG_ALPHA_WITH_BLUR

        mBlurEnabledListener = Consumer { enabled -> updateWindowForBlur(enabled) }

        window.windowManager.addCrossWindowBlurEnabledListener(mBlurEnabledListener!!)
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

    private class ChipStyleAdapter(
        context: Context,
        private val mEntries: Array<String>,
        private val mEntryValues: Array<String>,
        private val mPreference: StatusBarClockChipStylePreference,
        private val mSelectedIndex: Int
    ) : android.widget.BaseAdapter() {

        private val mInflater = LayoutInflater.from(context)
        private val mSelectedBackground =
            context.getDrawable(R.drawable.logo_style_item_selected)

        override fun getCount() = mEntries.size
        override fun getItem(position: Int) = mEntries[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView
                ?: mInflater.inflate(R.layout.m3_chip_style_list_item, parent, false)

            view.background =
                if (position == mSelectedIndex) mSelectedBackground else null

            val text: TextView = view.findViewById(android.R.id.text1)
            val chipContainer: View = view.findViewById(R.id.chip_preview_container)
            val timePreview: TextView = view.findViewById(R.id.chip_time_preview)

            text.text = mEntries[position]

            val styleIndex =
                if (position < mEntryValues.size) mEntryValues[position].toInt() else 0

            val chipBg = mPreference.getChipDrawableForStyle(styleIndex)

            if (styleIndex == 0) {
                chipContainer.setBackgroundResource(R.drawable.chip_preview_disabled)

                val tv = TypedValue()
                if (mPreference.context.theme.resolveAttribute(
                        android.R.attr.textColorSecondary,
                        tv,
                        true
                    )
                ) {
                    timePreview.setTextColor(
                        if (tv.resourceId != 0)
                            mPreference.context.getColor(tv.resourceId)
                        else tv.data
                    )
                }
            } else {
                chipContainer.background = chipBg

                if (OUTLINE_CHIP_STYLES.contains(styleIndex)) {
                    val tv = TypedValue()
                    if (mPreference.context.theme.resolveAttribute(
                            android.R.attr.textColorPrimary,
                            tv,
                            true
                        )
                    ) {
                        timePreview.setTextColor(
                            if (tv.resourceId != 0)
                                mPreference.context.getColor(tv.resourceId)
                            else tv.data
                        )
                    }
                } else {
                    timePreview.setTextColor(mPreference.getTextColorOnAccent())
                }
            }

            chipContainer.visibility = View.VISIBLE
            return view
        }
    }
}
