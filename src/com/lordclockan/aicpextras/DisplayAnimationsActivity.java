package com.lordclockan.aicpextras;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class DisplayAnimationsActivity extends Fragment {

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

        private static final String TAG = "AnimSettings";

        private static final String POWER_MENU_ANIMATIONS = "power_menu_animations";
        private static final String KEY_TOAST_ANIMATION = "toast_animation";
        private static final String TOAST_ICON_COLOR = "toast_icon_color";
        private static final String TOAST_TEXT_COLOR = "toast_text_color";

        private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
        private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
        private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";

        private ColorPickerPreference mIconColor;
        private ColorPickerPreference mTextColor;
        private ListPreference mPowerMenuAnimations;
        private ListPreference mTileAnimationStyle;
        private ListPreference mTileAnimationDuration;
        private ListPreference mTileAnimationInterpolator;
        private ListPreference mToastAnimation;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.display_anim_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            // Power Menu Animations
            mPowerMenuAnimations = (ListPreference) prefSet.findPreference(POWER_MENU_ANIMATIONS);
            mPowerMenuAnimations.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.POWER_MENU_ANIMATIONS, 0)));
            mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            mPowerMenuAnimations.setOnPreferenceChangeListener(this);

            // Toast Animations
            mToastAnimation = (ListPreference) prefSet.findPreference(KEY_TOAST_ANIMATION);
            mToastAnimation.setSummary(mToastAnimation.getEntry());
            int CurrentToastAnimation = Settings.System.getInt(
                    resolver, Settings.System.TOAST_ANIMATION, 1);
            mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
            mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
            mToastAnimation.setOnPreferenceChangeListener(this);

            int intColor = 0xffffffff;
            String hexColor = String.format("#%08x", (0xffffffff & 0xffffffff));

            // Toast icon color
            mIconColor = (ColorPickerPreference) findPreference(TOAST_ICON_COLOR);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.TOAST_ICON_COLOR, 0xffffffff);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mIconColor.setNewPreviewColor(intColor);
            mIconColor.setSummary(hexColor);
            mIconColor.setOnPreferenceChangeListener(this);

            // Toast text color
            mTextColor = (ColorPickerPreference) findPreference(TOAST_TEXT_COLOR);
            intColor = Settings.System.getInt(resolver,
                    Settings.System.TOAST_TEXT_COLOR, 0xffffffff);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setNewPreviewColor(intColor);
            mTextColor.setSummary(hexColor);
            mTextColor.setOnPreferenceChangeListener(this);

            // QS tile animation
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

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mPowerMenuAnimations) {
                Settings.System.putInt(resolver, Settings.System.POWER_MENU_ANIMATIONS,
                        Integer.parseInt((String) newValue));
                mPowerMenuAnimations.setValue(String.valueOf(newValue));
                mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
                return true;
            } else if (preference == mToastAnimation) {
                int index = mToastAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.TOAST_ANIMATION, index);
                mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
                Toast.makeText(getActivity(), mToastAnimation.getEntries()[index],
                        Toast.LENGTH_SHORT).show();
                return true;
            }  else if (preference == mIconColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                       .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                       Settings.System.TOAST_ICON_COLOR, intHex);
                Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                       Toast.LENGTH_SHORT).show();
                return true;
            } else if (preference == mTextColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                      .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                      Settings.System.TOAST_TEXT_COLOR, intHex);
                Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                      Toast.LENGTH_SHORT).show();
                return true;
            } else if (preference == mTileAnimationStyle) {
                int tileAnimationStyle = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_STYLE,
                        tileAnimationStyle, UserHandle.USER_CURRENT);
                updateTileAnimationStyleSummary(tileAnimationStyle);
                updateAnimTileStyle(tileAnimationStyle);
            } else if (preference == mTileAnimationDuration) {
                int tileAnimationDuration = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_DURATION,
                        tileAnimationDuration, UserHandle.USER_CURRENT);
                updateTileAnimationDurationSummary(tileAnimationDuration);
                return true;
            } else if (preference == mTileAnimationInterpolator) {
                int tileAnimationInterpolator = Integer.parseInt((String) newValue);
                Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_INTERPOLATOR,
                        tileAnimationInterpolator, UserHandle.USER_CURRENT);
                updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
                return true;
            }
            return false;
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
    }
}
