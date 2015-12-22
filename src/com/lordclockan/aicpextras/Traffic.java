/*
 * Copyright (C) 2014 The Dirty Unicorns Project
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

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.TrafficStats;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

public class Traffic extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new TrafficFragment()).commit();
    }

    public static class TrafficFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        private static final String TAG = "Traffic";

        private static final String NETWORK_TRAFFIC_STATE = "network_traffic_state";
        private static final String NETWORK_TRAFFIC_UNIT = "network_traffic_unit";
        private static final String NETWORK_TRAFFIC_PERIOD = "network_traffic_period";
        private static final String NETWORK_TRAFFIC_AUTOHIDE = "network_traffic_autohide";
        private static final String NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD = "network_traffic_autohide_threshold";

        private ListPreference mNetTrafficState;
        private ListPreference mNetTrafficUnit;
        private ListPreference mNetTrafficPeriod;
        private SwitchPreference mNetTrafficAutohide;
        private SeekBarPreferenceCham mNetTrafficAutohideThreshold;

        private int mNetTrafficVal;
        private int MASK_UP;
        private int MASK_DOWN;
        private int MASK_UNIT;
        private int MASK_PERIOD;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.traffic);

            loadResources();

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mNetTrafficState = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_STATE);
            mNetTrafficUnit = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_UNIT);
            mNetTrafficPeriod = (ListPreference) prefSet.findPreference(NETWORK_TRAFFIC_PERIOD);

            mNetTrafficAutohide =
                    (SwitchPreference) prefSet.findPreference(NETWORK_TRAFFIC_AUTOHIDE);
            mNetTrafficAutohide.setChecked((Settings.System.getInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE, 0) == 1));
            mNetTrafficAutohide.setOnPreferenceChangeListener(this);

            mNetTrafficAutohideThreshold =
                    (SeekBarPreferenceCham) prefSet.findPreference(NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD);
            int netTrafficAutohideThreshold = Settings.System.getInt(resolver,
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 10);
            mNetTrafficAutohideThreshold.setValue(netTrafficAutohideThreshold / 1);
            mNetTrafficAutohideThreshold.setOnPreferenceChangeListener(this);

            if (TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED &&
                    TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED) {
                mNetTrafficVal = Settings.System.getInt(resolver,
                        Settings.System.NETWORK_TRAFFIC_STATE, 0);
                int intIndex = mNetTrafficVal & (MASK_UP + MASK_DOWN);
                intIndex = mNetTrafficState.findIndexOfValue(String.valueOf(intIndex));
                updateNetworkTrafficState(intIndex);

                mNetTrafficState.setValueIndex(intIndex >= 0 ? intIndex : 0);
                mNetTrafficState.setSummary(mNetTrafficState.getEntry());
                mNetTrafficState.setOnPreferenceChangeListener(this);

                mNetTrafficUnit.setValueIndex(getBit(mNetTrafficVal, MASK_UNIT) ? 1 : 0);
                mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntry());
                mNetTrafficUnit.setOnPreferenceChangeListener(this);

                intIndex = (mNetTrafficVal & MASK_PERIOD) >>> 16;
                intIndex = mNetTrafficPeriod.findIndexOfValue(String.valueOf(intIndex));
                mNetTrafficPeriod.setValueIndex(intIndex >= 0 ? intIndex : 1);
                mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntry());
                mNetTrafficPeriod.setOnPreferenceChangeListener(this);
            }
        }

        private void updateNetworkTrafficState(int mIndex) {
            if (mIndex <= 0) {
                mNetTrafficUnit.setEnabled(false);
                mNetTrafficPeriod.setEnabled(false);
                mNetTrafficAutohide.setEnabled(false);
                mNetTrafficAutohideThreshold.setEnabled(false);
            } else {
                mNetTrafficUnit.setEnabled(true);
                mNetTrafficPeriod.setEnabled(true);
                mNetTrafficAutohide.setEnabled(true);
                mNetTrafficAutohideThreshold.setEnabled(true);
            }
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mNetTrafficState) {
                int intState = Integer.valueOf((String)newValue);
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_UP, getBit(intState, MASK_UP));
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_DOWN, getBit(intState, MASK_DOWN));
                Settings.System.putInt(resolver,
                        Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
                int index = mNetTrafficState.findIndexOfValue((String) newValue);
                mNetTrafficState.setSummary(mNetTrafficState.getEntries()[index]);
                updateNetworkTrafficState(index);
                return true;
            } else if (preference == mNetTrafficUnit) {
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_UNIT, ((String)newValue).equals("1"));
                Settings.System.putInt(resolver,
                        Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
                int index = mNetTrafficUnit.findIndexOfValue((String) newValue);
                mNetTrafficUnit.setSummary(mNetTrafficUnit.getEntries()[index]);
                return true;
            } else if (preference == mNetTrafficPeriod) {
                int intState = Integer.valueOf((String)newValue);
                mNetTrafficVal = setBit(mNetTrafficVal, MASK_PERIOD, false) + (intState << 16);
                Settings.System.putInt(resolver,
                        Settings.System.NETWORK_TRAFFIC_STATE, mNetTrafficVal);
                int index = mNetTrafficPeriod.findIndexOfValue((String) newValue);
                mNetTrafficPeriod.setSummary(mNetTrafficPeriod.getEntries()[index]);
                return true;
            } else if (preference == mNetTrafficAutohide) {
                boolean value = (Boolean) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.NETWORK_TRAFFIC_AUTOHIDE, value ? 1 : 0);
                return true;
            } else if (preference == mNetTrafficAutohideThreshold) {
                int threshold = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, threshold * 1);
                return true;
            }
            return false;
        }

        private void loadResources() {
            Resources resources = this.getResources();
            MASK_UP = resources.getInteger(R.integer.maskUp);
            MASK_DOWN = resources.getInteger(R.integer.maskDown);
            MASK_UNIT = resources.getInteger(R.integer.maskUnit);
            MASK_PERIOD = resources.getInteger(R.integer.maskPeriod);
        }

        private int setBit(int intNumber, int intMask, boolean blnState) {
            if (blnState) {
                return (intNumber | intMask);
            }
            return (intNumber & ~intMask);
        }

        private boolean getBit(int intNumber, int intMask) {
            return (intNumber & intMask) == intMask;
        }
    }
}
