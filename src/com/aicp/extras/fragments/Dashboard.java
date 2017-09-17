/*
 * Copyright (C) 2017 AICP
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


package com.aicp.extras.fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.Constants;
import com.aicp.extras.PreferenceMultiClickHandler;
import com.aicp.extras.R;
import com.aicp.extras.utils.Util;

import com.plattysoft.leonids.ParticleSystem;

import java.util.Random;

public class Dashboard extends BaseSettingsFragment {

    private static final String PREF_AICP_LOGO = "aicp_logo";
    private static final String PREF_AICP_OTA = "aicp_ota";
    private static final String PREF_LOG_IT = "log_it";

    private Preference mAicpLogo;
    private Preference mAicpOTA;

    private static final Intent INTENT_OTA = new Intent().setComponent(new ComponentName(
            Constants.AICP_OTA_PACKAGE, Constants.AICP_OTA_ACTIVITY));

    private Random mRandom = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.dashboard);

        mAicpLogo = findPreference(PREF_AICP_LOGO);
        mAicpOTA = findPreference(PREF_AICP_OTA);

        PackageManager pm = getActivity().getPackageManager();
        if (!Util.isPackageEnabled(Constants.AICP_OTA_PACKAGE, pm)) {
            mAicpOTA.getParent().removePreference(mAicpOTA);
        }


        Preference logIt = findPreference(PREF_LOG_IT);
        Util.requireRoot(logIt);


        mAicpLogo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int firstRandom = mRandom.nextInt(91 - 0);
                int secondRandom = mRandom.nextInt(181 - 90) + 90;
                int thirdRandom = mRandom.nextInt(181 - 0);

                // Let's color the star randomly
                Drawable star = getResources().getDrawable(R.drawable.star_white_border, null);
                int randomColor;
                randomColor = Color.rgb(
                        Color.red(mRandom.nextInt(0xFFFFFF)),
                        Color.green(mRandom.nextInt(0xFFFFFF)),
                        Color.blue(mRandom.nextInt(0xFFFFFF)));
                star.setTint(randomColor);

                ParticleSystem ps = new ParticleSystem(getActivity(), 100, star, 3000);
                ps.setScaleRange(0.7f, 1.3f);
                ps.setSpeedRange(0.1f, 0.25f);
                ps.setAcceleration(0.0001f, thirdRandom);
                ps.setRotationSpeedRange(firstRandom, secondRandom);
                ps.setFadeOut(200, new AccelerateInterpolator());
                ps.oneShot(getView(), 100);
                return true;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Preference long click
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ((ListView) view.findViewById(android.R.id.list)).setOnItemLongClickListener(
                new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                if (position == 0) {// mAicpLogo
                    int firstRandom = mRandom.nextInt(91 - 0);
                    int secondRandom = mRandom.nextInt(181 - 90) + 90;
                    int thirdRandom = mRandom.nextInt(181 - 0);

                    Drawable star = getResources().getDrawable(R.drawable.star_alternative, null);

                    ParticleSystem ps = new ParticleSystem(getActivity(), 100, star, 3000);
                    ps.setScaleRange(0.7f, 1.3f);
                    ps.setSpeedRange(0.1f, 0.25f);
                    ps.setAcceleration(0.0001f, thirdRandom);
                    ps.setRotationSpeedRange(firstRandom, secondRandom);
                    ps.setFadeOut(1000, new AccelerateInterpolator());
                    ps.oneShot(getView(), 100);
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mAicpOTA || preference == mAicpLogo) {
            startActivity(INTENT_OTA);
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }
}
