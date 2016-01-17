package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.Intent;
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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lordclockan.R;

public class RecentsPanelFragment extends Fragment {

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

        private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
        private static final String RECENTS_CLEAR_ALL = "show_clear_all_recents";
        private static final String RECENTS_CLEAR_ALL_DISMISS_ALL = "recents_clear_all_dismiss_all";
        private static final String RECENTS_SHOW_SEARCH_BAR = "recents_show_search_bar";
        private static final String RECENTS_MEM_DISPLAY = "systemui_recents_mem_display";
        private static final String RECENTS_FULL_SCREEN = "recents_full_screen";
        private static final String RECENTS_FULL_SCREEN_CLOCK = "recents_full_screen_clock";
        private static final String RECENTS_FULL_SCREEN_DATE = "recents_full_screen_date";

        // Package name of the omnniswitch app
        public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
        // Intent for launching the omniswitch settings actvity
        public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN)
                .setClassName(OMNISWITCH_PACKAGE_NAME, OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");

        private SwitchPreference mRecentsClearAll;
        private ListPreference mRecentsClearAllLocation;
        private SwitchPreference mRecentsClearAllDismissAll;
        private SwitchPreference mRecentsShowSearchBar;
        private SwitchPreference mRecentsMemDisplay;
        private SwitchPreference mRecentsFullScreen;
        private SwitchPreference mRecentsFullScreenClock;
        private SwitchPreference mRecentsFullScreenDate;

        ViewGroup viewGroup;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.recents_panel_layout);

            viewGroup = (ViewGroup) ((ViewGroup) getActivity()
                    .findViewById(android.R.id.content)).getChildAt(0);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
            int location = Settings.System.getIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setValue(String.valueOf(location));
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
            mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

            mRecentsClearAll = (SwitchPreference) prefSet.findPreference(RECENTS_CLEAR_ALL);
            mRecentsClearAllDismissAll = (SwitchPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_DISMISS_ALL);
            mRecentsShowSearchBar = (SwitchPreference) prefSet.findPreference(RECENTS_SHOW_SEARCH_BAR);
            mRecentsMemDisplay = (SwitchPreference) prefSet.findPreference(RECENTS_MEM_DISPLAY);
            mRecentsFullScreen = (SwitchPreference) prefSet.findPreference(RECENTS_FULL_SCREEN);
            mRecentsFullScreenClock = (SwitchPreference) prefSet.findPreference(RECENTS_FULL_SCREEN_CLOCK);
            mRecentsFullScreenDate = (SwitchPreference) prefSet.findPreference(RECENTS_FULL_SCREEN_DATE);

            updateSettingsVisibility();

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mRecentsClearAllLocation) {
                int location = Integer.valueOf((String) newValue);
                int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver,
                        Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
                mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
                return true;
            }
            return false;
        }

        private void updateSettingsVisibility() {
            ContentResolver resolver = getActivity().getContentResolver();
            final boolean isOmniEnabled = (Settings.System.getInt(resolver,
                    Settings.System.RECENTS_USE_OMNISWITCH, 0) == 1);
            final boolean isSlimEnabled = (Settings.System.getInt(resolver,
                    Settings.System.USE_SLIM_RECENTS, 0) == 1);
            if (isOmniEnabled || isSlimEnabled) {
                mRecentsClearAllLocation.setEnabled(false);
                mRecentsClearAll.setEnabled(false);
                mRecentsClearAllDismissAll.setEnabled(false);
                mRecentsShowSearchBar.setEnabled(false);
                mRecentsMemDisplay.setEnabled(false);
                mRecentsFullScreen.setEnabled(false);
                mRecentsFullScreenClock.setEnabled(false);
                mRecentsFullScreenDate.setEnabled(false);
                Snackbar snackbar = Snackbar
                    .make(viewGroup, R.string.stock_recents_disabled, Snackbar.LENGTH_LONG)
                    .setAction(R.string.alternatives_title, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (isOmniEnabled) {
                                getActivity().startActivity(INTENT_OMNISWITCH_SETTINGS);
                            }
                            if (isSlimEnabled) {
                                Intent intent = new Intent(getActivity(), SlimRecentPanel.class);
                                getActivity().startActivity(intent);
                            }
                        }
                });

                snackbar.show();
            }
        }
    }
}
