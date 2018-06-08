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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemProperties;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v14.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.Constants;
import com.aicp.extras.R;
import com.aicp.extras.preference.ScreenshotEditPackageListAdapter;
import com.aicp.extras.preference.ScreenshotEditPackageListAdapter.PackageItem;
import com.aicp.extras.utils.SuShell;
import com.aicp.extras.utils.SuTask;
import com.aicp.extras.utils.Util;

public class SystemBehaviour extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = SystemBehaviour.class.getSimpleName();

    private static final String SELINUX_CATEGORY = "selinux";

    private static final String KEY_VIBRATE_ON_CHARGE_STATE_CHANGED = "vibration_on_charge_state_changed";
    private static final String PREF_SCREENSHOT_EDIT_USER_APP = "screenshot_edit_app";
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "1";
    private static final String KEY_SMART_PIXELS = "smart_pixels";

    private static final int DIALOG_SCREENSHOT_EDIT_APP = 1;

    private ListPreference mScrollingCachePref;
    private Preference mScreenshotEditAppPref;
    private ScreenshotEditPackageListAdapter mPackageAdapter;
    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;
    private SwitchPreference mVibrateOnPlug;

    @Override
    protected int getPreferenceResource() {
        return R.xml.system_behaviour;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SELinux
        Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
        mSelinuxMode = (SwitchPreference) findPreference(Constants.PREF_SELINUX_MODE);
        mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
        mSelinuxMode.setOnPreferenceChangeListener(this);
        mSelinuxPersistence =
                (SwitchPreference) findPreference(Constants.PREF_SELINUX_PERSISTENCE);
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxPersistence.setChecked(getContext()
                .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
                .contains(Constants.PREF_SELINUX_MODE));
        Util.requireRoot(selinuxCategory);

        mPackageAdapter = new ScreenshotEditPackageListAdapter(getActivity());
        mScreenshotEditAppPref = findPreference(PREF_SCREENSHOT_EDIT_USER_APP);
        mScreenshotEditAppPref.setOnPreferenceClickListener(this);

        mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
        mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
        mScrollingCachePref.setOnPreferenceChangeListener(this);

        mVibrateOnPlug =
                (SwitchPreference) findPreference(KEY_VIBRATE_ON_CHARGE_STATE_CHANGED);

        if(!Util.hasVibrator(getActivity())){
            mVibrateOnPlug.getParent().removePreference(mVibrateOnPlug);
        }

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
        if (preference == mSelinuxMode) {
            if ((Boolean) newValue) {
                new SwitchSelinuxTask(getActivity()).execute(true);
                setSelinuxEnabled(true, mSelinuxPersistence.isChecked());
            } else {
                new SwitchSelinuxTask(getActivity()).execute(false);
                setSelinuxEnabled(false, mSelinuxPersistence.isChecked());
            }
            return true;
        } else if (preference == mSelinuxPersistence) {
            setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) newValue);
            return true;
        } else if (preference == mScrollingCachePref) {
            if (newValue != null) {
                SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String) newValue);
                return true;
            }
        }
        return false;
    }

    private void setSelinuxEnabled(boolean status, boolean persistent) {
        SharedPreferences.Editor editor = getContext()
                .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
        if (persistent) {
            editor.putBoolean(Constants.PREF_SELINUX_MODE, status);
        } else {
            editor.remove(Constants.PREF_SELINUX_MODE);
        }
        editor.apply();
        mSelinuxMode.setChecked(status);
    }

    private class SwitchSelinuxTask extends SuTask<Boolean> {
        public SwitchSelinuxTask(Context context) {
            super(context);
        }
        @Override
        protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
            if (params.length != 1) {
                Log.e(TAG, "SwitchSelinuxTask: invalid params count");
                return;
            }
            if (params[0]) {
                SuShell.runWithSuCheck("setenforce 1");
            } else {
                SuShell.runWithSuCheck("setenforce 0");
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                // Did not work, so restore actual value
                setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
            }
        }
    }
}
