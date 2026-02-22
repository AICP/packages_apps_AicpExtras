/*
 * Copyright (C) 2014 SlimRoms Project
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

import android.app.Activity
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.aicp.extras.R
import com.aicp.gear.util.AppHelper

class SlimShortcutPickerHelper(
    private val mParent: Activity,
    private val mListener: OnPickListener
) {

    companion object {
        const val REQUEST_PICK_SHORTCUT = 100
        const val REQUEST_PICK_APPLICATION = 101
        const val REQUEST_CREATE_SHORTCUT = 102
    }

    private val mPackageManager: PackageManager = mParent.packageManager
    private var lastFragmentId: Int = 0

    interface OnPickListener {
        fun shortcutPicked(uri: String, friendlyName: String, bmp: Bitmap?, isApplication: Boolean)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return

        when (requestCode) {
            REQUEST_PICK_APPLICATION -> completeSetCustomApp(data)
            REQUEST_CREATE_SHORTCUT -> completeSetCustomShortcut(data)
            REQUEST_PICK_SHORTCUT -> processShortcut(data, REQUEST_PICK_APPLICATION, REQUEST_CREATE_SHORTCUT)
        }
    }

    fun pickShortcut(fragmentId: Int) {
        pickShortcut(fragmentId, false)
    }

    fun pickShortcut(fragmentId: Int, fullAppsOnly: Boolean) {
        lastFragmentId = fragmentId

        if (fullAppsOnly) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val pickIntent = Intent(Intent.ACTION_PICK_ACTIVITY).apply {
                putExtra(Intent.EXTRA_INTENT, mainIntent)
            }
            startFragmentOrActivity(pickIntent, REQUEST_PICK_APPLICATION)
        } else {
            val bundle = android.os.Bundle().apply {
                putStringArrayList(
                    Intent.EXTRA_SHORTCUT_NAME,
                    arrayListOf(mParent.getString(R.string.shortcuts_applications))
                )
                putParcelableArrayList(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    arrayListOf(ShortcutIconResource.fromContext(mParent, android.R.drawable.sym_def_app_icon))
                )
            }

            val pickIntent = Intent(Intent.ACTION_PICK_ACTIVITY).apply {
                putExtra(Intent.EXTRA_INTENT, Intent(Intent.ACTION_CREATE_SHORTCUT))
                putExtra(Intent.EXTRA_TITLE, mParent.getText(R.string.shortcuts_select_custom_app_title))
                putExtras(bundle)
            }

            startFragmentOrActivity(pickIntent, REQUEST_PICK_SHORTCUT)
        }
    }

    private fun startFragmentOrActivity(pickIntent: Intent, requestCode: Int) {
        if (lastFragmentId == 0 || mParent !is FragmentActivity) {
            mParent.startActivityForResult(pickIntent, requestCode)
        } else {
            val fa = mParent as FragmentActivity
            val fragment: Fragment? = fa.supportFragmentManager.findFragmentById(lastFragmentId)
            if (fragment != null) {
                fa.startActivityFromFragment(fragment, pickIntent, requestCode)
            } else {
                mParent.startActivityForResult(pickIntent, requestCode)
            }
        }
    }

    private fun processShortcut(intent: Intent, requestCodeApplication: Int, requestCodeShortcut: Int) {
        val applicationName = mParent.getString(R.string.shortcuts_applications)
        val shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

        if (applicationName == shortcutName) {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val pickIntent = Intent(Intent.ACTION_PICK_ACTIVITY).apply { putExtra(Intent.EXTRA_INTENT, mainIntent) }
            startFragmentOrActivity(pickIntent, requestCodeApplication)
        } else {
            startFragmentOrActivity(intent, requestCodeShortcut)
        }
    }

    private fun completeSetCustomApp(data: Intent) {
        mListener.shortcutPicked(
            data.toUri(0),
            AppHelper.getFriendlyActivityName(mParent, mPackageManager, data, false),
            null,
            true
        )
    }

    private fun completeSetCustomShortcut(data: Intent) {
        val intent = data.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)!!.apply {
            putExtra(Intent.EXTRA_SHORTCUT_NAME, data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME))
        }

        var appUri = intent.toUri(0).replace(
            "com.android.contacts.action.QUICK_CONTACT",
            "android.intent.action.VIEW"
        )

        var bmp: Bitmap? = null
        val extra = data.getParcelableExtra<Parcelable>(Intent.EXTRA_SHORTCUT_ICON)
        if (extra is Bitmap) bmp = extra

        if (bmp == null) {
            val iconExtra = data.getParcelableExtra<Parcelable>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            if (iconExtra is ShortcutIconResource) {
                try {
                    val resources: Resources = mPackageManager.getResourcesForApplication(iconExtra.packageName)
                    val id = resources.getIdentifier(iconExtra.resourceName, null, null)
                    bmp = BitmapFactory.decodeResource(resources, id)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        mListener.shortcutPicked(
            appUri,
            AppHelper.getFriendlyShortcutName(mParent, mPackageManager, intent),
            bmp,
            false
        )
    }
}
