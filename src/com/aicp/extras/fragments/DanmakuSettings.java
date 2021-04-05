package com.aicp.extras.fragments;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;

import com.aicp.extras.R;
import com.aicp.extras.BaseSettingsFragment;
import com.aicp.gear.preference.AicpPreferenceFragment;

import android.provider.Settings;

import java.util.Arrays;
import java.util.HashSet;

public class DanmakuSettings extends BaseSettingsFragment implements
        OnPreferenceChangeListener {

    @Override
    protected int getPreferenceResource() {
        return R.xml.settings_gaming_danmaku;
    }


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.settings_gaming_danmaku);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return false;
    }

}
