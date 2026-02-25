/*
 * Copyright (C) 2017-2026 AICP
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
package com.aicp.extras.fragments

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.preference.Preference
import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.Constants
import com.aicp.extras.R
import com.aicp.extras.utils.Util
import com.aicp.gear.preference.LongClickablePreference
import com.plattysoft.leonids.ParticleSystem
import java.util.Random

class Dashboard : BaseSettingsFragment() {

    companion object {
        private const val PREF_AICP_LOGO = "aicp_logo"
        private const val PREF_AICP_OTA = "aicp_ota"
        private const val PREF_LOG_IT = "log_it"

        private val INTENT_OTA = Intent().setComponent(
            ComponentName(
                Constants.AICP_OTA_PACKAGE,
                Constants.AICP_OTA_ACTIVITY
            )
        )
    }

    private lateinit var aicpLogo: LongClickablePreference
    private lateinit var aicpOTA: Preference

    private val random = Random()
    private var logoClickCount = 0

    override fun getPreferenceResource(): Int = R.xml.dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pm: PackageManager = requireActivity().packageManager

        aicpLogo = findPreference(PREF_AICP_LOGO)!!
        aicpOTA = findPreference(PREF_AICP_OTA)!!

        if (!Util.isPackageEnabled(Constants.AICP_OTA_PACKAGE, pm)) {
            aicpOTA.parent?.removePreference(aicpOTA)
        }

        val logIt = findPreference<Preference>(PREF_LOG_IT)
        Util.requireRoot(requireActivity(), logIt)

        setupLogoClick()
        setupLogoLongClick()
    }

    private fun setupLogoClick() {
        aicpLogo.setOnPreferenceClickListener {

            val firstRandom = random.nextInt(91)
            val secondRandom = random.nextInt(91) + 90
            val thirdRandom = random.nextInt(181)

            val star: Drawable =
                requireContext().getDrawable(R.drawable.star_white_border)!!

            val randomColor = Color.rgb(
                Color.red(random.nextInt(0xFFFFFF)),
                Color.green(random.nextInt(0xFFFFFF)),
                Color.blue(random.nextInt(0xFFFFFF))
            )
            star.setTint(randomColor)

            val ps = ParticleSystem(requireActivity(), 100, star, 3000)
            ps.setScaleRange(0.7f, 1.3f)
            ps.setSpeedRange(0.1f, 0.25f)
            ps.setAcceleration(0.0001f, thirdRandom)
            ps.setRotationSpeedRange(firstRandom.toFloat(), secondRandom.toFloat())            
            ps.setFadeOut(200, AccelerateInterpolator())
            ps.oneShot(view, 100)

            aicpLogo.setLongClickBurst(2000 / ((++logoClickCount) % 5 + 1))
            true
        }
    }

    private fun setupLogoLongClick() {
        aicpLogo.setOnLongClickListener(
            R.id.logo_view,
            1000
        ) {

            val firstRandom = random.nextInt(91)
            val secondRandom = random.nextInt(91) + 90
            val thirdRandom = random.nextInt(181)

            val star: Drawable =
                requireContext().getDrawable(R.drawable.star_alternative)!!

            val ps = ParticleSystem(requireActivity(), 100, star, 3000)
            ps.setScaleRange(0.7f, 1.3f)
            ps.setSpeedRange(0.1f, 0.25f)
            ps.setAcceleration(0.0001f, thirdRandom)
            ps.setRotationSpeedRange(firstRandom.toFloat(), secondRandom.toFloat())            
            ps.setFadeOut(1000, AccelerateInterpolator())
            ps.oneShot(view, 100)

            true
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        return if (preference == aicpOTA || preference == aicpLogo) {
            startActivity(INTENT_OTA)
            true
        } else {
            super.onPreferenceTreeClick(preference)
        }
    }
}

