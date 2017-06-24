/*
 * Copyright 2014 ParanoidAndroid Project
 * Copyright 2015 AICP Project
 *
 * This file is part of Paranoid OTA.
 *
 * Paranoid OTA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paranoid OTA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paranoid OTA.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lordclockan.aicpextras.utils;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.lordclockan.R;

public class Utils {

    private static Properties sDictionary;
    public static final String PROPERTY_DEVICE = "ro.aicp.device";
    public static final String PROPERTY_DEVICE_EXT = "ro.product.device";

    public static String getDevice(Context context) {
        String device = getProp(PROPERTY_DEVICE);
        if (device == null || device.isEmpty()) {
            device = getProp(PROPERTY_DEVICE_EXT);
            device = translateDeviceName(context, device);
        }
        return device == null ? "" : device.toLowerCase();
   }

   public static String getProp(String prop) {
       try {
           Process process = Runtime.getRuntime().exec("getprop " + prop);
           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                   process.getInputStream()));
           StringBuilder log = new StringBuilder();
           String line;
           while ((line = bufferedReader.readLine()) != null) {
              log.append(line);
           }
           return log.toString();
       } catch (IOException e) {
           // Runtime error
       }
    return null;
    }

    public static String translateDeviceName(Context context, String device) {
        Properties dictionary = getDictionary(context);
        String translate = dictionary.getProperty(device);
        if (translate == null) {
            translate = device;
            String[] remove = dictionary.getProperty("@remove").split(",");
            for (int i = 0; i < remove.length; i++) {
                if (translate.indexOf(remove[i]) >= 0) {
                    translate = translate.replace(remove[i], "");
                    break;
                }
            }
        }
        return translate;
    }

    public static Properties getDictionary(Context context) {
        if (sDictionary == null) {
            sDictionary = new Properties();
            try {
                sDictionary.load(context.getAssets().open("dictionary.properties"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sDictionary;
    }

    /**
     * Returns whether the device is voice-capable (meaning, it is also a phone).
     */
    public static boolean isVoiceCapable(Context context) {
        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony != null && telephony.isVoiceCapable();
    }

    public static boolean isWifiOnly(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
    }


    public static boolean supportsLanguageFilter(Context context) {
        return context.getResources().getStringArray(R.array.ae_inappropriate_wordings).length > 0;
    }

    public static void enableLanguageFilter(Activity activity) {
        if (Settings.System.getInt(activity.getContentResolver(), Settings.System.AE_MODERATE_LANGUAGE, 0) == 1 &&
                supportsLanguageFilter(activity)) {
            filterLanguage(activity, activity.findViewById(android.R.id.content).getRootView(), true);
        }
    }

    private static void filterLanguage(Context context, View view, boolean withListener) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            String[] array1 = context.getResources().getStringArray(R.array.ae_inappropriate_wordings);
            String[] array2 = context.getResources().getStringArray(R.array.ae_inappropriate_wordings_replacements);
            String text1 = textView.getText().toString();
            String text2 = text1;
            for (int i = 0; i < array1.length; i++) {
                text2 = text2.replaceAll(array1[i], array2[i]);
            }
            if (!text1.equals(text2)) {
                textView.setText(text2);
            }
            if (withListener) {
                removeFilterListener(textView);
                textView.addTextChangedListener(new FilterListener(context, textView));
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                filterLanguage(context, viewGroup.getChildAt(i), withListener);
            }
            if (withListener) {
                Log.d("filterLanguage", "setting onHierarchyChangeListener; developer warning: " +
                        "this might overwrite an already set listener!");
                viewGroup.setOnHierarchyChangeListener(new FilterListener(context, viewGroup));
            }
        }
    }

    private static class FilterListener implements TextWatcher, ViewGroup.OnHierarchyChangeListener {
        private Context mContext;
        private View mView;
        private boolean mAlwaysSame;
        public FilterListener(Context context, View v) {
            mContext = context;
            mView = v;
            mAlwaysSame = false;
        }
        public static FilterListener getFakeFilterListener() {
            // Return a filter listener that can be used to remove others
            FilterListener listener = new FilterListener(null, null);
            listener.mAlwaysSame = true;
            return listener;
        }
        @Override
        public void afterTextChanged(Editable s) {
            filterLanguage(mContext, mView, false);
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void onChildViewAdded(View parent, View child) {
            filterLanguage(mContext, child, true);
        }
        @Override
        public void onChildViewRemoved(View parent, View child) {
            removeFilterListener(child);
        }
        @Override
        public boolean equals(Object o) {
            if (mAlwaysSame && o instanceof FilterListener) {
                return true;
            } else {
                return super.equals(o);
            }
        }
    };

    private static void removeFilterListener(View v) {
        if (v instanceof TextView) {
            ((TextView) v).removeTextChangedListener(FilterListener.getFakeFilterListener());
        }
        if (v instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v;
            viewGroup.setOnHierarchyChangeListener(null);
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                removeFilterListener(viewGroup.getChildAt(i));
            }
        }
    }

    /**
     * This can not reliably detect whether the user has root access,
     * but it can detect some cases when the user hasn't.
     */
    public static boolean hasSu() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(new String[] { "which", "su" });
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return br.readLine() != null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (p != null) p.destroy();
        }
        return false;
    }
}
