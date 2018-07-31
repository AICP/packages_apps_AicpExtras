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


package com.aicp.extras;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.aicp.extras.dslv.ActionListViewSettings;
import com.aicp.extras.fragments.Dashboard;
import com.aicp.extras.preference.MasterSwitchPreference;
import com.aicp.extras.preference.MasterSwitchPreferenceDependencyHandler;
import com.aicp.extras.preference.SecureSettingMasterSwitchPreference;
import com.aicp.extras.preference.SecureSettingSwitchBarController;
import com.aicp.extras.preference.SystemSettingMasterSwitchPreference;
import com.aicp.extras.preference.SystemSettingSwitchBarController;
import com.aicp.extras.utils.Util;
import com.aicp.extras.widget.SwitchBar;

public class SettingsActivity extends BaseActivity {

    private Fragment mFragment;
    private SwitchBar mSwitchBar;
    private MasterSwitchPreferenceDependencyHandler mMasterSwitchDependencyHandler;

    // String extra containing the fragment class
    private static final String EXTRA_FRAGMENT_CLASS =
            "com.aicp.extras.extra.preference_fragment";
    // Bundle extra containing arguments for the fragment
    private static final String EXTRA_FRAGMENT_ARGUMENTS =
            "com.aicp.extras.extra.preference_arguments";
    // String extra containing an optional system settings key to be controlled by the switch bar
    private static final String EXTRA_SWITCH_SYSTEM_SETTINGS_KEY =
            "com.aicp.extras.extra.preference_switch_system_settings_key";
    // Default value for switch bar controlling the system setting
    private static final String EXTRA_SWITCH_SYSTEM_SETTINGS_DEFAULT_VALUE =
            "com.aicp.extras.extra.preference_switch_system_settings_default_value";
    // String extra containing an optional secure settings key to be controlled by the switch bar
    private static final String EXTRA_SWITCH_SECURE_SETTINGS_KEY =
            "com.aicp.extras.extra.preference_switch_secure_settings_key";
    // Default value for switch bar controlling the system setting
    private static final String EXTRA_SWITCH_SECURE_SETTINGS_DEFAULT_VALUE =
            "com.aicp.extras.extra.preference_switch_secure_settings_default_value";
    // Mutual exclusive dependencies for master switches
    private static final String EXTRA_SWITCH_SYSTEM_SETTINGS_MUTUAL_KEYS =
            "com.aicp.extras.extra.preference_switch_system_settings_mutual_keys";
    private static final String EXTRA_SWITCH_SECURE_SETTINGS_MUTUAL_KEYS =
            "com.aicp.extras.extra.preference_switch_secure_settings_mutual_keys";
    private static final String EXTRA_SWITCH_THERE_SHOULD_BE_ONE =
            "com.aicp.extras.extra.preference_switch_there_should_be_one";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Intent mIntent = getIntent();

        String fragmentClass = mIntent.getStringExtra(EXTRA_FRAGMENT_CLASS);
        mFragment = getNewFragment(fragmentClass);
        if (mIntent.hasExtra(EXTRA_FRAGMENT_ARGUMENTS)) {
            mFragment.setArguments(mIntent.getBundleExtra(EXTRA_FRAGMENT_ARGUMENTS));
        }
        getFragmentManager().beginTransaction().replace(R.id.main_content, mFragment).commit();

        mMasterSwitchDependencyHandler = new MasterSwitchPreferenceDependencyHandler(this);
        // Add switchbar preferences with reserved grou id -1
        if (mIntent.hasExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_MUTUAL_KEYS)) {
            mMasterSwitchDependencyHandler.addSystemSettingPreferences(-1,
                    mIntent.getStringArrayExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_MUTUAL_KEYS));
        }
        if (mIntent.hasExtra(EXTRA_SWITCH_SECURE_SETTINGS_MUTUAL_KEYS)) {
            mMasterSwitchDependencyHandler.addSecureSettingPreferences(-1,
                    mIntent.getStringArrayExtra(EXTRA_SWITCH_SECURE_SETTINGS_MUTUAL_KEYS));
        }
        boolean thereShouldBeOne = mIntent.getBooleanExtra(EXTRA_SWITCH_THERE_SHOULD_BE_ONE, false);

        mSwitchBar = (SwitchBar) findViewById(R.id.switch_bar);
        if (mIntent.hasExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_KEY)) {
            mSwitchBar.show();
            BaseSettingsFragment settingsFragment = mFragment instanceof BaseSettingsFragment
                    ? (BaseSettingsFragment) mFragment : null;
            new SystemSettingSwitchBarController(mSwitchBar,
                    mIntent.getStringExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_KEY),
                    mIntent.getBooleanExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_DEFAULT_VALUE, false),
                    getContentResolver(),
                    settingsFragment,
                    mMasterSwitchDependencyHandler,
                    thereShouldBeOne);
        } else if (mIntent.hasExtra(EXTRA_SWITCH_SECURE_SETTINGS_KEY)) {
            mSwitchBar.show();
            BaseSettingsFragment settingsFragment = mFragment instanceof BaseSettingsFragment
                    ? (BaseSettingsFragment) mFragment : null;
            new SecureSettingSwitchBarController(mSwitchBar,
                    mIntent.getStringExtra(EXTRA_SWITCH_SECURE_SETTINGS_KEY),
                    mIntent.getBooleanExtra(EXTRA_SWITCH_SECURE_SETTINGS_DEFAULT_VALUE, false),
                    getContentResolver(),
                    settingsFragment,
                    mMasterSwitchDependencyHandler,
                    thereShouldBeOne);
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("is_first_time", true)) {
            firstStartNoRootDialog();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            if (mFragment instanceof TitleProvider) {
                CharSequence title = ((TitleProvider) mFragment).getTitle();
                if (title != null) {
                    actionBar.setTitle(title);
                }
            } else if (mFragment instanceof android.preference.PreferenceFragment) {
                android.preference.PreferenceScreen preferenceScreen =
                        ((android.preference.PreferenceFragment) mFragment).getPreferenceScreen();
                if (preferenceScreen != null) {
                    actionBar.setTitle(preferenceScreen.getTitle());
                }
            } else if (mFragment instanceof android.support.v14.preference.PreferenceFragment) {
                android.support.v7.preference.PreferenceScreen preferenceScreen =
                        ((android.support.v14.preference.PreferenceFragment) mFragment)
                                .getPreferenceScreen();
                if (preferenceScreen != null) {
                    actionBar.setTitle(preferenceScreen.getTitle());
                }
                handleMasterSwitchPreferences(preferenceScreen);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMasterSwitchDependencyHandler.onResume();
    }

    private void handleMasterSwitchPreferences(
                android.support.v7.preference.PreferenceGroup preferenceGroup) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            android.support.v7.preference.Preference pref = preferenceGroup.getPreference(i);
            if (pref instanceof MasterSwitchPreference) {
                mMasterSwitchDependencyHandler.addPreferences((MasterSwitchPreference) pref);
            } else if (pref instanceof android.support.v7.preference.PreferenceGroup) {
                // Recurse
                handleMasterSwitchPreferences((android.support.v7.preference.PreferenceGroup) pref);
            }
        }
    }

    public boolean onPreferenceClick(android.preference.Preference preference) {
        if (preference instanceof android.preference.PreferenceScreen) {
            String fragmentClass = preference.getFragment();
            if (fragmentClass != null) {
                startActivity(new Intent(this, SubSettingsActivity.class)
                        .putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass));
                return true;
            }
        }
        return false;
    }

    public boolean onPreferenceClick(android.support.v7.preference.Preference preference) {
        if (preference instanceof android.support.v7.preference.PreferenceScreen
                || preference instanceof MasterSwitchPreference) {
            String fragmentClass = preference.getFragment();
            if (fragmentClass != null) {
                Intent intent = new Intent(this, SubSettingsActivity.class);
                intent.putExtra(EXTRA_FRAGMENT_CLASS, fragmentClass);

                if (preference instanceof MasterSwitchPreference) {
                    if (fragmentClass.equals(ActionListViewSettings.class.getName())) {
                        // New activity requires setting to be enabled
                        ((MasterSwitchPreference) preference)
                                .setCheckedPersisting(true);
                    } else {
                        if (preference instanceof SystemSettingMasterSwitchPreference) {
                            intent.putExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_KEY, preference.getKey());
                            intent.putExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_DEFAULT_VALUE,
                                    ((SystemSettingMasterSwitchPreference) preference)
                                            .getDefaultValue());
                        }
                        if (preference instanceof SecureSettingMasterSwitchPreference) {
                            intent.putExtra(EXTRA_SWITCH_SECURE_SETTINGS_KEY, preference.getKey());
                            intent.putExtra(EXTRA_SWITCH_SECURE_SETTINGS_DEFAULT_VALUE,
                                    ((SecureSettingMasterSwitchPreference) preference)
                                            .getDefaultValue());
                        }
                        intent.putExtra(EXTRA_SWITCH_THERE_SHOULD_BE_ONE,
                                ((MasterSwitchPreference) preference).getThereShouldBeOneSwitch());
                        int groupId = ((MasterSwitchPreference) preference)
                                .getThereCanBeOnlyOneGroupId();
                        intent.putExtra(EXTRA_SWITCH_SYSTEM_SETTINGS_MUTUAL_KEYS,
                                mMasterSwitchDependencyHandler.getSystemSettingsForGroup(groupId));
                        intent.putExtra(EXTRA_SWITCH_SECURE_SETTINGS_MUTUAL_KEYS,
                                mMasterSwitchDependencyHandler.getSecureSettingsForGroup(groupId));
                    }
                }

                if (preference.peekExtras() != null) {
                    intent.putExtra(EXTRA_FRAGMENT_ARGUMENTS, preference.getExtras());
                }

                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    private Fragment getNewFragment(String fragmentClass) {
        if (fragmentClass != null) {
            try {
                return (Fragment) Class.forName(fragmentClass).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                    ClassCastException e) {
                e.printStackTrace();
            }
        }
        return getDefaultFragment();
    }

    protected Fragment getDefaultFragment() {
        return new Dashboard();
    }

    private void firstStartNoRootDialog() {
        if (Util.hasSu()) {
            // We are rooted, full functionality available
            return;
        }
        String title = getResources().getString(R.string.no_root_title);
        String message = getResources().getString(R.string.no_root_summary);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // No need to show again
                            PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this)
                                    .edit().putBoolean("is_first_time", false).apply();
                        }
                })
                .show();
    }
}
