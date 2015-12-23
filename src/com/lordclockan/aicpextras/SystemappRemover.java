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
package com.lordclockan.aicpextras;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Scanner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lordclockan.R;

public class SystemappRemover extends AppCompatActivity {

    private static final String TAG = "SystemappRemover";

    private final int STARTUP_DIALOG = 1;
    private final int DELETE_DIALOG = 2;
    private final int DELETE_MULTIPLE_DIALOG = 3;

    protected ArrayAdapter<String> adapter;
    private ArrayList<String> mSysApp;
    private boolean startup =true;
    static final boolean DEBUG = true;
    public final String systemPath = "/system/app/";
    public final String systemPrivPath = "/system/priv-app/";
    protected Process superUser;
    protected DataOutputStream dos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slim_sizer);

        if (adapter!=null && startup==true) {
            showDialog(STARTUP_DIALOG, null, adapter);
            startup=false;
        }

        final Button delButton = (Button) findViewById(R.id.btn_delete);
        final Button profileButton = (Button) findViewById(R.id.btn_profile);

        // create arraylist of apps not to be removed
        final ArrayList<String> safetyList = new ArrayList<String>();
        // app
        safetyList.add("CertInstaller");
        safetyList.add("DrmProvider");
        safetyList.add("PackageInstaller");
        safetyList.add("Superuser");
        safetyList.add("TelephonyProvider");
        // priv-app
        safetyList.add("AicpExtras");
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

        // create arraylist from /system/app and /system/priv-app content
        File system = new File(systemPath);
        File systemPriv = new File(systemPrivPath);
        String[] sysappArray = combine(system.list(), systemPriv.list());
        mSysApp = new ArrayList<String>(
                Arrays.asList(sysappArray));

        // remove .odex files from list
        filterOdex();

        // remove "apps not to be removed" from list and sort list
        mSysApp.removeAll(safetyList);
        Collections.sort(mSysApp);

        // populate listview via arrayadapter
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, mSysApp);

        // startup dialog
        //showDialog(STARTUP_DIALOG, null, adapter);

        final ListView lv = (ListView) findViewById(R.string.listsystem);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv.setAdapter(adapter);

        // longclick an entry
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                    final int arg2, long arg3) {
                // create deletion dialog
                String item = lv.getAdapter().getItem(arg2).toString();
                showDialog(DELETE_DIALOG, item, adapter);
                return false;
            }
        });
        // click button delete
        delButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check which items are selected
                String item = null;
                int len = lv.getCount();
                SparseBooleanArray checked = lv.getCheckedItemPositions();
                for (int i = lv.getCount() - 1; i > 0; i--) {
                    if (checked.get(i)) {
                        item = mSysApp.get(i);
                    }
                }
                if (item == null) {
                    toast(getResources().getString(
                            R.string.sizer_message_noselect));
                    return;
                } else {
                showDialog(DELETE_MULTIPLE_DIALOG, item, adapter);
                }
            }
        });
        // click button profile
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call select dialog
                selectDialog(mSysApp, adapter);
            }
        });
    }

    private void showDialog(int id, final String item,
            final ArrayAdapter<String> adapter) {
        // startup dialog
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        if (id == STARTUP_DIALOG) {
            // create warning dialog
            alert.setMessage(R.string.sizer_message_startup)
                    .setTitle(R.string.caution)
                    .setCancelable(true)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for ok
                                    dialog.cancel();
                                }
                            });
        // delete dialog
        } else if (id == DELETE_DIALOG) {
            alert.setMessage(R.string.sizer_message_delete)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for ok
                                    // call delete
                                    new SystemappRemover.Deleter().execute(item);
                                      // remove list entry
                                      adapter.remove(item);
                                      adapter.notifyDataSetChanged();
                                }
                            })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for cancel
                                    dialog.cancel();
                                }
                            });
        } else if (id == DELETE_MULTIPLE_DIALOG) {
            alert.setMessage(R.string.sizer_message_delete)
                    .setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    final ListView lv = (ListView) findViewById(R.string.listsystem);
                                    ArrayList<String> itemsList = new ArrayList<String>();
                                    SparseBooleanArray checked = lv.getCheckedItemPositions();
                                    for (int i = lv.getCount() - 1; i > 0; i--) {
                                        if (checked.get(i)) {
                                              String appName = mSysApp.get(i);
                                              itemsList.add(appName);
                                              // remove list entry
                                              lv.setItemChecked(i, false);
                                              adapter.remove(appName);
                                        }
                                    }
                                   adapter.notifyDataSetChanged();
                                      new SystemappRemover.Deleter().execute(itemsList.toArray(new String[itemsList.size()]));
                                }
                            })
                    .setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int id) {
                                    // action for cancel
                                    dialog.cancel();
                                }
                            });
        }
        // show warning dialog
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

    // profile select dialog
    private void selectDialog(final ArrayList<String> sysAppProfile,
            final ArrayAdapter<String> adapter) {
        AlertDialog.Builder select = new AlertDialog.Builder(this);
        select.setItems(R.array.slimsizer_profile_array,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        short state = sdAvailable();
                        File path = new File(Environment
                                .getExternalStorageDirectory() + "/aicp");
                        File savefile = new File(path + "/systemappremover.stf");
                        if (which == 0) {
                            // load profile action
                            if (state >= 1) {
                                String profile;
                                try {
                                    // read savefile and create arraylist
                                    profile = new Scanner(savefile, "UTF-8")
                                            .useDelimiter("\\A").next();
                                    ArrayList<String> profileState = new ArrayList<String>(
                                            Arrays.asList(profile.split(", ")));
                                    // create arraylist of unique entries in
                                    // sysAppProfile (currently installed apps)
                                    ArrayList<String> deleteList = new ArrayList<String>();
                                    for (String item : sysAppProfile) {
                                        if (!profileState.contains(item)) {
                                            deleteList.add(item);
                                        }
                                    }
                                    // delete all entries in deleteList
                                     ArrayList<String> itemsList = new ArrayList<String>();
                                     for (int i = deleteList.size() - 1; i > 0; i--) {
                                        String item = deleteList.get(i);
                                         itemsList.add(item);
                                         // remove list entry
                                         adapter.remove(item);
                                    }
                                    adapter.notifyDataSetChanged();
                                    new SystemappRemover.Deleter().execute(itemsList.toArray(new String[itemsList.size()]));
                                } catch (FileNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else {
                                toast(getResources().getString(
                                        R.string.sizer_message_sdnoread));
                            }
                        } else if (which == 1) {
                            // save profile action
                            if (state == 2) {
                                try {
                                    // create directory if it doesnt exist
                                    if (!path.exists()) {
                                        path.mkdirs();
                                    }
                                    // create string from arraylists
                                    String lists = sysAppProfile.toString();
                                    lists = lists.replace("][", ",");
                                    lists = lists.replace("[", "");
                                    lists = lists.replace("]", "");
                                    // delete savefile if it exists (overwrite)
                                    if (savefile.exists()) {
                                        savefile.delete();
                                    }
                                    // create savefile and output lists to it
                                    FileWriter outstream = new FileWriter(
                                            savefile);
                                    BufferedWriter save = new BufferedWriter(
                                            outstream);
                                    save.write(lists);
                                    save.close();
                                    // check for success
                                    if (savefile.exists()) {
                                        toast(getResources()
                                                .getString(
                                                        R.string.sizer_message_filesuccess));
                                    } else {
                                        toast(getResources()
                                                .getString(
                                                        R.string.sizer_message_filefail));
                                    }
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            } else {
                                toast(getResources().getString(
                                        R.string.sizer_message_sdnowrite));
                            }
                        }
                    }
                });
        select.show();
    }

    public void toast(String text) {
        // easy toasts for all!
        Toast toast = Toast.makeText(SystemappRemover.this, text,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    private short sdAvailable() {
        // check if sdcard is available
        // taken from developer.android.com
        short mExternalStorageAvailable = 0;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = 2;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = 1;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = 0;
        }
        return mExternalStorageAvailable;
    }

    // mount /system as ro on close
    protected void onStop(Bundle savedInstanceState) throws IOException {
        try {
            dos.writeBytes("\n" + "mount -o remount,ro /system" + "\n");
            dos.writeBytes("\n" + "exit" + "\n");
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class Deleter extends AsyncTask<String, String, Void> {

        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dos == null) {
                try {
                    superUser = new ProcessBuilder("su", "-c", "/system/bin/sh").start();
                    dos = new DataOutputStream(superUser.getOutputStream());
                    dos.writeBytes("mount -o remount,rw /system" + "\n");
                    if (DEBUG) {
                       Log.d(TAG, "File System Mounted as root");
                    }
                } catch (IOException e) {
                  e.printStackTrace();
                }
            }
            progress = new ProgressDialog(SystemappRemover.this);
            progress.setTitle(getString(R.string.delete_progress_title));
            progress.setMessage(getString(R.string.delete_progress));
            progress.show();
        }

        protected Void doInBackground(String... params) {
            for (String appName : params) {
                String basePath = systemPath;
                File app = new File(basePath + appName);

                    if (!app.exists()) {
                           basePath = systemPrivPath;
                    }
                    try {
                            File app2rm = new File(basePath + appName);
                            dos.writeBytes("\n" + "rm -rf " + app2rm + "\n" );
                    if (DEBUG) {
                            Log.d(TAG, "Applicattion removed was  " + app2rm );
                            }
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPreExecute();
            try {
                dos.flush(); 
            } catch (IOException e) {
                e.printStackTrace();
            }
            progress.dismiss();
        }
    }
}
