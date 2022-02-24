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
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;
import com.aicp.gear.preference.SystemSettingSeekBarPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickSettings extends BaseSettingsFragment {/*
    implements OnPreferenceChangeListener {*/

    private static final boolean DEBUG = false;
/*    private static final String QS_QUICKBAR_COLUMNS_AUTO = "qs_quickbar_columns_auto";
    private static final String QS_QUICKBAR_COLUMNS_COUNT = "qs_quickbar_columns";*/
    private static final String SYSTEM_INFO = "qs_system_info";
//    private static final String KEY_CUSTOM_FOOTER_TEXT = "custom_footer_text";

    private ListPreference mSysInfo;
/*    private Preference mCustomFooterTextPref;
    private SwitchPreference mQQSColsAuto;
    private SystemSettingSeekBarPreference mQQSColsCount;

    private String mCustomFooterText;
*/
    @Override
    protected int getPreferenceResource() {
        return R.xml.quick_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        mSysInfo = (ListPreference) findPreference(SYSTEM_INFO);
        configureSystemInfo();
/*
        mCustomFooterTextPref = (Preference) findPreference(KEY_CUSTOM_FOOTER_TEXT);
        updateCustomFooterTextSummary();

        mQQSColsAuto = (SwitchPreference) findPreference(QS_QUICKBAR_COLUMNS_AUTO);
        mQQSColsCount = (SystemSettingSeekBarPreference) findPreference(QS_QUICKBAR_COLUMNS_COUNT);

        boolean qqsColsAutoEnabled = Settings.System.getInt(resolver,
                Settings.System.QS_QUICKBAR_COLUMNS, 6) == -1;
        mQQSColsAuto.setChecked(qqsColsAutoEnabled);
        mQQSColsCount.setEnabled(!qqsColsAutoEnabled);
        mQQSColsAuto.setOnPreferenceChangeListener(this);*/
    }
/*
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mQQSColsAuto) {
            Boolean qqsColsAutoEnabled = (Boolean) newValue;
            mQQSColsCount.setEnabled(!qqsColsAutoEnabled);
            if (qqsColsAutoEnabled){
              Settings.System.putInt(resolver,
                      Settings.System.QS_QUICKBAR_COLUMNS, -1);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        final ContentResolver resolver = getActivity().getContentResolver();
        if (KEY_CUSTOM_FOOTER_TEXT.equals(preference.getKey())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.footer_text_label_title);
            alert.setMessage(R.string.footer_text_label_explain);

            LinearLayout container = new LinearLayout(getActivity());
            container.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(55, 20, 55, 20);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomFooterText) ? "" : mCustomFooterText);
            input.setSelection(input.getText().length());
            input.setLayoutParams(lp);
            input.setGravity(android.view.Gravity.TOP| Gravity.START);
            container.addView(input);
            alert.setView(container);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putStringForUser(resolver, Settings.System.AICP_FOOTER_TEXT_STRING, value, UserHandle.USER_CURRENT);
                            updateCustomFooterTextSummary();
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
*/
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
        mSysInfo.setEntries(entries.toArray(new String[entries.size()]));
        mSysInfo.setEntryValues(values.toArray(new String[values.size()]));
        if (entries.size() < 2) mSysInfo.getParent().removePreference(mSysInfo);
    }
/*
    private void updateCustomFooterTextSummary() {
        mCustomFooterText = Settings.System.getStringForUser(
                getActivity().getContentResolver(), Settings.System.AICP_FOOTER_TEXT_STRING, UserHandle.USER_CURRENT);

        if (TextUtils.isEmpty(mCustomFooterText)) {
            mCustomFooterTextPref.setSummary(R.string.footer_text_default);
        } else {
            mCustomFooterTextPref.setSummary(mCustomFooterText);
        }
    }*/
}
