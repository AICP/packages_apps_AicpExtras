package com.lordclockan.aicpextras;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.DataOutputStream;

import com.lordclockan.R;

public class RootExtras extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();
    }

    private class SettingsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        private static final String TAG = "RootExtras";

        private static final String SELINUX = "selinux";
        private static final String SELINUX_PERSISTENCE = "selinux_persistence";

        private SwitchPreference mSelinux;
        private SwitchPreference mSelinuxPersistence;

        private Toast mSuDeniedToast;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.root_extras);

            mSuDeniedToast = Toast.makeText(getActivity().getApplicationContext(),
                        getString(R.string.cannot_get_su_start), Toast.LENGTH_LONG);

            //SELinux
            mSelinux = (SwitchPreference) findPreference(SELINUX);
            mSelinux.setOnPreferenceChangeListener(this);
            mSelinuxPersistence = (SwitchPreference) findPreference(SELINUX_PERSISTENCE);
            mSelinuxPersistence.setOnPreferenceChangeListener(this);
            mSelinuxPersistence.setChecked(getContext()
                    .getSharedPreferences("selinux_pref", Context.MODE_PRIVATE)
                    .contains("selinux"));


            // Show a dialog to the user to inform about root requirement
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.root_extras_msg)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // We're staying here, so do the rest of the initialization that
                                // requires root
                                new InitPrefsTask().execute();
                            }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leave();
                            }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                // User was to lazy to use the buttons to close the dialog,
                                // so let's assume they know what they're doing and continue
                                // with initialization, now the part that requires root
                                new InitPrefsTask().execute();
                            }
                    })
                    .show();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (preference == mSelinux) {
                if (newValue.toString().equals("true")) {
                    new SwitchSelinuxTask().execute(true);
                    setSelinuxEnabled(true, mSelinuxPersistence.isChecked());
                } else if (newValue.toString().equals("false")) {
                    new SwitchSelinuxTask().execute(false);
                    setSelinuxEnabled(false, mSelinuxPersistence.isChecked());
                }
                return true;
            } else if (preference == mSelinuxPersistence) {
                setSelinuxEnabled(mSelinux.isChecked(), (Boolean) newValue);
                return true;
            }
            return false;
        }

        private abstract class SuTask<Params> extends AsyncTask<Params, Void, Void> {

            protected Exception mException = null;

            abstract protected void sudoInBackground(Params... params)
                    throws SuShell.SuDeniedException;

            @Override
            protected Void doInBackground(Params... params) {
                try {
                    sudoInBackground(params);
                } catch (SuShell.SuDeniedException e) {
                    mException = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (mException instanceof SuShell.SuDeniedException) {
                    mSuDeniedToast.show();
                } else {
                    mSuDeniedToast.cancel();
                }
            }
        }

        private class InitPrefsTask extends SuTask<Void> {

            @Override
            protected void sudoInBackground(Void... params) throws SuShell.SuDeniedException {
                boolean selinuxEnforcing =
                        SuShell.runWithSuCheck("getenforce").contains("Enforcing");
                Log.d(TAG, "Detected selinux enforcing status: " + selinuxEnforcing);
                getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setSelinuxStatus(selinuxEnforcing);
                        }
                });
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (mException instanceof SuShell.SuDeniedException) {
                    // Since we cannot get root access while trying to set up the fragment, leave
                    leave();
                }
            }
        }

        private void setSelinuxStatus(boolean status) {
            mSelinux.setChecked(status);
            if (status) {
                mSelinux.setSummary(R.string.selinux_enforcing_title);
            } else {
                mSelinux.setSummary(R.string.selinux_permissive_title);
            }
        }

        private void setSelinuxEnabled(boolean status, boolean persistent) {
            SharedPreferences.Editor editor =
                    getContext().getSharedPreferences("selinux_pref", Context.MODE_PRIVATE).edit();
            if (persistent) {
                editor.putBoolean("selinux", status);
            } else {
                editor.remove("selinux");
            }
            editor.apply();
            setSelinuxStatus(status);
        }

        private class SwitchSelinuxTask extends SuTask<Boolean> {

            @Override
            protected void sudoInBackground(Boolean... params) {
                if (params.length != 1) {
                    Log.e(TAG, "SwitchSelinuxTask: invalid params count");
                    return;
                }
                try {
                    if (params[0]) {
                        SuShell.runWithSuCheck("setenforce 1");
                    } else {
                        SuShell.runWithSuCheck("setenforce 0");
                    }
                } catch (SuShell.SuDeniedException e) {
                    mException = e;
                }
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (mException instanceof SuShell.SuDeniedException) {
                    // Did not work, so return to previous value
                    setSelinuxEnabled(!mSelinux.isChecked(), mSelinuxPersistence.isChecked());
                }
            }
        }
    }

    private void leave() {
        // Leave screen by restarting the activity - this should take us
        // to the initially viewed fragment when opening the activity
        getActivity().finish();
        startActivity(getActivity().getIntent());
    }
}
