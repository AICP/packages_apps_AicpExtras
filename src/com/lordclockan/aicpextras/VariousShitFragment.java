package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.View;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Helpers;

public class VariousShitFragment extends Fragment {

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

        private SharedPreferences mVariousShitSettings;
        private Editor toEditVariousShit;

        private static final String SCROLLINGCACHE_PREF = "pref_scrollingcache";
        private static final String SCROLLINGCACHE_PERSIST_PROP = "persist.sys.scrollingcache";
        private static final String SCROLLINGCACHE_DEFAULT = "1";
        private static final String PREF_SYSTEMUI_TUNER = "systemui_tuner";
        private static final String PREF_ADAWAY_START = "adaway_start";
        private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
        private static final String PREF_THREE_FINGER_GESTURE = "three_finger_gesture";

        // Package name of the SystemUI tuner
        public static final String SYSTEMUITUNER_PACKAGE_NAME = "com.android.systemui";
        // Intent for launching the SystemUI tuner actvity
        public static Intent INTENT_SYSTEMUITUNER_SETTINGS = new Intent(Intent.ACTION_MAIN)
                .setClassName(SYSTEMUITUNER_PACKAGE_NAME, SYSTEMUITUNER_PACKAGE_NAME + ".tuner.TunerActivity");

        // Package name of the SystemUI tuner
        public static final String AICPSETTINGS_PACKAGE_NAME = "com.android.settings";
        // Intent for launching the SystemUI tuner actvity
        public static Intent INTENT_AICPSETTINGS_SETTINGS = new Intent(Intent.ACTION_MAIN)
                .setClassName(AICPSETTINGS_PACKAGE_NAME, AICPSETTINGS_PACKAGE_NAME + ".Settings$AicpSettingsExternalActivity");

        // Package name of the AdAway app
        public static final String ADAWAY_PACKAGE_NAME = "org.adaway";
        // Intent for launching the AdAway main actvity
        public static Intent INTENT_ADAWAY = new Intent(Intent.ACTION_MAIN)
                .setClassName(ADAWAY_PACKAGE_NAME, ADAWAY_PACKAGE_NAME + ".ui.BaseActivity");

        private ListPreference mScrollingCachePref;
        private ListPreference mMsob;
        private Preference mSystemUITuner;
        private String mTunerFirstRun = "true";

        private static final String PREF_SYSTEMAPP_REMOVER = "system_app_remover";
        private static final String PREF_WAKELOCK_BLOCKER = "wakelock_blocker";

        private Preference mSystemappRemover;
        private Preference mWakelockBlocker;
        private Preference mAdAway;
        private Preference mThreeFingerGesture;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.various_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();
            PackageManager pm = getActivity().getPackageManager();

            mVariousShitSettings = getActivity().getSharedPreferences("SystemUITuner", Context.MODE_PRIVATE);

            mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
            mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                    SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
            mScrollingCachePref.setOnPreferenceChangeListener(this);

            mMsob = (ListPreference) findPreference(PREF_MEDIA_SCANNER_ON_BOOT);
            mMsob.setValue(String.valueOf(Settings.System.getInt(resolver,
                    Settings.System.MEDIA_SCANNER_ON_BOOT, 0)));
            mMsob.setSummary(mMsob.getEntry());
            mMsob.setOnPreferenceChangeListener(this);

            mSystemUITuner = prefSet.findPreference(PREF_SYSTEMUI_TUNER);
            mSystemappRemover = prefSet.findPreference(PREF_SYSTEMAPP_REMOVER);
            mWakelockBlocker = prefSet.findPreference(PREF_WAKELOCK_BLOCKER);
            mThreeFingerGesture = prefSet.findPreference(PREF_THREE_FINGER_GESTURE);

            mAdAway = (Preference) prefSet.findPreference(PREF_ADAWAY_START);
            if (!Helpers.isPackageInstalled(ADAWAY_PACKAGE_NAME, pm)) {
                prefSet.removePreference(mAdAway);
            }

            sharedPreferences();

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mScrollingCachePref) {
                if (newValue != null) {
                    SystemProperties.set(SCROLLINGCACHE_PERSIST_PROP, (String)newValue);
                    return true;
                }
            } else if (preference == mMsob) {
                Settings.System.putInt(resolver,
                    Settings.System.MEDIA_SCANNER_ON_BOOT,
                        Integer.valueOf(String.valueOf(newValue)));

                mMsob.setValue(String.valueOf(newValue));
                mMsob.setSummary(mMsob.getEntry());
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mSystemUITuner) {
                if (mVariousShitSettings.getString("firstRun", "").equals("true")) {
                    toEditVariousShit = mVariousShitSettings.edit();
                    toEditVariousShit.putString("firstRun", "false");
                    toEditVariousShit.commit();
                    mTunerFirstRun = "false";
                    systemUITunerDialogWarning();
                } else if (mVariousShitSettings.getString("firstRun", "").equals("false")) {
                    getActivity().startActivity(INTENT_SYSTEMUITUNER_SETTINGS);
                }
            } else if (preference == mSystemappRemover) {
                Intent intent = new Intent(getActivity(), SystemappRemover.class);
                startActivity(intent);
            } else if (preference == mWakelockBlocker) {
                getActivity().startActivity(INTENT_AICPSETTINGS_SETTINGS);
            } else if (preference == mAdAway) {
                getActivity().startActivity(INTENT_ADAWAY);
            } else if (preference == mThreeFingerGesture) {
                getActivity().startActivity(INTENT_AICPSETTINGS_SETTINGS);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        public void sharedPreferences() {
            toEditVariousShit = mVariousShitSettings.edit();
            toEditVariousShit.putString("firstRun", mTunerFirstRun);
            toEditVariousShit.commit();
        }

        public void systemUITunerDialogWarning() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.systemui_tuner_dilog_title);
            builder.setMessage(R.string.systemui_tuner_dilog_warning);

            builder.setPositiveButton(R.string.continue_title, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    getActivity().startActivity(INTENT_SYSTEMUITUNER_SETTINGS);
                }
             });

             AlertDialog alertDialog = builder.create();
             alertDialog.show();
        }
    }
}
