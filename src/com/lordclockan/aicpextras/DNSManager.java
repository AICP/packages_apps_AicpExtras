/*
 * Copyright (c) 2015 Bruno Parmentier. This file is part of DNSSetter.
 *
 * DNSSetter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DNSSetter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DNSSetter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.lordclockan.aicpextras;


import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DNSManager {
    private static final String TAG = DNSManager.class.getSimpleName();
    private Context ctx;


    public static void setDNS(String dns[]) {
            List<String> cmd = new ArrayList<>();
            for (int i = 1; i <= 2; i++) {
                SuShell.runWithSu("setprop net.dns" + i + " " + dns[i - 1]);
            }

    }

    public static List<String>  getDNS() {
        List<String> cmd = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
             cmd.add("getprop net.dns" + i);
        }
	     return cmd;
    }
}
