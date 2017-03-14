package com.lordclockan.aicpextras;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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

        private static final String TAG = "DisplayAndAnimSettings";

        private static final String POWER_MENU_ANIMATIONS = "power_menu_animations";
        private static final String KEY_TOAST_ANIMATION = "toast_animation";
        private static final String TOAST_ICON_COLOR = "toast_icon_color";
        private static final String TOAST_TEXT_COLOR = "toast_text_color";

        private ColorPickerPreference mIconColor;
        private ColorPickerPreference mTextColor;
        private ListPreference mPowerMenuAnimations;
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
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mPowerMenuAnimations) {
                Settings.System.putInt(resolver, Settings.System.POWER_MENU_ANIMATIONS,
                        Integer.parseInt((String) newValue));
                mPowerMenuAnimations.setValue(String.valueOf(newValue));
                mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            } else if (preference == mToastAnimation) {
                int index = mToastAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.TOAST_ANIMATION, index);
                mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
                Toast.makeText(getActivity(), mToastAnimation.getEntries()[index],
                        Toast.LENGTH_SHORT).show();
            }  else if (preference == mIconColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                       .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                       Settings.System.TOAST_ICON_COLOR, intHex);
                Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                       Toast.LENGTH_SHORT).show();
            } else if (preference == mTextColor) {
                String hex = ColorPickerPreference.convertToARGB(Integer
                      .valueOf(String.valueOf(newValue)));
                preference.setSummary(hex);
                int intHex = ColorPickerPreference.convertToColorInt(hex);
                Settings.System.putInt(resolver,
                      Settings.System.TOAST_TEXT_COLOR, intHex);
                Toast.makeText(getActivity(), mToastAnimation.getEntry(),
                      Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
