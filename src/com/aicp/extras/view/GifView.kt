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
package com.aicp.extras.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Movie
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import java.io.IOException
import java.io.InputStream

class GifView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var mMovie: Movie? = null
    private var mMovieStart: Long = 0
    private val mContext: Context = context

    fun setGifAssetPath(filename: String) {
        var inputStream: InputStream? = null
        try {
            inputStream = mContext.resources.assets.open(filename)
            mMovie = Movie.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mMovie ?: return

        val now = SystemClock.uptimeMillis()
        if (mMovieStart == 0L) mMovieStart = now

        val relTime = ((now - mMovieStart) % mMovie!!.duration()).toInt()
        mMovie!!.setTime(relTime)

        val viewWidth = width
        val viewHeight = height
        val movieWidth = mMovie!!.width()
        val movieHeight = mMovie!!.height()

        val scaleX = viewWidth.toFloat() / movieWidth
        val scaleY = viewHeight.toFloat() / movieHeight
        val scale = minOf(scaleX, scaleY)

        canvas.scale(scale, scale)
        mMovie!!.draw(canvas, 0f, 0f)
        invalidate()
    }
}

