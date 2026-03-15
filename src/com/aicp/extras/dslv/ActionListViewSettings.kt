/*
 * Copyright (C) 2014 Slimroms
 * Copyright (C) 2015-2026 Android Ice Cold Project
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
package com.aicp.extras.dslv

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.ListFragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.aicp.extras.R
import com.aicp.extras.TitleProvider
import com.aicp.extras.utils.SlimShortcutPickerHelper
import com.aicp.gear.util.ActionConfig
import com.aicp.gear.util.ActionConstants
import com.aicp.gear.util.ActionHelper
import com.aicp.gear.util.DeviceUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ActionListViewSettings : ListFragment(), SlimShortcutPickerHelper.OnPickListener, TitleProvider {

    companion object {
        const val DLG_SHOW_ACTION_DIALOG = 0
        const val DLG_SHOW_ICON_PICKER = 1
        const val DLG_DELETION_NOT_ALLOWED = 2
        const val DLG_SHOW_HELP_SCREEN = 3
        const val DLG_RESET_TO_DEFAULT = 4

        const val MENU_HELP = Menu.FIRST
        const val MENU_ADD = MENU_HELP + 1
        const val MENU_RESET = MENU_ADD + 1

        const val NAV_BAR = 0
        const val PIE = 1
        const val PIE_SECOND = 2
        const val NAV_RING = 3
        const val LOCKSCREEN_SHORTCUT = 4
        const val POWER_MENU_SHORTCUT = 5
        const val SHAKE_EVENTS_DISABLED = 6
        const val RECENT_APP_SIDEBAR = 7

        const val DEFAULT_MAX_ACTION_NUMBER = 5
        const val REQUEST_PICK_CUSTOM_ICON = 1000
    }

    private var mActionMode: Int = 0
    private var mMaxAllowedActions: Int = DEFAULT_MAX_ACTION_NUMBER
    private var mUseAppPickerOnly: Boolean = false
    private var mUseFullAppsOnly: Boolean = false
    private var mDisableLongpress: Boolean = false
    private var mDisableIconPicker: Boolean = false
    private var mDisableDeleteLastEntry: Boolean = false

    private lateinit var mDisableMessage: TextView

    private lateinit var mActionConfigsAdapter: ActionConfigsAdapter

    private var mActionConfigs: ArrayList<ActionConfig> = ArrayList()

    private var mAdditionalFragmentAttached: Boolean = false
    private var mAdditionalFragment: String? = null
    private lateinit var mDivider: View

    private var mPendingIndex: Int = -1
    private var mPendingLongpress: Boolean = false
    private var mPendingNewAction: Boolean = false

    private lateinit var mActionDialogValues: Array<String>
    private lateinit var mActionDialogEntries: Array<String>
    private lateinit var mActionValuesKey: String
    private lateinit var mActionEntriesKey: String

    private lateinit var mActivity: Activity
    private lateinit var mPicker: SlimShortcutPickerHelper

    private lateinit var mImageTmp: File

    private val onDrop = DragSortListView.DropListener { from, to ->
        val item = mActionConfigs[from]
        mActionConfigs.removeAt(from)
        mActionConfigs.add(to, item)
        mActionConfigsAdapter.notifyDataSetChanged()
        setConfig(mActionConfigs, false)
    }

    private val onRemove = DragSortListView.RemoveListener { which ->
        val item = mActionConfigs[which]
        mActionConfigs.removeAt(which)
        if (mDisableDeleteLastEntry && mActionConfigs.isEmpty()) {
            mActionConfigs.add(which, item)
            mActionConfigsAdapter.notifyDataSetChanged()
            showDialogInner(DLG_DELETION_NOT_ALLOWED, 0, false, false)
        } else {
            deleteIconFileIfPresent(item)
            setConfig(mActionConfigs, false)
            if (mActionConfigs.isEmpty()) {
                showDisableMessage(true)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dslv_action_list_view_main, container, false)
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        mActivity = activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val res: Resources = resources

        mActionMode = arguments?.getInt("actionMode", NAV_BAR) ?: NAV_BAR
        mMaxAllowedActions = arguments?.getInt("maxAllowedActions", DEFAULT_MAX_ACTION_NUMBER) ?: DEFAULT_MAX_ACTION_NUMBER
        mAdditionalFragment = arguments?.getString("fragment")
        mActionValuesKey = arguments?.getString("actionValues", "shortcut_action_values") ?: "shortcut_action_values"
        mActionEntriesKey = arguments?.getString("actionEntries", "shortcut_action_entries") ?: "shortcut_action_entries"
        mDisableLongpress = arguments?.getBoolean("disableLongpress", false) ?: false
        mUseAppPickerOnly = arguments?.getBoolean("useAppPickerOnly", false) ?: false
        mUseFullAppsOnly = arguments?.getBoolean("useOnlyFullAppPicker", false) ?: false
        mDisableIconPicker = arguments?.getBoolean("disableIconPicker", false) ?: false
        mDisableIconPicker = true
        mDisableDeleteLastEntry = arguments?.getBoolean("disableDeleteLastEntry", false) ?: false

        mDisableMessage = view.findViewById(R.id.disable_message)

        val finalActionDialogArray = DeviceUtils.filterUnsupportedDeviceFeatures(
            mActivity,
            res.getStringArray(res.getIdentifier(mActionValuesKey, "array", "com.aicp.extras")),
            res.getStringArray(res.getIdentifier(mActionEntriesKey, "array", "com.aicp.extras"))
        )
        mActionDialogValues = finalActionDialogArray?.values ?: emptyArray()
        mActionDialogEntries = finalActionDialogArray?.entries ?: emptyArray()

        mPicker = SlimShortcutPickerHelper(mActivity, this)

        val folder = File(Environment.getExternalStorageDirectory().toString() + File.separator + ".aicp" + File.separator + "icons")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        mImageTmp = File(folder.toString() + File.separator + "shortcut.tmp")

        val listView = getListView() as DragSortListView
        listView.setDropListener(onDrop)
        listView.setRemoveListener(onRemove)

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (mUseFullAppsOnly) {
                mPendingIndex = position
                mPendingLongpress = false
                mPendingNewAction = false
                mPicker.pickShortcut(id, true)
            } else if (!mUseAppPickerOnly) {
                showDialogInner(DLG_SHOW_ACTION_DIALOG, position, false, false)
            } else {
                mPendingIndex = position
                mPendingLongpress = false
                mPendingNewAction = false
                mPicker.pickShortcut(id)
            }
        }

        if (!mDisableLongpress) {
            listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                if (mUseFullAppsOnly) {
                    mPendingIndex = position
                    mPendingLongpress = true
                    mPendingNewAction = false
                    mPicker.pickShortcut(id, true)
                } else if (!mUseAppPickerOnly) {
                    showDialogInner(DLG_SHOW_ACTION_DIALOG, position, true, false)
                } else {
                    mPendingIndex = position
                    mPendingLongpress = true
                    mPendingNewAction = false
                    mPicker.pickShortcut(id)
                }
                true
            }
        }

        mActionConfigs = getConfig() ?: ArrayList()

        mActionConfigsAdapter = ActionConfigsAdapter(mActivity, mActionConfigs)
        listAdapter = mActionConfigsAdapter
        showDisableMessage(mActionConfigs.isEmpty())

        mDivider = view.findViewById(R.id.divider)
        loadAdditionalFragment()

        val preferences = mActivity.getSharedPreferences("dslv_settings", Activity.MODE_PRIVATE)
        if (!preferences.getBoolean("first_help_shown_mode_$mActionMode", false)) {
            preferences.edit().putBoolean("first_help_shown_mode_$mActionMode", true).commit()
            showDialogInner(DLG_SHOW_HELP_SCREEN, 0, false, false)
        }

        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mAdditionalFragmentAttached) {
            val fragmentManager = fragmentManager
            val fragment = fragmentManager?.findFragmentById(R.id.fragment_container)
            if (fragment != null && fragmentManager != null && !fragmentManager.isDestroyed) {
                fragmentManager.beginTransaction().remove(fragment).commit()
            }
        }
    }

    override fun getTitle(): CharSequence {
        if (mAdditionalFragmentAttached) {
            val fragmentManager = fragmentManager
            val fragment = fragmentManager?.findFragmentById(R.id.fragment_container)
            if (fragment is PreferenceFragmentCompat) {
                val preferenceScreen = fragment.preferenceScreen
                if (preferenceScreen != null) {
                    return preferenceScreen.title ?: ""
                }
            }
        }
        return ""
    }

    private fun loadAdditionalFragment() {
        if (!mAdditionalFragment.isNullOrEmpty()) {
            try {
                val classAdditionalFragment = Class.forName(mAdditionalFragment)
                val fragment = classAdditionalFragment.newInstance() as Fragment
                fragmentManager?.beginTransaction()?.replace(R.id.fragment_container, fragment)?.commit()
                if (::mDivider.isInitialized) {
                    mDivider.visibility = View.VISIBLE
                }
                mAdditionalFragmentAttached = true
            } catch (e: Exception) {
                mAdditionalFragmentAttached = false
                e.printStackTrace()
            }
        }
    }

    override fun shortcutPicked(action: String, description: String, bmp: Bitmap?, isApplication: Boolean) {
        if (mPendingIndex == -1) {
            return
        }
        if (bmp != null && !mPendingLongpress) {
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                val folder = File(Environment.getExternalStorageDirectory().toString() + File.separator + ".aicp" + File.separator + "icons")
                folder.mkdirs()
                val fileName = folder.toString() + File.separator + "shortcut_" + System.currentTimeMillis() + ".png"
                try {
                    val out = FileOutputStream(fileName)
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        if (mPendingNewAction) {
            addNewAction(action, description)
        } else {
            updateAction(action, description, null, mPendingIndex, mPendingLongpress)
        }
        mPendingLongpress = false
        mPendingNewAction = false
        mPendingIndex = -1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SlimShortcutPickerHelper.REQUEST_PICK_SHORTCUT ||
                requestCode == SlimShortcutPickerHelper.REQUEST_PICK_APPLICATION ||
                requestCode == SlimShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data)
            } else if (requestCode == REQUEST_PICK_CUSTOM_ICON && mPendingIndex != -1) {
                if (mImageTmp.length() == 0L || !mImageTmp.exists()) {
                    mPendingIndex = -1
                    Toast.makeText(mActivity, getString(R.string.shortcut_image_not_valid), Toast.LENGTH_LONG).show()
                    return
                }
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    val folder = File(Environment.getExternalStorageDirectory().toString() + File.separator + ".aicp" + File.separator + "icons")
                    folder.mkdirs()
                    val image = File(folder.toString() + File.separator + "shortcut_" + System.currentTimeMillis() + ".png")
                    val path = image.absolutePath
                    mImageTmp.renameTo(image)
                    image.setReadable(true, false)
                    updateAction(null, null, path, mPendingIndex, false)
                    mPendingIndex = -1
                }
            }
        } else {
            if (mImageTmp.exists()) {
                mImageTmp.delete()
            }
            mPendingLongpress = false
            mPendingNewAction = false
            mPendingIndex = -1
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateAction(action: String?, description: String?, icon: String?, which: Int, longpress: Boolean) {
        if (!longpress && checkForDuplicateMainNavActions(action)) {
            return
        }

        val actionConfig = mActionConfigs[which]

        if (!longpress) {
            deleteIconFileIfPresent(actionConfig)
        }

        if (icon != null) {
            actionConfig.icon = icon
        } else {
            if (longpress) {
                actionConfig.longpressAction = action ?: ""
                actionConfig.longpressActionDescription = description ?: ""
            } else {
                deleteIconFileIfPresent(actionConfig)
                actionConfig.clickAction = action ?: ""
                actionConfig.clickActionDescription = description ?: ""
                actionConfig.icon = ActionConstants.ICON_EMPTY
            }
        }

        mActionConfigsAdapter.notifyDataSetChanged()
        setConfig(mActionConfigs, false)
    }

    private fun checkForDuplicateMainNavActions(action: String?): Boolean {
        for (i in 0 until mActionConfigs.size) {
            val actionConfig = mActionConfigs[i]
            if (actionConfig.clickAction == action) {
                Toast.makeText(mActivity, getString(R.string.shortcut_duplicate_entry), Toast.LENGTH_LONG).show()
                return true
            }
        }
        return false
    }

    private fun deleteIconFileIfPresent(actionConfig: ActionConfig) {
        val oldImage = File(actionConfig.icon)
        if (oldImage.exists()) {
            oldImage.delete()
        }
        val oldImage2 = File(actionConfig.clickAction?.replace(".*?hasExtraIcon=".toRegex(), "") ?: "")
        if (oldImage2.exists()) {
            oldImage2.delete()
        }
    }

    private fun showDisableMessage(show: Boolean) {
        if (::mDisableMessage.isInitialized && !mDisableDeleteLastEntry) {
            mDisableMessage.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            MENU_ADD -> {
                if (mActionConfigs.size == mMaxAllowedActions) {
                    Toast.makeText(mActivity, getString(R.string.shortcut_action_max), Toast.LENGTH_LONG).show()
                    return true
                }
                if (mUseFullAppsOnly) {
                    mPendingIndex = 0
                    mPendingLongpress = false
                    mPendingNewAction = true
                    mPicker.pickShortcut(id, true)
                } else if (!mUseAppPickerOnly) {
                    showDialogInner(DLG_SHOW_ACTION_DIALOG, 0, false, true)
                } else {
                    mPendingIndex = 0
                    mPendingLongpress = false
                    mPendingNewAction = true
                    mPicker.pickShortcut(id)
                }
            }
            MENU_RESET -> showDialogInner(DLG_RESET_TO_DEFAULT, 0, false, true)
            MENU_HELP -> showDialogInner(DLG_SHOW_HELP_SCREEN, 0, false, true)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(0, MENU_HELP, 0, R.string.help).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, MENU_RESET, 0, R.string.shortcut_action_reset).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, MENU_ADD, 0, R.string.shortcut_action_add).setIcon(R.drawable.ic_menu_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    private fun addNewAction(action: String, description: String) {
        if (checkForDuplicateMainNavActions(action)) {
            return
        }
        val actionConfig = ActionConfig(
            action, description,
            ActionConstants.ACTION_NULL, getString(R.string.shortcut_action_none),
            ActionConstants.ICON_EMPTY
        )
        mActionConfigs.add(actionConfig)
        mActionConfigsAdapter.notifyDataSetChanged()
        showDisableMessage(false)
        setConfig(mActionConfigs, false)
    }

    private fun getConfig(): ArrayList<ActionConfig>? {
        return when (mActionMode) {
            RECENT_APP_SIDEBAR -> ActionHelper.getRecentAppSidebarConfigWithDescription(
                mActivity, mActionValuesKey, mActionEntriesKey)
            else -> ArrayList()
        }
    }

    private fun setConfig(actionConfigs: ArrayList<ActionConfig>?, reset: Boolean) {
        when (mActionMode) {
            RECENT_APP_SIDEBAR -> {
                actionConfigs?.let {
                    ActionHelper.setRecentAppSidebarConfig(mActivity, it, reset)
                }
            }
            else -> {}
        }
    }

    private fun showDialogInner(id: Int, which: Int, longpress: Boolean, newAction: Boolean) {
        val newFragment = MyAlertDialogFragment.newInstance(id, which, longpress, newAction)
        newFragment.setTargetFragment(this, 0)
        newFragment.show(fragmentManager!!, "dialog $id")
    }

    inner class ActionConfigsAdapter(context: Context, private val actionConfigs: ArrayList<ActionConfig>) :
        ArrayAdapter<ActionConfig>(context, R.layout.dslv_action_list_view_item, R.id.click_action_description, actionConfigs) {

        private var mIconColor: Int

        init {
            val attrs = intArrayOf(android.R.attr.textColorPrimary)
            val ta = context.theme.obtainStyledAttributes(attrs)
            mIconColor = ta.getColor(0, 0xff808080.toInt())
            ta.recycle()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v = super.getView(position, convertView, parent)

            if (v !== convertView && v != null) {
                val holder = ViewHolder()
                val longpressActionDescription = v.findViewById<TextView>(R.id.longpress_action_description)
                val icon = v.findViewById<ImageView>(R.id.icon)

                if (mDisableLongpress) {
                    longpressActionDescription.visibility = View.GONE
                } else {
                    holder.longpressActionDescriptionView = longpressActionDescription
                }

                holder.iconView = icon
                v.tag = holder
            }

            val holder = v.tag as ViewHolder
            val actionConfig = getItem(position)!!

            if (!mDisableLongpress) {
                holder.longpressActionDescriptionView.text = getString(R.string.shortcut_action_longpress) + " " +
                    actionConfig.longpressActionDescription ?: ""
            }

            var d: Drawable? = null
            val iconUri = actionConfig.icon
            if (iconUri != null && iconUri.startsWith(ActionConstants.SYSTEM_ICON_IDENTIFIER)) {
                d?.setTint(mIconColor)
            }
            holder.iconView.setImageDrawable(d)

            if (!mDisableIconPicker && holder.iconView.drawable != null) {
                holder.iconView.setOnClickListener {
                    mPendingIndex = position
                    showDialogInner(DLG_SHOW_ICON_PICKER, 0, false, false)
                }
            }

            return v
        }

        inner class ViewHolder {
            lateinit var longpressActionDescriptionView: TextView
            lateinit var iconView: ImageView
        }
    }

    class MyAlertDialogFragment : DialogFragment() {

        companion object {
            fun newInstance(id: Int, which: Int, longpress: Boolean, newAction: Boolean): MyAlertDialogFragment {
                val frag = MyAlertDialogFragment()
                val args = Bundle()
                args.putInt("id", id)
                args.putInt("which", which)
                args.putBoolean("longpress", longpress)
                args.putBoolean("newAction", newAction)
                frag.arguments = args
                return frag
            }
        }

        private fun getOwner(): ActionListViewSettings {
            return targetFragment as ActionListViewSettings
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val id = arguments?.getInt("id") ?: 0
            val which = arguments?.getInt("which") ?: 0
            val longpress = arguments?.getBoolean("longpress") ?: false
            val newAction = arguments?.getBoolean("newAction") ?: false
            return when (id) {
                DLG_RESET_TO_DEFAULT -> {
                    AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.shortcut_action_reset)
                        .setMessage(R.string.reset_message)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            val actionConfigs = getOwner().getConfig()
                            for (i in 0 until actionConfigs!!.size) {
                                getOwner().deleteIconFileIfPresent(actionConfigs[i])
                            }
                            getOwner().setConfig(null, true)
                            getOwner().mActionConfigs.clear()
                            getOwner().mActionConfigsAdapter.notifyDataSetChanged()
                            val newConfigs = getOwner().getConfig()
                            val newConfigsSize = newConfigs!!.size
                            for (i in 0 until newConfigsSize) {
                                getOwner().mActionConfigs.add(newConfigs[i])
                            }
                            if (newConfigsSize == 0) {
                                val emptyAction = ActionConfig(null, null, null, null, null)
                                getOwner().mActionConfigs.add(emptyAction)
                                getOwner().mActionConfigs.remove(emptyAction)
                            }
                            getOwner().mActionConfigsAdapter.notifyDataSetChanged()
                            getOwner().showDisableMessage(newConfigsSize == 0)
                        }
                        .create()
                }
                DLG_SHOW_HELP_SCREEN -> {
                    val res = resources
                    val actionMode = when (getOwner().mActionMode) {
                        LOCKSCREEN_SHORTCUT, POWER_MENU_SHORTCUT -> res.getString(R.string.shortcut_action_help_shortcut)
                        SHAKE_EVENTS_DISABLED -> res.getString(R.string.shortcut_action_help_app)
                        else -> res.getString(R.string.shortcut_action_help_button)
                    }
                    val icon = if (!getOwner().mDisableIconPicker) {
                        res.getString(R.string.shortcut_action_help_icon)
                    } else {
                        ""
                    }
                    val finalHelpMessage = res.getString(R.string.shortcut_action_help_main, actionMode, icon)
                    AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.help)
                        .setMessage(finalHelpMessage)
                        .setNegativeButton(R.string.ok) { dialog, _ -> dialog.cancel() }
                        .create()
                }
                DLG_DELETION_NOT_ALLOWED -> {
                    AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.shortcut_action_warning)
                        .setMessage(R.string.shortcut_action_warning_message)
                        .setNegativeButton(R.string.ok) { dialog, _ -> dialog.cancel() }
                        .create()
                }
                DLG_SHOW_ACTION_DIALOG -> {
                    val title = when {
                        longpress -> R.string.shortcut_action_select_action_longpress
                        newAction -> R.string.shortcut_action_select_action_newaction
                        else -> R.string.shortcut_action_select_action
                    }
                    var values: Array<String>? = null
                    var entries: Array<String>? = null
                    if (!longpress) {
                        val finalEntriesList = ArrayList<String>()
                        val finalValuesList = ArrayList<String>()
                        for (i in getOwner().mActionDialogValues.indices) {
                            if (getOwner().mActionDialogValues[i] != ActionConstants.ACTION_NULL) {
                                finalEntriesList.add(getOwner().mActionDialogEntries[i])
                                finalValuesList.add(getOwner().mActionDialogValues[i])
                            }
                        }
                        entries = finalEntriesList.toTypedArray()
                        values = finalValuesList.toTypedArray()
                    }
                    val finalDialogValues = if (longpress) getOwner().mActionDialogValues else values
                    val finalDialogEntries = if (longpress) getOwner().mActionDialogEntries else entries
                    AlertDialog.Builder(requireActivity())
                        .setTitle(title)
                        .setNegativeButton(R.string.cancel, null)
                        .setItems(finalDialogEntries) { _, item ->
                            if (finalDialogValues!![item] == ActionConstants.ACTION_APP) {
                                if (getOwner().mPicker != null) {
                                    getOwner().mPendingIndex = which
                                    getOwner().mPendingLongpress = longpress
                                    getOwner().mPendingNewAction = newAction
                                    getOwner().mPicker.pickShortcut(getOwner().id)
                                }
                            } else {
                                if (newAction) {
                                    getOwner().addNewAction(finalDialogValues[item], finalDialogEntries!![item])
                                } else {
                                    getOwner().updateAction(finalDialogValues[item], finalDialogEntries!![item], null, which, longpress)
                                }
                            }
                        }
                        .create()
                }
                DLG_SHOW_ICON_PICKER -> {
                    AlertDialog.Builder(requireActivity())
                        .setTitle(R.string.shortcuts_icon_picker_type)
                        .setNegativeButton(R.string.cancel, null)
                        .setItems(R.array.icon_types) { _, which ->
                            when (which) {
                                0 -> { // Default
                                    getOwner().updateAction(null, null, ActionConstants.ICON_EMPTY, getOwner().mPendingIndex, false)
                                    getOwner().mPendingIndex = -1
                                }
                                2 -> { // Custom user icon
                                    val intent = Intent(Intent.ACTION_GET_CONTENT, null).apply {
                                        type = "image/*"
                                        putExtra("crop", "true")
                                        putExtra("scale", true)
                                        putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString())
                                        putExtra("aspectX", 100)
                                        putExtra("aspectY", 100)
                                        putExtra("outputX", 100)
                                        putExtra("outputY", 100)
                                    }
                                    try {
                                        getOwner().mImageTmp.createNewFile()
                                        getOwner().mImageTmp.setWritable(true, false)
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getOwner().mImageTmp))
                                        intent.putExtra("return-data", false)
                                        getOwner().startActivityForResult(intent, REQUEST_PICK_CUSTOM_ICON)
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    } catch (e: ActivityNotFoundException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                        .create()
                }
                else -> throw IllegalArgumentException("unknown id $id")
            }
        }

        override fun onCancel(dialog: DialogInterface) {}
    }
}

