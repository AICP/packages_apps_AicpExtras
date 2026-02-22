/*
 * Copyright (C) 2012 The CyanogenMod Project
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
package com.aicp.extras.romstats

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.SystemProperties
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.preference.PreferenceManager
import java.io.File
import java.math.BigInteger
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.*

object Utilities {
    const val SETTINGS_PREF_NAME = "ROMStats"
    const val TAG = "ROMStats"

    fun getUniqueID(ctx: Context): String {
        // First try to get legacy device Id
        try {
            val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val deviceId = tm.deviceId
            if (!deviceId.isNullOrEmpty()) return digest(deviceId)
        } catch (e: Exception) {
            // Ignore
        }

        // Second try to get IMEI
        try {
            val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val imei = tm.imei
            if (!imei.isNullOrEmpty()) return digest(imei)
        } catch (e: Exception) {
            // Ignore
        }

        // Third try is get MAC address
        try {
            val wifiInterface = SystemProperties.get("wifi.interface")
            val wifiMac = String(NetworkInterface.getByName(wifiInterface)?.hardwareAddress ?: ByteArray(0))
            if (wifiMac.isNotEmpty()) return digest(wifiMac)
        } catch (e: Exception) {
            // Ignore
        }

        // Fallback to use ANDROID_ID
        val androidId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: generateInstallationId(ctx)
    }

    private fun generateInstallationId(ctx: Context): String {
        val prefs = ctx.getSharedPreferences("AicpExtrasPrefs", Context.MODE_PRIVATE)
        var id = prefs.getString("INSTALLATION_ID", null)

        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("INSTALLATION_ID", id).apply()
        }

        return id
    }

    fun getStatsUrl(): String? {
        val returnUrl = SystemProperties.get("ro.romstats.url")

        if (returnUrl.isNullOrEmpty()) {
            return null
        }

        // If the last char of the link is not /, add it
        if (!returnUrl.endsWith("/")) {
            return "$returnUrl/"
        }

        return returnUrl
    }

    fun getCarrier(ctx: Context): String {
        val operator = arrayOf(
            "41201", "AWCC",
            "41220", "Roshan",
            // Weitere Einträge hier...
            "313199", "700 MHz Public Safety Broadband"
        )

        var carrier = SystemProperties.get("ro.product.model", "")
        if (carrier == "GT-P7510" || carrier == "GT-P7511") {
            carrier = "WiFi"
        } else {
            val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            var networkOperatorName = tm.networkOperatorName
            if (networkOperatorName.isNullOrEmpty()) {
                networkOperatorName = "Unknown"
                val carrierId = tm.networkOperator
                for (i in operator.indices step 2) {
                    if (operator[i] == carrierId) {
                        networkOperatorName = operator[i + 1]
                    }
                }
            }
            carrier = networkOperatorName
        }
        return carrier ?: "Unknown"
    }

    fun getCarrierId(ctx: Context): String {
        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val carrierId = tm.networkOperator
        return if (carrierId.isNullOrEmpty()) "0" else carrierId
    }

    fun getCountryCode(ctx: Context): String {
        val world = arrayOf(
            "ad", "Andorra, Principality of",
            "ae", "United Arab Emirates",
            // Weitere Einträge hier...
            "zw", "Zimbabwe"
        )

        val tm = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var countryCode = tm.networkCountryIso
        if (countryCode.isNullOrEmpty()) {
            countryCode = ctx.resources.configuration.locales.get(0).country.lowercase(Locale.getDefault())
            if (countryCode.isEmpty()) {
                countryCode = "Unknown"
            }
        }
        var countryName = countryCode
        for (i in world.indices step 2) {
            if (world[i] == countryCode) {
                countryName = world[i + 1]
            }
        }
        return countryName
    }

    fun getDevice(): String {
        return SystemProperties.get("ro.aicp.device", Build.DEVICE)
    }

    fun getModVersion(): String {
        return SystemProperties.get("ro.build.id", Build.ID)
    }

    fun getBuildType(): String {
        return SystemProperties.get("ro.romstats.buildtype", "Unofficial")
    }

    fun getRomName(): String {
        return SystemProperties.get("ro.romstats.name", "AICP")
    }

    fun getRomVersion(): String {
        return SystemProperties.get("ro.romstats.version", "Unknown")
    }

    fun getRomVersionHash(): String {
        val romHash = getRomName() + getRomVersion()
        return digest(romHash)
    }

    fun getTimeFrame(): Long {
        val tFrameStr = SystemProperties.get("ro.romstats.tframe", "7")
        return tFrameStr.toLongOrNull() ?: 7L
    }

    fun digest(input: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            val number = BigInteger(1, messageDigest)
            String.format("%032x", number).uppercase(Locale.US)
        } catch (e: Exception) {
            ""
        }
    }

    fun getSigningCert(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            val signatures = packageInfo.signatures
            if (signatures != null && signatures.isNotEmpty()) {
                digest(signatures[0].toCharsString())
            } else {
                ""
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            ""
        }
    }

    fun getReportingMode(): Int {
        val askFirst = SystemProperties.get("ro.romstats.askfirst", "0")
        return if ("0" == askFirst) {
            Const.ROMSTATS_REPORTING_MODE_NEW
        } else {
            Const.ROMSTATS_REPORTING_MODE_OLD
        }
    }

    fun persistentOptOut(context: Context): Boolean {
        val prefs = AnonymousStats.getPreferences(context)

        Log.d(TAG, "[checkPersistentOptOut] Check prefs exist: ${prefs.contains(Const.ANONYMOUS_OPT_IN)}")
        if (!prefs.contains(Const.ANONYMOUS_OPT_IN)) {
            Log.d(TAG, "[checkPersistentOptOut] New install, check for 'Persistent cookie'")

            val sdCard = Environment.getExternalStorageDirectory()
            val dir = File(sdCard.absolutePath + "/.AICPROMStats")
            val cookieFile = File(dir, "optout")

            if (cookieFile.exists()) {
                Log.d(TAG, "[checkPersistentOptOut] Persistent cookie exists -> Disable everything")

                prefs.edit().putBoolean(Const.ANONYMOUS_OPT_IN, false).apply()
                prefs.edit().putBoolean(Const.ANONYMOUS_FIRST_BOOT, false).apply()

                val mainPrefs = PreferenceManager.getDefaultSharedPreferences(context)
                mainPrefs.edit().putBoolean(Const.ANONYMOUS_OPT_IN, false).apply()
                mainPrefs.edit().putBoolean(Const.ANONYMOUS_OPT_OUT_PERSIST, true).apply()

                return true
            } else {
                Log.d(TAG, "[checkPersistentOptOut] No persistent cookie found")
            }
        }
        return false
    }

    fun checkIconVisibility(context: Context) {
        val sdCard = Environment.getExternalStorageDirectory()
        val dir = File(sdCard.absolutePath + "/.AICPROMStats")
        val cookieFile = File(dir, "hide_icon")

        val p = context.packageManager
        val componentToDisable = ComponentName(
            context.applicationContext.packageName,
            AnonymousStats::class.java.name
        )
        if (cookieFile.exists()) {
            p.setComponentEnabledSetting(
                componentToDisable,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {
            p.setComponentEnabledSetting(
                componentToDisable,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}

