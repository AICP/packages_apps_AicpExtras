package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cyanogenmod.providers.CMSettings;

import com.lordclockan.aicpextras.utils.Helpers;
import com.lordclockan.aicpextras.utils.Utils;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import com.lordclockan.R;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NotificationsFragment extends Fragment {

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

        private static final String TAG = NotificationsFragment.class.getSimpleName();

        private static final String CUSTOM_HEADER_IMAGE = "status_bar_custom_header";
        private static final String DAYLIGHT_HEADER_PACK = "daylight_header_pack";
        private static final String DEFAULT_HEADER_PACKAGE = "com.android.systemui";
        private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
        private static final String CUSTOM_HEADER_PROVIDER = "custom_header_provider";
        private static final String CUSTOM_HEADER_BROWSE = "custom_header_browse";
        private static final String NOTIFICATION_GUTS_KILL_APP_BUTTON = "notification_guts_kill_app_button";
        private static final String PREF_QS_RUNNING_SERVICES = "qs_running_services_toggle";
        private static final String PREF_QS_MULTIUSER_SWITCH = "qs_multiuser_switch_toggle";
        private static final String PREF_QS_DATE_TIME_CENTER = "qs_date_time_center";

        private ListPreference mDaylightHeaderPack;
        private SeekBarPreferenceCham mHeaderShadow;
        private ListPreference mHeaderProvider;
        private String mDaylightHeaderProvider;
        private PreferenceScreen mHeaderBrowse;
        private Preference mNotificationKill;
        private SwitchPreference mQSRunningServicesIcon;
        private SwitchPreference mQSMultiUserIcon;
        private SwitchPreference mQSDateTimeCenter;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.notifications_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();
            final ContentResolver resolver = getActivity().getContentResolver();

            String settingHeaderPackage = Settings.System.getString(resolver,
                    Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK);
            if (settingHeaderPackage == null) {
                settingHeaderPackage = DEFAULT_HEADER_PACKAGE;
            }
            mDaylightHeaderPack = (ListPreference) findPreference(DAYLIGHT_HEADER_PACK);

            List<String> entries = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            getAvailableHeaderPacks(entries, values);
            mDaylightHeaderPack.setEntries(entries.toArray(new String[entries.size()]));
            mDaylightHeaderPack.setEntryValues(values.toArray(new String[values.size()]));

            int valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
            if (valueIndex == -1) {
                // no longer found
                settingHeaderPackage = DEFAULT_HEADER_PACKAGE;
                Settings.System.putString(resolver,
                        Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, settingHeaderPackage);
                valueIndex = mDaylightHeaderPack.findIndexOfValue(settingHeaderPackage);
            }
            mDaylightHeaderPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntry());
            mDaylightHeaderPack.setOnPreferenceChangeListener(this);

            mHeaderShadow = (SeekBarPreferenceCham) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
            final int headerShadow = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 80);
            mHeaderShadow.setValue((int)(((double) headerShadow / 255) * 100));
            mHeaderShadow.setOnPreferenceChangeListener(this);

            mDaylightHeaderProvider = getResources().getString(R.string.daylight_header_provider);
            String providerName = Settings.System.getString(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER);
            if (providerName == null) {
                providerName = mDaylightHeaderProvider;
            }
            mHeaderProvider = (ListPreference) findPreference(CUSTOM_HEADER_PROVIDER);
            valueIndex = mHeaderProvider.findIndexOfValue(providerName);
            mHeaderProvider.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mHeaderProvider.setSummary(mHeaderProvider.getEntry());
            mHeaderProvider.setOnPreferenceChangeListener(this);
            mDaylightHeaderPack.setEnabled(providerName.equals(mDaylightHeaderProvider));

            mHeaderBrowse = (PreferenceScreen) findPreference(CUSTOM_HEADER_BROWSE);
            mHeaderBrowse.setEnabled(isBrowseHeaderAvailable());

            mNotificationKill = findPreference(NOTIFICATION_GUTS_KILL_APP_BUTTON);
            mNotificationKill.setOnPreferenceChangeListener(this);

            mQSMultiUserIcon = (SwitchPreference) findPreference(PREF_QS_MULTIUSER_SWITCH);
            mQSRunningServicesIcon = (SwitchPreference) findPreference(PREF_QS_RUNNING_SERVICES);
            mQSDateTimeCenter = (SwitchPreference) findPreference(PREF_QS_DATE_TIME_CENTER);
            mQSDateTimeCenter.setOnPreferenceChangeListener(this);
            updateNotifHeaderIcons(mQSDateTimeCenter.isChecked());
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mDaylightHeaderPack) {
                String value = (String) newValue;
                Settings.System.putString(resolver,
                        Settings.System.STATUS_BAR_DAYLIGHT_HEADER_PACK, value);
                int valueIndex = mDaylightHeaderPack.findIndexOfValue(value);
                mDaylightHeaderPack.setSummary(mDaylightHeaderPack.getEntries()[valueIndex]);
                return true;
            } else if (preference == mHeaderShadow) {
                Integer headerShadow = (Integer) newValue;
                int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
                return true;
            } else if (preference == mHeaderProvider) {
                String value = (String) newValue;
                Settings.System.putString(resolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_PROVIDER, value);
                int valueIndex = mHeaderProvider.findIndexOfValue(value);
                mHeaderProvider.setSummary(mHeaderProvider.getEntries()[valueIndex]);
                mDaylightHeaderPack.setEnabled(value.equals(mDaylightHeaderProvider));
            } else if (preference == mNotificationKill) {
                // Setting will only apply to new created notifications.
                // By restarting SystemUI, we can re-create all notifications
                Helpers.showSystemUIrestartDialog(getActivity());
                return true;
            } else if (preference == mQSDateTimeCenter) {
                Boolean value = (Boolean) newValue;
                updateNotifHeaderIcons(value);
                return true;
            }
            return false;
        }

        private void getAvailableHeaderPacks(List<String> entries, List<String> values) {
            String defaultLabel = null;
            Map<String, String> headerMap = new HashMap<String, String>();
            Intent i = new Intent();
            PackageManager packageManager = getActivity().getPackageManager();
            i.setAction("org.omnirom.DaylightHeaderPack");
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
                if (label == null) {
                    label = r.activityInfo.packageName;
                }
                if (packageName.equals(DEFAULT_HEADER_PACKAGE)) {
                    defaultLabel = label;
                } else {
                    headerMap.put(label, packageName);
                }
            }
            i.setAction("org.omnirom.DaylightHeaderPack1");
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                String packageName = r.activityInfo.packageName;
                String label = r.activityInfo.loadLabel(getActivity().getPackageManager()).toString();
                if (label == null) {
                    label = packageName;
                }
                headerMap.put(label, packageName  + "/" + r.activityInfo.name);
            }
            List<String> labelList = new ArrayList<String>();
            labelList.addAll(headerMap.keySet());
            Collections.sort(labelList);
            for (String label : labelList) {
                entries.add(label);
                values.add(headerMap.get(label));
            }
            entries.add(0, defaultLabel);
            values.add(0, DEFAULT_HEADER_PACKAGE);
        }

        private boolean isBrowseHeaderAvailable() {
            PackageManager pm = getActivity().getPackageManager();
            Intent browse = new Intent();
            browse.setClassName("org.omnirom.omnistyle", "org.omnirom.omnistyle.BrowseHeaderActivity");
            return pm.resolveActivity(browse, 0) != null;
        }

        private void updateNotifHeaderIcons(Boolean state) {
            if (state) {
              mQSMultiUserIcon.setChecked(state);
              mQSRunningServicesIcon.setChecked(state);
            }
            mQSMultiUserIcon.setEnabled(!state);
            mQSRunningServicesIcon.setEnabled(!state);
        }
    }
}
