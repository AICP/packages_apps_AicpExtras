package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.ProgressDialog;
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
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import android.widget.Toast;

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
        private static final String PREF_GESTURE_ANYWHERE = "gestureanywhere";
        private static final String PREF_LOCKSCREEN_WEATHER = "lockscreen_weather";

        private ListPreference mLcdDensityPreference;
        private Preference mGestureAnywhere;
        private Preference mLockscreenWeather;

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

        mGestureAnywhere = prefSet.findPreference(PREF_GESTURE_ANYWHERE);
        mLockscreenWeather = prefSet.findPreference(PREF_LOCKSCREEN_WEATHER);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            ContentResolver resolver = getActivity().getContentResolver();
            if (preference == mLcdDensityPreference) {
                try {
                    int value = Integer.parseInt((String) newValue);
                    writeLcdDensityPreference(preference.getContext(), value);
                    updateLcdDensityPreferenceDescription(value);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "could not persist display density setting", e);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            if (preference == mGestureAnywhere) {
                Intent intent = new Intent(getActivity(), GestureAnywhereSettings.class);
                getActivity().startActivity(intent);
            } else if (preference == mLockscreenWeather) {
                Intent intent = new Intent(getActivity(), Weather.class);
                getActivity().startActivity(intent);
            } else {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
            return false;
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
