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


package com.aicp.extras.changelog;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.aicp.extras.Constants;
import com.aicp.extras.R;

/*
 * AppCompatActivity to mirror BaseActivity's overrides,
 * using the Changelog style
 */
public abstract class BaseChangelogActivity extends AppCompatActivity {

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
        String themePref = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Constants.PREF_THEME, "0");
        if ("1".equals(themePref)) {
            return R.style.ChangelogTheme_DarkAmber;
        } else {
            return R.style.ChangelogTheme;
        }
    }
}
