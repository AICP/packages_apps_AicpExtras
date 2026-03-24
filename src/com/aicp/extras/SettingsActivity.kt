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
package com.aicp.extras

import android.app.ActionBar
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import com.android.settingslib.widget.MainSwitchBar
import com.aicp.extras.dslv.ActionListViewSettings
import com.aicp.extras.fragments.Dashboard
import com.aicp.extras.preference.*
import com.aicp.extras.search.PartsList
import com.aicp.extras.utils.Util
import com.aicp.gear.preference.*

const val AE_FRAGMENT_ACTION_PREFIX =
    "com.aicp.extras.fragmentaction"

open class SettingsActivity :
    CollapsingToolbarBaseActivity(),
    PreferenceFragment.OnPreferenceStartFragmentCallback,
    PreferenceFragment.OnPreferenceStartScreenCallback {

    protected var mFragment: Fragment? = null
    private var mSwitchBar: MainSwitchBar? = null
    private lateinit var mMasterSwitchDependencyHandler: MasterSwitchPreferenceDependencyHandler

    companion object {

        private const val EXTRA_FRAGMENT_CLASS =
            PreferenceActivity.EXTRA_SHOW_FRAGMENT
        const val EXTRA_PREFERENCE_KEY = "preference_key"
        private const val EXTRA_FRAGMENT_ARGUMENTS =
            PreferenceActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS

        private const val EXTRA_SWITCH_SYSTEM_SETTINGS_KEY =
            "com.aicp.extras.extra.preference_switch_system_settings_key"
        private const val EXTRA_SWITCH_SYSTEM_SETTINGS_DEFAULT_VALUE =
            "com.aicp.extras.extra.preference_switch_system_settings_default_value"

        private const val EXTRA_SWITCH_SECURE_SETTINGS_KEY =
            "com.aicp.extras.extra.preference_switch_secure_settings_key"
        private const val EXTRA_SWITCH_SECURE_SETTINGS_DEFAULT_VALUE =
            "com.aicp.extras.extra.preference_switch_secure_settings_default_value"

        private const val EXTRA_SWITCH_GLOBAL_SETTINGS_KEY =
            "com.aicp.extras.extra.preference_switch_global_settings_key"
        private const val EXTRA_SWITCH_GLOBAL_SETTINGS_DEFAULT_VALUE =
            "com.aicp.extras.extra.preference_switch_global_settings_default_value"

        private const val EXTRA_SWITCH_SYSTEM_SETTINGS_MUTUAL_KEYS =
            "com.aicp.extras.extra.preference_switch_system_settings_mutual_keys"
        private const val EXTRA_SWITCH_SECURE_SETTINGS_MUTUAL_KEYS =
            "com.aicp.extras.extra.preference_switch_secure_settings_mutual_keys"
        private const val EXTRA_SWITCH_GLOBAL_SETTINGS_MUTUAL_KEYS =
            "com.aicp.extras.extra.preference_switch_global_settings_mutual_keys"

        private const val EXTRA_SWITCH_THERE_SHOULD_BE_ONE =
            "com.aicp.extras.extra.preference_switch_there_should_be_one"

        private const val FRAGMENT_TAG = "SettingsActivity.pref_fragment"

        const val EXTRA_SHOW_FRAGMENT = ":settings:show_fragment"
        const val EXTRA_SHOW_FRAGMENT_ARGUMENTS = ":settings:show_fragment_args"
        const val EXTRA_SHOW_FRAGMENT_TITLE = ":settings:show_fragment_title"
        const val EXTRA_SHOW_FRAGMENT_TITLE_RESID =
            ":settings:show_fragment_title_resid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val intent = intent

        mFragment = supportFragmentManager.findFragmentByTag(FRAGMENT_TAG)

        if (mFragment == null) {
            val action = intent.action
            val fragmentClass = if (action != null &&
                action.startsWith(AE_FRAGMENT_ACTION_PREFIX)
            ) {
                action.substring(AE_FRAGMENT_ACTION_PREFIX.length + 1)
            } else {
                intent.getStringExtra(EXTRA_FRAGMENT_CLASS)
            }

            mFragment = getNewFragment(fragmentClass)

            val arguments = intent.getBundleExtra(EXTRA_FRAGMENT_ARGUMENTS) ?: Bundle()

            intent.getStringExtra(EXTRA_PREFERENCE_KEY)?.let {
                arguments.putString(EXTRA_PREFERENCE_KEY, it)
            }

            if (this !is HiddenAnimActivity && savedInstanceState == null) {
                mFragment?.arguments = arguments
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_content, mFragment!!, FRAGMENT_TAG)
                    .commit()
            }
        }

        mMasterSwitchDependencyHandler =
            MasterSwitchPreferenceDependencyHandler(this)

        intent.getStringArrayExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_MUTUAL_KEYS)
            ?.let {
                mMasterSwitchDependencyHandler
                    .addSystemSettingPreferences(-1, *it)
            }

        intent.getStringArrayExtra(EXTRA_SWITCH_SECURE_SETTINGS_MUTUAL_KEYS)
            ?.let {
                mMasterSwitchDependencyHandler
                    .addSecureSettingPreferences(-1, *it)
            }

        intent.getStringArrayExtra(EXTRA_SWITCH_GLOBAL_SETTINGS_MUTUAL_KEYS)
            ?.let {
                mMasterSwitchDependencyHandler
                    .addGlobalSettingPreferences(-1, *it)
            }

        val thereShouldBeOne =
            intent.getBooleanExtra(EXTRA_SWITCH_THERE_SHOULD_BE_ONE, false)

        mSwitchBar = findViewById(R.id.main_switch_bar)

        val settingsFragment = mFragment as? BaseSettingsFragment

        when {
            intent.hasExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_KEY) -> {
                mSwitchBar?.show()
                settingsFragment?.let { fragment ->
                    SystemSettingSwitchBarController(
                        mSwitchBar!!,
                        intent.getStringExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_KEY)!!,
                        intent.getBooleanExtra(
                            EXTRA_SWITCH_SYSTEM_SETTINGS_DEFAULT_VALUE,
                            false
                        ),
                        contentResolver,
                        settingsFragment,
                        mMasterSwitchDependencyHandler,
                        thereShouldBeOne
                    )
                }
            }
            intent.hasExtra(EXTRA_SWITCH_SECURE_SETTINGS_KEY) -> {
                mSwitchBar?.show()
                settingsFragment?.let { fragment ->
                    SecureSettingSwitchBarController(
                        mSwitchBar!!,
                        intent.getStringExtra(EXTRA_SWITCH_SECURE_SETTINGS_KEY)!!,
                        intent.getBooleanExtra(
                            EXTRA_SWITCH_SECURE_SETTINGS_DEFAULT_VALUE,
                            false
                        ),
                        contentResolver,
                        settingsFragment,
                        mMasterSwitchDependencyHandler,
                        thereShouldBeOne
                    )
                }
            }
            intent.hasExtra(EXTRA_SWITCH_GLOBAL_SETTINGS_KEY) -> {
                mSwitchBar?.show()
                settingsFragment?.let { fragment ->
                    GlobalSettingSwitchBarController(
                        mSwitchBar!!,
                        intent.getStringExtra(EXTRA_SWITCH_GLOBAL_SETTINGS_KEY)!!,
                        intent.getBooleanExtra(
                            EXTRA_SWITCH_GLOBAL_SETTINGS_DEFAULT_VALUE,
                            false
                        ),
                        contentResolver,
                        settingsFragment,
                        mMasterSwitchDependencyHandler,
                        thereShouldBeOne
                    )
                }
            }
        }
        val sharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(this)

        if (sharedPreferences.getBoolean("is_first_time", true)) {
            firstStartNoRootDialog()
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragment,
        pref: Preference
    ): Boolean {
        startPreferencePanel(
            pref.fragment,
            pref.extras,
            -1,
            pref.title,
            null,
            0
        )
        return true
    }

    override fun onPreferenceStartScreen(
        caller: PreferenceFragment,
        pref: PreferenceScreen
    ): Boolean {
        startPreferencePanel(
            pref.fragment,
            pref.extras,
            -1,
            pref.title,
            null,
            0
        )
        return true
    }

    override fun onStart() {
        super.onStart()

        when (mFragment) {
            is TitleProvider -> {
                val title = (mFragment as TitleProvider).getTitle()
                if (title != null) this.title = title
            }
            is PreferenceFragmentCompat -> {
                val screen =
                    (mFragment as PreferenceFragmentCompat).preferenceScreen
                if (screen != null) this.title = screen.title
                handleMasterSwitchPreferences(screen)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mMasterSwitchDependencyHandler.onResume()
    }

    fun onPreferenceClick(preference: Preference): Boolean {
        val fragmentClass = preference.fragment

        if (checkClassAvailable(fragmentClass)) {
            val intent = Intent(this, SubSettingsActivity::class.java)
            intent.putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass)

            if (preference is MasterSwitchPreference) {

                if (fragmentClass == ActionListViewSettings::class.java.name) {
                    preference.setCheckedPersisting(true)
                } else {

                    when (preference) {
                        is SystemSettingMasterSwitchPreference -> {
                            intent.putExtra(
                                EXTRA_SWITCH_SYSTEM_SETTINGS_KEY,
                                preference.key
                            )
                            intent.putExtra(
                                EXTRA_SWITCH_SYSTEM_SETTINGS_DEFAULT_VALUE,
                                preference.defaultValue
                            )
                        }

                        is SecureSettingMasterSwitchPreference -> {
                            intent.putExtra(
                                EXTRA_SWITCH_SECURE_SETTINGS_KEY,
                                preference.key
                            )
                            intent.putExtra(
                                EXTRA_SWITCH_SECURE_SETTINGS_DEFAULT_VALUE,
                                preference.defaultValue
                            )
                        }

                        is GlobalSettingMasterSwitchPreference -> {
                            intent.putExtra(
                                EXTRA_SWITCH_GLOBAL_SETTINGS_KEY,
                                preference.key
                            )
                            intent.putExtra(
                                EXTRA_SWITCH_GLOBAL_SETTINGS_DEFAULT_VALUE,
                                preference.defaultValue
                            )
                        }
                    }

                    intent.putExtra(
                        EXTRA_SWITCH_THERE_SHOULD_BE_ONE,
                        preference.thereShouldBeOneSwitch
                    )

                    val groupId = preference.thereCanBeOnlyOneGroupId

                    intent.putExtra(
                        EXTRA_SWITCH_SYSTEM_SETTINGS_MUTUAL_KEYS,
                        mMasterSwitchDependencyHandler.getSystemSettingsForGroup(groupId)
                    )

                    intent.putExtra(
                        EXTRA_SWITCH_SECURE_SETTINGS_MUTUAL_KEYS,
                        mMasterSwitchDependencyHandler.getSecureSettingsForGroup(groupId)
                    )

                    intent.putExtra(
                        EXTRA_SWITCH_GLOBAL_SETTINGS_MUTUAL_KEYS,
                        mMasterSwitchDependencyHandler.getGlobalSettingsForGroup(groupId)
                    )
                }
            }

            preference.extras?.let {
                intent.putExtra(EXTRA_FRAGMENT_ARGUMENTS, it)
            }

            startActivity(intent)
            return true
        }

        return false
    }

    private fun handleMasterSwitchPreferences(
        preferenceGroup: androidx.preference.PreferenceGroup?
    ) {
        preferenceGroup ?: return

        for (i in 0 until preferenceGroup.preferenceCount) {
            val pref = preferenceGroup.getPreference(i)
            when (pref) {
                is MasterSwitchPreference ->
                    mMasterSwitchDependencyHandler.addPreferences(pref)
                is androidx.preference.PreferenceGroup ->
                    handleMasterSwitchPreferences(pref)
            }
        }
    }

    private fun checkClassAvailable(fragmentClass: String?): Boolean {
        return if (fragmentClass != null) {
            try {
                Class.forName(fragmentClass)
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        } else {
            false
        }
    }

    fun startPreferencePanel(
        fragmentClass: String?,
        args: Bundle?,
        titleRes: Int,
        titleText: CharSequence?,
        resultTo: Fragment?,
        resultRequestCode: Int
    ) {
        val intent = Intent()
        intent.component = PartsList.AE_ACTIVITY
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentClass)
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args)
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE_RESID, titleRes)
        intent.putExtra(EXTRA_SHOW_FRAGMENT_TITLE, titleText)

        if (resultTo == null) {
            startActivity(intent)
        } else {
            resultTo.startActivityForResult(intent, resultRequestCode)
        }
    }

    protected fun getDefaultFragment(): Fragment = Dashboard()

    private fun getNewFragment(fragmentClass: String?): Fragment {
        if (fragmentClass != null) {
            try {
                return Class.forName(fragmentClass)
                    .newInstance() as Fragment
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return getDefaultFragment()
    }

    private fun firstStartNoRootDialog() {
        if (Util.hasSu()) return

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_root_title))
            .setMessage(getString(R.string.no_root_summary))
            .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean("is_first_time", false)
                    .apply()
            }
            .show()
    }
}
