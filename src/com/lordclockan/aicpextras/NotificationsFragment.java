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

import cyanogenmod.providers.CMSettings;

import org.cyanogenmod.internal.util.CmLockPatternUtils;
import com.lordclockan.R;

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

        private static final String PREF_QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
        private static final String PREF_BLOCK_ON_SECURE_KEYGUARD = "block_on_secure_keyguard";
        private static final String PREF_STATUS_BAR_HEADER_FONT_STYLE = "status_bar_header_font_style";

        private SwitchPreference mBrightnessSlider;
        private SwitchPreference mBlockOnSecureKeyguard;
        private ListPreference mStatusBarHeaderFontStyle;
        private ListPreference mNumColumns;

        private static final int MY_USER_ID = UserHandle.myUserId();


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.notifications_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            Activity activity = getActivity();

            final ContentResolver resolver = getActivity().getContentResolver();
            final CmLockPatternUtils lockPatternUtils = new CmLockPatternUtils(getActivity());

            // Block QS on secure LockScreen
            mBlockOnSecureKeyguard = (SwitchPreference) findPreference(PREF_BLOCK_ON_SECURE_KEYGUARD);
            if (lockPatternUtils.isSecure(MY_USER_ID)) {
                mBlockOnSecureKeyguard.setChecked(Settings.Secure.getIntForUser(resolver,
                        Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD, 1, UserHandle.USER_CURRENT) == 1);
                mBlockOnSecureKeyguard.setOnPreferenceChangeListener(this);
            } else if (mBlockOnSecureKeyguard != null) {
                prefSet.removePreference(mBlockOnSecureKeyguard);
            }

            // Brightness slider
            mBrightnessSlider = (SwitchPreference) prefSet.findPreference(PREF_QS_SHOW_BRIGHTNESS_SLIDER);
            mBrightnessSlider.setChecked(CMSettings.System.getIntForUser(resolver,
                    CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) == 1);
            mBrightnessSlider.setOnPreferenceChangeListener(this);
            int brightnessSlider = CMSettings.System.getIntForUser(resolver,
                    CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT);
            updateBrightnessSliderSummary(brightnessSlider);

            // Status bar header font style
            mStatusBarHeaderFontStyle = (ListPreference) findPreference(PREF_STATUS_BAR_HEADER_FONT_STYLE);
            mStatusBarHeaderFontStyle.setOnPreferenceChangeListener(this);
            mStatusBarHeaderFontStyle.setValue(Integer.toString(Settings.System.getIntForUser(resolver,
                    Settings.System.STATUS_BAR_HEADER_FONT_STYLE, 0, UserHandle.USER_CURRENT)));
            mStatusBarHeaderFontStyle.setSummary(mStatusBarHeaderFontStyle.getEntry());

            // Number of QS Columns 3,4,5
            mNumColumns = (ListPreference) findPreference("sysui_qs_num_columns");
            int numColumns = Settings.System.getIntForUser(resolver,
                    Settings.System.QS_NUM_TILE_COLUMNS, getDefaultNumColums(),
                    UserHandle.USER_CURRENT);
            mNumColumns.setValue(String.valueOf(numColumns));
            updateNumColumnsSummary(numColumns);
            mNumColumns.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mBrightnessSlider) {
                CMSettings.System.putIntForUser(resolver,
                        CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER,
                        (Boolean) newValue ? 1 : 0, UserHandle.USER_CURRENT);
                int brightnessSlider = CMSettings.System.getIntForUser(resolver,
                        CMSettings.System.QS_SHOW_BRIGHTNESS_SLIDER, 1,
                        UserHandle.USER_CURRENT);
                updateBrightnessSliderSummary(brightnessSlider);
                return true;
            } else if (preference == mBlockOnSecureKeyguard) {
                Settings.Secure.putInt(resolver,
                        Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD,
                        (Boolean) newValue ? 1 : 0);
                return true;
            } else if (preference == mStatusBarHeaderFontStyle) {
                int val = Integer.parseInt((String) newValue);
                int index = mStatusBarHeaderFontStyle.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver,
                        Settings.System.STATUS_BAR_HEADER_FONT_STYLE, val, UserHandle.USER_CURRENT);
                mStatusBarHeaderFontStyle.setSummary(mStatusBarHeaderFontStyle.getEntries()[index]);
                return true;
            } else if (preference == mNumColumns) {
                int numColumns = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.QS_NUM_TILE_COLUMNS,
                        numColumns, UserHandle.USER_CURRENT);
                updateNumColumnsSummary(numColumns);
                return true;
            }
            return false;
        }

        private void updateBrightnessSliderSummary(int value) {
            String summary = value != 0
                    ? getResources().getString(R.string.qs_brightness_slider_enabled)
                    : getResources().getString(R.string.qs_brightness_slider_disabled);
            mBrightnessSlider.setSummary(summary);
        }

        private void updateNumColumnsSummary(int numColumns) {
            String prefix = (String) mNumColumns.getEntries()[mNumColumns.findIndexOfValue(String
                    .valueOf(numColumns))];
            mNumColumns.setSummary(getResources().getString(R.string.qs_num_columns_showing, prefix));
        }

        private int getDefaultNumColums() {
            try {
                Resources res = getActivity().getPackageManager()
                        .getResourcesForApplication("com.android.systemui");
                int val = res.getInteger(res.getIdentifier("quick_settings_num_columns", "integer",
                        "com.android.systemui")); // better not be larger than 5, that's as high as the
                                                  // list goes atm
                return Math.max(1, val);
            } catch (Exception e) {
                return 3;
            }
        }
    }
}
