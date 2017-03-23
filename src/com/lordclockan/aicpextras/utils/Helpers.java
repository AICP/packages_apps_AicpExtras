package com.lordclockan.aicpextras.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import com.lordclockan.R;
import com.lordclockan.aicpextras.SuShell;

public class Helpers {
    // avoids hardcoding the tag
    private static final String TAG = Thread.currentThread().getStackTrace()[1].getClassName();

    public Helpers() {
        // dummy constructor
    }

    public static void restartSystemUI(Context context) {
        new RestartSystemUITask().execute(context);
    }

    public static void showSystemUIrestartDialog(Activity a) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setTitle(R.string.systemui_restart_title);
        builder.setMessage(R.string.systemui_restart_message);
        builder.setPositiveButton(R.string.print_restart,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                RestartSystemUITask task = new RestartSystemUITask() {
                    private ProgressDialog dialog;
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        dialog = new ProgressDialog(a);
                        dialog.setMessage(a.getResources().getString(R.string.restarting_ui));
                        dialog.setCancelable(false);
                        dialog.setIndeterminate(true);
                        dialog.show();
                    }
                    @Override
                    protected Void doInBackground(Context... params) {
                        // Give the user a second to see the dialog
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // Ignore
                        }

                        // Restart the UI
                        super.doInBackground(params);
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void param) {
                        super.onPostExecute(param);
                        dialog.dismiss();
                    }
                };
                task.execute(a);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private static class RestartSystemUITask extends AsyncTask<Context, Void, Void> {
        private Context mContext;
        private Exception mException;
        @Override
        protected Void doInBackground(Context... params) {
            if (params.length > 0) {
                mContext = params[0].getApplicationContext();
            }
            try {
                SuShell.runWithSuCheck("pkill -f com.android.systemui");
            } catch (SuShell.SuDeniedException e) {
                mException = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
            super.onPreExecute();
            if (mException instanceof SuShell.SuDeniedException) {
                if (mContext != null) {
                    Toast.makeText(mContext, mContext.getString(R.string.systemui_restart_missing_su),
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "Could not restart systemui due to missing SU permission");
                }
            }
        }
    }

    public static boolean isPackageInstalled(String packageName, PackageManager pm) {
        try {
            String mVersion = pm.getPackageInfo(packageName, 0).versionName;
            if (mVersion == null) {
                return false;
            }
        } catch (NameNotFoundException notFound) {
            Log.i(TAG, "Package could not be found!", notFound);
            return false;
        }
        return true;
    }

    public static String readStringFromFile(File inputFile ) throws IOException {
            FileReader fileReader = new FileReader(inputFile);
            StringBuffer stringBuffer = new StringBuffer();
            int numCharsRead;
            char[] charArray = new char[1024];
            while ((numCharsRead = fileReader.read(charArray)) > 0) {
                stringBuffer.append(charArray, 0, numCharsRead);
            }
            fileReader.close();
        return stringBuffer.toString();
    }

    /**
    // DelayCallback usage
    int secs = 2; // Delay in seconds
    Helpers.delay(secs, new Utils.DelayCallback() {
        @Override
        public void afterDelay() {
            // Do something after delay
        }
    });
    */

    public interface DelayCallback{
        void afterDelay();
    }

    public static void delay(int secs, final DelayCallback delayCallback){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayCallback.afterDelay();
            }
        }, secs * 1000);
    }
}
