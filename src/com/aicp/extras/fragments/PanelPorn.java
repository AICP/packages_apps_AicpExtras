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

import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.provider.Settings;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

public class PanelPorn extends BaseSettingsFragment
        implements Preference.OnPreferenceChangeListener {

    private ListPreference mVolumeDialogStroke;
    private Preference mVolumeDialogStrokeColor;
    private Preference mVolumeDialogStrokeThickness;
    private Preference mVolumeDialogDashWidth;
    private Preference mVolumeDialogDashGap;

    @Override
    protected int getPreferenceResource() {
        return R.xml.panel_porn;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mVolumeDialogStroke =
                (ListPreference) findPreference(Settings.System.VOLUME_DIALOG_STROKE);
        mVolumeDialogStroke.setOnPreferenceChangeListener(this);
        mVolumeDialogStrokeColor = findPreference(Settings.System.VOLUME_DIALOG_STROKE_COLOR);
        mVolumeDialogStrokeThickness =
                findPreference(Settings.System.VOLUME_DIALOG_STROKE_THICKNESS);
        mVolumeDialogDashWidth = findPreference(Settings.System.VOLUME_DIALOG_STROKE_DASH_WIDTH);
        mVolumeDialogDashGap = findPreference(Settings.System.VOLUME_DIALOG_STROKE_DASH_GAP);
        updateVolumeDialogDependencies(mVolumeDialogStroke.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mVolumeDialogStroke) {
            updateVolumeDialogDependencies((String) newValue);
            return true;
        } else {
            return false;
        }
    }


    private void updateVolumeDialogDependencies(String volumeDialogStroke) {
        if (volumeDialogStroke.equals("0")) {
            mVolumeDialogStrokeColor.setEnabled(false);
            mVolumeDialogStrokeThickness.setEnabled(false);
            mVolumeDialogDashWidth.setEnabled(false);
            mVolumeDialogDashGap.setEnabled(false);
        } else if (volumeDialogStroke.equals("1")) {
            mVolumeDialogStrokeColor.setEnabled(false);
            mVolumeDialogStrokeThickness.setEnabled(true);
            mVolumeDialogDashWidth.setEnabled(true);
            mVolumeDialogDashGap.setEnabled(true);
        } else {
            mVolumeDialogStrokeColor.setEnabled(true);
            mVolumeDialogStrokeThickness.setEnabled(true);
            mVolumeDialogDashWidth.setEnabled(true);
            mVolumeDialogDashGap.setEnabled(true);
        }
    }
}
