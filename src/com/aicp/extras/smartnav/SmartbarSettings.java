/*
 * Copyright (C) 2016 The DirtyUnicorns Project
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

package com.aicp.extras.smartnav;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.utils.ActionConstants;
import com.android.internal.utils.ActionHandler;
import com.android.internal.utils.ActionUtils;
import com.android.internal.utils.Config;
import com.android.internal.utils.Config.ButtonConfig;
import com.aicp.extras.BaseSettingsFragment;
import com.aicp.extras.R;

import com.aicp.gear.preference.SecureSettingIntListPreference;
import com.aicp.gear.preference.SecureSettingSeekBarPreference;

public class SmartbarSettings extends BaseSettingsFragment {
    private SecureSettingIntListPreference mSmartBarContext;
    private SecureSettingIntListPreference mImeActions;
    private SecureSettingIntListPreference mButtonAnim;
    private SecureSettingIntListPreference mButtonLongpressDelay;
    private SecureSettingSeekBarPreference mSmartBarCustomIconSize;
    private SecureSettingSeekBarPreference mButtonsAlpha;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static final int MENU_RESTORE = Menu.FIRST + 2;

    private static final int DIALOG_RESET_CONFIRM = 1;
    private static final int DIALOG_RESTORE_PROFILE = 2;
    private static final int DIALOG_SAVE_PROFILE = 3;
    private static final String CONFIG_STORAGE = Environment.getExternalStorageDirectory()
            + File.separator
            + "smartbar_configs";
    private static final String SMARTBAR_CONFIGS_PREFIX = "smartbar_config_";
    private static final String KEY_SMARTBAR_BACKUP = "smartbar_profile_save";
    private static final String KEY_SMARTBAR_RESTORE = "smartbar_profile_restore";
    private static final String PREF_NAVBAR_BUTTONS_ALPHA = "navbar_buttons_alpha";
    private static final String PREF_SMARTBAR_CUSTOM_ICON_SIZE =
                                      "smartbar_custom_icon_size";
    private static final String PREF_SMARTBAR_LONGPRESS_DELAY =
                                      "smartbar_longpress_delay";
    private static final String PREF_SMARTBAR_BUTTON_ANIMATION_STYLE =
                                      "smartbar_button_animation_style";
    private static final String PREF_SMARTBAR_IME_HINT_MODE =
                                      "smartbar_ime_hint_mode";
    private static final String PREF_SMARTBAR_CONTEXT_MENU_MODE =
                                      "smartbar_context_menu_mode";

    @Override
    protected int getPreferenceResource() {
        return R.xml.smartbar_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSmartBarContext = (SecureSettingIntListPreference) findPreference(
                          PREF_SMARTBAR_CONTEXT_MENU_MODE);
        mImeActions = (SecureSettingIntListPreference) findPreference(
                          PREF_SMARTBAR_IME_HINT_MODE);
        mButtonAnim = (SecureSettingIntListPreference) findPreference(
                          PREF_SMARTBAR_BUTTON_ANIMATION_STYLE);
        mButtonLongpressDelay = (SecureSettingIntListPreference) findPreference(
                          PREF_SMARTBAR_LONGPRESS_DELAY);
        mSmartBarCustomIconSize = (SecureSettingSeekBarPreference) findPreference(
                          PREF_SMARTBAR_CUSTOM_ICON_SIZE);
        mButtonsAlpha = (SecureSettingSeekBarPreference) findPreference(
                          PREF_NAVBAR_BUTTONS_ALPHA);

        setHasOptionsMenu(true);
    }

    private void showDialog(int dialogId) {
        onCreateDialog(dialogId).show();
    }

    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case DIALOG_RESET_CONFIRM: {
                Dialog dialog;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.smartbar_factory_reset_title);
                alertDialog.setMessage(R.string.smartbar_factory_reset_confirm);
                alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetSmartbar();
                    }
                });
                alertDialog.setNegativeButton(android.R.string.cancel, null);
                dialog = alertDialog.create();
                return dialog;
            }
            case DIALOG_RESTORE_PROFILE: {
                Dialog dialog;
                final ConfigAdapter configAdapter = new ConfigAdapter(getActivity(),
                        getConfigFiles(CONFIG_STORAGE));
                AlertDialog.Builder configDialog = new AlertDialog.Builder(getActivity());
                configDialog.setTitle(R.string.smartbar_config_dialog_title);
                configDialog.setNegativeButton(getString(android.R.string.cancel), null);
                configDialog.setAdapter(configAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String resultMsg;
                        try {
                            File configFile = (File) configAdapter.getItem(item);
                            String config = getSmartbarConfigFromStorage(configFile);
                            restoreConfig(getActivity(), config);
                            resultMsg = getString(R.string.smartbar_config_restore_success_toast);
                        } catch (Exception e) {
                            resultMsg = getString(R.string.smartbar_config_restore_error_toast);
                        }
                        Toast.makeText(getActivity(), resultMsg, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog = configDialog.create();
                return dialog;
            }
            case DIALOG_SAVE_PROFILE: {
                Dialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                final EditText input = new EditText(getActivity());
                builder.setTitle(getString(R.string.smartbar_config_name_edit_dialog_title));
                builder.setMessage(R.string.smartbar_config_name_edit_dialog_message);
                builder.setView(input);
                builder.setNegativeButton(getString(android.R.string.cancel), null);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputText = input.getText().toString();
                                if (TextUtils.isEmpty(inputText)) {
                                    inputText = String.valueOf(android.text.format.DateFormat
                                            .format(
                                                    "yyyy-MM-dd_hh:mm:ss", new java.util.Date()));
                                }
                                String resultMsg;
                                try {
                                    String currentConfig = getCurrentConfig(getActivity());
                                    backupSmartbarConfig(currentConfig, inputText);
                                    resultMsg = getString(R.string.smartbar_config_backup_success_toast);
                                } catch (Exception e) {
                                    resultMsg = getString(R.string.smartbar_config_backup_error_toast);
                                }
                                Toast.makeText(getActivity(), resultMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                return dialog;
            }
        }
        return null;//super.onCreateDialog(dialogId);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == findPreference("smartbar_editor_mode")) {
            ActionHandler.performTask(getActivity(), ActionHandler.SYSTEMUI_TASK_EDITING_SMARTBAR);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_SAVE, 0, R.string.smartbar_backup_current_config_title)
                .setIcon(R.drawable.ic_smartbar_save_profile)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_RESTORE, 0, R.string.smartbar_restore_config_title)
                .setIcon(R.drawable.ic_smartbar_restore_profile)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialog(DIALOG_RESET_CONFIRM);
                return true;
            case MENU_SAVE:
                showDialog(DIALOG_SAVE_PROFILE);
                return true;
            case MENU_RESTORE:
                showDialog(DIALOG_RESTORE_PROFILE);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetSmartbar() {
        ArrayList<ButtonConfig> buttonConfigs = Config.getDefaultConfig(
                getActivity(),
                ActionConstants.getDefaults(ActionConstants.SMARTBAR));
        Config.setConfig(getActivity(),
                ActionConstants.getDefaults(ActionConstants.SMARTBAR),
                buttonConfigs);
        Intent intent = new Intent(ActionUtils.INTENT_NAVBAR_EDIT_RESET_LAYOUT);
        ActionHandler.dispatchNavigationEditorResult(intent);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.SMARTBAR_CONTEXT_MENU_MODE, 0);
        mSmartBarContext.setValue(String.valueOf(0));

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.SMARTBAR_IME_HINT_MODE, 0);
        mImeActions.setValue(String.valueOf(0));

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.SMARTBAR_BUTTON_ANIMATION_STYLE, 0);
        mButtonAnim.setValue(String.valueOf(0));

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.NAVBAR_BUTTONS_ALPHA, 255);
        mButtonsAlpha.setValue(255);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.SMARTBAR_LONGPRESS_DELAY, 0);
        mButtonLongpressDelay.setValue(String.valueOf(0));

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.SMARTBAR_CUSTOM_ICON_SIZE, 60);
        mSmartBarCustomIconSize.setValue(60);
    }

    static class ConfigAdapter extends ArrayAdapter<File> {
        private final ArrayList<File> mConfigFiles;
        private final Context mContext;

        public ConfigAdapter(Context context, ArrayList<File> files) {
            super(context, android.R.layout.select_dialog_item, files);
            mContext = context;
            mConfigFiles = files;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View itemRow = convertView;
            File f = mConfigFiles.get(position);
            itemRow = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(android.R.layout.select_dialog_item, null);
            String name = f.getName();
            if (name.startsWith(SMARTBAR_CONFIGS_PREFIX)) {
                name = f.getName().substring(SMARTBAR_CONFIGS_PREFIX.length(), f.getName().length());
            }
            ((TextView) itemRow.findViewById(android.R.id.text1)).setText(name);

            return itemRow;
        }
    }

    private static class StartsWithFilter implements FileFilter {
        private String[] mStartsWith;

        public StartsWithFilter(String[] startsWith) {
            mStartsWith = startsWith;
        }

        @Override
        public boolean accept(File file) {
            for (String extension : mStartsWith) {
                if (file.getName().toLowerCase().startsWith(extension)) {
                    return true;
                }
            }
            return false;
        }
    }

    static String getCurrentConfig(Context ctx) {
        String config = Settings.Secure.getStringForUser(
                ctx.getContentResolver(), ActionConstants.getDefaults(ActionConstants.SMARTBAR)
                        .getUri(),
                UserHandle.USER_CURRENT);
        if (TextUtils.isEmpty(config)) {
            config = ActionConstants.getDefaults(ActionConstants.SMARTBAR).getDefaultConfig();
        }
        return config;
    }

    static void restoreConfig(Context context, String config) {
        Settings.Secure.putStringForUser(context.getContentResolver(),
                ActionConstants.getDefaults(ActionConstants.SMARTBAR)
                        .getUri(), config,
                UserHandle.USER_CURRENT);
        Intent intent = new Intent("intent_navbar_edit_reset_layout");
        ActionHandler.dispatchNavigationEditorResult(intent);
    }

    static void backupSmartbarConfig(String config, String suffix) {
        File dir = new File(CONFIG_STORAGE);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File configFile = new File(dir, SMARTBAR_CONFIGS_PREFIX + suffix);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(configFile);
            stream.write(config.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getSmartbarConfigFromStorage(File file) {
        int length = (int) file.length();
        byte[] bytes = new byte[length];
        FileInputStream in;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String contents = new String(bytes);
        return contents;
    }

    public static ArrayList<File> getConfigFiles(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ArrayList<File> list = new ArrayList<File>();
        for (File tmp : dir.listFiles(new StartsWithFilter(new String[] {
                SMARTBAR_CONFIGS_PREFIX
        }))) {
            list.add(tmp);
        }
        return list;
    }
}
