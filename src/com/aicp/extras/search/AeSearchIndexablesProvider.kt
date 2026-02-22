/*
 * Copyright (C) 2016 The CyanogenMod Project
 * Copyright (C) 2019-2026 Android Ice Cold Project
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
package com.aicp.extras.search

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.SearchIndexablesContract.*
import android.provider.SearchIndexablesProvider
import android.util.ArraySet
import android.util.Log
import com.aicp.extras.R
import java.lang.reflect.Field

class AeSearchIndexablesProvider : SearchIndexablesProvider() {

    companion object {
        private const val TAG = "AeSearchIndexablesProvider"
        private const val FIELD_NAME_SEARCH_INDEX_DATA_PROVIDER = "SEARCH_INDEX_DATA_PROVIDER"
    }

    override fun queryXmlResources(strings: Array<String>?): Cursor {
        val cursor = MatrixCursor(INDEXABLES_XML_RES_COLUMNS)
        val keys = PartsList.get(context!!).getPartsList()

        for (key in keys) {
            val i = PartsList.get(context!!).getPartInfo(key)
            if (i == null || i.xmlRes <= 0) {
                continue
            }

            val ref = arrayOfNulls<Any>(INDEXABLES_XML_RES_COLUMNS.size)
            ref[COLUMN_INDEX_XML_RES_RANK] = 2
            ref[COLUMN_INDEX_XML_RES_RESID] = i.xmlRes
            ref[COLUMN_INDEX_XML_RES_CLASS_NAME] = null
            ref[COLUMN_INDEX_XML_RES_ICON_RESID] = R.mipmap.ic_launcher
            ref[COLUMN_INDEX_XML_RES_INTENT_ACTION] = i.getAction()
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE] = PartsList.AE_ACTIVITY.packageName
            ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS] = PartsList.AE_ACTIVITY.className
            cursor.addRow(ref)
        }
        return cursor
    }

    override fun queryRawData(strings: Array<String>?): Cursor {
        val cursor = MatrixCursor(INDEXABLES_RAW_COLUMNS)
        val keys = PartsList.get(context!!).getPartsList()

        for (key in keys) {
            val i = PartsList.get(context!!).getPartInfo(key)
            if (i == null) {
                continue
            }

            val sip = getSearchIndexProvider(i.fragmentClass)
            if (sip == null) {
                continue
            }

            val rawList = sip.getRawDataToIndex(context!!)
            if (rawList.isEmpty()) {
                if (i.xmlRes > 0) {
                    continue
                }
            }

            for (raw in rawList) {
                val ref = arrayOfNulls<Any>(INDEXABLES_RAW_COLUMNS.size)
                ref[COLUMN_INDEX_RAW_RANK] = if (raw.rank > 0) raw.rank else 2
                ref[COLUMN_INDEX_RAW_TITLE] = raw.title ?: i.title
                ref[COLUMN_INDEX_RAW_SUMMARY_ON] = i.summary
                ref[COLUMN_INDEX_RAW_SUMMARY_OFF] = null
                ref[COLUMN_INDEX_RAW_ENTRIES] = raw.entries
                ref[COLUMN_INDEX_RAW_KEYWORDS] = raw.keywords
                ref[COLUMN_INDEX_RAW_SCREEN_TITLE] = raw.screenTitle ?: i.title
                ref[COLUMN_INDEX_RAW_CLASS_NAME] = null
                ref[COLUMN_INDEX_RAW_ICON_RESID] = if (raw.iconResId > 0) raw.iconResId else (if (i.iconRes > 0) i.iconRes else R.mipmap.ic_launcher)
                ref[COLUMN_INDEX_RAW_INTENT_ACTION] = raw.intentAction ?: i.getAction()
                ref[COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE] = raw.intentTargetPackage ?: PartsList.AE_ACTIVITY.packageName
                ref[COLUMN_INDEX_RAW_INTENT_TARGET_CLASS] = raw.intentTargetClass ?: PartsList.AE_ACTIVITY.className
                ref[COLUMN_INDEX_RAW_KEY] = raw.key ?: i.name
                ref[COLUMN_INDEX_RAW_USER_ID] = -1
                ref[COLUMN_INDEX_RAW_PAYLOAD_TYPE] = null
                ref[COLUMN_INDEX_RAW_PAYLOAD] = null
                cursor.addRow(ref)
            }
        }
        return cursor
    }

    override fun queryNonIndexableKeys(strings: Array<String>?): Cursor {
        val cursor = MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS)
        val keys = PartsList.get(context!!).getPartsList()
        val nonIndexables = ArraySet<String>()

        for (key in keys) {
            val i = PartsList.get(context!!).getPartInfo(key)
            if (i == null) {
                continue
            }

            val sip = getSearchIndexProvider(i.fragmentClass)
            if (sip == null) {
                continue
            }

            val nik = sip.getNonIndexableKeys(context!!)
            nonIndexables.addAll(nik)
        }

        for (nik in nonIndexables) {
            val ref = arrayOf(nik)
            cursor.addRow(ref)
        }
        return cursor
    }

    override fun onCreate(): Boolean {
        return true
    }

    private fun getSearchIndexProvider(className: String?): Searchable.SearchIndexProvider? {
        if (className == null) return null

        val clazz: Class<*>
        try {
            clazz = Class.forName(className)
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, "Cannot find class: $className")
            return null
        }

        if (!Searchable::class.java.isAssignableFrom(clazz)) {
            return null
        }

        try {
            val f = clazz.getField(FIELD_NAME_SEARCH_INDEX_DATA_PROVIDER)
            return f.get(null) as Searchable.SearchIndexProvider
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }
        return null
    }
}

