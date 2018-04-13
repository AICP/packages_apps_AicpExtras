/*
 * Copyright (C) 2018 CarbonROM
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

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.PowerManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

public class SmartPixels extends BaseSettingsFragment
    implements OnPreferenceChangeListener {
    private static final String TAG = "SmartPixels";
    private static final String ENABLE = "smart_pixels_enable";
    private static final String ON_POWER_SAVE = "smart_pixels_on_power_save";

    private Handler mHandler = new Handler();
    private SmartPixelsObserver mSmartPixelsObserver;
    private SwitchPreference mSmartPixelsEnable;
    private SwitchPreference mSmartPixelsOnPowerSave;

    private boolean mIsSmartPixelsEnabled;
    private boolean mIsSmartPixelsOnPowerSave;

    ContentResolver resolver;

    @Override
    protected int getPreferenceResource() {
        return R.xml.smart_pixels;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resolver = getActivity().getContentResolver();

        mSmartPixelsEnable = (SwitchPreference) findPreference(ENABLE);
        mSmartPixelsOnPowerSave = (SwitchPreference) findPreference(ON_POWER_SAVE);
        mSmartPixelsObserver = new SmartPixelsObserver(mHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSmartPixelsObserver != null) {
            mSmartPixelsObserver.register();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSmartPixelsObserver != null) {
            mSmartPixelsObserver.unregister();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        return true;
    }

    private void updatePreferences() {
        mIsSmartPixelsEnabled = (Settings.System.getIntForUser(
                resolver, Settings.System.SMART_PIXELS_ENABLE,
                0, UserHandle.USER_CURRENT) != 0);
        mIsSmartPixelsOnPowerSave = (Settings.System.getIntForUser(
                resolver, Settings.System.SMART_PIXELS_ON_POWER_SAVE,
                0, UserHandle.USER_CURRENT) != 0);

        mSmartPixelsEnable.setChecked(mIsSmartPixelsEnabled);
        mSmartPixelsOnPowerSave.setChecked(mIsSmartPixelsOnPowerSave);
    }

    private class SmartPixelsObserver extends ContentObserver {
        public SmartPixelsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_ENABLE),
                    false, this, UserHandle.USER_CURRENT);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SMART_PIXELS_ON_POWER_SAVE),
                    false, this, UserHandle.USER_CURRENT);
        }

        public void unregister() {
            resolver.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updatePreferences();
        }
    }
}
