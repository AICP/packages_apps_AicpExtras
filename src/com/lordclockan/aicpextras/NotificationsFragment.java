package com.lordclockan.aicpextras;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreferenceFix;
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
import java.util.HashSet;
import java.util.Set;

import cyanogenmod.providers.CMSettings;

import org.cyanogenmod.internal.util.CmLockPatternUtils;
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

        private static final String PREF_QS_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
        private static final String PREF_BLOCK_ON_SECURE_KEYGUARD = "block_on_secure_keyguard";
        private static final String PREF_STATUS_BAR_HEADER_FONT_STYLE = "status_bar_header_font_style";
        private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
        private static final String CUSTOM_HEADER_TEXT_SHADOW = "status_bar_custom_header_text_shadow";
        private static final String CUSTOM_HEADER_TEXT_SHADOW_COLOR = "status_bar_custom_header_text_shadow_color";
        private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
        private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
        private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
        private static final String LOCKCLOCK_WEATHER = "lock_clock_weather";
        private static final String PREF_QS_PANEL_LOGO = "qs_panel_logo";
        private static final String PREF_QS_PANEL_LOGO_COLOR = "qs_panel_logo_color";
        private static final String PREF_QS_PANEL_LOGO_ALPHA = "qs_panel_logo_alpha";
        private static final String PREF_THEMES_TILE = "themes_tile_components";

        private SwitchPreference mBrightnessSlider;
        private SwitchPreference mBlockOnSecureKeyguard;
        private ListPreference mStatusBarHeaderFontStyle;
        private ListPreference mNumColumns;
        private ListPreference mNumRows;
        private ListPreference mTileAnimationStyle;
        private ListPreference mTileAnimationDuration;
        private ListPreference mTileAnimationInterpolator;
        private Preference mWeatherSettings;
        private SeekBarPreferenceCham mTextShadow;
        private ColorPickerPreference mTShadowColor;
        private SeekBarPreferenceCham mHeaderShadow;
        private ListPreference mQSPanelLogo;
        private ColorPickerPreference mQSPanelLogoColor;
        private SeekBarPreferenceCham mQSPanelLogoAlpha;
        private MultiSelectListPreferenceFix mThemesTile;

        static final int DEFAULT_QS_PANEL_LOGO_COLOR = 0xFF80CBC4;
        static final int DEFAULT_HEADER_SHADOW_COLOR = 0xFF000000;

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

            // Settings for Notification Drawer Weather
            mWeatherSettings = (Preference) findPreference(LOCKCLOCK_WEATHER);

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

            // Status Bar header text shadow
            mTextShadow =
                    (SeekBarPreferenceCham) findPreference(CUSTOM_HEADER_TEXT_SHADOW);
            int textShadow = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_TEXT_SHADOW, 0);
            mTextShadow.setValue(textShadow / 1);
            mTextShadow.setOnPreferenceChangeListener(this);

            //Status Bar header text shadow color
            mTShadowColor =
                    (ColorPickerPreference) findPreference(CUSTOM_HEADER_TEXT_SHADOW_COLOR);
            mTShadowColor.setOnPreferenceChangeListener(this);
            int shadowColor = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_TEXT_SHADOW_COLOR, DEFAULT_HEADER_SHADOW_COLOR);
            String HexColor = String.format("#%08x", (0x000000 & shadowColor));
            mTShadowColor.setSummary(HexColor);
            mTShadowColor.setNewPreviewColor(shadowColor);

            // Status Bar header shadow on custom header images
            mHeaderShadow =
                    (SeekBarPreferenceCham) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
            int headerShadow = Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
            mHeaderShadow.setValue(headerShadow / 1);
            mHeaderShadow.setOnPreferenceChangeListener(this);

            // Number of QS Columns 3,4,5
            mNumColumns = (ListPreference) findPreference("sysui_qs_num_columns");
            int numColumns = Settings.System.getIntForUser(resolver,
                    Settings.System.QS_NUM_TILE_COLUMNS, getDefaultNumColumns(),
                    UserHandle.USER_CURRENT);
            mNumColumns.setValue(String.valueOf(numColumns));
            updateNumColumnsSummary(numColumns);
            mNumColumns.setOnPreferenceChangeListener(this);

            // Number of QS Rows 3,4
            mNumRows = (ListPreference) findPreference("sysui_qs_num_rows");
            int numRows = Settings.System.getIntForUser(resolver,
                    Settings.System.QS_NUM_TILE_ROWS, getDefaultNumRows(),
                    UserHandle.USER_CURRENT);
            mNumRows.setValue(String.valueOf(numRows));
            updateNumRowsSummary(numRows);
            mNumRows.setOnPreferenceChangeListener(this);

            mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
            int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_STYLE, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            mTileAnimationStyle.setOnPreferenceChangeListener(this);

            mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
            int tileAnimationDuration = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_DURATION, 1500,
                    UserHandle.USER_CURRENT);
            mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
            updateTileAnimationDurationSummary(tileAnimationDuration);
            mTileAnimationDuration.setOnPreferenceChangeListener(this);

            mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
            int tileAnimationInterpolator = Settings.System.getIntForUser(resolver,
                    Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                    UserHandle.USER_CURRENT);
            mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            mTileAnimationInterpolator.setOnPreferenceChangeListener(this);

            // QS panel AICP logo
            mQSPanelLogo =
                    (ListPreference) findPreference(PREF_QS_PANEL_LOGO);
            int qSPanelLogo = Settings.System.getIntForUser(resolver,
                            Settings.System.QS_PANEL_LOGO, 0,
                            UserHandle.USER_CURRENT);
            mQSPanelLogo.setValue(String.valueOf(qSPanelLogo));
            mQSPanelLogo.setSummary(mQSPanelLogo.getEntry());
            mQSPanelLogo.setOnPreferenceChangeListener(this);

            // QS panel AICP logo color
            mQSPanelLogoColor =
                    (ColorPickerPreference) findPreference(PREF_QS_PANEL_LOGO_COLOR);
            mQSPanelLogoColor.setOnPreferenceChangeListener(this);
            int qSPanelLogoColor = Settings.System.getInt(resolver,
                    Settings.System.QS_PANEL_LOGO_COLOR, DEFAULT_QS_PANEL_LOGO_COLOR);
            String qSHexLogoColor = String.format("#%08x", (0xFF80CBC4 & qSPanelLogoColor));
            mQSPanelLogoColor.setSummary(qSHexLogoColor);
            mQSPanelLogoColor.setNewPreviewColor(qSPanelLogoColor);

            // QS panel AICP logo alpha
            mQSPanelLogoAlpha =
                    (SeekBarPreferenceCham) findPreference(PREF_QS_PANEL_LOGO_ALPHA);
            int qSPanelLogoAlpha = Settings.System.getInt(resolver,
                    Settings.System.QS_PANEL_LOGO_ALPHA, 51);
            mQSPanelLogoAlpha.setValue(qSPanelLogoAlpha / 1);
            mQSPanelLogoAlpha.setOnPreferenceChangeListener(this);

            mThemesTile = (MultiSelectListPreferenceFix) findPreference(PREF_THEMES_TILE);
            mThemesTile.setValues(getThemesTileValues());
            mThemesTile.setOnPreferenceChangeListener(this);

        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mWeatherSettings) {
                launchWeatherSettings();
                return true;
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void launchWeatherSettings() {
            Intent intent = new Intent(getActivity(), Weather.class);
            getActivity().startActivity(intent);
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
            } else if (preference == mNumRows) {
                int numRows = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.QS_NUM_TILE_ROWS,
                        numRows, UserHandle.USER_CURRENT);
                updateNumRowsSummary(numRows);
                return true;
            } else if (preference == mTextShadow) {
                int textShadow = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_TEXT_SHADOW, textShadow * 1);
                return true;
            } else if (preference == mTShadowColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.STATUS_BAR_CUSTOM_HEADER_TEXT_SHADOW_COLOR, intHex);
                return true;
            } else if (preference == mHeaderShadow) {
               int headerValue = (Integer) newValue;
               Settings.System.putInt(resolver,
                       Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, headerValue * 1);
               return true;
            } else if (preference == mTileAnimationStyle) {
                int tileAnimationStyle = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_STYLE,
                        tileAnimationStyle, UserHandle.USER_CURRENT);
                updateTileAnimationStyleSummary(tileAnimationStyle);
                updateAnimTileStyle(tileAnimationStyle);
                return true;
            } else if (preference == mTileAnimationDuration) {
                int tileAnimationDuration = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_DURATION,
                        tileAnimationDuration, UserHandle.USER_CURRENT);
                updateTileAnimationDurationSummary(tileAnimationDuration);
                return true;
            } else if (preference == mTileAnimationInterpolator) {
                int tileAnimationInterpolator = Integer.valueOf((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_INTERPOLATOR,
                        tileAnimationInterpolator, UserHandle.USER_CURRENT);
                updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
                return true;
            } else if (preference == mQSPanelLogo) {
                int qSPanelLogo = Integer.parseInt((String) newValue);
                int index = mQSPanelLogo.findIndexOfValue((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.
                        QS_PANEL_LOGO, qSPanelLogo, UserHandle.USER_CURRENT);
                mQSPanelLogo.setSummary(mQSPanelLogo.getEntries()[index]);
                QSPanelLogoSettingsDisabler(qSPanelLogo);
                return true;
            } else if (preference == mQSPanelLogoColor) {
                String hex = ColorPickerPreference.convertToARGB(
                        Integer.valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                        Settings.System.QS_PANEL_LOGO_COLOR, intHex);
                return true;
            } else if (preference == mQSPanelLogoAlpha) {
                int val = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.QS_PANEL_LOGO_ALPHA, val * 1);
                return true;
            } else if (preference == mThemesTile) {
            Set<String> vals = (Set<String>) newValue;
//            Log.e(TAG, "mThemesTileChanged " + vals.toString());
            setThemesTileValues(vals);
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

        private void updateNumRowsSummary(int numRows) {
            String prefix = (String) mNumRows.getEntries()[mNumRows.findIndexOfValue(String
                    .valueOf(numRows))];
            mNumRows.setSummary(getResources().getString(R.string.qs_num_rows_showing, prefix));
        }

        private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
            String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                    .valueOf(tileAnimationStyle))];
            mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
        }

        private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
            String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                    .valueOf(tileAnimationDuration))];
            mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
        }

        private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
            String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                    .valueOf(tileAnimationInterpolator))];
            mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
        }

        private void updateAnimTileStyle(int tileAnimationStyle) {
            if (mTileAnimationDuration != null) {
                if (tileAnimationStyle == 0) {
                    mTileAnimationDuration.setSelectable(false);
                    mTileAnimationInterpolator.setSelectable(false);
                } else {
                    mTileAnimationDuration.setSelectable(true);
                    mTileAnimationInterpolator.setSelectable(true);
                }
            }
        }

        private int getDefaultNumColumns() {
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

        private int getDefaultNumRows() {
            try {
                Resources res = getActivity().getPackageManager()
                        .getResourcesForApplication("com.android.systemui");
                int val = res.getInteger(res.getIdentifier("quick_settings_num_rows", "integer",
                        "com.android.systemui")); // better not be larger than 4, that's as high as the
                                                  // list goes atm
                return Math.max(1, val);
            } catch (Exception e) {
                return 3;
            }
        }

        private void QSPanelLogoSettingsDisabler(int qSPanelLogo) {
            if (qSPanelLogo == 0) {
                mQSPanelLogoColor.setEnabled(false);
                mQSPanelLogoAlpha.setEnabled(false);
            } else if (qSPanelLogo == 1) {
                mQSPanelLogoColor.setEnabled(false);
                mQSPanelLogoAlpha.setEnabled(true);
            } else {
                mQSPanelLogoColor.setEnabled(true);
                mQSPanelLogoAlpha.setEnabled(true);
            }
        }

        private void setThemesTileValues(Set<String> vals) {
            if (vals.isEmpty()) {
                // if user unchecks everything, reset to default
                vals.addAll(Arrays.asList(getResources().getStringArray(
                        R.array.themes_tile_default_values)));
//                Log.e(TAG, "setThemesTileValues called but is empty list = " + vals.toString());
                mThemesTile.setValues(vals);
            }
//            Log.e(TAG, "setThemesTileValues called = " + vals.toString());
            StringBuilder b = new StringBuilder();
            for (String val : vals) {
                b.append(val);
                b.append("|");
            }
            String newVal = b.toString();
            if (newVal.endsWith("|")) {
                newVal = removeLastChar(newVal);
            }
//            Log.e(TAG, "Themes tile components writing to provider = " + newVal);
            Settings.Secure.putStringForUser(getActivity().getContentResolver(),
                    Settings.Secure.THEMES_TILE_COMPONENTS,
                    newVal, UserHandle.USER_CURRENT);
        }

        private Set<String> getThemesTileValues() {
            Set<String> vals = new HashSet<>();
            String components = Settings.Secure.getStringForUser(getActivity().getContentResolver(),
                    Settings.Secure.THEMES_TILE_COMPONENTS,
                    UserHandle.USER_CURRENT);
            if (components != null) {
//                Log.e(TAG, "Themes tile components from provider raw = " + components);
            }
            if (TextUtils.isEmpty(components)) {
                vals.addAll(Arrays.asList(getResources().getStringArray(
                        R.array.themes_tile_default_values)));
//                Log.e(TAG, "Themes tile components from provider is empty. get defaults = " + vals.toString());
            } else {
                vals.addAll(Arrays.asList(components.split("\\|")));
//                Log.e(TAG, "Themes tile components from provider = " + vals.toString());
            }
            return vals;
        }

        static String removeLastChar(String s) {
            if (s == null || s.length() == 0) {
                return s;
            }
            return s.substring(0, s.length() - 1);
        }
    }
}
