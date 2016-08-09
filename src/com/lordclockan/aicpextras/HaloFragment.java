/*
 * Copyright (C) 2012 ParanoidAndroid Project
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

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.lordclockan.R;

public class HaloFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new HaloSettingsFragment())
                .commit();
    }

    public static class HaloSettingsFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public HaloSettingsFragment() {
        }

        private static final String KEY_HALO_SIZE = "halo_size";
        private static final String KEY_HALO_MSGBOX_ANIMATION = "halo_msgbox_animation";
        private static final String KEY_HALO_NOTIFY_COUNT = "halo_notify_count";
        private static final String KEY_HALO_FLOAT_NOTIFICATIONS = "halo_float_notifications";

        private ListPreference mHaloSize;
        private ListPreference mHaloNotifyCount;
        private ListPreference mHaloMsgAnimate;
        private SwitchPreference mHaloFloat;

        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.halo_settings);
            PreferenceScreen prefSet = getPreferenceScreen();
            mContext = getActivity();

            mHaloSize = (ListPreference) prefSet.findPreference(KEY_HALO_SIZE);
            try {
                float haloSize = Settings.Secure.getFloat(mContext.getContentResolver(),
                        Settings.Secure.HALO_SIZE, 1.0f);
                mHaloSize.setValue(String.valueOf(haloSize));
            } catch(Exception ex) {
                // So what
            }
            mHaloSize.setOnPreferenceChangeListener(this);

            mHaloNotifyCount = (ListPreference) prefSet.findPreference(KEY_HALO_NOTIFY_COUNT);
            try {
                int haloCounter = Settings.Secure.getInt(mContext.getContentResolver(),
                        Settings.Secure.HALO_NOTIFY_COUNT, 4);
                mHaloNotifyCount.setValue(String.valueOf(haloCounter));
            } catch(Exception ex) {
                // fail...
            }
            mHaloNotifyCount.setOnPreferenceChangeListener(this);

            mHaloMsgAnimate = (ListPreference) prefSet.findPreference(KEY_HALO_MSGBOX_ANIMATION);
            try {
                int haloMsgAnimation = Settings.Secure.getInt(mContext.getContentResolver(),
                        Settings.Secure.HALO_MSGBOX_ANIMATION, 2);
                mHaloMsgAnimate.setValue(String.valueOf(haloMsgAnimation));
            } catch(Exception ex) {
                // fail...
            }
            mHaloMsgAnimate.setOnPreferenceChangeListener(this);

            mHaloFloat = (SwitchPreference) prefSet.findPreference(KEY_HALO_FLOAT_NOTIFICATIONS);
            if (Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.FLOATING_WINDOW_MODE, 0) == 0) {
                mHaloFloat.setEnabled(false);
                mHaloFloat.setSummary(R.string.halo_enable_float_summary);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mHaloSize) {
                float haloSize = Float.valueOf((String) newValue);
                Settings.Secure.putFloat(getActivity().getContentResolver(),
                        Settings.Secure.HALO_SIZE, haloSize);
                return true;
            } else if (preference == mHaloMsgAnimate) {
                int haloMsgAnimation = Integer.valueOf((String) newValue);
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.HALO_MSGBOX_ANIMATION, haloMsgAnimation);
                return true;
            } else if (preference == mHaloNotifyCount) {
                int haloNotifyCount = Integer.valueOf((String) newValue);
                Settings.Secure.putInt(getActivity().getContentResolver(),
                        Settings.Secure.HALO_NOTIFY_COUNT, haloNotifyCount);
                return true;
            }
            return false;
        }
    }
}
