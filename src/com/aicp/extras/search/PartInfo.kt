/*
 * Copyright (C) 2016 The CyanogenMod Project
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
package com.aicp.extras.search

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.aicp.extras.AE_FRAGMENT_ACTION_PREFIX

data class PartInfo(
    val name: String,
    var title: String? = null,
    var summary: String? = null,
    var fragmentClass: String? = null,
    var iconRes: Int = 0,
    var isAvailable: Boolean = true,
    var xmlRes: Int = 0
) : Parcelable {

    constructor(name: String) : this(name, null, null)

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt() == 1,
        parcel.readInt()
    )

    fun updateFrom(other: PartInfo?): Boolean {
        if (other == null || other == this) {
            return false
        }
        title = other.title
        summary = other.summary
        fragmentClass = other.fragmentClass
        iconRes = other.iconRes
        isAvailable = other.isAvailable
        xmlRes = other.xmlRes
        return true
    }

    override fun toString(): String {
        return "PartInfo=[ name=$name title=$title summary=$summary fragment=$fragmentClass xmlRes=${Integer.toHexString(xmlRes)} ]"
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(name)
        out.writeString(title)
        out.writeString(summary)
        out.writeString(fragmentClass)
        out.writeInt(iconRes)
        out.writeInt(if (isAvailable) 1 else 0)
        out.writeInt(xmlRes)
    }

    fun getAction(): String {
        return "$AE_FRAGMENT_ACTION_PREFIX.$fragmentClass"
    }

    fun getIntentForActivity(): Intent {
        return Intent(getAction()).apply {
            component = PartsList.AE_ACTIVITY
        }
    }

    companion object CREATOR : Parcelable.Creator<PartInfo> {
        override fun createFromParcel(parcel: Parcel): PartInfo {
            return PartInfo(parcel)
        }

        override fun newArray(size: Int): Array<PartInfo?> {
            return arrayOfNulls(size)
        }
    }
}

