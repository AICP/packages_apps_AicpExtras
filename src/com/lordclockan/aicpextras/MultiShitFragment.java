package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.lordclockan.R;

public class MultiShitFragment extends Fragment {

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

        private static final String PREF_GESTURE_ANYWHERE = "gestureanywhere";
        private static final String PREF_APP_CIRCLE_BAR = "app_circle_bar";

        private Preference mGestureAnywhere;
        private Preference mAppCircleBar;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.multishit_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();

            final ContentResolver resolver = getActivity().getContentResolver();

            mGestureAnywhere = prefSet.findPreference(PREF_GESTURE_ANYWHERE);
            mAppCircleBar = prefSet.findPreference(PREF_APP_CIRCLE_BAR);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mGestureAnywhere) {
                Intent intent = new Intent(getActivity(), GestureAnywhereSettings.class);
                getActivity().startActivity(intent);
            } else if (preference == mAppCircleBar) {
                Intent intent = new Intent(getActivity(), AppCircleBar.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return false;
        }
    }
}
