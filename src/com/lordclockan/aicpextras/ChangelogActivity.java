package com.lordclockan.aicpextras;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

//import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

//import io.fabric.sdk.android.Fabric;

import com.lordclockan.R;

public class ChangelogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_changelog);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Changelog");
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.CollapsedAppBar);

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageDrawable(getResources().getDrawable(R.drawable.aicp_wall));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.changelog);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        ArrayList<ChangelogItem> changeLogArray = new ArrayList<>();

        String CHANGELOG_PATH = "/system/etc/Changelog.txt";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH);
            Date date;
            Date nowDate = new Date();
            BufferedReader reader = new BufferedReader(new FileReader(CHANGELOG_PATH));
            String line;
            String directory = "";
            String commits = "";
            boolean checknext = false;
            while ((line = reader.readLine()) != null) {
                if (!line.matches("={20}") && !Objects.equals(line.trim(), "")) {
                    if (line.matches("     (\\d\\d\\-\\d\\d\\-\\d{4})")) {//it's date
                        date = sdf.parse(line.trim());
                        long now = nowDate.getTime();
                        long time = date.getTime();
                        final long diff = now - time;
                        String timeString;
                        if (diff < 1000 * 60 * 60 * 24) {
                            timeString = "Today";
                        } else if (diff < 1000 * 60 * 60 * 24 * 2) {
                            timeString = "Yesterday";
                        } else if (diff < 1000 * 60 * 60 * 24 * 3) {
                            timeString = "Two days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 3) {
                            timeString = "Three days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 4) {
                            timeString = "Four days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 5) {
                            timeString = "Five days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 6) {
                            timeString = "Six days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 7) {
                            timeString = "A week ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 14) {
                            timeString = "Two weeks ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 21) {
                            timeString = "Three weeks ago";
                        } else {
                            timeString = line.trim().replaceAll("-", "/");
                        }
                        changeLogArray.add(new ChangelogItem(timeString));
                    } else if (line.matches("^\\s*(   \\* )\\S*")) {//it's directory
                        if (checknext) {
                            commits = commits.substring(0, commits.lastIndexOf("\n\n"));//remove lf on end
                            changeLogArray.add(new ChangelogItem(directory, commits));
                            commits = ""; //reset commits
                            checknext = false;
                        } else {
                            checknext = true;
                            commits = "";
                        }
                        directory = line.replaceAll("(   \\* )", "");
                    } else {
                        commits += line.substring(8, line.length()) + "\n\n";
                        checknext = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ChangeLogAdapter adapter = new ChangeLogAdapter(this, changeLogArray);
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}
