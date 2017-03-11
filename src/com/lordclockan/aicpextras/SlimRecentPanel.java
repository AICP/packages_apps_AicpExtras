/*
 * Copyright (C) 2015-2017 SlimRoms Project
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

package com.lordclockan.aicpextras;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

//import com.slim.settings.DialogCreatable;
//import com.slim.settings.SettingsPreferenceFragment;
import com.lordclockan.aicpextras.preferences.SystemSettingSwitchPreference;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import com.lordclockan.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class SlimRecentPanel extends /*Slim*/PreferenceFragment implements /*DialogCreatable,*/
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "RecentPanelSettings";

    // Preferences
    private static final String RECENTS_MAX_APPS = "recents_max_apps";
    private static final String RECENT_PANEL_LEFTY_MODE =
            "recent_panel_lefty_mode";
    private static final String RECENT_PANEL_SCALE =
            "recent_panel_scale";
    private static final String RECENT_PANEL_EXPANDED_MODE =
            "recent_panel_expanded_mode";
    private static final String RECENT_PANEL_BG_COLOR =
            "recent_panel_bg_color";
    private static final String RECENT_CARD_BG_COLOR =
            "recent_card_bg_color";
    private static final String APP_SIDEBAR_CONTENT =
            "recent_app_sidebar_content";

    private SeekBarPreferenceCham mMaxApps;
    private SystemSettingSwitchPreference mRecentPanelLeftyMode;
    private SeekBarPreferenceCham mRecentPanelScale;
    private ListPreference mRecentPanelExpandedMode;
    private ColorPickerPreference mRecentPanelBgColor;
    private ColorPickerPreference mRecentCardBgColor;
    private Preference mAppSidebarContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.slim_recent_panel_settings);
        initializeAllPreferences();

        if (screenPinningEnabled()) {
            SystemSettingSwitchPreference pref = (SystemSettingSwitchPreference) findPreference("recent_panel_show_topmost");
            pref.setChecked(true);
            pref.setEnabled(false);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.RECENT_PANEL_GRAVITY,
                    ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
            return true;
        } else if (preference == mRecentPanelScale) {
            Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.RECENT_PANEL_SCALE_FACTOR, Integer.valueOf(String.valueOf(newValue)));
            return true;
        } else if (preference == mRecentPanelExpandedMode) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.RECENT_PANEL_EXPANDED_MODE, value);
            return true;
        } else if (preference == mRecentPanelBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#00ffffff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.RECENT_PANEL_BG_COLOR,
                    intHex);
            return true;
        } else if (preference == mRecentCardBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#00ffffff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.RECENT_CARD_BG_COLOR,
                    intHex);
            return true;
        } else if (preference == mMaxApps) {
            Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.RECENTS_MAX_APPS, Integer.valueOf(String.valueOf(newValue)));
            return true;
        }
        return false;
    }

    private boolean screenPinningEnabled() {
        return Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.LOCK_TO_APP_ENABLED, 0) != 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRecentPanelPreferences();
    }

    private void updateRecentPanelPreferences() {
        final boolean recentLeftyMode = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_PANEL_GRAVITY, Gravity.RIGHT) == Gravity.LEFT;
        mRecentPanelLeftyMode.setChecked(recentLeftyMode);

        mMaxApps.setValue(Settings.System.getIntForUser(getContext().getContentResolver(),
                Settings.System.RECENTS_MAX_APPS, ActivityManager.getMaxRecentTasksStatic(),
                UserHandle.USER_CURRENT));

        final int recentScale = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_PANEL_SCALE_FACTOR, 100);
        mRecentPanelScale.setValue(recentScale);

        final int recentExpandedMode = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_PANEL_EXPANDED_MODE, 0);
        mRecentPanelExpandedMode.setValue(recentExpandedMode + "");
    }

    private void initializeAllPreferences() {
        mRecentPanelLeftyMode =
                (SystemSettingSwitchPreference) findPreference(RECENT_PANEL_LEFTY_MODE);
        mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);

        mMaxApps = (SeekBarPreferenceCham) findPreference(RECENTS_MAX_APPS);
        mMaxApps.setOnPreferenceChangeListener(this);

        // Recent panel background color
        mRecentPanelBgColor =
                (ColorPickerPreference) findPreference(RECENT_PANEL_BG_COLOR);
        mRecentPanelBgColor.setOnPreferenceChangeListener(this);
        final int intColor = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_PANEL_BG_COLOR, 0x00ffffff);
        String hexColor = String.format("#%08x", (0x00ffffff & intColor));
        if (hexColor.equals("#00ffffff")) {
            mRecentPanelBgColor.setSummary(R.string.default_string);
        } else {
            mRecentPanelBgColor.setSummary(hexColor);
        }
        mRecentPanelBgColor.setNewPreviewColor(intColor);

        // Recent card background color
        mRecentCardBgColor =
                (ColorPickerPreference) findPreference(RECENT_CARD_BG_COLOR);
        mRecentCardBgColor.setOnPreferenceChangeListener(this);
        final int intColorCard = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_CARD_BG_COLOR, 0x00ffffff);
        String hexColorCard = String.format("#%08x", (0x00ffffff & intColorCard));
        if (hexColorCard.equals("#00ffffff")) {
            mRecentCardBgColor.setSummary(R.string.default_string);
        } else {
            mRecentCardBgColor.setSummary(hexColorCard);
        }
        mRecentCardBgColor.setNewPreviewColor(intColorCard);

        mRecentPanelScale =
                (SeekBarPreferenceCham) findPreference(RECENT_PANEL_SCALE);
        mRecentPanelScale.setOnPreferenceChangeListener(this);

        mRecentPanelExpandedMode =
                (ListPreference) findPreference(RECENT_PANEL_EXPANDED_MODE);
        mRecentPanelExpandedMode.setOnPreferenceChangeListener(this);

        mAppSidebarContent = findPreference(APP_SIDEBAR_CONTENT);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAppSidebarContent) {
            Intent intent = new Intent(getActivity(), SlimRecentAppSidebarActivity.class);
            getActivity().startActivity(intent);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
