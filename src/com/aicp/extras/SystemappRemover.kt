/*=========================================================================
 *
 *  PROJECT:  SlimRoms
 *            Team Slimroms (http://www.slimroms.net)
 *
 *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
 *            Copyright (C) 2014 Dirty Unicorns
 *            Copyright (C) 2026 AICP
 *            All rights reserved
 *
 *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
 *
 *  AUTHORS:     fronti90
 *  DESCRIPTION: SlimSizer: manage your apps
 *
 *  MODS: Dirty Unicorns
 *        Team D.I.R.T.
 *        Added priv-app and odex files support
 *
 *=========================================================================
 */
package com.aicp.extras

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import com.aicp.extras.utils.SuShell
import com.aicp.extras.utils.SuTask
import java.io.File
import java.util.*

class SystemappRemover : SubActivity() {

    companion object {
        private const val TAG = "SystemappRemover"
        private const val DELETE_DIALOG = 1
        private const val DEBUG = true
    }

    private val systemPath = "/system/app/"
    private val systemPrivPath = "/system/priv-app/"
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var mSysApp: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.system_app_remover)
        val fabButton = findViewById<ImageButton>(R.id.fab_delete)

        val safetyList = arrayListOf(
            "CertInstaller", "DrmProvider", "PackageInstaller", "TelephonyProvider",
            "AicpExtras", "CMAudioService", "LineageParts", "LineageSettingsProvider",
            "ContactsProvider", "DefaultContainerService", "Dialer", "DownloadProvider",
            "FusedLocation", "Keyguard", "MediaProvider", "ProxyHandler", "Settings",
            "SettingsProvider", "SystemUI", "TeleService"
        )

        val system = File(systemPath)
        val systemPriv = File(systemPrivPath)
        val sysappArray = combine(system.list(), systemPriv.list())
        mSysApp = ArrayList(Arrays.asList(*sysappArray))

        filterOdex()

        mSysApp.removeAll(safetyList)
        Collections.sort(mSysApp)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, mSysApp)

        val lv = findViewById<ListView>(android.R.id.list)
        lv.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        lv.adapter = adapter

        val footer = LayoutInflater.from(this).inflate(R.layout.system_app_remover_empty_list_entry_footer, lv, false)
        lv.addFooterView(footer)
        lv.setFooterDividersEnabled(false)
        footer.setOnClickListener(null)

        fabButton.setOnClickListener {
            var item: String? = null
            val checked = lv.checkedItemPositions
            for (i in lv.count - 1 downTo 0) {
                if (checked.get(i)) {
                    item = mSysApp[i]
                    break
                }
            }
            if (item == null) {
                toast(getString(R.string.system_app_remover_message_noselect))
            } else {
                showDialog(DELETE_DIALOG, item, adapter)
            }
        }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun showDialog(id: Int, item: String, adapter: ArrayAdapter<String>) {
        val alert = AlertDialog.Builder(this)

        if (id == DELETE_DIALOG) {
            alert.setMessage(R.string.system_app_remover_message_delete)
                .setCancelable(false)
                .setPositiveButton(R.string.ok) { dialog: DialogInterface, _: Int ->
                    val lv = findViewById<ListView>(android.R.id.list)
                    val itemsList = ArrayList<String>()
                    val checked = lv.checkedItemPositions
                    for (i in lv.count - 1 downTo 0) {
                        if (checked.get(i)) {
                            val appName = mSysApp[i]
                            itemsList.add(appName)
                            lv.setItemChecked(i, false)
                            adapter.remove(appName)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    Deleter(this).execute(*itemsList.toTypedArray())
                }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int ->
                    dialog.cancel()
                }
        }
        alert.show()
    }

    private fun combine(a: Array<String>?, b: Array<String>?): Array<String> {
        val aSize = a?.size ?: 0
        val bSize = b?.size ?: 0
        val result = arrayOfNulls<String>(aSize + bSize)
        if (a != null) System.arraycopy(a, 0, result, 0, a.size)
        if (b != null) System.arraycopy(b, 0, result, aSize, b.size)
        return result.filterNotNull().toTypedArray()
    }

    private fun filterOdex() {
        val it = mSysApp.listIterator()
        while (it.hasNext()) {
            val str = it.next()
            if (str.endsWith(".odex")) {
                it.remove()
            }
        }
    }

    private inner class Deleter(context: Context) : SuTask<String>(context) {

        override fun sudoInBackground(vararg params: String) {
            try {
                val commands = mutableListOf("mount -o rw,remount /")
                for (appName in params) {
                    var basePath = systemPath
                    val app = File("$basePath$appName")

                    if (!app.exists()) {
                        basePath = systemPrivPath
                    }
                    val app2rm = File("$basePath$appName")
                    Log.d(TAG, "Removing ${app2rm.absolutePath}")
                    commands.add("rm -rf ${app2rm.absolutePath}")
                }
                SuShell.runWithSuCheck(*commands.toTypedArray())
            } catch (e: SuShell.SuDeniedException) {
                Log.e(TAG, "Su access denied", e)
            }
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (!result) {
                recreate()
            }
        }
    }
}

