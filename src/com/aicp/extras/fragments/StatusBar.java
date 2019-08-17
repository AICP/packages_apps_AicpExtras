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
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.aicp.gear.preference.SystemSettingIntListPreference;
import com.aicp.gear.preference.SystemSettingListPreference;
import com.aicp.gear.preference.SystemSettingSwitchPreference;

import com.android.internal.util.aicp.AicpUtils;

public class StatusBar extends BaseSettingsFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SMART_PULLDOWN = "qs_smart_pulldown";
    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String KEY_CARRIER_LABEL = "status_bar_show_carrier";
    private static final String KEY_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String KEY_HIDE_NOTCH = "statusbar_hide_notch";
    private static final String KEY_ESTIMATE_IN_QQS = "show_battery_estimate_qqs";
    private static final String KEY_BATTERY_PERCENTAGE = "status_bar_show_battery_percent";

    private ListPreference mSmartPulldown;
    private ListPreference mQuickPulldown;
    private Preference mCustomCarrierLabel;
    private SystemSettingIntListPreference mShowBatteryPercentage;
    private SystemSettingListPreference mShowCarrierLabel;
    private SystemSettingSwitchPreference mShowBatteryInQQS;

    private String mCustomCarrierLabelText;

    @Override
    protected int getPreferenceResource() {
        return R.xml.status_bar;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        mSmartPulldown = (ListPreference) findPreference(SMART_PULLDOWN);
        int smartPulldown = Settings.System.getInt(resolver,
                Settings.System.QS_SMART_PULLDOWN, 0);
        updateSmartPulldownSummary(smartPulldown);
        mSmartPulldown.setOnPreferenceChangeListener(this);

        // Quick Pulldown
        mQuickPulldown = (ListPreference) findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int quickPulldownValue = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 0, UserHandle.USER_CURRENT);
        mQuickPulldown.setValue(String.valueOf(quickPulldownValue));
        updateQuickPulldownSummary(quickPulldownValue);

        mShowCarrierLabel = (SystemSettingListPreference) findPreference(KEY_CARRIER_LABEL);
        int showCarrierLabel = Settings.System.getInt(resolver,
        Settings.System.STATUS_BAR_SHOW_CARRIER, 1);
        CharSequence[] NonNotchEntries = { getResources().getString(R.string.show_carrier_disabled),
                getResources().getString(R.string.show_carrier_keyguard),
                getResources().getString(R.string.show_carrier_statusbar), getResources().getString(
                        R.string.show_carrier_enabled) };
        CharSequence[] NotchEntries = { getResources().getString(R.string.show_carrier_disabled),
                getResources().getString(R.string.show_carrier_keyguard) };
        CharSequence[] NonNotchValues = {"0", "1" , "2", "3"};
        CharSequence[] NotchValues = {"0", "1"};
        mShowCarrierLabel.setEntries(AicpUtils.hasNotch(getActivity()) ? NotchEntries : NonNotchEntries);
        mShowCarrierLabel.setEntryValues(AicpUtils.hasNotch(getActivity()) ? NotchValues : NonNotchValues);
        mShowCarrierLabel.setValue(String.valueOf(showCarrierLabel));
        mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntry());
        mShowCarrierLabel.setOnPreferenceChangeListener(this);

        mCustomCarrierLabel = (Preference) findPreference(KEY_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        // Battery Percentage
        mShowBatteryPercentage = (SystemSettingIntListPreference) findPreference(KEY_BATTERY_PERCENTAGE);
        int showBatteryPercentage = Settings.System.getIntForUser(resolver,
                Settings.System.SHOW_BATTERY_PERCENT, 0, UserHandle.USER_CURRENT);
        mShowBatteryPercentage.setOnPreferenceChangeListener(this);

        // Battery estimate in Quick QS
        mShowBatteryInQQS = (SystemSettingSwitchPreference) findPreference(KEY_ESTIMATE_IN_QQS);
        updateShowBatteryInQQS(showBatteryPercentage);

        final String displayCutout = getResources().getString(
                com.android.internal.R.string.config_mainBuiltInDisplayCutout);
        if(displayCutout.isEmpty()) {
            final Preference hideNotchPref = (Preference) findPreference(KEY_HIDE_NOTCH);
            hideNotchPref.getParent().removePreference(hideNotchPref);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mSmartPulldown) {
            int value = Integer.parseInt((String) newValue);
            updateSmartPulldownSummary(value);
            return true;
        } else if (preference == mQuickPulldown) {
            int quickPulldownValue = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.STATUS_BAR_QUICK_QS_PULLDOWN,
                    quickPulldownValue, UserHandle.USER_CURRENT);
            updateQuickPulldownSummary(quickPulldownValue);
            return true;
        } else if (preference == mShowBatteryPercentage) {
            int showBatteryPercentage = Integer.valueOf((String) newValue);
            updateShowBatteryInQQS(showBatteryPercentage);
            return true;
        } else if (preference == mShowCarrierLabel) {
            int value = Integer.parseInt((String) newValue);
            updateCarrierLabelSummary(value);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        super.onPreferenceTreeClick(preference);
        final ContentResolver resolver = getActivity().getContentResolver();
        if (KEY_CUSTOM_CARRIER_LABEL.equals(preference.getKey())) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(TextUtils.isEmpty(mCustomCarrierLabelText) ? "" : mCustomCarrierLabelText);
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String value = ((Spannable) input.getText()).toString().trim();
                            Settings.System.putString(resolver, Settings.System.CUSTOM_CARRIER_LABEL, value);
                            updateCustomLabelTextSummary();
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_CUSTOM_CARRIER_LABEL_CHANGED);
                            getActivity().sendBroadcast(i);
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
            return true;
        } else {
            return false;
        }
    }

    private void updateCarrierLabelSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Carrier Label disabled
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_disabled));
        } else if (value == 1) {
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_keyguard));
        } else if (value == 2) {
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_statusbar));
        } else if (value == 3) {
            mShowCarrierLabel.setSummary(res.getString(R.string.show_carrier_enabled));
        }
    }

    private void updateSmartPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Smart pulldown deactivated
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off_summary));
        } else if (value == 3) {
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_none_summary));
        } else {
            String type = res.getString(value == 1
                    ? R.string.smart_pulldown_dismissable
                    : R.string.smart_pulldown_ongoing);
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
        }
    }

    private void updateQuickPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_off));
        } else if (value == 3) {
            // quick pulldown always
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary_always));
        } else {
            String direction = res.getString(value == 2
                    ? R.string.quick_pulldown_left
                    : R.string.quick_pulldown_right);
            mQuickPulldown.setSummary(res.getString(R.string.quick_pulldown_summary, direction));
        }
    }


    private void updateCustomLabelTextSummary() {
        mCustomCarrierLabelText = Settings.System.getString(
                getActivity().getContentResolver(), Settings.System.CUSTOM_CARRIER_LABEL);

        if (TextUtils.isEmpty(mCustomCarrierLabelText)) {
            mCustomCarrierLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomCarrierLabel.setSummary(mCustomCarrierLabelText);
        }
    }

    private void updateShowBatteryInQQS(int value) {
        switch (value) {
            case 1: {
                mShowBatteryInQQS.setEnabled(true);
                break;
            }
            case 0:
            case 2: {
                mShowBatteryInQQS.setChecked(false);
                mShowBatteryInQQS.setEnabled(false);
                break;
            }
        }
    }
}
