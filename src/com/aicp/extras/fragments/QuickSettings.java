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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickSettings extends BaseSettingsFragment
    implements OnPreferenceChangeListener {

    private static final boolean DEBUG = false;
    private static final String QS_QUICKBAR_COLUMNS_AUTO = "qs_quickbar_columns_auto";
    private static final String QS_QUICKBAR_COLUMNS_COUNT = "qs_quickbar_columns";
    private static final String SYSTEM_INFO = "qs_system_info";
    private static final String QS_HEADER_CATEGORY = "qs_header_category";
    private static final String KEY_CUSTOM_FOOTER_TEXT = "custom_footer_text";

    private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
    private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
    private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
    private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
    private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
    private static final String STATUS_BAR_CUSTOM_HEADER = "status_bar_custom_header";
    private static final String CUSTOM_HEADER_ENABLED = "status_bar_custom_header";
    private static final String FILE_HEADER_SELECT = "file_header_select";

    private static final int REQUEST_PICK_IMAGE = 0;

    private ListPreference mSYSInfo;
    private PreferenceCategory mQSHeaderCategory;
    private Preference mCustomFooterTextPref;
    private SwitchPreference mQQSColsAuto;
    private SystemSettingSeekBarPreference mQQSColsCount;

    private Preference mHeaderBrowse;
    private ListPreference mDaylightHeaderPack;
    private SystemSettingSeekBarPreference mHeaderShadow;
    private ListPreference mHeaderProvider;
    private String mDaylightHeaderProvider;
    private Preference mFileHeader;
    private String mFileHeaderProvider;

    private String mCustomFooterText;

    @Override
    protected int getPreferenceResource() {
        return R.xml.quick_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        mSYSInfo = (ListPreference) findPreference(SYSTEM_INFO);
        mQSHeaderCategory = (PreferenceCategory) findPreference(QS_HEADER_CATEGORY);
        configureSystemInfo();

        mCustomFooterTextPref = (Preference) findPreference(KEY_CUSTOM_FOOTER_TEXT);
        updateCustomFooterTextSummary();

        mHeaderBrowse = findPreference(CUSTOM_HEADER_BROWSE);

        mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

        List<String> entries = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        getAvailableHeaderPacks(entries, values);
        mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
        mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

        boolean headerEnabled = Settings.System.getInt(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER, 0) != 0;
        updateHeaderProviderSummary(headerEnabled);
        mDaylightHeaderPack.setOnPreferenceChangeListener(this);

        mHeaderShadow = (SystemSettingSeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        final int headerShadow = Settings.System.getInt(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
        mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
        mHeaderShadow.setOnPreferenceChangeListener(this);

        mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
        mFileHeaderProvider = getResources().getString(R.string.file_header_provider);
        String providerName = Settings.System.getString(resolver,
                Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER);
        if (providerName == null) {
            providerName = mDaylightHeaderProvider;
        }
        mHeaderBrowse.setEnabled(isBrowseHeaderAvailable() && !providerName.equals(mFileHeaderProvider));

        mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
        int valueIndex = mHeaderProvider.findIndexOfValue(providerName);
        mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mHeaderProvider.setSummary(mHeaderProvider.getEntry());
        mHeaderProvider.setOnPreferenceChangeListener(this);
        mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));

        mFileHeader = findPreference(FILE_HEADER_SELECT);
        mFileHeader.setEnabled(providerName.equals(mFileHeaderProvider));
/*        mQQSColsAuto = (SwitchPreference) findPreference(QS_QUICKBAR_COLUMNS_AUTO);
        mQQSColsCount = (SystemSettingSeekBarPreference) findPreference(QS_QUICKBAR_COLUMNS_COUNT);

        boolean qqsColsAutoEnabled = Settings.System.getInt(resolver,
                Settings.System.AICP_QS_QUICKBAR_COLUMNS, 6) == -1;
        mQQSColsAuto.setChecked(qqsColsAutoEnabled);
        mQQSColsCount.setEnabled(!qqsColsAutoEnabled);
        mQQSColsAuto.setOnPreferenceChangeListener(this);
        */
    }

    private void updateHeaderProviderSummary(boolean headerEnabled) {
        mDaylightHeaderPack.setSummary(getResources().getString(R.string.header_provider_disabled));
        if (headerEnabled) {
            String settingHeaderPackage = Settings.System.getString(getActivity().getContentResolver(),
                    Settings.System.OMNI_STATUS_BAR_DAYLIGHT_HEADER_PACK);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
            if (valueIndex == -1) {
                // no longer found
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER, 0);
            } else {
                mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
                mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
            }
        }
    }

    private boolean isBrowseHeaderAvailable() {
        PackageManager pm = getActivity().getPackageManager();
        Intent browse = new Intent();
        browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.PickHeaderActivity");
        return pm.resolveActivity(browse, 0) != null;
    }

    private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
        Map<String, String> headerMap = new HashMap<String, String>();
        Intent i = new Intent();
        PackageManager packageManager = getActivity().getPackageManager();
        i.setAction("org.omnirom.DaylightHeaderPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            headerMap.put(label, packageName);
        }
        i.setAction("org.omnirom.DaylightHeaderPack1");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
            if (r.activityInfo.name.endsWith(".theme")) {
                continue;
            }
            if (label == null) {
                label = packageName;
            }
            headerMap.put(label, packageName  + "/" + r.activityInfo.name);
        }
        List<String> labelList = new ArrayList<String>();
        labelList.addAll(headerMap.keySet());
        Collections.sort(labelList);
        for (String label : labelList) {
            entries.add(label);
            values.add(headerMap.get(label));
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        /*
        if (preference == mQQSColsAuto) {
            Boolean qqsColsAutoEnabled = (Boolean) newValue;
            mQQSColsCount.setEnabled(!qqsColsAutoEnabled);
            if (qqsColsAutoEnabled){
              Settings.System.putInt(resolver,
                      Settings.System.AICP_QS_QUICKBAR_COLUMNS, -1);
            }
            return true;
        }
        */
        if (preference == mHeaderShadow) {
            Integer headerShadow = (Integer) newValue;
            int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
            Settings.System.putInt(resolver,
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
            return true;
        } else if (preference == mDaylightHeaderPack) {
            String value = (String) newValue;
            Settings.System.putString(resolver,
                    Settings.System.OMNI_STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
            int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
            return true;
        } else if (preference == mHeaderProvider) {
            String value = (String) newValue;
            Settings.System.putString(resolver,
                    Settings.System.OMNI_STATUS_BAR_CUSTOM_HEADER_PROVIDER, value);
            int valueIndex = mHeaderProvider.findIndexOfValue(value);
            mHeaderProvider.setSummary(mHeaderProvider.getEntries()[valueIndex]);
            mDaylightHeaderPack.setEnabled(value.equals(mDaylightHeaderProvider));
            mHeaderBrowse.setEnabled(!value.equals(mFileHeaderProvider));
            mHeaderBrowse.setTitle(valueIndex == 0 ? R.string.custom_header_browse_title : R.string.custom_header_pick_title);
            mHeaderBrowse.setSummary(valueIndex == 0 ? R.string.custom_header_browse_summary_new : R.string.custom_header_pick_summary);
            mFileHeader.setEnabled(value.equals(mFileHeaderProvider));
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_PICK_IMAGE) {
            if (resultCode != Activity.RESULT_OK) {
                return;
            }
            final Uri imageUri = result.getData();
            Settings.System.putString(getContentResolver(), Settings.System.OMNI_STATUS_BAR_FILE_HEADER_IMAGE, imageUri.toString());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (KEY_CUSTOM_FOOTER_TEXT.equals(preference.getKey())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.footer_text_label_title);
            alert.setMessage(R.string.footer_text_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomFooterText) ? "" : mCustomFooterText);
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putStringForUser(resolver, Settings.System.AICP_FOOTER_TEXT_STRING, value, UserHandle.USER_CURRENT);
                            updateCustomFooterTextSummary();
/*                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                            getActivity().sendBroadcast(i);*/
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
            return true;
        } else if (preference == mFileHeader) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PICK_IMAGE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void configureSystemInfo() {
        Resources res = getResources();
        String[] entriesArray = res.getStringArray(R.array.qs_system_info_entries);
        String[] valuesArray = res.getStringArray(R.array.qs_system_info_values);
        String[] checksArray = res.getStringArray(R.array.qs_system_info_checks);
        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();

        entries.add(entriesArray[0]);
        values.add(valuesArray[0]);
        int count = valuesArray.length;
        for (int i = 1 ; i < count ; i++) {
            int resID = res.getIdentifier(checksArray[i-1], "string", "android");
            if (DEBUG) Log.d("systemInfo", "resID= " + resID);
            if (resID > 0 && !res.getString(resID).isEmpty()) {
                  if (DEBUG) Log.d("systemInfo", "sysPath= " + res.getString(resID));
                  entries.add(entriesArray[i]);
                  values.add(valuesArray[i]);
            }
        }
        mSYSInfo.setEntries(entries.toArray(new String[entries.size()]));
        mSYSInfo.setEntryValues(values.toArray(new String[values.size()]));
        if (entries.size() < 2) mQSHeaderCategory.getParent().removePreference(mQSHeaderCategory);
    }

    private void updateCustomFooterTextSummary() {
        mCustomFooterText = Settings.System.getStringForUser(
                getActivity().getContentResolver(), Settings.System.AICP_FOOTER_TEXT_STRING, UserHandle.USER_CURRENT);

        if (TextUtils.isEmpty(mCustomFooterText)) {
            mCustomFooterTextPref.setSummary(R.string.footer_text_default);
        } else {
            mCustomFooterTextPref.setSummary(mCustomFooterText);
        }
    }
}
