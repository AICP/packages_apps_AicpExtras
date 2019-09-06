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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SELinux;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;
import android.util.Log;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.Constants;
import com.aicp.extras.R;
import com.aicp.extras.utils.SuShell;
import com.aicp.extras.utils.SuTask;
import com.aicp.extras.utils.Util;

public class SystemBehaviour extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = SystemBehaviour.class.getSimpleName();

    private static final String KEY_SMART_PIXELS = "smart_pixels_enable";
    private static final String KEY_AUDIO_PANEL_POSITION = "audio_panel_view_position";
    private static final String KEY_BARS = "bars_settings";
    private static final String SELINUX_CATEGORY = "selinux";

    private SwitchPreference mSelinuxMode;
    private SwitchPreference mSelinuxPersistence;

    @Override
    protected int getPreferenceResource() {
        return R.xml.system_behaviour;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // SELinux
        Preference selinuxCategory = findPreference(SELINUX_CATEGORY);
        mSelinuxMode = (SwitchPreference) findPreference(Constants.PREF_SELINUX_MODE);
        mSelinuxMode.setChecked(SELinux.isSELinuxEnforced());
        mSelinuxMode.setOnPreferenceChangeListener(this);
        mSelinuxPersistence =
                (SwitchPreference) findPreference(Constants.PREF_SELINUX_PERSISTENCE);
        mSelinuxPersistence.setOnPreferenceChangeListener(this);
        mSelinuxPersistence.setChecked(getContext()
                .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
                .contains(Constants.PREF_SELINUX_MODE));
        Util.requireRoot(getActivity(), selinuxCategory);

        /*
        Util.requireConfig(getActivity(), findPreference(KEY_SMART_PIXELS),
                com.android.internal.R.bool.config_enableSmartPixels, true, false);

        Util.requireConfig(getActivity(), findPreference(KEY_AUDIO_PANEL_POSITION),
                com.android.internal.R.bool.config_audioPanelOnLeftSide, true, false);

        Util.requireConfig(getActivity(), findPreference(KEY_BARS),
                com.android.internal.R.bool.config_haveHigherAspectRatioScreen, true, false);
                */
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mSelinuxMode) {
            if ((Boolean) newValue) {
                new SwitchSelinuxTask(getActivity()).execute(true);
                setSelinuxEnabled(true, mSelinuxPersistence.isChecked());
            } else {
                new SwitchSelinuxTask(getActivity()).execute(false);
                setSelinuxEnabled(false, mSelinuxPersistence.isChecked());
            }
            return true;
        } else if (preference == mSelinuxPersistence) {
            setSelinuxEnabled(mSelinuxMode.isChecked(), (Boolean) newValue);
            return true;
        }
        return false;
    }

    private void setSelinuxEnabled(boolean status, boolean persistent) {
        SharedPreferences.Editor editor = getContext()
                .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
        if (persistent) {
            editor.putBoolean(Constants.PREF_SELINUX_MODE, status);
        } else {
            editor.remove(Constants.PREF_SELINUX_MODE);
        }
        editor.apply();
        mSelinuxMode.setChecked(status);
    }

    private class SwitchSelinuxTask extends SuTask<Boolean> {
        public SwitchSelinuxTask(Context context) {
            super(context);
        }
        @Override
        protected void sudoInBackground(Boolean... params) throws SuShell.SuDeniedException {
            if (params.length != 1) {
                Log.e(TAG, "SwitchSelinuxTask: invalid params count");
                return;
            }
            if (params[0]) {
                SuShell.runWithSuCheck("setenforce 1");
            } else {
                SuShell.runWithSuCheck("setenforce 0");
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                // Did not work, so restore actual value
                setSelinuxEnabled(SELinux.isSELinuxEnforced(), mSelinuxPersistence.isChecked());
            }
        }
    }
}
