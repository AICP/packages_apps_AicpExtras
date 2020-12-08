/*
 * Copyright (C) 2017 AICP
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


package com.aicp.extras;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {

    private int mThemeRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeRes = getThemeRes();
        setTheme(mThemeRes);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mThemeRes != getThemeRes()) {
            recreate();
        }
    }

    protected int getThemeRes() {
        int themePref = Settings.System.getInt(getContentResolver(), Settings.System.AE_THEME, 0);
        switch (themePref) {
            case 6:
                return R.style.AppTheme_MoreAccent;
            case 8:
                return R.style.AppTheme_LessAccent;
            default:
                return R.style.AppTheme;
        }
    }

}
