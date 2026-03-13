package com.aicp.extras.preference

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

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KeyboxDataPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private var mFilePickerLauncher: ActivityResultLauncher<Intent>? = null

    init {
        layoutResource = R.layout.pref_with_delete
    }

    fun setFilePickerLauncher(launcher: ActivityResultLauncher<Intent>?) {
        this.mFilePickerLauncher = launcher
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val ctx = context ?: return
        val cr = ctx.contentResolver

        val title = holder.findViewById(R.id.title) as TextView
        val summary = holder.findViewById(R.id.summary) as TextView
        val deleteButton = holder.findViewById(R.id.delete_button) as ImageButton

        title.text = getTitle()

        // Settings.Secure.KEYBOX_DATA and KEYBOX_DATA_TIMESTAMP are expected to be available
        // in the android.provider.Settings.Secure class in the build environment.
        val keyboxData = Settings.Secure.getString(cr, "keybox_data")
        val keyboxTimestamp = Settings.Secure.getString(cr, "keybox_data_timestamp")
        val hasData = keyboxData != null

        if (hasData) {
            val info = parseKeyboxInfo(keyboxData!!)
            val ts = keyboxTimestamp ?: SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
            summary.text = ctx.getString(
                R.string.keybox_data_loaded_summary,
                info.type,
                info.certCount,
                ts
            )
        } else {
            summary.text = ctx.getString(R.string.keybox_data_summary)
        }

        deleteButton.visibility = if (hasData) View.VISIBLE else View.GONE
        deleteButton.isEnabled = hasData

        holder.itemView.setOnClickListener { v: View? ->
            if (mFilePickerLauncher != null) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.type = "*/*"
                intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/xml", "application/xml"))
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                mFilePickerLauncher!!.launch(intent)
            }
        }

        deleteButton.setOnClickListener { v: View? ->
            Settings.Secure.putString(cr, "keybox_data", null)
            Settings.Secure.putString(cr, "keybox_data_timestamp", null)
            Toast.makeText(ctx, ctx.getString(R.string.keybox_toast_file_cleared), Toast.LENGTH_SHORT).show()
            notifyChanged()
        }
    }

    fun handleFileSelected(uri: Uri?) {
        val ctx = context ?: return
        val cr = ctx.contentResolver

        if (uri == null) {
            Toast.makeText(ctx,
                ctx.getString(R.string.keybox_toast_invalid_file_selected), Toast.LENGTH_SHORT).show()
            return
        }

        val type = cr.getType(uri)
        val isXmlMime = "text/xml" == type || "application/xml" == type
        val hasXmlExt = (uri.path != null && uri.path!!.lowercase(Locale.getDefault()).endsWith(".xml"))
        if (!isXmlMime && !hasXmlExt) {
            Toast.makeText(ctx,
                ctx.getString(R.string.keybox_toast_invalid_file_selected), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            cr.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8)).use { reader ->
                    val xmlContent = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        xmlContent.append(line).append('\n')
                    }

                    val xml = xmlContent.toString()
                    if (!validateXml(xml)) {
                        Toast.makeText(ctx,
                            ctx.getString(R.string.keybox_toast_missing_data), Toast.LENGTH_SHORT).show()
                        return
                    }

                    Settings.Secure.putString(cr, "keybox_data", xml)
                    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                    Settings.Secure.putString(cr, "keybox_data_timestamp", timestamp)
                    Toast.makeText(ctx,
                        ctx.getString(R.string.keybox_toast_file_loaded), Toast.LENGTH_SHORT).show()
                    notifyChanged()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read XML file", e)
            Toast.makeText(ctx,
                ctx.getString(R.string.keybox_toast_invalid_file_selected), Toast.LENGTH_SHORT).show()
        }
    }

    private class KeyboxInfo(val type: String, val certCount: Int, val timestamp: String)

    private fun parseKeyboxInfo(xml: String): KeyboxInfo {
        var hasEcdsaKey = false
        var hasRsaKey = false
        var certCount = 0

        try {
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(xml))

            var currentAlg: String? = null
            var eventType = parser.next()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    val name = parser.name
                    if ("Key" == name) {
                        currentAlg = parser.getAttributeValue(null, "algorithm")
                        if ("ecdsa".equals(currentAlg, ignoreCase = true)) {
                            hasEcdsaKey = true
                        } else if ("rsa".equals(currentAlg, ignoreCase = true)) {
                            hasRsaKey = true
                        }
                    } else if ("Certificate" == name) {
                        certCount++
                    }
                } else if (eventType == XmlPullParser.END_TAG && "Key" == parser.name) {
                    currentAlg = null
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse keybox info", e)
        }

        val type = if (hasEcdsaKey && hasRsaKey) {
            "RSA + ECDSA"
        } else if (hasEcdsaKey) {
            "ECDSA"
        } else if (hasRsaKey) {
            "RSA"
        } else {
            "Unknown"
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        return KeyboxInfo(type, certCount, timestamp)
    }

    private fun validateXml(xml: String): Boolean {
        var hasEcdsaKey = false
        var hasRsaKey = false
        var hasEcdsaPrivKey = false
        var hasRsaPrivKey = false
        var ecdsaCertCount = 0
        var rsaCertCount = 0
        var numberOfKeyboxes = -1

        try {
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(StringReader(xml))

            var currentAlg: String? = null

            var eventType = parser.next()
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    val name = parser.name
                    when (name) {
                        "NumberOfKeyboxes" -> {
                            parser.next() // move to TEXT event
                            if (parser.eventType == XmlPullParser.TEXT) {
                                numberOfKeyboxes = try {
                                    parser.text.trim().toInt()
                                } catch (e: NumberFormatException) {
                                    -1
                                }
                            }
                        }

                        "Key" -> {
                            currentAlg = parser.getAttributeValue(null, "algorithm")
                            if ("ecdsa".equals(currentAlg, ignoreCase = true)) {
                                hasEcdsaKey = true
                            } else if ("rsa".equals(currentAlg, ignoreCase = true)) {
                                hasRsaKey = true
                            } else {
                                currentAlg = null // unsupported key
                            }
                        }

                        "PrivateKey" -> {
                            val format = parser.getAttributeValue(null, "format")
                            if (!"pem".equals(format, ignoreCase = true)) {
                                Log.w(TAG, "Invalid or missing format for PrivateKey")
                                return false
                            }
                            if ("ecdsa".equals(currentAlg, ignoreCase = true)) {
                                hasEcdsaPrivKey = true
                            } else if ("rsa".equals(currentAlg, ignoreCase = true)) {
                                hasRsaPrivKey = true
                            }
                        }

                        "Certificate" -> {
                            val format = parser.getAttributeValue(null, "format")
                            if (!"pem".equals(format, ignoreCase = true)) {
                                Log.w(TAG, "Invalid or missing format for Certificate")
                                return false
                            }

                            if ("ecdsa".equals(currentAlg, ignoreCase = true)) {
                                ecdsaCertCount++
                            } else if ("rsa".equals(currentAlg, ignoreCase = true)) {
                                rsaCertCount++
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && "Key" == parser.name) {
                    currentAlg = null
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e(TAG, "XML validation failed", e)
            return false
        }

        return (numberOfKeyboxes == 1
                && hasEcdsaKey && hasEcdsaPrivKey && ecdsaCertCount >= 1
                && hasRsaKey && hasRsaPrivKey && rsaCertCount >= 1)
    }

    companion object {
        private const val TAG = "KeyboxDataPref"
    }
}
