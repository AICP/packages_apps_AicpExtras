/*
 * Copyright (C) 2015 VRToxin Project
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
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.lordclockan.R;

import java.util.List;

public class FloatingWindows extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new FloatingWindowsFragment()).commit();
    }

    public static class FloatingWindowsFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String FLOATING_WINDOW_MODE = "floating_window_mode";
        private static final String GESTURE_ANYWHERE_FLOATING = "gesture_anywhere_floating";
        private static final String SLIM_ACTION_FLOATS = "slim_action_floats";

        SwitchPreference mFloatingWindowMode;
        SwitchPreference mGestureAnywhereFloatingWindow;
        SwitchPreference mSlimActionFloatingWindow;

        private ContentResolver mResolver;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            refreshSettings();
        }

        public void refreshSettings() {
            PreferenceScreen prefs = getPreferenceScreen();
            if (prefs != null) {
                prefs.removeAll();
            }

            addPreferencesFromResource(R.xml.floating_windows);
            mResolver = getActivity().getContentResolver();

            mFloatingWindowMode = (SwitchPreference) findPreference(FLOATING_WINDOW_MODE);
            mFloatingWindowMode.setChecked(Settings.System.getInt(mResolver,
                Settings.System.FLOATING_WINDOW_MODE, 0) == 1);
            mFloatingWindowMode.setOnPreferenceChangeListener(this);

            mGestureAnywhereFloatingWindow = (SwitchPreference) findPreference(GESTURE_ANYWHERE_FLOATING);
            mGestureAnywhereFloatingWindow.setChecked(Settings.System.getInt(mResolver,
                Settings.System.GESTURE_ANYWHERE_FLOATING, 0) == 1);
            mGestureAnywhereFloatingWindow.setOnPreferenceChangeListener(this);

            mSlimActionFloatingWindow = (SwitchPreference) findPreference(SLIM_ACTION_FLOATS);
            mSlimActionFloatingWindow.setChecked(Settings.System.getInt(mResolver,
                Settings.System.SLIM_ACTION_FLOATS, 0) == 1);
            mSlimActionFloatingWindow.setOnPreferenceChangeListener(this);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mFloatingWindowMode) {
                Settings.System.putInt(mResolver,
                        Settings.System.FLOATING_WINDOW_MODE,
                (Boolean) newValue ? 1 : 0);
                return true;
           } else if (preference == mGestureAnywhereFloatingWindow) {
                Settings.System.putInt(mResolver,
                        Settings.System.GESTURE_ANYWHERE_FLOATING,
                (Boolean) newValue ? 1 : 0);
                return true;
            } else if (preference == mSlimActionFloatingWindow) {
                Settings.System.putInt(mResolver,
                        Settings.System.SLIM_ACTION_FLOATS,
                (Boolean) newValue ? 1 : 0);
                return true;
            }
            return false;
        }
    }
}
