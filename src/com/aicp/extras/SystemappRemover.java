/*=========================================================================
 *
 *  PROJECT:  SlimRoms
 *            Team Slimroms (http://www.slimroms.net)
 *
 *  COPYRIGHT Copyright (C) 2013 Slimroms http://www.slimroms.net
 *            Copyright (C) 2014 Dirty Unicorns
 *            All rights reserved
 *
 *  LICENSE   http://www.gnu.org/licenses/gpl-2.0.html GNU/GPL
 *
 *  AUTHORS:     fronti90
 *  DESCRIPTION: SlimSizer: manage your apps
 *
 *  MODS: Dirty Unicorns
 *        Team D.I.R.T.
 *        Added priv-app and odex files support
 *
 *=========================================================================
 */
package com.aicp.extras;

import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.aicp.extras.utils.SuShell;
import com.aicp.extras.utils.SuTask;

public class SystemappRemover extends SubActivity {

    private static final String TAG = "SystemappRemover";

    private final int DELETE_DIALOG = 1;

    protected ArrayAdapter<String> adapter;
    private ArrayList<String> mSysApp;
    static final boolean DEBUG = true;
    public final String systemPath = "/system/app/";
    public final String systemPrivPath = "/system/priv-app/";
    protected Process superUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_app_remover);

        ImageButton fabButton = (ImageButton) findViewById(R.id.fab_delete);

        final ArrayList<String> safetyList = new ArrayList<String>();
        safetyList.add("CertInstaller");
        safetyList.add("DrmProvider");
        safetyList.add("PackageInstaller");
        safetyList.add("TelephonyProvider");
        safetyList.add("AicpExtras");
        safetyList.add("CMAudioService");
        safetyList.add("CMParts");
        safetyList.add("CMSettingsProvider");
        safetyList.add("ContactsProvider");
        safetyList.add("DefaultContainerService");
        safetyList.add("Dialer");
        safetyList.add("DownloadProvider");
        safetyList.add("FusedLocation");
        safetyList.add("Keyguard");
        safetyList.add("MediaProvider");
        safetyList.add("ProxyHandler");
        safetyList.add("Settings");
        safetyList.add("SettingsProvider");
        safetyList.add("SystemUI");
        safetyList.add("TeleService");

        File system = new File(systemPath);
        File systemPriv = new File(systemPrivPath);
        String[] sysappArray = combine(system.list(), systemPriv.list());
        mSysApp = new ArrayList<String>(
                Arrays.asList(sysappArray));

        filterOdex();

        mSysApp.removeAll(safetyList);
        Collections.sort(mSysApp);

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, mSysApp);

        final ListView lv = (ListView) findViewById(android.R.id.list);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setAdapter(adapter);

        View footer = LayoutInflater.from(this)
                .inflate(R.layout.system_app_remover_empty_list_entry_footer, lv, false);
        lv.addFooterView(footer);
        lv.setFooterDividersEnabled(false);
        footer.setOnClickListener(null);

        fabButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String item = null;
                SparseBooleanArray checked = lv.getCheckedItemPositions();
                for (int i = lv.getCount() - 1; i >= 0; i--) {
                    if (checked.get(i)) {
                        item = mSysApp.get(i);
                    }
                }
                if (item == null) {
                    toast(getResources().getString(
                            R.string.system_app_remover_message_noselect));
                    return;
                } else {
                    showDialog(DELETE_DIALOG, item, adapter);
                }
            }
        });
    }

    public void toast(String text) {
        Toast toast = Toast.makeText(SystemappRemover.this, text,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showDialog(int id, final String item,
                            final ArrayAdapter<String> adapter) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (id == DELETE_DIALOG) {
            alert.setMessage(R.string.system_app_remover_message_delete)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    final ListView lv = (ListView) findViewById(android.R.id.list);
                                    ArrayList<String> itemsList = new ArrayList<String>();
                                    SparseBooleanArray checked = lv.getCheckedItemPositions();
                                    for (int i = lv.getCount() - 1; i > 0; i--) {
                                        if (checked.get(i)) {
                                            String appName = mSysApp.get(i);
                                            itemsList.add(appName);
                                            lv.setItemChecked(i, false);
                                            adapter.remove(appName);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                    new SystemappRemover.Deleter(SystemappRemover.this)
                                            .execute(itemsList.toArray(
                                                    new String[itemsList.size()]));
                                }
                            })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    dialog.cancel();
                                }
                            });
        }
        alert.show();
    }

    private String[] combine(String[] a, String[] b) {
        int length = a.length + b.length;
        String[] result = new String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private void filterOdex() {
        ListIterator<String> it = mSysApp.listIterator();
        while ( it.hasNext() ) {
            String str = it.next();
            if ( str.endsWith(".odex") ) {
                it.remove();
            }
        }
    }

    private class Deleter extends SuTask<String> {

        public Deleter(Context context) {
            super(context);
        }

        protected void sudoInBackground(String... params) throws SuShell.SuDeniedException {
            String[] commands = new String[params.length+1];
            commands[0] = "mount -o rw,remount /";
            int commandCount = 1;
            for (String appName : params) {
                String basePath = systemPath;
                File app = new File(basePath + appName);

                if (!app.exists()) {
                       basePath = systemPrivPath;
                }
                File app2rm = new File(basePath + appName);
                Log.d(TAG, "Removing " + app2rm.getAbsolutePath());
                commands[commandCount++] = "rm -rf " + app2rm.getAbsolutePath();
            }
            ArrayList<String> output = SuShell.runWithSuCheck(commands);
            for (String out: output) {
                Log.e(TAG, out);
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result) {
                // Su denied
                recreate();
            }
        }
    }
}
