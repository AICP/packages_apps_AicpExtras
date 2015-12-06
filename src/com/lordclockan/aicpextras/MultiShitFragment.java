package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;

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

        private static final String TAG = "MultiShit";

        private static final String PREF_GESTURE_ANYWHERE = "gestureanywhere";
        private static final String PREF_APP_CIRCLE_BAR = "app_circle_bar";
        private static final String CAT_OMNISWITCH = "omniswitch_category";
        private static final String RECENTS_USE_OMNISWITCH = "recents_use_omniswitch";
        private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";

        // Package name of the omnniswitch app
        public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
        // Intent for launching the omniswitch settings actvity
        public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN)
                .setClassName(OMNISWITCH_PACKAGE_NAME, OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");

        private Preference mGestureAnywhere;
        private Preference mAppCircleBar;
        private PreferenceCategory mOmniSwitchCategory;
        private SwitchPreference mRecentsUseOmniSwitch;
        private Preference mOmniSwitchSettings;
        private boolean mOmniSwitchInitCalled;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.multishit_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            PackageManager pm = getActivity().getPackageManager();
            final ContentResolver resolver = getActivity().getContentResolver();

            mGestureAnywhere = prefSet.findPreference(PREF_GESTURE_ANYWHERE);
            mAppCircleBar = prefSet.findPreference(PREF_APP_CIRCLE_BAR);

            mRecentsUseOmniSwitch = (SwitchPreference)
                    findPreference(RECENTS_USE_OMNISWITCH);
            try {
                mRecentsUseOmniSwitch.setChecked(Settings.System.getInt(resolver,
                        Settings.System.RECENTS_USE_OMNISWITCH) == 1);
                mOmniSwitchInitCalled = true;
            } catch(SettingNotFoundException e){
                // if the settings value is unset
            }
            mRecentsUseOmniSwitch.setOnPreferenceChangeListener(this);

            mOmniSwitchSettings = (Preference)
                    findPreference(OMNISWITCH_START_SETTINGS);
            mOmniSwitchSettings.setEnabled(mRecentsUseOmniSwitch.isChecked());

            mOmniSwitchCategory = (PreferenceCategory) prefSet.findPreference(CAT_OMNISWITCH);
            if (!Helpers.isPackageInstalled(OMNISWITCH_PACKAGE_NAME, pm)) {
                mOmniSwitchCategory.removePreference(mOmniSwitchCategory);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mGestureAnywhere) {
                Intent intent = new Intent(getActivity(), GestureAnywhereSettings.class);
                getActivity().startActivity(intent);
            } else if (preference == mAppCircleBar) {
                Intent intent = new Intent(getActivity(), AppCircleBar.class);
                getActivity().startActivity(intent);
            } else if (preference == mOmniSwitchSettings){
                getActivity().startActivity(INTENT_OMNISWITCH_SETTINGS);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mRecentsUseOmniSwitch) {
                boolean value = (Boolean) newValue;

                // if value has never been set before
                if (value && !mOmniSwitchInitCalled){
                    openOmniSwitchFirstTimeWarning();
                    mOmniSwitchInitCalled = true;
                }

                Settings.System.putInt(
                        resolver, Settings.System.RECENTS_USE_OMNISWITCH, value ? 1 : 0);
                mOmniSwitchSettings.setEnabled(value);
            } else {
                return false;
            }

            return true;
        }

        private void openOmniSwitchFirstTimeWarning() {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getResources().getString(R.string.omniswitch_first_time_title))
                    .setMessage(getResources().getString(R.string.omniswitch_first_time_message))
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                    }).show();
        }
    }
}
