/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Utils;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class CarrierLabel extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new CarrierLabelFragment()).commit();
    }

    public static class CarrierLabelFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "CarrierLabel";

        private static final String SHOW_CARRIER_LABEL = "status_bar_show_carrier";
        private static final String CUSTOM_CARRIER_LABEL = "custom_carrier_label";
        private static final String STATUS_BAR_CARRIER_FONT_SIZE = "status_bar_carrier_font_size";
        private static final String STATUS_BAR_CARRIER_COLOR = "status_bar_carrier_color";
        private static final String STATUS_BAR_CARRIER_SPOT = "status_bar_carrier_spot";

        static final int DEFAULT_STATUS_CARRIER_COLOR = 0xffffffff;

        private PreferenceScreen mCustomCarrierLabel;

        private ListPreference mShowCarrierLabel;
        private String mCustomCarrierLabelText;
        private SeekBarPreferenceCham mStatusBarCarrierSize;
        private ColorPickerPreference mCarrierColorPicker;
        private ListPreference mStatusBarCarrierSpot;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.carrierlabel);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();


            int intColor;
            String hexColor;

            mShowCarrierLabel =
                    (ListPreference) findPreference(SHOW_CARRIER_LABEL);
            int showCarrierLabel = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_SHOW_CARRIER, 1, UserHandle.USER_CURRENT);
            mShowCarrierLabel.setValue(String.valueOf(showCarrierLabel));
            mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntry());
            mShowCarrierLabel.setOnPreferenceChangeListener(this);

            if (Utils.isWifiOnly(getActivity())) {
                prefSet.removePreference(mShowCarrierLabel);
            }
            mCustomCarrierLabel = (PreferenceScreen) prefSet.findPreference(CUSTOM_CARRIER_LABEL);

            mStatusBarCarrierSize = (SeekBarPreferenceCham) prefSet.findPreference(STATUS_BAR_CARRIER_FONT_SIZE);
            mStatusBarCarrierSize.setValue(Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, 14));
            mStatusBarCarrierSize.setOnPreferenceChangeListener(this);

            mCarrierColorPicker = (ColorPickerPreference) findPreference(STATUS_BAR_CARRIER_COLOR);
            mCarrierColorPicker.setOnPreferenceChangeListener(this);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CARRIER_COLOR, DEFAULT_STATUS_CARRIER_COLOR);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mCarrierColorPicker.setSummary(hexColor);
            mCarrierColorPicker.setNewPreviewColor(intColor);

            mStatusBarCarrierSpot =
                    (ListPreference) findPreference(STATUS_BAR_CARRIER_SPOT);
            int statusBarCarrierSpot = Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_CARRIER_SPOT, 0, UserHandle.USER_CURRENT);
            mStatusBarCarrierSpot.setValue(String.valueOf(statusBarCarrierSpot));
            mStatusBarCarrierSpot.setSummary(mStatusBarCarrierSpot.getEntry());
            mStatusBarCarrierSpot.setOnPreferenceChangeListener(this);

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

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mCarrierColorPicker) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                        Settings.System.STATUS_BAR_CARRIER_COLOR, intHex);
                return true;
            } else if (preference == mShowCarrierLabel) {
                int showCarrierLabel = Integer.valueOf((String) newValue);
                int index = mShowCarrierLabel.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.
                        STATUS_BAR_SHOW_CARRIER, showCarrierLabel, UserHandle.USER_CURRENT);
                mShowCarrierLabel.setSummary(mShowCarrierLabel.getEntries()[index]);
                return true;
            } else if (preference == mStatusBarCarrierSize) {
                int width = ((Integer) newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_CARRIER_FONT_SIZE, width);
                return true;
            } else if (preference == mStatusBarCarrierSpot) {
                int statusBarCarrierSpot = Integer.valueOf((String) newValue);
                int index = mStatusBarCarrierSpot.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.
                        STATUS_BAR_CARRIER_SPOT, statusBarCarrierSpot, UserHandle.USER_CURRENT);
                mStatusBarCarrierSpot.setSummary(mStatusBarCarrierSpot.getEntries()[index]);
                return true;
            }
            return false;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                             final Preference preference) {
            final ContentResolver resolver = getActivity().getContentResolver();
            if (preference.getKey().equals(CUSTOM_CARRIER_LABEL)) {
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
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
