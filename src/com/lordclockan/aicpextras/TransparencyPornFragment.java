package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;

import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

public class TransparencyPornFragment extends Fragment {

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

        private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
        private static final String PREF_QS_TRANSPARENT_HEADER = "qs_transparent_header";
        private static final String PREF_TRANSPARENT_VOLUME_DIALOG = "transparent_volume_dialog";
        private static final String PREF_TRANSPARENT_POWER_MENU = "transparent_power_menu";
        private static final String PREF_TRANSPARENT_POWER_DIALOG_DIM = "transparent_power_dialog_dim";

        private SeekBarPreferenceCham mQSShadeAlpha;
        private SeekBarPreferenceCham mQSHeaderAlpha;
        private SeekBarPreferenceCham mVolumeDialogAlpha;
        private SeekBarPreferenceCham mPowerMenuAlpha;
        private SeekBarPreferenceCham mPowerDialogDim;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.transparency_porn_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            final ContentResolver resolver = getActivity().getContentResolver();

            // QS shade alpha
            mQSShadeAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_QS_TRANSPARENT_SHADE);
            int qSShadeAlpha = Settings.System.getInt(resolver,
                    Settings.System.QS_TRANSPARENT_SHADE, 255);
            mQSShadeAlpha.setValue(qSShadeAlpha / 1);
            mQSShadeAlpha.setOnPreferenceChangeListener(this);

            // QS header alpha
            mQSHeaderAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_QS_TRANSPARENT_HEADER);
            int qSHeaderAlpha = Settings.System.getInt(resolver,
                    Settings.System.QS_TRANSPARENT_HEADER, 255);
            mQSHeaderAlpha.setValue(qSHeaderAlpha / 1);
            mQSHeaderAlpha.setOnPreferenceChangeListener(this);

            // Volume dialog alpha
            mVolumeDialogAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_TRANSPARENT_VOLUME_DIALOG);
            int volumeDialogAlpha = Settings.System.getInt(resolver,
                    Settings.System.TRANSPARENT_VOLUME_DIALOG, 255);
            mVolumeDialogAlpha.setValue(volumeDialogAlpha / 1);
            mVolumeDialogAlpha.setOnPreferenceChangeListener(this);

            // Power menu alpha
            mPowerMenuAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_TRANSPARENT_POWER_MENU);
            int powerMenuAlpha = Settings.System.getInt(resolver,
                    Settings.System.TRANSPARENT_POWER_MENU, 100);
            mPowerMenuAlpha.setValue(powerMenuAlpha / 1);
            mPowerMenuAlpha.setOnPreferenceChangeListener(this);

            // Power/reboot dialog dim
            mPowerDialogDim =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_TRANSPARENT_POWER_DIALOG_DIM);
            int powerDialogDim = Settings.System.getInt(resolver,
                    Settings.System.TRANSPARENT_POWER_DIALOG_DIM, 50);
            mPowerDialogDim.setValue(powerDialogDim / 1);
            mPowerDialogDim.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            int alpha = (Integer) newValue;
            if (preference == mQSShadeAlpha) {
                Settings.System.putInt(resolver,
                        Settings.System.QS_TRANSPARENT_SHADE, alpha * 1);
                return true;
            } else if (preference == mQSHeaderAlpha) {
                Settings.System.putInt(resolver,
                        Settings.System.QS_TRANSPARENT_HEADER, alpha * 1);
                return true;
            } else if (preference == mVolumeDialogAlpha) {
                Settings.System.putInt(resolver,
                        Settings.System.TRANSPARENT_VOLUME_DIALOG, alpha * 1);
                return true;
            } else if (preference == mPowerMenuAlpha) {
                Settings.System.putInt(resolver,
                        Settings.System.TRANSPARENT_POWER_MENU, alpha * 1);
                return true;
            } else if (preference == mPowerDialogDim) {
                Settings.System.putInt(resolver,
                        Settings.System.TRANSPARENT_POWER_DIALOG_DIM, alpha * 1);
                return true;
            }
            return false;
        }
    }
}
