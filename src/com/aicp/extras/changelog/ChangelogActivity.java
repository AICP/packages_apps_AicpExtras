package com.aicp.extras.changelog;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

//import com.crashlytics.android.Crashlytics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

//import io.fabric.sdk.android.Fabric;

import com.aicp.extras.R;

public class ChangelogActivity extends AppCompatActivity {

    private static final String CHANGELOG_PATH = "/system/etc/Changelog.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.changelog_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.changelog_name);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.changelog);
        recyclerView.setHasFixedSize(true);
        ArrayList<ChangelogItem> changeLogArray = new ArrayList<>();

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
                        } else if (diff < 1000 * 60 * 60 * 24 * 4) {
                            timeString = "Three days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 5) {
                            timeString = "Four days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 6) {
                            timeString = "Five days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 7) {
                            timeString = "Six days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 8) {
                            timeString = "A week ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 9) {
                            timeString = "Eight days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 10) {
                            timeString = "Nine days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 11) {
                            timeString = "Ten days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 12) {
                            timeString = "Eleven days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 13) {
                            timeString = "Twelve days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 14) {
                            timeString = "Thirteen days ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 15) {
                            timeString = "Two weeks ago";
                        } else if (diff < 1000 * 60 * 60 * 24 * 21) {
                            timeString = "Between two and three weeks ago";
                        } else if (diff < 1000L * 60 * 60 * 24 * 28) {
                            timeString = "Between three and four weeks ago";
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
                        final String re = "^([a-f0-9]{1,12}) ";
                        line = line.replaceFirst(re, "");
                        commits += line + "\n\n";
                        checknext = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ChangeLogAdapter adapter = new ChangeLogAdapter(this, changeLogArray);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}
