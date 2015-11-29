/*
 * Copyright (C) 2013 The ChameleonOS Project
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

import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;

import com.lordclockan.aicpextras.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

public class GestureAnywhereSettings extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new GestureAnywhereSettingsFragment()).commit();
    }

    public static class GestureAnywhereSettingsFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "GestureAnywhereSettings";


        private static final String KEY_ENABLED = "gesture_anywhere_enabled";
        private static final String KEY_POSITION = "gesture_anywhere_position";
        private static final String KEY_GESTURES = "gesture_anywhere_gestures";
        private static final String KEY_TRIGGER_WIDTH = "gesture_anywhere_trigger_width";
        private static final String KEY_TRIGGER_TOP = "gesture_anywhere_trigger_top";
        private static final String KEY_TRIGGER_BOTTOM = "gesture_anywhere_trigger_bottom";

        private SwitchPreference mEnabledPref;
        private ListPreference mPositionPref;
        private SeekBarPreferenceCham mTriggerWidthPref;
        private SeekBarPreferenceCham mTriggerTopPref;
        private SeekBarPreferenceCham mTriggerBottomPref;
        private Preference mGestures;

        private CharSequence mPreviousTitle;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.gesture_anywhere);

            ContentResolver resolver = getActivity().getContentResolver();

            mEnabledPref = (SwitchPreference) findPreference(KEY_ENABLED);
            mEnabledPref.setChecked((Settings.System.getInt(resolver,
                    Settings.System.GESTURE_ANYWHERE_ENABLED, 0) == 1));
            mEnabledPref.setOnPreferenceChangeListener(this);

            PreferenceScreen prefSet = getPreferenceScreen();
            mPositionPref = (ListPreference) prefSet.findPreference(KEY_POSITION);
            mPositionPref.setOnPreferenceChangeListener(this);
            int position = Settings.System.getInt(resolver,
                    Settings.System.GESTURE_ANYWHERE_POSITION, Gravity.LEFT);
            mPositionPref.setValue(String.valueOf(position));
            updatePositionSummary(position);

            mTriggerWidthPref = (SeekBarPreferenceCham) findPreference(KEY_TRIGGER_WIDTH);
            mTriggerWidthPref.setValue(Settings.System.getInt(resolver,
                    Settings.System.GESTURE_ANYWHERE_TRIGGER_WIDTH, 40));
            mTriggerWidthPref.setOnPreferenceChangeListener(this);

            mTriggerTopPref = (SeekBarPreferenceCham) findPreference(KEY_TRIGGER_TOP);
            mTriggerTopPref.setValue(Settings.System.getInt(resolver,
                    Settings.System.GESTURE_ANYWHERE_TRIGGER_TOP, 0));
            mTriggerTopPref.setOnPreferenceChangeListener(this);

            mTriggerBottomPref = (SeekBarPreferenceCham) findPreference(KEY_TRIGGER_BOTTOM);
            mTriggerBottomPref.setValue(Settings.System.getInt(resolver,
                    Settings.System.GESTURE_ANYWHERE_TRIGGER_HEIGHT, 100));
            mTriggerBottomPref.setOnPreferenceChangeListener(this);

            mGestures = prefSet.findPreference(KEY_GESTURES);

        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mPositionPref) {
                int position = Integer.valueOf((String) newValue);
                updatePositionSummary(position);
                return true;
            } else if (preference == mEnabledPref) {
                Settings.System.putInt(resolver,
                        Settings.System.GESTURE_ANYWHERE_ENABLED,
                        ((Boolean) newValue).booleanValue() ? 1 : 0);
                return true;
            } else if (preference == mTriggerWidthPref) {
                int width = ((Integer) newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.GESTURE_ANYWHERE_TRIGGER_WIDTH, width);
                return true;
            } else if (preference == mTriggerTopPref) {
                int top = ((Integer) newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.GESTURE_ANYWHERE_TRIGGER_TOP, top);
                return true;
            } else if (preference == mTriggerBottomPref) {
                int bottom = ((Integer) newValue).intValue();
                Settings.System.putInt(resolver,
                        Settings.System.GESTURE_ANYWHERE_TRIGGER_HEIGHT, bottom);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mGestures) {
                Intent intent = new Intent(getActivity(), GestureAnywhereBuilderActivity.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        private void updatePositionSummary(int value) {
            mPositionPref.setSummary(mPositionPref.getEntries()[mPositionPref.findIndexOfValue("" + value)]);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GESTURE_ANYWHERE_POSITION, value);
        }

        @Override
        public void onPause() {
            super.onPause();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GESTURE_ANYWHERE_SHOW_TRIGGER, 0);
        }

        @Override
        public void onResume() {
            super.onResume();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.GESTURE_ANYWHERE_SHOW_TRIGGER, 1);
        }

    }    
}
