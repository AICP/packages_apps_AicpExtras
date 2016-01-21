package com.lordclockan.aicpextras;

import android.app.AlertDialog;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import android.widget.Toast;

import com.lordclockan.R;
import com.lordclockan.aicpextras.widget.SeekBarPreferenceCham;

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

        private static final String KEY_LCD_DENSITY = "lcd_density";
        private static final String PREF_AOKP_ANIMATION = "aokp_animation";
        private static final String KEY_TOAST_ANIMATION = "toast_animation";
        private static final String POWER_MENU_ANIMATIONS = "power_menu_animations";
        private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
        private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
        private static final String PREF_IME_ANIMATIONS = "ime_animations";
        private static final String PREF_TRANSPARENT_VOLUME_DIALOG = "transparent_volume_dialog";

        private ListPreference mLcdDensityPreference;
        private Preference mAokpAnimation;
        private ListPreference mToastAnimation;
        private ListPreference mPowerMenuAnimations;
        private ListPreference mListViewAnimation;
        private ListPreference mListViewInterpolator;
        private Preference mImeAnimations;
        private SeekBarPreferenceCham mVolumeDialogAlpha;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.display_anim_layout);

            PreferenceScreen prefSet = getPreferenceScreen();
            ContentResolver resolver = getActivity().getContentResolver();

            mLcdDensityPreference = (ListPreference) prefSet.findPreference(KEY_LCD_DENSITY);
            if (mLcdDensityPreference != null) {
                if (UserHandle.myUserId() != UserHandle.USER_OWNER) {
                    prefSet.removePreference(mLcdDensityPreference);
                } else {
                    int defaultDensity = getDefaultDensity();
                    int currentDensity = getCurrentDensity();
                    if (currentDensity < 10 || currentDensity >= 1000) {
                        // Unsupported value, force default
                        currentDensity = defaultDensity;
                    }

                    int factor = defaultDensity >= 480 ? 40 : 20;
                    int minimumDensity = defaultDensity - 4 * factor;
                    int currentIndex = -1;
                    String[] densityEntries = new String[7];
                    String[] densityValues = new String[7];
                    for (int idx = 0; idx < 7; ++idx) {
                        int val = minimumDensity + factor * idx;
                        int valueFormatResId = val == defaultDensity
                                ? R.string.lcd_density_default_value_format
                                : R.string.lcd_density_value_format;

                        densityEntries[idx] = getString(valueFormatResId, val);
                        densityValues[idx] = Integer.toString(val);
                        if (currentDensity == val) {
                            currentIndex = idx;
                        }
                    }
                    mLcdDensityPreference.setEntries(densityEntries);
                    mLcdDensityPreference.setEntryValues(densityValues);
                    if (currentIndex != -1) {
                        mLcdDensityPreference.setValueIndex(currentIndex);
                    }
                    mLcdDensityPreference.setOnPreferenceChangeListener(this);
                    updateLcdDensityPreferenceDescription(currentDensity);
                }
            }

            mAokpAnimation = prefSet.findPreference(PREF_AOKP_ANIMATION);

            mImeAnimations = prefSet.findPreference(PREF_IME_ANIMATIONS);

            // Toast Animations
            mToastAnimation = (ListPreference) prefSet.findPreference(KEY_TOAST_ANIMATION);
            mToastAnimation.setSummary(mToastAnimation.getEntry());
            int CurrentToastAnimation = Settings.System.getInt(resolver, Settings.System.TOAST_ANIMATION, 1);
            mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
            mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
            mToastAnimation.setOnPreferenceChangeListener(this);

            // Power Menu Animations
            mPowerMenuAnimations = (ListPreference) prefSet.findPreference(POWER_MENU_ANIMATIONS);
            mPowerMenuAnimations.setValue(String.valueOf(Settings.System.getInt(
                    resolver, Settings.System.POWER_MENU_ANIMATIONS, 0)));
            mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            mPowerMenuAnimations.setOnPreferenceChangeListener(this);

            // ListView Animations
            mListViewAnimation = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_ANIMATION);
            int listviewanimation = Settings.System.getInt(resolver,
                    Settings.System.LISTVIEW_ANIMATION, 0);
            mListViewAnimation.setValue(String.valueOf(listviewanimation));
            mListViewAnimation.setSummary(mListViewAnimation.getEntry());
            mListViewAnimation.setOnPreferenceChangeListener(this);

            mListViewInterpolator = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_INTERPOLATOR);
            int listviewinterpolator = Settings.System.getInt(resolver,
                    Settings.System.LISTVIEW_INTERPOLATOR, 0);
            mListViewInterpolator.setValue(String.valueOf(listviewinterpolator));
            mListViewInterpolator.setSummary(mListViewInterpolator.getEntry());
            mListViewInterpolator.setOnPreferenceChangeListener(this);
            mListViewInterpolator.setEnabled(listviewanimation > 0);

            // Volume dialog alpha
            mVolumeDialogAlpha =
                    (SeekBarPreferenceCham) prefSet.findPreference(PREF_TRANSPARENT_VOLUME_DIALOG);
            int volumeDialogAlpha = Settings.System.getInt(resolver,
                    Settings.System.TRANSPARENT_VOLUME_DIALOG, 255);
            mVolumeDialogAlpha.setValue(volumeDialogAlpha / 1);
            mVolumeDialogAlpha.setOnPreferenceChangeListener(this);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mLcdDensityPreference) {
                String tempValue = (String) newValue;
                String oldValue = mLcdDensityPreference.getValue();
                if (!TextUtils.equals(tempValue, oldValue)) {
                    showLcdConfirmationDialog((String) newValue);
                }
                return false;
            } else if (preference == mToastAnimation) {
                int index = mToastAnimation.findIndexOfValue((String) newValue);
                Settings.System.putString(resolver,
                        Settings.System.TOAST_ANIMATION, (String) newValue);
                mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
                Toast.makeText(getActivity(), "Toast test!!!", Toast.LENGTH_SHORT).show();
            } else if (preference == mPowerMenuAnimations) {
                Settings.System.putInt(resolver, Settings.System.POWER_MENU_ANIMATIONS,
                        Integer.valueOf((String) newValue));
                mPowerMenuAnimations.setValue(String.valueOf(newValue));
                mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            } else if (preference == mListViewAnimation) {
                int value = Integer.parseInt((String) newValue);
                int index = mListViewAnimation.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_ANIMATION, value);
                mListViewAnimation.setSummary(mListViewAnimation.getEntries()[index]);
                mListViewInterpolator.setEnabled(value > 0);
            } else if (preference == mListViewInterpolator) {
                int value = Integer.parseInt((String) newValue);
                int index = mListViewInterpolator.findIndexOfValue((String) newValue);
                Settings.System.putInt(resolver,
                        Settings.System.LISTVIEW_INTERPOLATOR, value);
                mListViewInterpolator.setSummary(mListViewInterpolator.getEntries()[index]);
            } else if (preference == mVolumeDialogAlpha) {
                int alpha = (Integer) newValue;
                Settings.System.putInt(resolver,
                        Settings.System.TRANSPARENT_VOLUME_DIALOG, alpha * 1);
                return true;
            }
            return true;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mAokpAnimation) {
                Intent intent = new Intent(getActivity(), AnimationControls.class);
                getActivity().startActivity(intent);
            } else if (preference == mImeAnimations) {
                Intent intent = new Intent(getActivity(), KeyboardAnimationInterfaceSettings.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
        }

        private void showLcdConfirmationDialog(final String lcdDensity) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.lcd_density);
            builder.setMessage(R.string.lcd_density_prompt_message);
            builder.setPositiveButton(R.string.print_restart,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    try {
                        int value = Integer.parseInt(lcdDensity);
                        writeLcdDensityPreference(getActivity(), value);
                        updateLcdDensityPreferenceDescription(value);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "could not persist display density setting", e);
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }

        private int getDefaultDensity() {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.checkService(
                    Context.WINDOW_SERVICE));
            try {
                return wm.getInitialDisplayDensity(Display.DEFAULT_DISPLAY);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return DisplayMetrics.DENSITY_DEVICE;
        }

        private int getCurrentDensity() {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.checkService(
                    Context.WINDOW_SERVICE));
            try {
                return wm.getBaseDisplayDensity(Display.DEFAULT_DISPLAY);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return DisplayMetrics.DENSITY_DEVICE;
        }

        private void updateLcdDensityPreferenceDescription(int currentDensity) {
            final int summaryResId = currentDensity == getDefaultDensity()
                    ? R.string.lcd_density_default_value_format : R.string.lcd_density_value_format;
            mLcdDensityPreference.setSummary(getString(summaryResId, currentDensity));
        }

        private void writeLcdDensityPreference(final Context context, final int density) {
            final IActivityManager am = ActivityManagerNative.asInterface(
                    ServiceManager.checkService("activity"));
            final IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.checkService(
                    Context.WINDOW_SERVICE));
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected void onPreExecute() {
                    ProgressDialog dialog = new ProgressDialog(context);
                    dialog.setMessage(getResources().getString(R.string.restarting_ui));
                    dialog.setCancelable(false);
                    dialog.setIndeterminate(true);
                    dialog.show();
                }
                @Override
                protected Void doInBackground(Void... params) {
                    // Give the user a second to see the dialog
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // Ignore
                    }

                    try {
                        wm.setForcedDisplayDensity(Display.DEFAULT_DISPLAY, density);
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to set density to " + density, e);
                    }

                    // Restart the UI
                    try {
                        am.restart();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to restart");
                    }
                    return null;
                }
            };
            task.execute();
        }
    }
}
