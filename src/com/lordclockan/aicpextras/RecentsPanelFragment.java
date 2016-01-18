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
        private static final String RECENTS_FULL_SCREEN_CLOCK = "recents_full_screen_clock";
        private static final String RECENTS_FULL_SCREEN_DATE = "recents_full_screen_date";
        private static final String IMMERSIVE_RECENTS = "immersive_recents";

        private SwitchPreference mRecentsClearAll;
        private ListPreference mRecentsClearAllLocation;
        private SwitchPreference mRecentsClearAllDismissAll;
        private SwitchPreference mRecentsShowSearchBar;
        private SwitchPreference mRecentsMemDisplay;
        private SwitchPreference mRecentsFullScreenClock;
        private SwitchPreference mRecentsFullScreenDate;
        private ListPreference mImmersiveRecents;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.recents_panel_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();
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
            mRecentsFullScreenClock = (SwitchPreference) prefSet.findPreference(RECENTS_FULL_SCREEN_CLOCK);
            mRecentsFullScreenDate = (SwitchPreference) prefSet.findPreference(RECENTS_FULL_SCREEN_DATE);

            mImmersiveRecents = (ListPreference) prefSet.findPreference(IMMERSIVE_RECENTS);
            mImmersiveRecents.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.IMMERSIVE_RECENTS, 0)));
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
            mImmersiveRecents.setOnPreferenceChangeListener(this);

            updateImmersiveDateClock();
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
            } else if (preference == mImmersiveRecents) {
                Settings.System.putInt(resolver, Settings.System.IMMERSIVE_RECENTS,
                        Integer.valueOf((String) newValue));
                mImmersiveRecents.setValue(String.valueOf(newValue));
                mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
                updateImmersiveDateClock();
                return true;
            }
            return false;
        }

        private void updateSettingsVisibility() {
            ContentResolver resolver = getActivity().getContentResolver();
            if ((Settings.System.getInt(resolver,
                    Settings.System.RECENTS_USE_OMNISWITCH, 0) == 1) ||
                    (Settings.System.getInt(resolver,
                    Settings.System.USE_SLIM_RECENTS, 0) == 1)) {
                mRecentsClearAllLocation.setEnabled(false);
                mRecentsClearAll.setEnabled(false);
                mRecentsClearAllDismissAll.setEnabled(false);
                mRecentsShowSearchBar.setEnabled(false);
                mRecentsMemDisplay.setEnabled(false);
                mImmersiveRecents.setEnabled(false);
                mRecentsFullScreenClock.setEnabled(false);
                mRecentsFullScreenDate.setEnabled(false);
                Toast.makeText(getActivity(), getString(R.string.stock_recents_disabled),
                    Toast.LENGTH_LONG).show();
            }
        }

        private void updateImmersiveDateClock() {
            if ((Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.IMMERSIVE_RECENTS, 0) == 0) ||
                    (Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.IMMERSIVE_RECENTS, 0) == 2)) {
                mRecentsFullScreenClock.setEnabled(false);
                mRecentsFullScreenDate.setEnabled(false);
            } else {
                mRecentsFullScreenClock.setEnabled(true);
                mRecentsFullScreenDate.setEnabled(true);
            }
        }
    }
}
