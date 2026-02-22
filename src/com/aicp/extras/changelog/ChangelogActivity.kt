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
package com.aicp.extras.changelog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aicp.extras.R
import java.io.BufferedReader
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

class ChangelogActivity : AppCompatActivity() {

    companion object {
        private const val CHANGELOG_PATH = "/system/etc/Changelog.txt"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.ChangelogTheme)
        setContentView(R.layout.changelog_activity)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.changelog_name)
        setSupportActionBar(toolbar)

        val recyclerView = findViewById<RecyclerView>(R.id.changelog)
        recyclerView.setHasFixedSize(true)
        val changeLogArray = ArrayList<ChangelogItem>()

        try {
            val sdf = SimpleDateFormat("MM-dd-yyyy", Locale.ENGLISH)
            val nowDate = Date()
            val reader = BufferedReader(FileReader(CHANGELOG_PATH))
            var line: String?
            var directory = ""
            var commits = ""
            var checknext = false

            while (reader.readLine().also { line = it } != null) {
                if (!line!!.matches("={20}".toRegex()) && line!!.trim().isNotEmpty()) {
                    if (line!!.matches("     (\\d\\d-\\d\\d-\\d{4})".toRegex())) { // it's date
                        val date = sdf.parse(line!!.trim())
                        val now = nowDate.time
                        val time = date.time
                        val diff = now - time
                        val timeString = when {
                            diff < 1000 * 60 * 60 * 24 -> "Today"
                            diff < 1000 * 60 * 60 * 24 * 2 -> "Yesterday"
                            diff < 1000 * 60 * 60 * 24 * 3 -> "Two days ago"
                            diff < 1000 * 60 * 60 * 24 * 4 -> "Three days ago"
                            diff < 1000 * 60 * 60 * 24 * 5 -> "Four days ago"
                            diff < 1000 * 60 * 60 * 24 * 6 -> "Five days ago"
                            diff < 1000 * 60 * 60 * 24 * 7 -> "Six days ago"
                            diff < 1000 * 60 * 60 * 24 * 8 -> "A week ago"
                            diff < 1000 * 60 * 60 * 24 * 9 -> "Eight days ago"
                            diff < 1000 * 60 * 60 * 24 * 10 -> "Nine days ago"
                            diff < 1000 * 60 * 60 * 24 * 11 -> "Ten days ago"
                            diff < 1000 * 60 * 60 * 24 * 12 -> "Eleven days ago"
                            diff < 1000 * 60 * 60 * 24 * 13 -> "Twelve days ago"
                            diff < 1000 * 60 * 60 * 24 * 14 -> "Thirteen days ago"
                            diff < 1000 * 60 * 60 * 24 * 15 -> "Two weeks ago"
                            diff < 1000L * 60 * 60 * 24 * 21 -> "Between two and three weeks ago"
                            diff < 1000L * 60 * 60 * 24 * 28 -> "Between three and four weeks ago"
                            else -> line!!.trim().replace("-", "/")
                        }
                        changeLogArray.add(ChangelogItem(timeString))
                    } else if (line!!.matches("^\\s*(   \\* )\\S*".toRegex())) { // it's directory
                        if (checknext) {
                            commits = commits.substring(0, commits.lastIndexOf("\n\n")) // remove lf on end
                            changeLogArray.add(ChangelogItem(directory, commits))
                            commits = "" // reset commits
                            checknext = false
                        } else {
                            checknext = true
                            commits = ""
                        }
                        directory = line!!.replace("(   \\* )".toRegex(), "")
                    } else {
                        val re = "^([a-f0-9]{1,12}) "
                        line = line!!.replaceFirst(re.toRegex(), "")
                        commits += "$line\n\n"
                        checknext = true
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val adapter = ChangeLogAdapter(this, changeLogArray)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}

