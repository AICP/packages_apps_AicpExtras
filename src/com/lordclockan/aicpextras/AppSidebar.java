/*
 * Copyright (C) 2015 AICP
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

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

public class AppSidebar extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
            new AppSidebarFragment()).commit();
    }

    public static class AppSidebarFragment extends PreferenceFragment
            implements OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private static final String TAG = "AppSideBar";

        private static final String KEY_ENABLED = "sidebar_enable";
        private static final String KEY_TRANSPARENCY = "sidebar_transparency";
        private static final String KEY_SETUP_ITEMS = "sidebar_setup_items";
        private static final String KEY_POSITION = "sidebar_position";
        private static final String KEY_HIDE_LABELS = "sidebar_hide_labels";
        private static final String KEY_TRIGGER_WIDTH = "trigger_width";
        private static final String KEY_TRIGGER_TOP = "trigger_top";
        private static final String KEY_TRIGGER_BOTTOM = "trigger_bottom";
        private static final String KEY_HIDE_TIMEOUT = "app_sidebar_hide_timeout";

        private SwitchPreference mEnabledPref;
        private SeekBarPreferenceCham mTransparencyPref;
        private ListPreference mPositionPref;
        private CheckBoxPreference mHideLabelsPref;
        private SeekBarPreferenceCham mTriggerWidthPref;
        private SeekBarPreferenceCham mTriggerTopPref;
        private SeekBarPreferenceCham mTriggerBottomPref;
        private SeekBarPreferenceCham mHideTimeoutPref;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.app_sidebar_settings);

            ContentResolver resolver = getActivity().getContentResolver();

            mEnabledPref = (SwitchPreference) findPreference(KEY_ENABLED);
            mEnabledPref.setChecked((Settings.System.getInt(resolver,
                Settings.System.APP_SIDEBAR_ENABLED, 0) == 1));
            mEnabledPref.setOnPreferenceChangeListener(this);

            mHideLabelsPref = (CheckBoxPreference) findPreference(KEY_HIDE_LABELS);
            mHideLabelsPref.setChecked((Settings.System.getInt(resolver,
                Settings.System.APP_SIDEBAR_DISABLE_LABELS, 0) == 1));

            PreferenceScreen prefSet = getPreferenceScreen();
            mPositionPref = (ListPreference) prefSet.findPreference(KEY_POSITION);
            int position = Settings.System.getInt(resolver, Settings.System.APP_SIDEBAR_POSITION, 0);
            mPositionPref.setValue(String.valueOf(position));
            updatePositionSummary(position);
            mPositionPref.setOnPreferenceChangeListener(this);

            mTransparencyPref = (SeekBarPreferenceCham) findPreference(KEY_TRANSPARENCY);
            mTransparencyPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_SIDEBAR_TRANSPARENCY, 0));
            mTransparencyPref.setOnPreferenceChangeListener(this);

            mTriggerWidthPref = (SeekBarPreferenceCham) findPreference(KEY_TRIGGER_WIDTH);
            mTriggerWidthPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_SIDEBAR_TRIGGER_WIDTH, 10));
            mTriggerWidthPref.setOnPreferenceChangeListener(this);

            mTriggerTopPref = (SeekBarPreferenceCham) findPreference(KEY_TRIGGER_TOP);
            mTriggerTopPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_SIDEBAR_TRIGGER_TOP, 0));
            mTriggerTopPref.setOnPreferenceChangeListener(this);

            mTriggerBottomPref = (SeekBarPreferenceCham) findPreference(KEY_TRIGGER_BOTTOM);
            mTriggerBottomPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_SIDEBAR_TRIGGER_HEIGHT, 100));
            mTriggerBottomPref.setOnPreferenceChangeListener(this);

            mHideTimeoutPref = (SeekBarPreferenceCham) findPreference(KEY_HIDE_TIMEOUT);
            mHideTimeoutPref.setValue(Settings.System.getInt(resolver,
                Settings.System.APP_SIDEBAR_HIDE_TIMEOUT, 3000));
            mHideTimeoutPref.setOnPreferenceChangeListener(this);

            findPreference(KEY_SETUP_ITEMS).setOnPreferenceClickListener(this);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mTransparencyPref) {
                int transparency = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                    Settings.System.APP_SIDEBAR_TRANSPARENCY, transparency);
                return true;
            } else if (preference == mTriggerWidthPref) {
                int width = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                    Settings.System.APP_SIDEBAR_TRIGGER_WIDTH, width);
                return true;
            } else if (preference == mTriggerTopPref) {
                int top = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                    Settings.System.APP_SIDEBAR_TRIGGER_TOP, top);
                return true;
            } else if (preference == mTriggerBottomPref) {
                int bottom = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                    Settings.System.APP_SIDEBAR_TRIGGER_HEIGHT, bottom);
                return true;
            } else if (preference == mPositionPref) {
                int position = Integer.valueOf((String) newValue);
                updatePositionSummary(position);
                return true;
            } else if (preference == mEnabledPref) {
                boolean value = ((Boolean)newValue).booleanValue();
                Settings.System.putInt(resolver,
                    Settings.System.APP_SIDEBAR_ENABLED,
                    value ? 1 : 0);
                return true;
            } else if (preference == mHideTimeoutPref) {
                int timeout = ((Integer)newValue).intValue();
                Settings.System.putInt(resolver,
                    Settings.System.APP_SIDEBAR_HIDE_TIMEOUT, timeout);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            boolean value;
            ContentResolver resolver = getActivity().getContentResolver();

            if (preference == mHideLabelsPref) {
                value = mHideLabelsPref.isChecked();
                Settings.System.putInt(resolver,
                    Settings.System.APP_SIDEBAR_DISABLE_LABELS,
                    value ? 1 : 0);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }

            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if(preference.getKey().equals(KEY_SETUP_ITEMS)) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName("com.android.systemui",
                    "com.android.systemui.statusbar.sidebar.SidebarConfigurationActivity"));
                getActivity().startActivity(intent);
                return true;
            }
            return false;
        }

        private void updatePositionSummary(int value) {
            mPositionPref.setSummary(mPositionPref.getEntries()[mPositionPref.findIndexOfValue("" + value)]);
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.APP_SIDEBAR_POSITION, value);
        }

        @Override
        public void onPause() {
            super.onPause();
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.APP_SIDEBAR_SHOW_TRIGGER, 0);
        }

        @Override
        public void onResume() {
            super.onResume();
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.APP_SIDEBAR_SHOW_TRIGGER, 1);
        }
    }
}
