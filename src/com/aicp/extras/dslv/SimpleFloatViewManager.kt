/*
 * Copyright 2012 Carl Bauer
 * Copyright (C) 2014 SlimRoms Project
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
package com.aicp.extras.dslv

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView

/**
 * Simple implementation of the FloatViewManager class. Uses list
 * items as they appear in the ListView to create the floating View.
 */
open class SimpleFloatViewManager(private val mListView: ListView) : DragSortListView.FloatViewManager {

    private var mFloatBitmap: Bitmap? = null
    private var mImageView: ImageView? = null
    private var mFloatBGColor = Color.BLACK

    fun setBackgroundColor(color: Int) {
        mFloatBGColor = color
    }

    /**
     * This simple implementation creates a Bitmap copy of the
     * list item currently shown at ListView `position`.
     */
    override fun onCreateFloatView(position: Int): View? {
        val v = mListView.getChildAt(
            position + mListView.headerViewsCount - mListView.firstVisiblePosition
        ) ?: return null

        v.isPressed = false

        v.isDrawingCacheEnabled = true
        mFloatBitmap = Bitmap.createBitmap(v.drawingCache)
        v.isDrawingCacheEnabled = false

        if (mImageView == null) {
            mImageView = ImageView(mListView.context)
        }
        mImageView?.setBackgroundColor(mFloatBGColor)
        mImageView?.setPadding(0, 0, 0, 0)
        mImageView?.setImageBitmap(mFloatBitmap)
        mImageView?.layoutParams = ViewGroup.LayoutParams(v.width, v.height)

        return mImageView
    }

    /**
     * This does nothing
     */
    override fun onDragFloatView(floatView: View, position: Point, touch: Point) {
        // do nothing
    }

    /**
     * Removes the Bitmap from the ImageView created in
     * onCreateFloatView() and tells the system to recycle it.
     */
    override fun onDestroyFloatView(floatView: View) {
        (floatView as? ImageView)?.setImageDrawable(null)
        mFloatBitmap?.recycle()
        mFloatBitmap = null
    }
}

