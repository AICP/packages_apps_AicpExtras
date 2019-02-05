/*
 * Copyright (C) 2014 TeamEos
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

import com.android.internal.utils.ActionConstants;
import com.android.internal.utils.ActionHandler;
import com.android.internal.utils.ActionUtils;
import com.android.internal.utils.Config.ButtonConfig;

import com.aicp.extras.R;
import com.aicp.extras.smartnav.ActionPreference;
import com.aicp.extras.smartnav.IconPickHelper;
import com.aicp.extras.smartnav.IconPickHelper.OnPickListener;

import com.aicp.gear.preference.SecureSettingColorPickerPreference;
import com.aicp.gear.preference.SecureSettingSeekBarPreference;
import com.aicp.gear.preference.SecureSettingSwitchPreference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
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

public class FlingSettings extends ActionFragment implements
        Preference.OnPreferenceChangeListener, IconPickHelper.OnPickListener {
    private static final String TAG = FlingSettings.class.getSimpleName();
    public static final String FLING_LOGO_URI = "fling_custom_icon_config";

    private static final int MENU_RESET = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static final int MENU_RESTORE = Menu.FIRST + 2;

    private static final int DIALOG_RESET_CONFIRM = 1;
    private static final int DIALOG_RESTORE_PROFILE = 2;
    private static final int DIALOG_SAVE_PROFILE = 3;
    private static final String CONFIG_STORAGE = Environment.getExternalStorageDirectory()
            + File.separator
            + "fling_configs";
    private static final String FLING_CONFIGS_PREFIX = "fling_config_";
    private static final String KEY_FLING_BACKUP = "fling_profile_save";
    private static final String KEY_FLING_RESTORE = "fling_profile_restore";

    private static final String PREF_FLING_KEYBOARD_CURSORS = "fling_keyboard_cursors";
    private static final String PREF_FLING_LOGO_ANIMATES = "fling_logo_animates";
    private static final String PREF_FLING_LOGO_OPACITY = "fling_logo_opacity";
    private static final String PREF_FLING_LOGO_VISIBLE = "fling_logo_visible";
    private static final String PREF_FLING_RIPPLE_ENABLED = "fling_ripple_enabled";
    private static final String PREF_FLING_RIPPLE_COLOR = "fling_ripple_color";
    private static final String PREF_FLING_TRAILS_ENABLED = "fling_trails_enabled";
    private static final String PREF_FLING_TRAILS_COLOR = "fling_trails_color";
    private static final String PREF_FLING_TRAILS_WIDTH = "fling_trails_width";
    private static final String PREF_FLING_LONGPRESS_TIMEOUT = "fling_longpress_timeout";
    private static final String PREF_FLING_LONGSWIPE_RIGHT_PORT = "fling_longswipe_threshold_right_port";
    private static final String PREF_FLING_LONGSWIPE_LEFT_PORT = "fling_longswipe_threshold_left_port";
    private static final String PREF_FLING_LONGSWIPE_RIGHT_LAND = "fling_longswipe_threshold_right_land";
    private static final String PREF_FLING_LONGSWIPE_LEFT_LAND = "fling_longswipe_threshold_left_land";
    private static final String PREF_FLING_LONGSWIPE_UP_LAND = "fling_longswipe_threshold_up_land";
    private static final String PREF_FLING_LONGSWIPE_DOWN_LAND = "fling_longswipe_threshold_down_land";

    private static final String PREF_FLING_CUSTOM_LOGO_PICK = "fling_custom_logo_pick";
    private static final String PREF_FLING_CUSTOM_LOGO_GALLERY_PICK = "fling_custom_logo_gallery_pick";
    private static final String PREF_FLING_CUSTOM_LOGO_RESET = "fling_custom_logo_reset";

    private static final String KEY_LONG_SWIPE_CATEGORY = "eos_long_swipe_category";

    Context mContext;
    IconPickHelper mIconPickHelper;
    boolean mIsTablet;

    SecureSettingSwitchPreference mShowLogo;
    SecureSettingSwitchPreference mAnimateLogo;
    SecureSettingSwitchPreference mShowRipple;
    SecureSettingSwitchPreference mTrailsEnabled;
    SecureSettingSwitchPreference mKbCursors;

    SecureSettingSeekBarPreference mTrailsWidth;
    SecureSettingSeekBarPreference mLongPressTimeout;

    SecureSettingSeekBarPreference mLogoOpacity;

    SecureSettingSeekBarPreference mSwipePortRight;
    SecureSettingSeekBarPreference mSwipePortLeft;
    SecureSettingSeekBarPreference mSwipeLandRight;
    SecureSettingSeekBarPreference mSwipeLandLeft;
    SecureSettingSeekBarPreference mSwipeVertUp;
    SecureSettingSeekBarPreference mSwipeVertDown;

    SecureSettingColorPickerPreference mRippleColor;
    SecureSettingColorPickerPreference mTrailsColor;

    @Override
    protected int getPreferenceResource() {
        return R.xml.fling_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = (Context) getActivity();
        mIconPickHelper = new IconPickHelper(getActivity(), this);

        mShowLogo = (SecureSettingSwitchPreference) findPreference(PREF_FLING_LOGO_VISIBLE);
        mAnimateLogo = (SecureSettingSwitchPreference) findPreference(PREF_FLING_LOGO_ANIMATES);
        mShowRipple = (SecureSettingSwitchPreference) findPreference(PREF_FLING_RIPPLE_ENABLED);
        mTrailsEnabled = (SecureSettingSwitchPreference) findPreference(PREF_FLING_TRAILS_ENABLED);
        mKbCursors = (SecureSettingSwitchPreference) findPreference(PREF_FLING_KEYBOARD_CURSORS);

        mLogoOpacity = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LOGO_OPACITY);
        mTrailsWidth = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_TRAILS_WIDTH);
        // NOTE: we display to the user actual timeouts starting from touch event
        // but framework wants the value less tap timeout, which is 100ms
        // so we always write 100ms less but display 100ms more
        mLongPressTimeout = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LONGPRESS_TIMEOUT);
        int val = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_LONGPRESS_TIMEOUT, 250, UserHandle.USER_CURRENT);
        val += 100;
        mLongPressTimeout.setValue(val);
        mLongPressTimeout.setOnPreferenceChangeListener(this);

        int rippleColor = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_RIPPLE_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mRippleColor = (SecureSettingColorPickerPreference) findPreference(PREF_FLING_RIPPLE_COLOR);
        mRippleColor.setNewPreviewColor(rippleColor);

        int trailsColor = Settings.Secure.getIntForUser(getContentResolver(),
                Settings.Secure.FLING_TRAILS_COLOR, Color.WHITE, UserHandle.USER_CURRENT);
        mTrailsColor = (SecureSettingColorPickerPreference) findPreference(PREF_FLING_TRAILS_COLOR);
        mTrailsColor.setNewPreviewColor(trailsColor);

        mIsTablet = !ActionUtils.navigationBarCanMove();

        mSwipePortRight = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LONGSWIPE_RIGHT_PORT);
        val = Settings.Secure.getIntForUser(
                getContentResolver(), Settings.Secure.FLING_LONGSWIPE_THRESHOLD_RIGHT_PORT,
                mIsTablet ? 30 : 40, UserHandle.USER_CURRENT);
        mSwipePortRight.setValue(val);

        mSwipePortLeft = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LONGSWIPE_LEFT_PORT);
        val = Settings.Secure.getIntForUser(
                getContentResolver(), Settings.Secure.FLING_LONGSWIPE_THRESHOLD_LEFT_PORT,
                mIsTablet ? 30 : 40, UserHandle.USER_CURRENT);
        mSwipePortLeft.setValue(val);

        mSwipeLandRight = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LONGSWIPE_RIGHT_LAND);
        mSwipeLandLeft = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LONGSWIPE_LEFT_LAND);
        mSwipeVertUp = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LONGSWIPE_UP_LAND);
        mSwipeVertDown = (SecureSettingSeekBarPreference) findPreference(PREF_FLING_LONGSWIPE_DOWN_LAND);

        PreferenceCategory longSwipeCategory = (PreferenceCategory) getPreferenceScreen()
                .findPreference(KEY_LONG_SWIPE_CATEGORY);

        if (mIsTablet) {
            longSwipeCategory.removePreference(mSwipeVertUp);
            longSwipeCategory.removePreference(mSwipeVertDown);
            val = Settings.Secure.getIntForUser(
                    getContentResolver(), Settings.Secure.FLING_LONGSWIPE_THRESHOLD_RIGHT_LAND,
                    25, UserHandle.USER_CURRENT);
            mSwipeLandRight.setValue(val);

            val = Settings.Secure.getIntForUser(
                    getContentResolver(), Settings.Secure.FLING_LONGSWIPE_THRESHOLD_LEFT_LAND,
                    25, UserHandle.USER_CURRENT);
            mSwipeLandLeft.setValue(val);
        } else {
            longSwipeCategory.removePreference(mSwipeLandRight);
            longSwipeCategory.removePreference(mSwipeLandLeft);
            val = Settings.Secure.getIntForUser(
                    getContentResolver(), Settings.Secure.FLING_LONGSWIPE_THRESHOLD_UP_LAND,
                    40, UserHandle.USER_CURRENT);
            mSwipeVertUp.setValue(val);

            val = Settings.Secure.getIntForUser(
                    getContentResolver(), Settings.Secure.FLING_LONGSWIPE_THRESHOLD_DOWN_LAND,
                    40, UserHandle.USER_CURRENT);
            mSwipeVertDown.setValue(val);
        }

        onPreferenceScreenLoaded(ActionConstants.getDefaults(ActionConstants.FLING));

        setHasOptionsMenu(true);
    }

    @Override
    protected void showDialog(int dialogId) {
        onCreateDialog(dialogId).show();
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case DIALOG_RESET_CONFIRM: {
                Dialog dialog;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.fling_factory_reset_title);
                alertDialog.setMessage(R.string.fling_factory_reset_confirm);
                alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetFling();
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
                configDialog.setTitle(R.string.fling_config_dialog_title);
                configDialog.setNegativeButton(getString(android.R.string.cancel), null);
                configDialog.setAdapter(configAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String resultMsg;
                        try {
                            File configFile = (File) configAdapter.getItem(item);
                            String config = getFlingConfigFromStorage(configFile);
                            restoreConfig(getActivity(), config);
                            loadAndSetConfigs();
                            onActionPolicyEnforced(mPrefHolder);
                            resultMsg = getString(R.string.fling_config_restore_success_toast);
                        } catch (Exception e) {
                            resultMsg = getString(R.string.fling_config_restore_error_toast);
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
                builder.setTitle(getString(R.string.fling_config_name_edit_dialog_title));
                builder.setMessage(R.string.fling_config_name_edit_dialog_message);
                builder.setView(input);
                builder.setNegativeButton(getString(android.R.string.cancel), null);
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputText = input.getText().toString();
                                if (TextUtils.isEmpty(inputText)) {
                                    inputText = String.valueOf(android.text.format.DateFormat
                                            .format("yyyy-MM-dd_hh:mm:ss", new java.util.Date()));
                                }
                                String resultMsg;
                                try {
                                    String currentConfig = getCurrentConfig(getActivity());
                                    backupFlingConfig(currentConfig, inputText);
                                    resultMsg = getString(R.string.fling_config_backup_success_toast);
                                } catch (Exception e) {
                                    resultMsg = getString(R.string.fling_config_backup_error_toast);
                                }
                                Toast.makeText(getActivity(), resultMsg, Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                return dialog;
            }
            default: {
                return super.onCreateDialog(dialogId);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_SAVE, 0, R.string.fling_backup_current_config_title)
                .setIcon(R.drawable.ic_fling_save_profile)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_RESTORE, 0, R.string.fling_restore_config_title)
                .setIcon(R.drawable.ic_fling_restore_profile)
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

    private void resetFling() {
        restoreConfig(getActivity(), getDefaultConfig(getActivity()));
        loadAndSetConfigs();
        onActionPolicyEnforced(mPrefHolder);

        ButtonConfig logoConfig = ButtonConfig.getButton(mContext, FLING_LOGO_URI, true);
        logoConfig.clearCustomIconIconUri();
        ButtonConfig.setButton(mContext, logoConfig, FLING_LOGO_URI, true);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LOGO_VISIBLE, 1);
        mShowLogo.setChecked(true);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LOGO_ANIMATES, 1);
        mAnimateLogo.setChecked(true);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_RIPPLE_ENABLED, 1);
        mShowRipple.setChecked(true);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_RIPPLE_COLOR, Color.WHITE);
        mRippleColor.setNewPreviewColor(Color.WHITE);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LONGPRESS_TIMEOUT, 250);
        mLongPressTimeout.setValue(250+100);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LONGSWIPE_THRESHOLD_RIGHT_PORT, mIsTablet ? 30 : 40);
        mSwipePortRight.setValue(mIsTablet ? 30 : 40);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LONGSWIPE_THRESHOLD_LEFT_PORT, mIsTablet ? 30 : 40);
        mSwipePortLeft.setValue(mIsTablet ? 30 : 40);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LONGSWIPE_THRESHOLD_RIGHT_LAND, 25);
        mSwipeLandRight.setValue(25);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LONGSWIPE_THRESHOLD_LEFT_LAND, 25);
        mSwipeLandLeft.setValue(25);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LONGSWIPE_THRESHOLD_UP_LAND, 40);
        mSwipeVertUp.setValue(40);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LONGSWIPE_THRESHOLD_DOWN_LAND, 40);
        mSwipeVertDown.setValue(40);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_KEYBOARD_CURSORS, 1);
        mKbCursors.setChecked(true);

        Settings.Secure.putInt(getContentResolver(),
                Settings.Secure.FLING_LOGO_OPACITY, 255);
        mLogoOpacity.setValue(255);
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
            if (name.startsWith(FLING_CONFIGS_PREFIX)) {
                name = f.getName().substring(FLING_CONFIGS_PREFIX.length(), f.getName().length());
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
                ctx.getContentResolver(), ActionConstants.getDefaults(ActionConstants.FLING)
                        .getUri(),
                UserHandle.USER_CURRENT);
        if (TextUtils.isEmpty(config)) {
            config = getDefaultConfig(ctx);
        }
        return config;
    }

    static String getDefaultConfig(Context ctx) {
        return ActionConstants.getDefaults(ActionConstants.FLING).getDefaultConfig();
    }

    static void restoreConfig(Context context, String config) {
        Settings.Secure.putStringForUser(context.getContentResolver(),
                ActionConstants.getDefaults(ActionConstants.FLING)
                        .getUri(), config,
                UserHandle.USER_CURRENT);
    }

    static void backupFlingConfig(String config, String suffix) {
        File dir = new File(CONFIG_STORAGE);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File configFile = new File(dir, FLING_CONFIGS_PREFIX + suffix);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(configFile);
            stream.write(config.getBytes());
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getFlingConfigFromStorage(File file) {
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
                FLING_CONFIGS_PREFIX
        }))) {
            list.add(tmp);
        }
        return list;
    }

    @Override
    public void iconPicked(String iconType, String iconPackage, String iconName) {
        if (TextUtils.isEmpty(iconType)
                || TextUtils.isEmpty(iconPackage)
                || TextUtils.isEmpty(iconName)) {
            return;
        }
        ButtonConfig logoConfig = ButtonConfig.getButton(mContext, FLING_LOGO_URI, true);
        logoConfig.setCustomIconUri(iconType, iconPackage, iconName);
        ButtonConfig.setButton(mContext, logoConfig, FLING_LOGO_URI, true);
    }

    @Override
    public void imagePicked(Uri uri) {
        if (uri != null) {
            ButtonConfig logoConfig = ButtonConfig.getButton(mContext, FLING_LOGO_URI, true);
            logoConfig.setCustomImageUri(uri);
            ButtonConfig.setButton(mContext, logoConfig, FLING_LOGO_URI, true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mIconPickHelper.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == findPreference(PREF_FLING_CUSTOM_LOGO_PICK)) {
            mIconPickHelper.pickIcon(getId(), IconPickHelper.REQUEST_PICK_ICON_PACK);
            return true;
        } else if (preference == findPreference(PREF_FLING_CUSTOM_LOGO_RESET)) {
            ButtonConfig logoConfig = ButtonConfig.getButton(mContext, FLING_LOGO_URI, true);
            logoConfig.clearCustomIconIconUri();
            ButtonConfig.setButton(mContext, logoConfig, FLING_LOGO_URI, true);
            return true;
        } else if (preference == findPreference(PREF_FLING_CUSTOM_LOGO_GALLERY_PICK)) {
            mIconPickHelper.pickIcon(getId(), IconPickHelper.REQUEST_PICK_ICON_GALLERY);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mLongPressTimeout) {
            int val = (Integer) newValue;
            val -= 100;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.FLING_LONGPRESS_TIMEOUT, val, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    protected boolean usesExtendedActionsList() {
        return true;
    }

    protected void onActionPolicyEnforced(ArrayList<ActionPreference> prefs) {
        enforceAction(prefs, ActionHandler.SYSTEMUI_TASK_BACK);
        enforceAction(prefs, ActionHandler.SYSTEMUI_TASK_HOME);
    }

    /*
     * Iterate the list: if only one instance, disable it otherwise, enable
     */
    private void enforceAction(ArrayList<ActionPreference> prefs, String action) {
        ArrayList<ActionPreference> actionPrefs = new ArrayList<ActionPreference>();
        for (ActionPreference pref : prefs) {
            if (pref.getActionConfig().getAction().equals(action)) {
                actionPrefs.add(pref);
            }
        }
        boolean moreThanOne = actionPrefs.size() > 1;
        for (ActionPreference pref : actionPrefs) {
            pref.setEnabled(moreThanOne);
        }
    }
}
