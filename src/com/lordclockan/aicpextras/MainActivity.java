package com.lordclockan.aicpextras;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.System;

import com.lordclockan.R;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String INTENT_EXTRA_INIT_FRAGMENT = "init_fragment";
    public static final String INIT_FRAGMENT_HALO = "halo";

    private static final String NAV_ITEM_ID = "navItemId";
    static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private int id;
    private long startTime;
    private boolean secondBack=false;
    private boolean restartRequired = false;

    private View mView;

    @Override
    protected void onStart() {
	    super.onStart();

	    new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                setProgressBarIndeterminateVisibility(true);
            }

            @Override
            protected Boolean doInBackground(Void... params) {
	            try {
                    boolean canGainSu = SuShell.canGainSu(getApplicationContext());
                    return canGainSu;
                } catch (Exception e) {
                    Log.e(TAG, "Error: " + e.getMessage(), e);
                    Snackbar.make(mView, R.string.cannot_get_su_start,
                            Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                    return true; // I want to start the app regardles of having root or not
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                setProgressBarIndeterminateVisibility(false);
                if (!result) {
                    Snackbar.make(mView, R.string.cannot_get_su,
                            Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                    finish();
                }
            }
        }.execute();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mView = (View) findViewById(R.id.drawer_layout);

        Fragment fragment;
        String title = null;
        String fragmentExtra = getIntent().getStringExtra(INTENT_EXTRA_INIT_FRAGMENT);
        /*if (INIT_FRAGMENT_HALO.equals(fragmentExtra)) {
            fragment = new HaloFragment();
            title = getString(R.string.halo_settings_title);
        } else {
            if (title != null) {
                Log.w(TAG, "Unknown init fragment: " + fragmentExtra);
            }*/
            fragment = new AboutFragment();
        //}

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(R.id.content_main, fragment);
        tx.commit();
        if (title != null) {
            setTitle(title);
        }

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent emailIntent =
                        new Intent(Intent.ACTION_SEND);
                String[] recipients = new String[]{"davor@losinj.com", "",};
                emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "AICP talk");
                emailIntent.setType("text/plain");
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail_intent)));
                finish();
            }
        });*/

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = setupDrawerToggle();
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        int navBgColor = getResources().getColor(R.color.navDrawerBg, null);

        if (Settings.System.getInt(getApplicationContext().getContentResolver(),
                Settings.System.AE_CUSTOM_COLORS, 0) != 0) {
            // Use colors from settings
            navBgColor = Settings.System.getInt(this.getApplicationContext().getContentResolver(),
                Settings.System.AE_NAV_DRAWER_BG_COLOR, navBgColor);
        }

        navigationView.setBackgroundColor(navBgColor);

        navigationView.getBackground().setAlpha(Settings.System.getInt(this.
                getApplicationContext().getContentResolver(),
                Settings.System.AE_NAV_DRAWER_OPACITY, 178));

        navigationView.getHeaderView(0).findViewById(R.id.nav_header_layout).
                getBackground().setAlpha(Settings.System.getInt(this.
                getApplicationContext().getContentResolver(),
                Settings.System.AE_NAV_HEADER_BG_IMAGE_OPACITY, 255));

        navigationView.setItemTextColor(navDrawerItemColor());
        navigationView.setItemIconTintList(navDrawerItemColor());

   }

    @Override
    public void onResume() {
        super.onResume();

        if (restartRequired) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            /*
            **If back was pressed the first time,only show snackbar
            **else exit the app provided the user presses back
            **the second time within 2.5 seconds of the first
            */
            if(!secondBack) {
            Snackbar snackbar = Snackbar
            .make(mView, R.string.app_exit_toast, Snackbar.LENGTH_SHORT)
            .setAction("Action", null);
            View sbView = snackbar.getView();
            sbView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(getResources().getColor(R.color.primary_text_material_dark));
            snackbar.show();
            secondBack=!secondBack;
            startTime = System.currentTimeMillis();
            } else if( System.currentTimeMillis()-startTime < 2500)
            super.onBackPressed();
            else {
            secondBack=!secondBack;
            onBackPressed();
           }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivity(intent);
            restartRequired = true;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        // update highlighted item in the navigation menu
        item.setChecked(true);
        id = item.getItemId();
        Fragment fragment = null;

        Class fragmentClass;

        switch (id) {
            case R.id.nav_display_animations:
                fragmentClass = DisplayAnimationsActivity.class;
                break;
            case R.id.nav_statusbar:
                fragmentClass = StatusBarFragment.class;
                break;
            case R.id.nav_notif_drawer:
                fragmentClass = NotificationsFragment.class;
                break;
            case R.id.nav_log_it:
                fragmentClass = LogThatShitFragment.class;
                break;
            case R.id.nav_about:
                fragmentClass = AboutFragment.class;
                break;
            default:
                fragmentClass = AboutFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RelativeLayout contentMain = (RelativeLayout) findViewById(R.id.content_main);
        contentMain.removeAllViewsInLayout();
        contentMain.invalidate();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_main, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        item.setChecked(true);
        setTitle(item.getTitle());
        mDrawer.closeDrawers();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, id);
    }

    private ColorStateList navDrawerItemColor() {
        int checkedColor = getResources().getColor(R.color.navDrawerTextChecked, null);
        int uncheckedColor = getResources().getColor(R.color.navDrawerText, null);
        int defaultColor = uncheckedColor;
        if (Settings.System.getInt(getApplicationContext().getContentResolver(),
                Settings.System.AE_CUSTOM_COLORS, 0) != 0) {
            // Use colors from settings
            checkedColor = Settings.System.getInt(this.getApplicationContext().getContentResolver(),
                    Settings.System.AE_NAV_DRAWER_CHECKED_TEXT, checkedColor);
            uncheckedColor = Settings.System.getInt(this.getApplicationContext().getContentResolver(),
                    Settings.System.AE_NAV_DRAWER_UNCHECKED_TEXT, uncheckedColor);
        }

        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},  // unchecked
                new int[]{android.R.attr.state_checked},   // checked
                new int[]{}                                // default
        };

        int[] colors = new int[]{
                uncheckedColor,
                checkedColor,
                defaultColor,
        };

        ColorStateList navigationViewColorStateList = new ColorStateList(states, colors);

        return navigationViewColorStateList;
    }
}
