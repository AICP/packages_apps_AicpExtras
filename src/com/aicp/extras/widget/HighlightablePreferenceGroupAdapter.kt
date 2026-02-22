/*
 * Copyright (C) 2018 The Android Open Source Project
 * Copyright (C) 2026 AICP
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
package com.aicp.extras.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceGroupAdapter
import androidx.preference.PreferenceScreen
import androidx.preference.PreferenceViewHolder
import androidx.recyclerview.widget.RecyclerView
import com.aicp.extras.R

class HighlightablePreferenceGroupAdapter(
    preferenceGroup: PreferenceGroup,
    private val mHighlightKey: String,
    highlightRequested: Boolean
) : PreferenceGroupAdapter(preferenceGroup) {

    companion object {
        private const val TAG = "HighlightableAdapter"
        const val DELAY_HIGHLIGHT_DURATION_MILLIS = 600L
        private const val HIGHLIGHT_DURATION = 15000L
        private const val HIGHLIGHT_FADE_OUT_DURATION = 500L
        private const val HIGHLIGHT_FADE_IN_DURATION = 200L
    }

    private val mHighlightColor: Int
    private var mFadeInAnimated: Boolean = false
    private val mNormalBackgroundRes: Int
    private var mHighlightRequested: Boolean = highlightRequested
    private var mHighlightPosition: Int = RecyclerView.NO_POSITION

    init {
        val context = preferenceGroup.context
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        mNormalBackgroundRes = outValue.resourceId
        mHighlightColor = context.getColor(R.color.preference_highligh_color)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        updateBackground(holder, position)
    }

    fun updateBackground(holder: PreferenceViewHolder, position: Int) {
        val v = holder.itemView
        if (position == mHighlightPosition) {
            addHighlightBackground(v, !mFadeInAnimated)
        } else if (v.getTag(R.id.preference_highlighted) == true) {
            removeHighlightBackground(v, false)
        }
    }

    fun requestHighlight(root: View, recyclerView: RecyclerView) {
        if (mHighlightRequested || TextUtils.isEmpty(mHighlightKey)) {
            return
        }
        root.postDelayed({
            val position = getPreferenceAdapterPosition(mHighlightKey)
            if (position < 0) {
                return@postDelayed
            }
            mHighlightRequested = true
            recyclerView.smoothScrollToPosition(position)
            mHighlightPosition = position
            notifyItemChanged(position)
        }, DELAY_HIGHLIGHT_DURATION_MILLIS)
    }

    fun isHighlightRequested(): Boolean {
        return mHighlightRequested
    }

    fun requestRemoveHighlightDelayed(v: View) {
        v.postDelayed({
            mHighlightPosition = RecyclerView.NO_POSITION
            removeHighlightBackground(v, true)
        }, HIGHLIGHT_DURATION)
    }

    private fun addHighlightBackground(v: View, animate: Boolean) {
        v.setTag(R.id.preference_highlighted, true)
        if (!animate) {
            v.setBackgroundColor(mHighlightColor)
            requestRemoveHighlightDelayed(v)
            return
        }
        mFadeInAnimated = true
        val colorFrom = Color.WHITE
        val colorTo = mHighlightColor
        val fadeInLoop = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        fadeInLoop.duration = HIGHLIGHT_FADE_IN_DURATION
        fadeInLoop.addUpdateListener { animator ->
            v.setBackgroundColor(animator.animatedValue as Int)
        }
        fadeInLoop.repeatMode = ValueAnimator.REVERSE
        fadeInLoop.repeatCount = 4
        fadeInLoop.start()
        requestRemoveHighlightDelayed(v)
    }

    private fun removeHighlightBackground(v: View, animate: Boolean) {
        if (!animate) {
            v.setTag(R.id.preference_highlighted, false)
            v.setBackgroundResource(mNormalBackgroundRes)
            return
        }

        if (v.getTag(R.id.preference_highlighted) != true) {
            return
        }
        val colorFrom = mHighlightColor
        val colorTo = Color.WHITE

        v.setTag(R.id.preference_highlighted, false)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = HIGHLIGHT_FADE_OUT_DURATION
        colorAnimation.addUpdateListener { animator ->
            v.setBackgroundColor(animator.animatedValue as Int)
        }
        colorAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                v.setBackgroundResource(mNormalBackgroundRes)
            }
        })
        colorAnimation.start()
    }
}

