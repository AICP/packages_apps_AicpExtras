/*
 * Copyright 2012 Carl Bauer
 * Copyright (C) 2014 SlimRoms Project
 * Copyright (C) 2026 SlimRoms Project
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
package com.aicp.extras.dslv

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView

/**
 * Lightweight ViewGroup that wraps list items obtained from user's
 * ListAdapter. ItemView expects a single child that has a definite
 * height (i.e. the child's layout height is not MATCH_PARENT).
 * The width of
 * ItemView will always match the width of its child (that is,
 * the width MeasureSpec given to ItemView is passed directly
 * to the child, and the ItemView measured width is set to the
 * child's measured width). The height of ItemView can be anything;
 * the
 *
 * The purpose of this class is to optimize slide
 * shuffle animations.
 */
class DragSortItemView(context: Context) : ViewGroup(context) {

    private var mGravity = Gravity.TOP

    init {
        // always init with standard ListView layout params
        layoutParams = AbsListView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    fun setGravity(gravity: Int) {
        mGravity = gravity
    }

    fun getGravity(): Int {
        return mGravity
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val child = getChildAt(0) ?: return

        if (mGravity == Gravity.TOP) {
            child.layout(0, 0, measuredWidth, child.measuredHeight)
        } else {
            child.layout(0, measuredHeight - child.measuredHeight, measuredWidth, measuredHeight)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val child = getChildAt(0) ?: run {
            setMeasuredDimension(0, width)
            return
        }

        if (child.isLayoutRequested) {
            // Always let child be as tall as it wants.
            measureChild(
                child,
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
        }

        if (heightMode == MeasureSpec.UNSPECIFIED) {
            val lp = layoutParams
            val childHeight = if (lp.height > 0) lp.height else child.measuredHeight
            setMeasuredDimension(width, childHeight)
        } else {
            setMeasuredDimension(width, height)
        }
    }
}

