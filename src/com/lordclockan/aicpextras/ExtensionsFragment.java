package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;

import com.lordclockan.R;

public class ExtensionsFragment extends Fragment {

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

        private static final String PREF_APP_SIDE_BAR = "app_side_bar";
        private static final String PREF_SYSTEMAPP_REMOVER = "system_app_remover";

        private Preference mAppSideBar;
        private Preference mSystemappRemover;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.extensions_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mAppSideBar = prefSet.findPreference(PREF_APP_SIDE_BAR);
            mSystemappRemover = prefSet.findPreference(PREF_SYSTEMAPP_REMOVER);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            ContentResolver resolver = getActivity().getContentResolver();
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mSystemappRemover) {
                Intent intent = new Intent(getActivity(), SystemappRemover.class);
                startActivity(intent);
            } else if (preference == mAppSideBar) {
                Intent intent = new Intent(getActivity(), AppSidebar.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }
    }
}
