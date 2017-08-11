package com.lordclockan.aicpextras;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.IThemeCallback;
import android.app.ThemeManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.view.MenuItem;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.Utils;

public class SubActivity extends Activity {

    public static final String EXTRA_TITLE =
            "com.lordclockan.aicpextras.SubActivity.title";

    public static final String EXTRA_FRAGMENT_CLASS =
            "com.lordclockan.aicpextras.SubActivity.fragment_class";

    private int mTheme;

    private ThemeManager mThemeManager;
    private final IThemeCallback mThemeCallback = new IThemeCallback.Stub() {

        @Override
        public void onThemeChanged(int themeMode, int color) {
            onCallbackAdded(themeMode, color);
            SubActivity.this.runOnUiThread(() -> {
                SubActivity.this.recreate();
            });
        }

        @Override
        public void onCallbackAdded(int themeMode, int color) {
            mTheme = color;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final int themeMode = Secure.getInt(getContentResolver(),
                Secure.THEME_PRIMARY_COLOR, 0);
        final int accentColor = Secure.getInt(getContentResolver(),
                Secure.THEME_ACCENT_COLOR, 0);
        mThemeManager = (ThemeManager) getSystemService(Context.THEME_SERVICE);
        if (mThemeManager != null) {
            mThemeManager.addCallback(mThemeCallback);
        }
        if (themeMode != 0 || accentColor != 0) {
            getTheme().applyStyle(mTheme, true);
            getTheme().applyStyle(R.style.PreferenceThemeAddition, true);
        }
        if (themeMode == 2) {
            getTheme().applyStyle(R.style.settings_pixel_theme, true);
        }

        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String fragmentExtra = getIntent().getStringExtra(EXTRA_FRAGMENT_CLASS);
        if (fragmentExtra != null  && !fragmentExtra.isEmpty()) {
            try {
                Class<?> fragmentClass = Class.forName(fragmentExtra);
                Fragment fragment = (Fragment) fragmentClass.newInstance();
                getFragmentManager().beginTransaction()
                        .replace(android.R.id.content, fragment).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null  && !title.isEmpty()) {
            setTitle(title);
        }

        Utils.enableLanguageFilter(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
