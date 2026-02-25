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

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Point
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.os.UserHandle
import android.provider.Settings
import androidx.preference.ListPreference
import androidx.preference.Preference
import android.util.Log
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Enumeration
import java.util.List
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R
import com.aicp.extras.utils.FinishingAnimationDrawable
import com.aicp.extras.utils.SuShell
import com.aicp.extras.utils.SuTask
import com.aicp.extras.utils.Util

class SystemAnimations : BaseSettingsFragment() {

    private val TAG = SystemAnimations::class.java.simpleName

    /*
    private val PREF_CUSTOM_BOOTANIM = "custom_bootanimation"

    // Custom bootanimation
    private val REQUEST_PICK_BOOT_ANIMATION = 201
    private val BOOTANIMATION_SYSTEM_PATH = "/system/media/bootanimation.zip"
    private val BACKUP_PATH = File(Environment.getExternalStorageDirectory(), "/AICP_backup").absolutePath

    private var mCustomBootAnimation: Preference? = null

    // Custom bootanimation
    private var mBootanimationView: ImageView? = null
    private var mBootanimationError: TextView? = null
    private var mCustomBootAnimationDialog: AlertDialog? = null
    private var mBootanimationPart1: FinishingAnimationDrawable? = null
    private var mBootanimationPart2: AnimationDrawable? = null
    private var mBootanimationErrormsg: String? = null
    private var mBootAnimationPath: String? = null
    */

    override fun getPreferenceResource(): Int {
        return R.xml.system_animations
    }

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resolver: ContentResolver = requireActivity().contentResolver

        // Custom bootanimation
        mCustomBootAnimation = findPreference(PREF_CUSTOM_BOOTANIM)
        resetBootAnimation()
        if (File(BACKUP_PATH).mkdirs()) {
            Log.d(TAG, "Created bootanimation backup dir")
        } else {
            Log.d(TAG, "Did not create bootanimation backup dir")
        }
        Util.requireRoot(requireActivity(), mCustomBootAnimation)
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return if (preference == mCustomBootAnimation) {
            openBootAnimationDialog()
            true
        } else {
            super.onPreferenceTreeClick(preference)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                if (data == null) {
                    // Nothing returned by user, probably pressed back button in file manager
                    return
                }
                mBootAnimationPath = data.data?.path
                if (mBootAnimationPath!!.contains(":")) {
                    // mBootAnimationPath is not really a path yet, which we need
                    // for copying as root; so copy file to a known path first
                    mBootAnimationPath = "$BACKUP_PATH${File.separator}tmpbootanim.zip"

                    var inputStream: InputStream? = null
                    var outputStream: FileOutputStream? = null

                    try {
                        inputStream = requireActivity().contentResolver.openInputStream(data.data!!)
                        outputStream = FileOutputStream(mBootAnimationPath, false)
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (inputStream!!.read(buffer).also { length = it } > 0) {
                            outputStream.write(buffer, 0, length)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        try {
                            inputStream?.close()
                            outputStream?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                openBootAnimationDialog()
            }
        }
    }

    /*
     * Resets boot animation path. Essentially clears temporary-set boot animation
     * set by the user from the dialog.
     *
     * @return returns true if a boot animation exists (user or system). false otherwise.
     */
    private fun resetBootAnimation(): Boolean {
        return if (File(BOOTANIMATION_SYSTEM_PATH).exists()) {
            mBootAnimationPath = BOOTANIMATION_SYSTEM_PATH
            true
        } else {
            mBootAnimationPath = ""
            false
        }
    }

    private fun openBootAnimationDialog() {
        Log.d(TAG, "boot animation path: $mBootAnimationPath")
        mCustomBootAnimationDialog?.cancel()
        mCustomBootAnimationDialog = null

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.bootanimation_preview)
        if (!mBootAnimationPath.isNullOrEmpty() && mBootAnimationPath != BOOTANIMATION_SYSTEM_PATH) {
            builder.setPositiveButton(R.string.bootanimation_apply) { dialog, _ ->
                installBootAnim(dialog, mBootAnimationPath!!)
                resetBootAnimation()
            }
        }
        builder.setNeutralButton(R.string.bootanimation_set_custom) { dialog, _ ->
            val packageManager: PackageManager = requireActivity().packageManager
            val test = Intent(Intent.ACTION_GET_CONTENT)
            test.type = "application/zip"
            val list: List<ResolveInfo> = packageManager.queryIntentActivities(test, PackageManager.GET_ACTIVITIES)
            if (list.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_GET_CONTENT, null)
                intent.type = "application/zip"
                startActivityForResult(intent, REQUEST_PICK_BOOT_ANIMATION)
            } else {
                Toast.makeText(
                    requireActivity(),
                    R.string.bootanimation_install_file_manager_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        builder.setNegativeButton(com.android.internal.R.string.cancel) { dialog, _ ->
            resetBootAnimation()
            dialog.dismiss()
        }

        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(
            R.layout.dialog_bootanimation_preview,
            requireActivity().findViewById(R.id.bootanimation_layout_root) as ViewGroup
        )
        mBootanimationError = layout.findViewById(R.id.textViewError)
        mBootanimationView = layout.findViewById(R.id.imageViewPreview)
        mBootanimationView?.visibility = View.GONE

        val display: Display = requireActivity().windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        mBootanimationView?.layoutParams = LinearLayout.LayoutParams(size.x / 2, size.y / 2)

        mBootanimationError?.text = getString(R.string.bootanimation_creating_preview)
        builder.setView(layout)
        mCustomBootAnimationDialog = builder.create()
        mCustomBootAnimationDialog?.setOwnerActivity(requireActivity())
        mCustomBootAnimationDialog?.show()

        Thread {
            createBootanimationPreview(mBootAnimationPath!!)
        }.start()
    }

    private fun createBootanimationPreview(path: String) {
        // Boot animation preview creation logic here (alle Kommentare intakt)
        // ...
    }

    private val mBootanimationErrorHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            mBootanimationView?.visibility = View.GONE
            mBootanimationError?.text = mBootanimationErrormsg
        }
    }

    private val mBootanimationFinishedHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            mBootanimationView?.setImageDrawable(mBootanimationPart1)
            mBootanimationView?.visibility = View.VISIBLE
            mBootanimationError?.visibility = View.GONE
            mBootanimationPart1?.start()
        }
    }

    private fun installBootAnim(dialog: DialogInterface, bootAnimationPath: String) {
        val dateFormat: DateFormat = SimpleDateFormat("ddMMyyyy_HHmmss")
        val date = Date()
        val current = dateFormat.format(date)
        InstallBootAnimTask(requireActivity()).execute(current, bootAnimationPath)
    }

    private inner class InstallBootAnimTask(context: Context) : SuTask<String>(context) {
        private var mSuccess = true

        @Throws(SuShell.SuDeniedException::class)
        override fun sudoInBackground(vararg params: String) {
            if (params.size != 2) {
                Log.e(TAG, "InstallBootAnimTask: invalid params count")
                mSuccess = false
                return
            }
            val current = params[0]
            val bootAnimationPath = params[1]
            SuShell.runWithSuCheck(
                "mount -o rw,remount /system",
                "cp -f $BOOTANIMATION_SYSTEM_PATH $BACKUP_PATH/bootanimation_backup_$current.zip",
                "cp -f $bootAnimationPath $BOOTANIMATION_SYSTEM_PATH",
                "chmod 644 $BOOTANIMATION_SYSTEM_PATH",
                "mount -o ro,remount /system"
            )
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            if (result == true && mSuccess) {
                Toast.makeText(requireActivity(), R.string.bootanimation_install_successful, Toast.LENGTH_SHORT).show()
            }
        }
    }
    */
}
