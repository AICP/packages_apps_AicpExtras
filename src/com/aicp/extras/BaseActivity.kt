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

package com.aicp.extras

import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import androidx.preference.PreferenceManager

open class BaseActivity : SettingsActivity() {

    private var mThemeRes: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mThemeRes = getThemeRes()
        setTheme(mThemeRes)
    }

    override fun onResume() {
        super.onResume()
        if (mThemeRes != getThemeRes()) {
            recreate()
        }
    }

    protected fun getThemeRes(): Int {
        val themePref = Settings.System.getInt(contentResolver, Settings.System.AE_THEME, 0)
        return when (themePref) {
            6 -> R.style.AppTheme_MoreAccent
            8 -> R.style.AppTheme_LessAccent
            else -> R.style.AppTheme
        }
    }
}

