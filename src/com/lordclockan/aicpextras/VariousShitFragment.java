package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.app.Fragment;
import android.view.View;

import com.lordclockan.R;

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

        private ListPreference mScrollingCachePref;
        private Preference mSystemUITuner;
        private String mTunerFirstRun = "true";

        private static final String PREF_SYSTEMAPP_REMOVER = "system_app_remover";
        private static final String PREF_WAKELOCK_BLOCKER = "wakelock_blocker";

        private Preference mSystemappRemover;
        private Preference mWakelockBlocker;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.various_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mVariousShitSettings = getActivity().getSharedPreferences("SystemUITuner", Context.MODE_PRIVATE);

            mScrollingCachePref = (ListPreference) findPreference(SCROLLINGCACHE_PREF);
            mScrollingCachePref.setValue(SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP,
                    SystemProperties.get(SCROLLINGCACHE_PERSIST_PROP, SCROLLINGCACHE_DEFAULT)));
            mScrollingCachePref.setOnPreferenceChangeListener(this);

            mSystemUITuner = prefSet.findPreference(PREF_SYSTEMUI_TUNER);
            mSystemappRemover = prefSet.findPreference(PREF_SYSTEMAPP_REMOVER);
            mWakelockBlocker = prefSet.findPreference(PREF_WAKELOCK_BLOCKER);

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
