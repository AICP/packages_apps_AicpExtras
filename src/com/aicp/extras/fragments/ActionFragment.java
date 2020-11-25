/*
 * Copyright (C) 2015 TeamEos project
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
 *
 * Handle assignable action dialogs and instances of the ActionPreference
 * class that holds target widget state
 */

package com.aicp.extras.fragments;

import java.util.ArrayList;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.hwkeys.ActionConstants;
import com.android.internal.util.hwkeys.ActionConstants.Defaults;
import com.android.internal.util.hwkeys.ActionHandler;
import com.android.internal.util.hwkeys.Config;
import com.android.internal.util.hwkeys.Config.ActionConfig;
import com.android.internal.util.hwkeys.Config.ButtonConfig;

import com.aicp.extras.preference.ShortcutPickHelper;
import com.aicp.extras.preference.ActionPreference;
import com.aicp.extras.preference.CustomActionListAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.aicp.extras.R;
import com.aicp.extras.BaseSettingsFragment;

public class ActionFragment extends BaseSettingsFragment implements
        ShortcutPickHelper.OnPickListener {

    private static final int DIALOG_CATEGORY = 69;
    private static final int DIALOG_CUSTOM_ACTIONS = 70;
    private static final String KEY_FOCUSED_PREFERENCE = "key_focused_preference";

    private static final String CATEGORY_HOME = "home_key";
    private static final String CATEGORY_MENU = "menu_key";
    private static final String CATEGORY_BACK = "back_key";
    private static final String CATEGORY_ASSIST = "assist_key";
    private static final String CATEGORY_APPSWITCH = "app_switch_key";

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    public static final int KEY_MASK_HOME = 0x01;
    public static final int KEY_MASK_BACK = 0x02;
    public static final int KEY_MASK_MENU = 0x04;
    public static final int KEY_MASK_ASSIST = 0x08;
    public static final int KEY_MASK_APP_SWITCH = 0x10;
    public static final int KEY_MASK_CAMERA = 0x20;
    public static final int KEY_MASK_VOLUME = 0x40;

    private ShortcutPickHelper mPicker;
    protected ArrayList<ActionPreference> mPrefHolder;
    private String mHolderTag;
    private Defaults mDefaults;
    private ArrayList<ButtonConfig> mButtons;
    private ArrayList<ButtonConfig> mDefaultButtons;

    @Override
    protected int getPreferenceResource() {
        return R.xml.hw_button_settings;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            String holderTag = icicle.getString(KEY_FOCUSED_PREFERENCE);
            if (holderTag != null) {
                mHolderTag = holderTag;
            }
        }
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mPicker = new ShortcutPickHelper(getActivity(), this);
        mPrefHolder = new ArrayList<ActionPreference>();

        // bits for hardware keys present on device
        final int deviceKeys = getResources().getInteger(
                com.android.internal.R.integer.config_deviceHardwareKeys);

        // read bits for present hardware keys
        final boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        final boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        final boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        final boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        final boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;

        // load categories and init/remove preferences based on device
        // configuration
        final PreferenceCategory backCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_BACK);
        final PreferenceCategory homeCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_HOME);
        final PreferenceCategory menuCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_MENU);
        final PreferenceCategory assistCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_ASSIST);
        final PreferenceCategory appSwitchCategory =
                (PreferenceCategory) prefScreen.findPreference(CATEGORY_APPSWITCH);

        // back key
        if (!hasBackKey) {
            prefScreen.removePreference(backCategory);
        }

        // home key
        if (!hasHomeKey) {
            prefScreen.removePreference(homeCategory);
        }

        // App switch key (recents)
        if (!hasAppSwitchKey) {
            prefScreen.removePreference(appSwitchCategory);
        }

        // menu key
        if (!hasMenuKey) {
            prefScreen.removePreference(menuCategory);
        }

        // search/assist key
        if (!hasAssistKey) {
            prefScreen.removePreference(assistCategory);
        }

        // let super know we can load ActionPreferences
        onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.HWKEYS));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPicker.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void shortcutPicked(String uri, String friendlyName, boolean isApplication) {
        // activity dialogs pass null here if they are dismissed
        // if null, do nothing, no harm
        if (uri == null) {
            return;
        }
        findAndUpdatePreference(new ActionConfig(getActivity(), uri), mHolderTag);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof ActionPreference) {
            mHolderTag = ((ActionPreference)preference).getTag();
            showDialog(DIALOG_CATEGORY);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDefaults != null) {
            loadAndSetConfigs();
            onActionPolicyEnforced(mPrefHolder);
        }
    }

/*
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mHolderTag != null) {
            outState.putString(KEY_FOCUSED_PREFERENCE, mHolderTag);
        }
    }
*/
    protected void showDialog(int dialogId) {
        switch (dialogId) {
            case DIALOG_CATEGORY: {
                Dialog dialog;
                final DialogInterface.OnClickListener categoryClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        onTargetChange(getResources().getStringArray(R.array.action_dialog_values)[item]);
                        dialog.dismiss();
                    }
                };
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.choose_action_title)
                        .setItems(getResources().getStringArray(R.array.action_dialog_entries),
                                categoryClickListener)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                dialog.show();
                break;
            }
            case DIALOG_CUSTOM_ACTIONS: {
                Dialog dialog;
                final CustomActionListAdapter adapter = new CustomActionListAdapter(getActivity());
                if (!usesExtendedActionsList()) {
                    adapter.removeAction(ActionHandler.SYSTEMUI_TASK_HOME);
                    adapter.removeAction(ActionHandler.SYSTEMUI_TASK_BACK);
                }
                final DialogInterface.OnClickListener customActionClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        findAndUpdatePreference(adapter.getItem(item), mHolderTag);
                        dialog.dismiss();
                    }
                };
                dialog = new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.action_entry_custom_action))
                        .setAdapter(adapter, customActionClickListener)
                        .setNegativeButton(getString(R.string.cancel), null)
                        .create();
                dialog.show();
                break;
            }
        }
    }

    public int getDialogMetricsCategory(int dialogId) {
        switch (dialogId) {
            case DIALOG_CATEGORY:
            case DIALOG_CUSTOM_ACTIONS:
                return MetricsEvent.AICP_METRICS;
            default:
                return 0;
        }
    }

    // subclass overrides to include back and home actions
    protected boolean usesExtendedActionsList() {
        return true;
    }

    protected void onActionPolicyEnforced(ArrayList<ActionPreference> prefs) {
    }

    protected void setActionPreferencesEnabled(boolean enabled) {
        for (ActionPreference pref : mPrefHolder) {
            pref.setEnabled(enabled);
        }
    }

    /**
     * load our button lists and ActionPreferences map button action targets from preference keys
     * and defaults config maps subclass is required to set desired Defaults interface int
     * ActionContants
     */
    protected void onPreferenceScreenLoaded(Defaults defaults) {
        mDefaults = defaults;
        final PreferenceScreen prefScreen = getPreferenceScreen();
        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            Preference pref = prefScreen.getPreference(i);
            if (pref instanceof PreferenceCategory) {
                PreferenceCategory cat = (PreferenceCategory) pref;
                for (int j = 0; j < cat.getPreferenceCount(); j++) {
                    Preference child = cat.getPreference(j);
                    if (child instanceof ActionPreference) {
                        mPrefHolder.add((ActionPreference) child);
                    }
                }
            } else if (pref instanceof ActionPreference) {
                mPrefHolder.add((ActionPreference) pref);
            }
        }
        loadAndSetConfigs();
    }

    protected void loadAndSetConfigs() {
        mButtons = Config.getConfig(getActivity(), mDefaults);
        mDefaultButtons = Config.getDefaultConfig(getActivity(), mDefaults);
        for (ActionPreference pref : mPrefHolder) {
            pref.setDefaults(mDefaults);
            ButtonConfig button = mButtons.get(pref.getConfigMap().button);
            ActionConfig action = button.getActionConfig(pref.getConfigMap().action);
            pref.setActionConfig(action);
            ButtonConfig defButton = mDefaultButtons.get(pref.getConfigMap().button);
            ActionConfig defAction = defButton.getActionConfig(pref.getConfigMap().action);
            pref.setDefaultActionConfig(defAction);
        }
    }

    private void onTargetChange(String uri) {
        if (uri == null) {
            return;
        } else if (uri.equals(getString(R.string.action_value_default_action))) {
            findAndUpdatePreference(null, mHolderTag);
        } else if (uri.equals(getString(R.string.action_value_select_app))) {
            mPicker.pickShortcut(null, null, getId());
        } else if (uri.equals(getString(R.string.action_value_custom_action))) {
            showDialog(DIALOG_CUSTOM_ACTIONS);
        }
    }

    protected void findAndUpdatePreference(ActionConfig action, String tag) {
        for (ActionPreference pref : mPrefHolder) {
            if (pref.getTag().equals(mHolderTag)) {
                if (action == null) {
                    action = pref.getDefaultActionConfig();
                }
                pref.setActionConfig(action);
                ButtonConfig button = mButtons.get(pref.getConfigMap().button);
                ActionConfig newAction = pref.getActionConfig();
                button.setActionConfig(newAction, pref.getConfigMap().action);
                mButtons = Config.replaceButtonAtPosition(mButtons, button, pref.getConfigMap());
                Config.setConfig(getActivity(), mDefaults, mButtons);
                onActionPolicyEnforced(mPrefHolder);
                break;
            }
        }
    }
}
