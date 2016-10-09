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
import android.text.Spannable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import android.widget.EditText;
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
        private static final String POWER_MENU_ANIMATIONS = "power_menu_animations";
        private static final String KEY_TOAST_ANIMATION = "toast_animation";

        private ListPreference mLcdDensityPreference;
        private ListPreference mPowerMenuAnimations;
        private ListPreference mToastAnimation;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

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
                    String[] densityEntries = new String[8];
                    String[] densityValues = new String[8];
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
                    densityEntries[7] = getString(R.string.custom_density);
                    densityValues[7] = getString(R.string.custom_density_value);
                    mLcdDensityPreference.setEntries(densityEntries);
                    mLcdDensityPreference.setEntryValues(densityValues);
                    if (currentIndex != -1) {
                        mLcdDensityPreference.setValueIndex(currentIndex);
                    } else {
                        mLcdDensityPreference.setValueIndex(7);
                    }
                    mLcdDensityPreference.setOnPreferenceChangeListener(this);
                    updateLcdDensityPreferenceDescription(currentDensity);
                }
            }

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

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mLcdDensityPreference) {
                String tempValue = (String) newValue;
                int index = mLcdDensityPreference.findIndexOfValue((String) newValue);
                if (index == 7) {
                    customDpiDialog();
                } else {
                    String oldValue = mLcdDensityPreference.getValue();
                    if (!TextUtils.equals(tempValue, oldValue)) {
                        showLcdConfirmationDialog((String) newValue);
                    }
                }
                return false;
            } else if (preference == mPowerMenuAnimations) {
                Settings.System.putInt(resolver, Settings.System.POWER_MENU_ANIMATIONS,
                        Integer.valueOf((String) newValue));
                mPowerMenuAnimations.setValue(String.valueOf(newValue));
                mPowerMenuAnimations.setSummary(mPowerMenuAnimations.getEntry());
            } else if (preference == mToastAnimation) {
                int index = mToastAnimation.findIndexOfValue((String) newValue);
                Settings.System.putString(resolver,
                        Settings.System.TOAST_ANIMATION, (String) newValue);
                mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
                Toast.makeText(getActivity(), mToastAnimation.getEntries()[index],
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
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

        private void customDpiDialog() {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
            alert.setTitle(R.string.custom_density_dialog_title);
            alert.setMessage(R.string.custom_density_dialog_summary);

            final EditText input = new EditText(getActivity());
            input.setSelection(input.getText().length());
            alert.setView(input);
            alert.setPositiveButton(getString(android.R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String editText = ((Spannable) input.getText()).toString().trim();
                            try {
                                int value = Integer.parseInt(editText);
                                writeLcdDensityPreference(getActivity(), value);
                                updateLcdDensityPreferenceDescription(value);
                            } catch (NumberFormatException e) {
                                Log.e(TAG, "could not persist display density setting", e);
                            }
                        }
                    });
            alert.setNegativeButton(getString(android.R.string.cancel), null);
            alert.show();
        }
    }
}
