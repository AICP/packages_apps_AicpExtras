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


package com.aicp.extras.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.preference.ScreenshotEditPackageListAdapter;
import com.aicp.extras.preference.ScreenshotEditPackageListAdapter.PackageItem;
import com.aicp.extras.utils.Util;
import com.aicp.extras.R;

public class OtherUi extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String SHOW_CPU_INFO_KEY = "show_cpu_info";
    private static final String KEY_VIBRATE_ON_CHARGE_STATE_CHANGED = "vibration_on_charge_state_changed";
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "1";
    private static final String PREF_SCREENSHOT_EDIT_USER_APP = "screenshot_edit_app";
    private static final String KEY_SMART_PIXELS = "smart_pixels";
    private static final int DIALOG_SCREENSHOT_EDIT_APP = 1;

    private Preference mScreenshotEditAppPref;
    private ScreenshotEditPackageListAdapter mPackageAdapter;
    private SwitchPreference mShowCpuInfo;
    private SwitchPreference mVibrateOnPlug;
    private ListPreference mScrollingCachePref;

    @Override
    protected int getPreferenceResource() {
        return R.xml.other_ui;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShowCpuInfo = (SwitchPreference) findPreference(SHOW_CPU_INFO_KEY);
        mShowCpuInfo.setChecked(Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, 0) == 1);
        mShowCpuInfo.setOnPreferenceChangeListener(this);

        mVibrateOnPlug =
                (SwitchPreference) findPreference(KEY_VIBRATE_ON_CHARGE_STATE_CHANGED);

        if(!Util.hasVibrator(getActivity())){
            mVibrateOnPlug.getParent().removePreference(mVibrateOnPlug);
        }

        mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setOnPreferenceChangeListener(this);

        mPackageAdapter = new ScreenshotEditPackageListAdapter(getActivity());
        mScreenshotEditAppPref = findPreference(PREF_SCREENSHOT_EDIT_USER_APP);
        mScreenshotEditAppPref.setOnPreferenceClickListener(this);

        boolean enableSmartPixels = getContext().getResources().
                getBoolean(com.android.internal.R.bool.config_enableSmartPixels);
        Preference SmartPixels = findPreference(KEY_SMART_PIXELS);

        if (!enableSmartPixels){
            SmartPixels.getParent().removePreference(SmartPixels);
        }

    }

    private Dialog selectPackages(int dialogId) {
        switch (dialogId) {
            case DIALOG_SCREENSHOT_EDIT_APP: {
                Dialog dialog;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                final ListView list = new ListView(getActivity());
                list.setAdapter(mPackageAdapter);
                alertDialog.setTitle(R.string.profile_choose_app);
                alertDialog.setView(list);
                dialog = alertDialog.create();
                list.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Add empty application definition, the user will be able to edit it later
                        PackageItem info = (PackageItem) parent.getItemAtPosition(position);
                        Settings.System.putString(getActivity().getContentResolver(),
                                Settings.System.SCREENSHOT_EDIT_USER_APP, info.packageName);
                        dialog.cancel();
                    }
                });
                return dialog;
            }
         }
        return null;
    }

    private void writeCpuInfoOptions(boolean value) {
        Settings.Global.putInt(getActivity().getContentResolver(),
                Settings.Global.SHOW_CPU_OVERLAY, value ? 1 : 0);
        Intent service = (new Intent())
                .setClassName("com.android.systemui", "com.android.systemui.CPUInfoService");
        if (value) {
            getActivity().startService(service);
        } else {
            getActivity().stopService(service);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mScreenshotEditAppPref) {
            if (mPackageAdapter.getCount() > 0) {
                Dialog packageDialog = (Dialog) selectPackages(DIALOG_SCREENSHOT_EDIT_APP);
                packageDialog.show();
                return true;
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.screenshot_edit_app_no_editor),
                        Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mShowCpuInfo) {
            writeCpuInfoOptions((Boolean) newValue);
            return true;
        } else if (preference == mScrollingCachePref) {
            if (newValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String) newValue);
                return true;
            }
        }
        return false;
    }
}
