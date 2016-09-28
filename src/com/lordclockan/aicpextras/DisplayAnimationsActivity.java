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

        private ListPreference mLcdDensityPreference;

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
    }
}
