/*
 * Copyright (C) 2015 TeamEos project
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

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference

//import com.android.internal.util.hwkeys.ActionConstants.Defaults
//import com.android.internal.util.hwkeys.ActionConstants.ConfigMap
//import com.android.internal.util.hwkeys.ActionHolder
//import com.android.internal.util.hwkeys.Config.ActionConfig
//import com.android.internal.util.hwkeys.Config.ButtonConfig

// Dummy / Stub classes to get it compiled
class Defaults {
    val actionMap: Map<String, ConfigMap> = emptyMap()
}

class ConfigMap

class ActionConfig(val label: String? = null)

class ButtonConfig

class ActionPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) /*, ActionHolder*/ {

    private var mDefaults: Defaults? = null
    private var mMap: ConfigMap? = null
    private var mAction: ActionConfig? = null
    private var mDefaultAction: ActionConfig? = null

    // Tag Getter/Setter
    var tagValue: String?
        get() = key
        set(value) {
            key = value
        }

    // Defaults Getter/Setter
    fun getDefaults(): Defaults? = mDefaults
    fun setDefaults(defaults: Defaults?) {
        mDefaults = defaults
        val tag = key
        defaults?.actionMap?.entries?.forEach { entry ->
            if (entry.key == tag) {
                mMap = entry.value
                return@forEach
            }
        }
    }

    // ConfigMap Getter/Setter
    fun getConfigMap(): ConfigMap? = mMap
    fun setConfigMap(map: ConfigMap?) {
        mMap = map
    }

    // ButtonConfig Getter/Setter (Stub, wie im Original)
    fun getButtonConfig(): ButtonConfig? = null
    fun setButtonConfig(button: ButtonConfig?) {
        // leer
    }

    // ActionConfig Getter/Setter
    fun getActionConfig(): ActionConfig? = mAction
    fun setActionConfig(action: ActionConfig?) {
        mAction = action
        summary = action?.label
    }

    // Default ButtonConfig Getter/Setter
    fun getDefaultButtonConfig(): ButtonConfig? = null
    fun setDefaultButtonConfig(button: ButtonConfig?) {
        // leer
    }

    // Default ActionConfig Getter/Setter
    fun getDefaultActionConfig(): ActionConfig? = mDefaultAction
    fun setDefaultActionConfig(action: ActionConfig?) {
        mDefaultAction = action
    }
}
