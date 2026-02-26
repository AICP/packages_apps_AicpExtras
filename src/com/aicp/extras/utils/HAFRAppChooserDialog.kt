/*
 * Copyright (C) 2026 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.aicp.extras.utils

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.aicp.extras.R

abstract class HAFRAppChooserDialog(
    context: Context
) : Dialog(context) {

    private val dAdapter: HAFRAppChooserAdapter
    private val dProgressBar: ProgressBar
    private val dListView: ListView
    private val dSearch: EditText
    private val dButton: ImageButton

    private var mId: Int = 0

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_app_chooser_list)

        dListView = findViewById(R.id.listView1)
        dSearch = findViewById(R.id.searchText)
        dButton = findViewById(R.id.searchButton)
        dProgressBar = findViewById(R.id.progressBar1)

        dAdapter = object : HAFRAppChooserAdapter(context) {
            override fun onStartUpdate() {
                dProgressBar.visibility = View.VISIBLE
            }

            override fun onFinishUpdate() {
                dProgressBar.visibility = View.GONE
            }
        }

        dListView.adapter = dAdapter

        dListView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val info = parent.getItemAtPosition(position)
                        as HAFRAppChooserAdapter.AppItem
                onListViewItemClick(info, mId)
                dismiss()
            }

        dButton.setOnClickListener {
            dAdapter.filter.filter(
                dSearch.text.toString()
            ) {
                dAdapter.update()
            }
        }

        dSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                dAdapter.filter.filter(
                    dSearch.text.toString()
                ) {
                    dAdapter.update()
                }
                true
            } else {
                false
            }
        }

        dAdapter.update()
    }

    fun show(id: Int) {
        mId = id
        show()
    }

    fun setLauncherFilter(enabled: Boolean) {
        dAdapter.setLauncherFilter(enabled)
    }

    abstract fun onListViewItemClick(
        info: HAFRAppChooserAdapter.AppItem,
        id: Int
    )
}
