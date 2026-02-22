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

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aicp.extras.R

class ChangeLogAdapter(private val mContext: Context, private val mChangelogItems: List<ChangelogItem>) :
    RecyclerView.Adapter<ChangeLogAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val changeLogView = inflater.inflate(R.layout.changelog_item, parent, false)
        return ViewHolder(changeLogView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val changelogItem = mChangelogItems[position]
        val commitID = viewHolder.commitID
        val commitMessage = viewHolder.commitMessage

        commitID.text = changelogItem.commitId
        if (changelogItem.commitMessage != null) {
            commitID.textAlignment = View.TEXT_ALIGNMENT_INHERIT
            commitMessage.visibility = View.VISIBLE
            commitMessage.text = changelogItem.commitMessage
            commitID.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                mContext.resources.getDimension(R.dimen.changelog_header_small)
            )
            commitID.typeface = Typeface.DEFAULT_BOLD

            val attrs = intArrayOf(android.R.attr.textColorPrimary)
            val ta = mContext.theme.obtainStyledAttributes(attrs)
            commitID.setTextColor(ta.getColor(0, Color.GRAY))
            ta.recycle()
        } else {
            commitID.textAlignment = View.TEXT_ALIGNMENT_CENTER
            commitMessage.visibility = View.GONE
            commitID.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                mContext.resources.getDimension(R.dimen.changelog_header_big)
            )
            commitID.typeface = Typeface.DEFAULT

            val attrs = intArrayOf(R.attr.colorAccent)
            val ta = mContext.theme.obtainStyledAttributes(attrs)
            commitID.setTextColor(ta.getColor(0, Color.GRAY))
            ta.recycle()
        }
    }

    override fun getItemCount(): Int {
        return mChangelogItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commitID: TextView = itemView.findViewById(R.id.commit_id)
        val commitMessage: TextView = itemView.findViewById(R.id.commit_message)
    }
}

