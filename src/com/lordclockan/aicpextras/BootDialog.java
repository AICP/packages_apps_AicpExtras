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
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Build;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class BootDialog extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new BootDialogFragment()).commit();
    }

    public static class BootDialogFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "BootDialog";

        private static final String PREF_BOOT_DIALOG_BG_COLOR = "boot_dialog_bg_color";
        private static final String PREF_BOOT_DIALOG_STROKE_COLOR = "boot_dialog_stroke_color";
        private static final String PREF_BOOT_DIALOG_STROKE_THICKNESS = "boot_dialog_stroke_thickness";
        private static final String PREF_BOOT_DIALOG_CORNER_RADIUS = "boot_dialog_corner_radius";
        private static final String PREF_BOOT_DIALOG_TITLE = "boot_dialog_title";
        private static final String PREF_BOOT_DIALOG_TEST = "boot_dialog_test";
        private static final String PREF_BOOT_DIALOG_MESSAGE_COLOR = "boot_dialog_message_color";
        private static final String PREF_BOOT_DIALOG_TITLE_COLOR = "boot_dialog_title_color";
        private static final String PREF_BOOT_DIALOG_PACKAGES_RANDOM_COLOR = "boot_dialog_packages_random_color";

        static final int DEFAULT_BOOT_DIALOG_BG_COLOR = 0xFF000000;
        static final int DEFAULT_BOOT_DIALOG_MESSAGE_COLOR = 0xFF000000;
        static final int DEFAULT_BOOT_DIALOG_TITLE_COLOR = 0xFF000000;
        static final int DEFAULT_BOOT_DIALOG_STROKE_COLOR = 0xFF33B5E5;

        private ColorPickerPreference mBootDialogBgColor;
        private ColorPickerPreference mBootDialogStrokeColor;
        private SeekBarPreferenceCham mBootDialogStrokeThickness;
        private SeekBarPreferenceCham mBootDialogCornerRadius;
        private PreferenceScreen mBootDialogTitle;
        private String mBootDialogTitleText;
        private PreferenceScreen mBootDialogTest;
        private ColorPickerPreference mBootDialogMessageColor;
        private ColorPickerPreference mBootDialogTitleColor;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.boot_dialog);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();


            // Boot dialog bg color
            mBootDialogBgColor =
                    (ColorPickerPreference) findPreference(PREF_BOOT_DIALOG_BG_COLOR);
            mBootDialogBgColor.setOnPreferenceChangeListener(this);
            int dialogBgColor = Settings.System.getInt(resolver,
                    Settings.System.BOOT_DIALOG_BG_COLOR, DEFAULT_BOOT_DIALOG_BG_COLOR);
            String dialogHexColor = String.format("#%08x", (0xFF000000 & dialogBgColor));
            mBootDialogBgColor.setSummary(dialogHexColor);
            mBootDialogBgColor.setNewPreviewColor(dialogBgColor);

            // Boot dialog stroke color
            mBootDialogStrokeColor =
                    (ColorPickerPreference) findPreference(PREF_BOOT_DIALOG_STROKE_COLOR);
            mBootDialogStrokeColor.setOnPreferenceChangeListener(this);
            int dialogStrokeIntColor = Settings.System.getInt(resolver,
                    Settings.System.BOOT_DIALOG_STROKE_COLOR, DEFAULT_BOOT_DIALOG_STROKE_COLOR);
            String dialogStrokeHexColor = String.format("#%08x", (0xFF33B5E5 & dialogStrokeIntColor));
            mBootDialogStrokeColor.setSummary(dialogStrokeHexColor);
            mBootDialogStrokeColor.setNewPreviewColor(dialogStrokeIntColor);

            // Boot dialog stroke thickness
            mBootDialogStrokeThickness =
                    (SeekBarPreferenceCham) findPreference(PREF_BOOT_DIALOG_STROKE_THICKNESS);
            int dialogStrokeThickness = Settings.System.getInt(resolver,
                    Settings.System.BOOT_DIALOG_STROKE_THICKNESS, 12);
            mBootDialogStrokeThickness.setValue(dialogStrokeThickness / 1);
            mBootDialogStrokeThickness.setOnPreferenceChangeListener(this);

            // Boot dialog corner radius
            mBootDialogCornerRadius =
                    (SeekBarPreferenceCham) findPreference(PREF_BOOT_DIALOG_CORNER_RADIUS);
            int dialogCornerRadius = Settings.System.getInt(resolver,
                    Settings.System.BOOT_DIALOG_CORNER_RADIUS, 45);
            mBootDialogCornerRadius.setValue(dialogCornerRadius / 1);
            mBootDialogCornerRadius.setOnPreferenceChangeListener(this);

            // Boot dialog message color
            mBootDialogMessageColor =
                    (ColorPickerPreference) findPreference(PREF_BOOT_DIALOG_MESSAGE_COLOR);
            mBootDialogMessageColor.setOnPreferenceChangeListener(this);
            int dialogMessageColor = Settings.System.getInt(resolver,
                    Settings.System.BOOT_DIALOG_MESSAGE_COLOR, DEFAULT_BOOT_DIALOG_MESSAGE_COLOR);
            String dialogMessageHexColor = String.format("#%08x", (0xFF000000 & dialogMessageColor));
            mBootDialogMessageColor.setSummary(dialogMessageHexColor);
            mBootDialogMessageColor.setNewPreviewColor(dialogMessageColor);

            // Boot dialog title color
            mBootDialogTitleColor =
                    (ColorPickerPreference) findPreference(PREF_BOOT_DIALOG_TITLE_COLOR);
            mBootDialogTitleColor.setOnPreferenceChangeListener(this);
            int dialogTitleColor = Settings.System.getInt(resolver,
                    Settings.System.BOOT_DIALOG_TITLE_COLOR, DEFAULT_BOOT_DIALOG_TITLE_COLOR);
            String dialogTitleHexColor = String.format("#%08x", (0xFF000000 & dialogTitleColor));
            mBootDialogTitleColor.setSummary(dialogTitleHexColor);
            mBootDialogTitleColor.setNewPreviewColor(dialogTitleColor);

            mBootDialogTitle = (PreferenceScreen) prefSet.findPreference(PREF_BOOT_DIALOG_TITLE);
            mBootDialogTest = (PreferenceScreen) prefSet.findPreference(PREF_BOOT_DIALOG_TEST);

            updateCustomTitleTextSummary();

        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBootDialogBgColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.BOOT_DIALOG_BG_COLOR, intHex);
                return true;
            } else if (preference == mBootDialogStrokeColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.BOOT_DIALOG_STROKE_COLOR, intHex);
                return true;
            } else if (preference == mBootDialogStrokeThickness) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.BOOT_DIALOG_STROKE_THICKNESS, val * 1);
                return true;
            } else if (preference == mBootDialogCornerRadius) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.BOOT_DIALOG_CORNER_RADIUS, val * 1);
                return true;
            } else if (preference == mBootDialogMessageColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.BOOT_DIALOG_MESSAGE_COLOR, intHex);
                return true;
            } else if (preference == mBootDialogTitleColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.BOOT_DIALOG_TITLE_COLOR, intHex);
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
            if (preference.getKey().equals(PREF_BOOT_DIALOG_TEST)) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.boot_dialog_test_title);
                alertDialog.setMessage(R.string.boot_dialog_test_message);
                alertDialog.setIcon(R.drawable.boot_icon);
                alertDialog.setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        });
                //alertDialog.setNegativeButton(R.string.cancel, null);
                final GradientDrawable dialogGd = new GradientDrawable();
                dialogGd.setColor(Settings.System.getInt(resolver,
                        Settings.System.BOOT_DIALOG_BG_COLOR, 0xFF000000));
                dialogGd.setStroke(Settings.System.getInt(resolver,
                        Settings.System.BOOT_DIALOG_STROKE_THICKNESS, 12),
                        Settings.System.getInt(resolver,
                        Settings.System.BOOT_DIALOG_STROKE_COLOR, 0xFF33B5E5));
                dialogGd.setCornerRadius(Settings.System.getInt(resolver,
                        Settings.System.BOOT_DIALOG_CORNER_RADIUS, 45));

                AlertDialog dialog = alertDialog.create();
                dialog.getWindow().setBackgroundDrawable(dialogGd);
                dialog.show();
                int textViewTitle = dialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
                if (textViewTitle != 0) {
                    int dialogTitleColor = Settings.System.getInt(resolver,
                            Settings.System.BOOT_DIALOG_TITLE_COLOR, 0xFF000000);
                    TextView tvTitle = (TextView) dialog.findViewById(textViewTitle);
                    if (((Settings.System.getInt(resolver,
                            Settings.System.BOOT_DIALOG_BG_COLOR, 0xFF000000)) == 0xFF000000) &&
                            (dialogTitleColor == 0xFF000000)) {
                        tvTitle.setTextColor(0xFFFFFFFF);
                    } else {
                        tvTitle.setTextColor(dialogTitleColor);
                    }
                    String dialogTitle = Build.MODEL + " " + getContext().getResources().getString(
                            R.string.boot_dialog_starting);
                    String customDialogTitle = Settings.System.getString(getActivity().getContentResolver(),
                            Settings.System.BOOT_DIALOG_TITLE);
                    if (!TextUtils.isEmpty(customDialogTitle)) {
                        tvTitle.setText(customDialogTitle);
                    } else {
                        tvTitle.setText(dialogTitle);
                    }
                }
                int textViewMessage = dialog.getContext().getResources().getIdentifier("android:id/message", null, null);
                if (textViewMessage != 0) {
                    int dialogMessageColor = Settings.System.getInt(resolver,
                        Settings.System.BOOT_DIALOG_MESSAGE_COLOR, 0xFF000000);
                    TextView tvMessage = (TextView) dialog.findViewById(textViewMessage);
                    if (((Settings.System.getInt(resolver,
                            Settings.System.BOOT_DIALOG_BG_COLOR, 0xFF000000)) == 0xFF000000) &&
                            (dialogMessageColor == 0xFF000000)) {
                        tvMessage.setTextColor(0xFFFFFFFF);
                    } else {
                        tvMessage.setTextColor(dialogMessageColor);
                    }
                }
                int dialogIcon = dialog.getContext().getResources().getIdentifier("android:id/icon", null, null);
                if (dialogIcon != 0) {
                    ImageView icon = (ImageView) dialog.findViewById(dialogIcon);
                    if ((Settings.System.getInt(resolver,
                            Settings.System.BOOT_DIALOG_BG_COLOR, 0xFFFFFFFF)) == 0xFF000000) {
                        icon.setColorFilter(0xFFFFFFFF);
                    }
                }
            }
            if (preference.getKey().equals(PREF_BOOT_DIALOG_TITLE)) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.boot_dialog_custom_title);
                alert.setMessage(R.string.boot_dialog_custom_explain);

                // Set an EditText view to get user input
                final EditText input = new EditText(getActivity());
                input.setText(TextUtils.isEmpty(mBootDialogTitleText) ? "" : mBootDialogTitleText);
                input.setSelection(input.getText().length());
                alert.setView(input);
                alert.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = ((Spannable) input.getText()).toString().trim();
                                Settings.System.putString(resolver, Settings.System.BOOT_DIALOG_TITLE, value);
                                updateCustomTitleTextSummary();
                            }
                        });
                alert.setNegativeButton(getString(android.R.string.cancel), null);
                alert.show();
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void updateCustomTitleTextSummary() {
            mBootDialogTitleText = Settings.System.getString(
                    getActivity().getContentResolver(), Settings.System.BOOT_DIALOG_TITLE);

            if (TextUtils.isEmpty(mBootDialogTitleText)) {
                mBootDialogTitle.setSummary(R.string.boot_dialog_custom_title_notset);
            } else {
                mBootDialogTitle.setSummary(mBootDialogTitleText);
            }
        }
    }
}
