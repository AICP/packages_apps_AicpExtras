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

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;

public abstract class BaseActivity extends Activity {

    private int mThemeRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mThemeRes = getThemeRes();
        setTheme(mThemeRes);
        super.onCreate(savedInstanceState);
        fixStatusBarFg();
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
            case 1:
                return R.style.AppTheme_DarkAmber;
            default:
                return R.style.AppTheme;
        }
    }

    /**
     * When changing from a theme with light to one with dark status bar, recreating
     * the activity seems to be not enough to update status bar foreground color,
     * so it's black on black.
     * This is a workaround for that, basically adapted from Launcher3's dynamic
     * status bar color (fg color changing when opening/closing drawer).
     */
    private void fixStatusBarFg() {
        int oldSystemUiFlags = getWindow().getDecorView().getSystemUiVisibility();
        int newSystemUiFlags = oldSystemUiFlags;
        int[] attrs = new int[] {
                android.R.attr.windowLightStatusBar,
                android.R.attr.windowLightNavigationBar,
        };
        TypedArray ta = getTheme().obtainStyledAttributes(attrs);
        boolean lightStatusBar = ta.getBoolean(0, false);
        boolean lightNavigationBar = ta.getBoolean(1, false);
        ta.recycle();
        if (lightStatusBar) {
            newSystemUiFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            newSystemUiFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        if (lightNavigationBar) {
            newSystemUiFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            newSystemUiFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        if (newSystemUiFlags != oldSystemUiFlags) {
            getWindow().getDecorView().setSystemUiVisibility(newSystemUiFlags);
        }
    }
}
