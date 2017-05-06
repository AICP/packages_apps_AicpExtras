package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;

public class RecentsPanelFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new RecentsPanelPreferenceFragment())
                .commit();
    }

    public static class RecentsPanelPreferenceFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {

        public RecentsPanelPreferenceFragment() {
        }

        private static final String IMMERSIVE_RECENTS = "immersive_recents";
        private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";
        private static final String OMNISWITCH_START_SETTINGS = "omniswitch_start_settings";
        public static final String OMNISWITCH_PACKAGE_NAME = "org.omnirom.omniswitch";
        public static Intent INTENT_OMNISWITCH_SETTINGS = new Intent(Intent.ACTION_MAIN).setClassName(OMNISWITCH_PACKAGE_NAME,
                                    OMNISWITCH_PACKAGE_NAME + ".SettingsActivity");
        private static final String SLIM_RECENTS_SETTINGS = "slim_recent_panel";
        private static final String RECENTS_STYLE = "navigation_bar_recents";
        private static final String CATEGORY_STOCK_RECENTS = "stock_recents";
        private static final String CATEGORY_OMNI_RECENTS = "omni_recents";
        private static final String CATEGORY_SLIM_RECENTS = "slim_recents";
        private static final String PREF_HIDE_APP_FROM_RECENTS = "hide_app_from_recents";

        private ListPreference mImmersiveRecents;
        private ListPreference mRecentsClearAllLocation;
        private Preference mOmniSwitchSettings;
        private Preference mSlimRecentsSettings;
        private ListPreference mRecentsStyle;
        private PreferenceCategory mOmniRecents;
        private PreferenceCategory mSlimRecents;
        private PreferenceCategory mStockRecents;
        private SwitchPreference mRecentsClearAll;
        private Preference mHideAppsFromRecents;

        private boolean mOmniSwitchInitCalled;

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

            mStockRecents = (PreferenceCategory) findPreference(CATEGORY_STOCK_RECENTS);
            mOmniRecents = (PreferenceCategory) findPreference(CATEGORY_OMNI_RECENTS);
            mSlimRecents = (PreferenceCategory) findPreference(CATEGORY_SLIM_RECENTS);

            // Immersive recents
            mImmersiveRecents = (ListPreference) prefSet.findPreference(IMMERSIVE_RECENTS);
            mImmersiveRecents.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.IMMERSIVE_RECENTS, 0)));
            mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
            mImmersiveRecents.setOnPreferenceChangeListener(this);

            // Clear all location
            mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
            int location = Settings.System.getIntForUser(resolver,
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setValue(String.valueOf(location));
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
            mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

            mRecentsStyle = (ListPreference) prefSet.findPreference(RECENTS_STYLE);
            int style = Settings.System.getIntForUser(resolver,
                    Settings.System.NAVIGATION_BAR_RECENTS, 0, UserHandle.USER_CURRENT);
            mRecentsStyle.setValue(String.valueOf(style));
            mRecentsStyle.setSummary(mRecentsStyle.getEntry());
            mRecentsStyle.setOnPreferenceChangeListener(this);

            mOmniSwitchSettings = (Preference) prefSet.findPreference(OMNISWITCH_START_SETTINGS);

            mSlimRecentsSettings = (Preference) prefSet.findPreference(SLIM_RECENTS_SETTINGS);

            updateRecents(style);

            mHideAppsFromRecents = prefSet.findPreference(PREF_HIDE_APP_FROM_RECENTS);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mRecentsStyle) {
                int style = Integer.parseInt((String) newValue);
                int index = mRecentsStyle.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver,
                        Settings.System.NAVIGATION_BAR_RECENTS, style, UserHandle.USER_CURRENT);
                mRecentsStyle.setSummary(mRecentsStyle.getEntries()[index]);
                updateRecents(style);
                if (style == 0 || style == 2) {
                    Helpers.showSystemUIrestartDialog(getActivity());
                }
                return true;
            } else if (preference == mImmersiveRecents) {
                Settings.System.putInt(resolver, Settings.System.IMMERSIVE_RECENTS,
                        Integer.parseInt((String) newValue));
                mImmersiveRecents.setValue(String.valueOf(newValue));
                mImmersiveRecents.setSummary(mImmersiveRecents.getEntry());
                return true;
            } else if (preference == mRecentsClearAllLocation) {
                int location = Integer.parseInt((String) newValue);
                int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver,
                        Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
                mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mOmniSwitchSettings) {
                startActivity(INTENT_OMNISWITCH_SETTINGS);
                return true;
            } else if (preference == mSlimRecentsSettings) {
                Intent intent = new Intent(getActivity(), SubActivity.class);
                intent.putExtra(SubActivity.EXTRA_FRAGMENT_CLASS, SlimRecentPanel.class.getName());
                intent.putExtra(SubActivity.EXTRA_TITLE,
                        getResources().getString(R.string.recent_panel_category));
                getActivity().startActivity(intent);
                return true;
            } else if (preference == mHideAppsFromRecents) {
                Intent intent = new Intent(getActivity(), HAFRAppListActivity.class);
                getActivity().startActivity(intent);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
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

        private void updateRecents(int style) {
            if (style == 0 || style == 2) {
                mStockRecents.setEnabled(true);
                mOmniRecents.setEnabled(false);
                mSlimRecents.setEnabled(false);
            } else if (style == 1) {
                mStockRecents.setEnabled(false);
                mOmniRecents.setEnabled(true);
                mSlimRecents.setEnabled(false);
            } else if (style == 3) {
                mStockRecents.setEnabled(false);
                mOmniRecents.setEnabled(false);
                mSlimRecents.setEnabled(true);
            }
        }
    }
}
