/*
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
 *
 * Simple preference class implementing ActionHolder interface to assign
 * actions to buttons. It is ABSOLUTELY IMPERITIVE that the preference
 * key is identical to the target ConfigMap tag in ActionConstants
 */
package com.aicp.extras.preference

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

import androidx.activity.result.ActivityResultLauncher
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder

import com.aicp.extras.R

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

public class PifDataPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private var mFilePickerLauncher: ActivityResultLauncher<Intent>? = null

    init {
        setLayoutResource(R.layout.pref_with_delete)
    }

    public fun setFilePickerLauncher(launcher: ActivityResultLauncher<Intent>) {
        this.mFilePickerLauncher = launcher
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val ctx: Context? = getContext()
        if (ctx == null) return
        val cr: ContentResolver = ctx.getContentResolver()

        val title: TextView = holder.findViewById(R.id.title) as TextView
        val summary: TextView = holder.findViewById(R.id.summary) as TextView
        val deleteButton: ImageButton = holder.findViewById(R.id.delete_button) as ImageButton

        title.setText(getTitle())

        val hasData: Boolean = Settings.Secure.getString(
                cr, Settings.Secure.PIF_DATA) != null

        if (hasData) {
            val json: String? = Settings.Secure.getString(cr, Settings.Secure.PIF_DATA)
            val pifTimestamp: String? = Settings.Secure.getString(cr, Settings.Secure.PIF_DATA_TIMESTAMP)
            val propsCount: Int = countPifProps(json)
            val ts: String = if (pifTimestamp != null) pifTimestamp else SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            summary.setText(ctx.getString(R.string.pif_data_loaded_summary, propsCount, ts))
        } else {
            summary.setText(ctx.getString(R.string.pif_data_summary))
        }

        deleteButton.setVisibility(if (hasData) View.VISIBLE else View.GONE)
        deleteButton.setEnabled(hasData)

        holder.itemView.setOnClickListener { v ->
            if (mFilePickerLauncher != null) {
                val intent: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.setType("*/*")
                intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/json", "text/json"))
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                mFilePickerLauncher!!.launch(intent)
            }
        }

        deleteButton.setOnClickListener { v ->
            Settings.Secure.putString(cr, Settings.Secure.PIF_DATA, null)
            Settings.Secure.putString(cr, Settings.Secure.PIF_DATA_TIMESTAMP, null)
            Toast.makeText(ctx, ctx.getString(R.string.pif_toast_file_cleared), Toast.LENGTH_SHORT).show()
            notifyChanged()
            killPackages()
        }
    }

    public fun handleFileSelected(uri: Uri?) {
        val ctx: Context? = getContext()
        if (ctx == null) return
        val cr: ContentResolver = ctx.getContentResolver()

        if (uri == null) {
            Toast.makeText(ctx,
                ctx.getString(R.string.pif_toast_invalid_file_selected), Toast.LENGTH_SHORT).show()
            return
        }

        val type: String? = cr.getType(uri)
        val isJsonMime: Boolean = "application/json".equals(type) || "text/json".equals(type)
        val hasJsonExt: Boolean = (uri.getPath() != null && uri.getPath()!!.lowercase().endsWith(".json"))
        if (!isJsonMime && !hasJsonExt) {
            Toast.makeText(ctx,
                ctx.getString(R.string.pif_toast_invalid_file_selected), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val inputStream: InputStream? = cr.openInputStream(uri)
            if (inputStream != null) {
                inputStream.use { stream ->
                    val reader = BufferedReader(
                        InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)
                    )
                    reader.use { br ->
                        val jsonContent = StringBuilder()
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            jsonContent.append(line).append('\n')
                        }

                        val json: String = jsonContent.toString()

                        Settings.Secure.putString(cr, Settings.Secure.PIF_DATA, json)
                        val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                        Settings.Secure.putString(cr, Settings.Secure.PIF_DATA_TIMESTAMP, timestamp)
                        Toast.makeText(ctx,
                                ctx.getString(R.string.pif_toast_file_loaded), Toast.LENGTH_SHORT).show()
                        notifyChanged()
                        killPackages()
                    }
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read JSON file", e)
            Toast.makeText(ctx,
                ctx.getString(R.string.pif_toast_invalid_file_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private fun countPifProps(json: String?): Int {
        if (json == null || json.trim().isEmpty()) return 0
        try {
            val trimmed: String = json.trim()
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                // Prefer nested "props" object if present
                if (obj.has("props") && obj.opt("props") is JSONObject) {
                    return (obj.get("props") as JSONObject).length()
                }
                return obj.length()
            } else if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                return arr.length()
            }
        } catch (ignore: JSONException) {
        }
        return 0
    }

    private fun killPackages() {
        val ctx: Context? = getContext()
        if (ctx == null) return
        try {
            val am: ActivityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val packages: Array<String> = arrayOf("com.google.android.gms", "com.android.vending")
            for (pkg in packages) {
                am.javaClass
                  .getMethod("forceStopPackage", String::class.java)
                  .invoke(am, pkg)
                Log.i(TAG, pkg + " process killed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to kill packages", e)
        }
    }

    companion object {
        private const val TAG = "PifDataPref"
    }
}
