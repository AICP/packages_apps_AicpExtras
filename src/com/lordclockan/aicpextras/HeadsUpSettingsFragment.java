package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

public class HeadsUpSettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public SettingsPreferenceFragment() {
        }

        private static final String PREF_HEADS_UP_TIME_OUT = "heads_up_time_out";

        private ListPreference mHeadsUpTimeOut;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.headsup_settings);

            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();

            Resources systemUiResources;
            try {
                systemUiResources = getActivity().getPackageManager().getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                return;
            }

            int defaultTimeOut = systemUiResources.getInteger(systemUiResources.getIdentifier(
                        "com.android.systemui:integer/heads_up_notification_decay", null, null));
            mHeadsUpTimeOut = (ListPreference) findPreference(PREF_HEADS_UP_TIME_OUT);
            mHeadsUpTimeOut.setOnPreferenceChangeListener(this);
            int headsUpTimeOut = Settings.System.getInt(resolver,
                    Settings.System.HEADS_UP_TIMEOUT, defaultTimeOut);
            mHeadsUpTimeOut.setValue(String.valueOf(headsUpTimeOut));
            updateHeadsUpTimeOutSummary(headsUpTimeOut);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mHeadsUpTimeOut) {
                int headsUpTimeOut = Integer.valueOf((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.HEADS_UP_TIMEOUT,
                        headsUpTimeOut);
                updateHeadsUpTimeOutSummary(headsUpTimeOut);
                return true;
            }
            return false;
        }

        private void updateHeadsUpTimeOutSummary(int value) {
            String summary = getResources().getString(R.string.heads_up_time_out_summary,
                    value / 1000);
            mHeadsUpTimeOut.setSummary(summary);
        }

        @Override
        public void onResume() {
            super.onResume();
        }
    }
}
