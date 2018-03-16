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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v14.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.utils.Util;
import com.aicp.extras.R;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class OtherUi extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String SHOW_CPU_INFO_KEY = "show_cpu_info";
    private static final String KEY_VIBRATE_ON_CHARGE_STATE_CHANGED = "vibration_on_charge_state_changed";
    private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
    private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
    private static final String SCROLLINGCACHE_DEFAULT = "1";
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
        mScreenshotEditAppPref = findPreference("screenshot_edit_app");
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
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // Add empty application definition, the user will be able to edit it later
                        ScreenshotEditPackageListAdapter.PackageItem info = (ScreenshotEditPackageListAdapter.PackageItem) parent.getItemAtPosition(position);
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
    public boolean onPreferenceTreeClick(Preference preference) {
        // Don't show the dialog if there are no available editor apps
        if (preference == mScreenshotEditAppPref && mPackageAdapter.getCount() > 0) {
            selectPackages(DIALOG_SCREENSHOT_EDIT_APP);
            return true;
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.screenshot_edit_app_no_editor),
                    Toast.LENGTH_LONG).show();
        }
        return super.onPreferenceTreeClick(preference);
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

    private class ScreenshotEditPackageListAdapter extends BaseAdapter implements Runnable {
        private PackageManager mPm;
        private LayoutInflater mInflater;
        private final List<PackageItem> mInstalledPackages = new LinkedList<PackageItem>();

        private final Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                PackageItem item = (PackageItem) msg.obj;
                int index = Collections.binarySearch(mInstalledPackages, item);
                if (index < 0) {
                    mInstalledPackages.add(-index - 1, item);
                }
                notifyDataSetChanged();
            }
        };

        public class PackageItem implements Comparable<PackageItem> {
            public final String packageName;
            public final CharSequence title;
            public final Drawable icon;

            PackageItem(String packageName, CharSequence title, Drawable icon) {
                this.packageName = packageName;
                this.title = title;
                this.icon = icon;
            }

            @Override
            public int compareTo(PackageItem another) {
                int result = title.toString().compareToIgnoreCase(another.title.toString());
                return result != 0 ? result : packageName.compareTo(another.packageName);
            }
        }

        public ScreenshotEditPackageListAdapter(Context context) {
            mPm = context.getPackageManager();
            mInflater = LayoutInflater.from(context);
            reloadList();
        }

        @Override
        public int getCount() {
            synchronized (mInstalledPackages) {
                return mInstalledPackages.size();
            }
        }

        @Override
        public PackageItem getItem(int position) {
            synchronized (mInstalledPackages) {
                return mInstalledPackages.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            synchronized (mInstalledPackages) {
                // packageName is guaranteed to be unique in mInstalledPackages
                return mInstalledPackages.get(position).packageName.hashCode();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.preference_icon, null, false);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            }

            PackageItem applicationInfo = getItem(position);
            holder.title.setText(applicationInfo.title);
            holder.icon.setImageDrawable(applicationInfo.icon);

            return convertView;
        }

        private void reloadList() {
            mInstalledPackages.clear();
            new Thread(this).start();
        }

        @Override
        public void run() {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_EDIT);
            // setData is needed because some apps like PhotoEditor need an uri for the EDIT intent action,
            // otherwise they don't answer to the intent query. So we give them the generic EXTERNAL_CONTENT_URI
            // as a fake uri here.
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/png");
            List<ResolveInfo> installedAppsInfo = mPm.queryIntentActivities(intent, 0);
            for (ResolveInfo info : installedAppsInfo) {
                ApplicationInfo appInfo = info.activityInfo.applicationInfo;
                final PackageItem item = new PackageItem(appInfo.packageName,
                        appInfo.loadLabel(mPm), appInfo.loadIcon(mPm));
                mHandler.obtainMessage(0, item).sendToTarget();
            }
        }

        private class ViewHolder {
            TextView title;
            ImageView icon;
        }
    }
}
