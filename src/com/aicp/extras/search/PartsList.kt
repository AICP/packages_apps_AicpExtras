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

import android.content.ComponentName
import android.content.Context
import android.util.ArrayMap
import android.util.Log
import com.aicp.extras.R

class PartsList private constructor(private val mContext: Context) {

    companion object {
        private const val TAG = "PartsList"
        @JvmField
        val AE_ACTIVITY = ComponentName("com.aicp.extras", "com.aicp.extras.SubSettingsActivity")

        @Volatile
        private var sInstance: PartsList? = null
        private val sInstanceLock = Any()

        @JvmStatic
        fun get(context: Context): PartsList {
            synchronized(sInstanceLock) {
                var instance = sInstance
                if (instance == null) {
                    instance = PartsList(context.applicationContext)
                    sInstance = instance
                }
                return instance
            }
        }
    }

    private val mParts: MutableMap<String, PartInfo> = ArrayMap()

    init {
        loadParts()
    }

    private fun loadParts() {
        synchronized(mParts) {
            for (aeInfo in AeFragmentList.FRAGMENT_LIST) {
                if (aeInfo.key.isEmpty()) {
                    Log.e(TAG, "Found no key for ${aeInfo.fragmentClass}, please add it to your xml's root PreferenceScreen")
                    continue
                }
                val info = PartInfo(aeInfo.key)
                info.title = mContext.getString(aeInfo.title)
                if (aeInfo.summary != 0) {
                    info.summary = mContext.getString(aeInfo.summary)
                }
                info.fragmentClass = aeInfo.fragmentClass
                info.xmlRes = aeInfo.xmlRes
                mParts[aeInfo.key] = info
            }
        }
    }

    fun getPartsList(): Set<String> {
        synchronized(mParts) {
            return mParts.keys
        }
    }

    fun getPartInfo(key: String): PartInfo? {
        synchronized(mParts) {
            return mParts[key]
        }
    }

    fun getPartInfoForClass(clazz: String): PartInfo? {
        synchronized(mParts) {
            return mParts.values.firstOrNull { it.fragmentClass == clazz }
        }
    }
}

