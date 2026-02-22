/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.aicp.extras.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton

class ToggleSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : CompoundButton(context, attrs, defStyleAttr, defStyleRes) {

    interface OnBeforeCheckedChangeListener {
        fun onBeforeCheckedChanged(toggleSwitch: ToggleSwitch, checked: Boolean): Boolean
    }

    private var mOnBeforeListener: OnBeforeCheckedChangeListener? = null

    fun setOnBeforeCheckedChangeListener(listener: OnBeforeCheckedChangeListener?) {
        mOnBeforeListener = listener
    }

    override fun setChecked(checked: Boolean) {
        if (mOnBeforeListener != null && mOnBeforeListener!!.onBeforeCheckedChanged(this, checked)) {
            return
        }
        super.setChecked(checked)
    }

    fun setCheckedInternal(checked: Boolean) {
        super.setChecked(checked)
    }
}

