package com.lordclockan.aicpextras;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;

import com.lordclockan.R;

public class RootExtras extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.content_main, new SettingsPreferenceFragment())
                .commit();

        // Show a dialog to the user to inform about root requirement
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.root_extras_msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Only dismiss dialog
                        }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Leave screen by restarting the activity - this should take us
                            // to the initially viewed fragment when opening the activity
                            getActivity().finish();
                            startActivity(getActivity().getIntent());
                        }
                })
                .show();

    }

    private static class SettingsPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.root_extras);
        }
    }
}
