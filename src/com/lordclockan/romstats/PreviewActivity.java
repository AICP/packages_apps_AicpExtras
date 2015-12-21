/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lordclockan.romstats;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;


import com.lordclockan.R;

public class PreviewActivity extends PreferenceActivity {

    private static final String UNIQUE_ID = "preview_id";
    private static final String DEVICE = "preview_device";
    private static final String VERSION = "preview_version";
    private static final String BUILDTYPE = "preview_buildtype";
    private static final String COUNTRY = "preview_country";
    private static final String CARRIER = "preview_carrier";
    private static final String ROMNAME = "preview_romname";
    private static final String ROMVERSION = "preview_romversion";



	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPermissionToReadPhoneState();

		addPreferencesFromResource(R.xml.preview_data);
		
		final PreferenceScreen prefSet = getPreferenceScreen();
		final Context context = getApplicationContext();

        prefSet.findPreference(UNIQUE_ID).setSummary(Utilities.getUniqueID(context));
        prefSet.findPreference(DEVICE).setSummary(Utilities.getDevice());
        prefSet.findPreference(VERSION).setSummary(Utilities.getModVersion());
        prefSet.findPreference(BUILDTYPE).setSummary(Utilities.getBuildType());
        prefSet.findPreference(COUNTRY).setSummary(Utilities.getCountryCode(context));
        prefSet.findPreference(CARRIER).setSummary(Utilities.getCarrier(context));
        prefSet.findPreference(ROMNAME).setSummary(Utilities.getRomName());
        prefSet.findPreference(ROMVERSION).setSummary(Utilities.getRomVersion());

	}

	private static final int READ_PHONE_STATE_PERMISSIONS_REQUEST = 1;

	 public void getPermissionToReadPhoneState() {

	     if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
	             != PackageManager.PERMISSION_GRANTED) {

	        if (shouldShowRequestPermissionRationale(
	                Manifest.permission.READ_PHONE_STATE)) {

                }

               requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                       READ_PHONE_STATE_PERMISSIONS_REQUEST);

            }
       }

       @Override
       public void onRequestPermissionsResult(int requestCode,
		       @NonNull String permissions[],
		       @NonNull int[] grantResults) {

	       if (requestCode ==  READ_PHONE_STATE_PERMISSIONS_REQUEST) {
		if (grantResults.length == 1 &&
			grantResults[0] == PackageManager.PERMISSION_GRANTED) {
		    Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();
		} else {
		    Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
		}
	       } else {
		  super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	     }
	}


  /* Last Bracket */
  }
