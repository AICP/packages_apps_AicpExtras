/*
 * Copyright (C) 2017-2026 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aicp.extras.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemProperties;
import android.util.AttributeSet;

import com.aicp.gear.preference.SecureSettingsStore;
import com.android.settingslib.PrimarySwitchPreference;

import com.aicp.extras.R;

public class SecureSettingMasterSwitchPreference extends PrimarySwitchPreference {

    private boolean mDefaultValue;

    public SecureSettingMasterSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
    }

    public SecureSettingMasterSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
    }

    public SecureSettingMasterSwitchPreference(Context context) {
        super(context);
        setPreferenceDataStore(new SecureSettingsStore(context.getContentResolver()));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // This is called in super constructor, so we cannot load required
        // attrs for this method from init() -> do here (and use Preference stylables only)
        String systemPropDefaultOverride =
                a.getString(R.styleable.Preference_systemPropDefaultOverride);

        if (systemPropDefaultOverride != null) {
            int sep1 = systemPropDefaultOverride.indexOf('?');
            int sep2 = systemPropDefaultOverride.indexOf(':');
            String override = SystemProperties.get(systemPropDefaultOverride.substring(0, sep1));
            String onValue = systemPropDefaultOverride.substring(sep1+1, sep2);
            String offValue = systemPropDefaultOverride.substring(sep2+1);
            if (onValue.equals(override)) {
                return true;
            } else if (offValue.equals(override)) {
                return false;
            } // else: don't override
        }
        return mDefaultValue = a.getBoolean(index, false);
    }

    /**
     * Get default value for external use.
     */
    public boolean getDefaultValue() {
        return mDefaultValue;
    }

}
