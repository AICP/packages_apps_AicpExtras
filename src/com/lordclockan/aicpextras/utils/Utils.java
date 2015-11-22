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
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

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
}
