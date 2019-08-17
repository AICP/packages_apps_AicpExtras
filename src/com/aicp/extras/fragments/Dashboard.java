/*
 * Copyright (C) 2017-2018 AICP
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
import androidx.preference.Preference;
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
import com.aicp.gear.preference.LongClickablePreference;
import com.aicp.extras.utils.Util;

import com.plattysoft.leonids.ParticleSystem;

import java.util.Random;

public class Dashboard extends BaseSettingsFragment {

    private static final String PREF_AICP_LOGO = "aicp_logo";
    private static final String PREF_AICP_OTA = "aicp_ota";
    private static final String PREF_LOG_IT = "log_it";
    private static final String PREF_WEATHER = "weather_option";

    private static final Intent INTENT_OTA = new Intent().setComponent(new ComponentName(
            Constants.AICP_OTA_PACKAGE, Constants.AICP_OTA_ACTIVITY));

    private LongClickablePreference mAicpLogo;
    private Preference mAicpOTA;
    private Preference mWeatherOption;

    private Random mRandom = new Random();
    private int mLogoClickCount = 0;

    @Override
    protected int getPreferenceResource() {
        return R.xml.dashboard;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager pm = getActivity().getPackageManager();

        mAicpLogo = (LongClickablePreference) findPreference(PREF_AICP_LOGO);

        mWeatherOption = findPreference(PREF_WEATHER);
        if (!Util.isPackageInstalled(Constants.WEATHER_SERVICE_PACKAGE, pm)) {
            mWeatherOption.getParent().removePreference(mWeatherOption);
        }

        mAicpOTA = findPreference(PREF_AICP_OTA);
        if (!Util.isPackageEnabled(Constants.AICP_OTA_PACKAGE, pm)) {
            mAicpOTA.getParent().removePreference(mAicpOTA);
        }

        Preference logIt = findPreference(PREF_LOG_IT);
        Util.requireRoot(getActivity(), logIt);

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

                mAicpLogo.setLongClickBurst(2000/((++mLogoClickCount)%5+1));
                return true;
            }
        });
        mAicpLogo.setOnLongClickListener(R.id.logo_view, 1000,
                new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            int firstRandom = mRandom.nextInt(91 - 0);
                            int secondRandom = mRandom.nextInt(181 - 90) + 90;
                            int thirdRandom = mRandom.nextInt(181 - 0);

                            Drawable star =
                                    getResources().getDrawable(R.drawable.star_alternative, null);

                            ParticleSystem ps = new ParticleSystem(getActivity(), 100, star, 3000);
                            ps.setScaleRange(0.7f, 1.3f);
                            ps.setSpeedRange(0.1f, 0.25f);
                            ps.setAcceleration(0.0001f, thirdRandom);
                            ps.setRotationSpeedRange(firstRandom, secondRandom);
                            ps.setFadeOut(1000, new AccelerateInterpolator());
                            ps.oneShot(getView(), 100);
                            return true;
                        }
                });
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mAicpOTA || preference == mAicpLogo) {
            startActivity(INTENT_OTA);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }
}
