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

import androidx.preference.Preference

class PreferenceMultiClickHandler(
    private val runnable: Runnable,
    private var requiredHits: Int = 3,
    private var allowedBreak: Int = 500
) : Preference.OnPreferenceClickListener {

    private var hits: Int = 0
    private var lastHit: Long = -1L

    override fun onPreferenceClick(preference: Preference): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastHit > allowedBreak) {
            hits = 0
        }
        lastHit = now
        hits++
        if (hits == requiredHits) {
            hits = 0
            runnable.run()
        }
        return true
    }
}

