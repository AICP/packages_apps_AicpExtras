/*
 * Copyright (C) 2018 AICP
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


package com.aicp.extras.fragments;

import android.os.Bundle;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

public class BatteryLight extends BaseSettingsFragment {

    private static final String KEY_CATEGORY_FAST_CHARGE = "fast_color_cat";
    private static final String KEY_CATEGORY_CHARGE_COLORS = "colors_list";

    @Override
    protected int getPreferenceResource() {
        return R.xml.battery_light;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Util.requireConfig(getActivity(), findPreference(KEY_CATEGORY_CHARGE_COLORS),
                com.android.internal.R.bool.config_multiColorBatteryLed, true, false);
        Util.requireConfig(getActivity(), findPreference(KEY_CATEGORY_FAST_CHARGE),
                com.android.internal.R.bool.config_multiColorBatteryLed, true, false);
    }

}
