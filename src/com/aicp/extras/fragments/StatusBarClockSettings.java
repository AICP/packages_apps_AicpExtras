/*
 * Copyright (C) 2018 Android Ice Cold Project
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

import static android.view.DisplayCutout.BOUNDS_POSITION_LEFT;
import static android.view.DisplayCutout.BOUNDS_POSITION_RIGHT;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.aicp.gear.preference.SystemSettingIntListPreference;
import com.aicp.gear.preference.SystemSettingListPreference;
import com.aicp.gear.util.DeviceUtils;
import com.android.internal.util.aicp.AicpUtils;

import java.util.Date;

public class StatusBarClockSettings extends BaseSettingsFragment implements OnPreferenceChangeListener {


    private static final String TAG = "StatusBarClockSettings";

    private static final String CLOCK_DATE_FORMAT = "status_bar_clock_date_format";
    private static final String CLOCK_POSITION = "status_bar_clock_position";

    public static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    public static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int CUSTOM_CLOCK_DATE_FORMAT_INDEX = 18;

    private SystemSettingIntListPreference mClockPosition;
    private SystemSettingListPreference mClockDateFormat;

    @Override
    protected int getPreferenceResource() {
        return R.xml.status_bar_clock;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ContentResolver resolver = getActivity().getContentResolver();

        mClockPosition = (SystemSettingIntListPreference) findPreference(CLOCK_POSITION);

        mClockDateFormat = (SystemSettingListPreference) findPreference(CLOCK_DATE_FORMAT);
        mClockDateFormat.setOnPreferenceChangeListener(this);
        if (mClockDateFormat.getValue() == null) {
            mClockDateFormat.setValue("EEE");
        }

        parseClockDateFormats();
    }

    @Override
    public void onResume() {
        super.onResume();

        final boolean hasNotch = AicpUtils.hasNotch(getActivity());
        final int notchType = DeviceUtils.getCutoutType(getActivity());
        Log.v(TAG,"notchType: " + notchType);

        // Adjust status bar preferences for RTL
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            if (hasNotch && !(notchType == BOUNDS_POSITION_LEFT || notchType == BOUNDS_POSITION_RIGHT)) {
                mClockPosition.setEntries(R.array.clock_position_entries_notch_rtl);
                mClockPosition.setEntryValues(R.array.clock_position_values_notch_rtl);
            } else {
                mClockPosition.setEntries(R.array.clock_position_entries_rtl);
                mClockPosition.setEntryValues(R.array.clock_position_values_rtl);
            }
        } else if (hasNotch && !(notchType == BOUNDS_POSITION_LEFT || notchType == BOUNDS_POSITION_RIGHT)) {
            mClockPosition.setEntries(R.array.clock_position_entries_notch);
            mClockPosition.setEntryValues(R.array.clock_position_values_notch);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        AlertDialog dialog;
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mClockDateFormat) {
            int index = mClockDateFormat.findIndexOfValue((String) newValue);

            if (index == CUSTOM_CLOCK_DATE_FORMAT_INDEX) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.clock_date_string_edittext_title);
                alert.setMessage(R.string.clock_date_string_edittext_summary);

                LinearLayout container = new LinearLayout(getActivity());
                container.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(55, 20, 55, 20);

                final EditText input = new EditText(getActivity());
                String oldText = Settings.System.getString(
                    resolver,
                    Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT);
                if (oldText != null) {
                    input.setText(oldText);
                }
                input.setLayoutParams(lp);
                input.setGravity(android.view.Gravity.TOP| Gravity.START);
                container.addView(input);
                alert.setView(container);

                alert.setPositiveButton(R.string.menu_save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        String value = input.getText().toString();
                        if (value.equals("")) {
                            return;
                        }
                        Settings.System.putString(resolver,
                            Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT, value);

                        return;
                    }
                });

                alert.setNegativeButton(R.string.menu_cancel,
                    new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int which) {
                        return;
                    }
                });
                dialog = alert.create();
                dialog.show();
            } else {
                if ((String) newValue != null) {
                    Settings.System.putString(resolver,
                        Settings.System.STATUS_BAR_CLOCK_DATE_FORMAT, (String) newValue);
                }
            }
            return true;
        }
        return false;
    }

    private void parseClockDateFormats() {
        String[] dateEntries = getResources().getStringArray(
                R.array.clock_date_format_entries_values);
        CharSequence parsedDateEntries[];
        parsedDateEntries = new String[dateEntries.length];
        Date now = new Date();

        int lastEntry = dateEntries.length - 1;
        int dateFormat = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_CLOCK_DATE_STYLE, 0);
        for (int i = 0; i < dateEntries.length; i++) {
            if (i == lastEntry) {
                parsedDateEntries[i] = dateEntries[i];
            } else {
                String newDate;
                CharSequence dateString = DateFormat.format(dateEntries[i], now);
                if (dateFormat == CLOCK_DATE_STYLE_LOWERCASE) {
                    newDate = dateString.toString().toLowerCase();
                } else if (dateFormat == CLOCK_DATE_STYLE_UPPERCASE) {
                    newDate = dateString.toString().toUpperCase();
                } else {
                    newDate = dateString.toString();
                }

                parsedDateEntries[i] = newDate;
            }
        }
        mClockDateFormat.setEntries(parsedDateEntries);
    }
}
