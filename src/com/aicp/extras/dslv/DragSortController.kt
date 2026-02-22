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

import android.graphics.Point
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.AdapterView

/**
 * Class that starts and stops item drags on a [DragSortListView]
 * based on touch gestures. This class also inherits from
 * [SimpleFloatViewManager], which provides basic float View
 * creation.
 */
open class DragSortController(dslv: DragSortListView) : SimpleFloatViewManager(dslv),
    View.OnTouchListener, GestureDetector.OnGestureListener {

    companion object {
        const val ON_DOWN = 0
        const val ON_DRAG = 1
        const val ON_LONG_PRESS = 2
        const val CLICK_REMOVE = 0
        const val FLING_REMOVE = 1
        const val MISS = -1
    }

    private var mDragInitMode: Int = ON_DOWN
    private var mSortEnabled: Boolean = true
    private var mRemoveMode: Int = FLING_REMOVE
    private var mRemoveEnabled: Boolean = false
    private var mIsRemoving: Boolean = false
    private lateinit var mDetector: GestureDetector
    private lateinit var mFlingRemoveDetector: GestureDetector
    private var mTouchSlop: Int = 0
    private var mHitPos: Int = MISS
    private var mFlingHitPos: Int = MISS
    private var mClickRemoveHitPos: Int = MISS
    private var mTempLoc: IntArray = IntArray(2)
    private var mItemX: Int = 0
    private var mItemY: Int = 0
    private var mCurrX: Int = 0
    private var mCurrY: Int = 0
    private var mDragging: Boolean = false
    private var mFlingSpeed: Float = 500f
    private var mDragHandleId: Int = 0
    private var mClickRemoveId: Int = 0
    private var mFlingHandleId: Int = 0
    private var mCanDrag: Boolean = false
    private var mPositionX: Int = 0
    private val mDslv: DragSortListView

    private val mFlingRemoveListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (mRemoveEnabled && mIsRemoving) {
                val w = mDslv.width
                val minPos = w / 5
                if (velocityX > mFlingSpeed) {
                    if (mPositionX > -minPos) {
                        mDslv.stopDragWithVelocity(true, velocityX)
                    }
                } else if (velocityX < -mFlingSpeed) {
                    if (mPositionX < minPos) {
                        mDslv.stopDragWithVelocity(true, velocityX)
                    }
                }
                mIsRemoving = false
            }
            return false
        }
    }

    init {
        mDslv = dslv
        mDetector = GestureDetector(dslv.context, this)
        mFlingRemoveDetector = GestureDetector(dslv.context, mFlingRemoveListener)
        mFlingRemoveDetector.setIsLongpressEnabled(false)
        mTouchSlop = ViewConfiguration.get(dslv.context).scaledTouchSlop
    }

    constructor(dslv: DragSortListView, dragHandleId: Int, dragInitMode: Int, removeMode: Int) :
        this(dslv) {
        mDragHandleId = dragHandleId
        mDragInitMode = dragInitMode
        mRemoveMode = removeMode
    }

    constructor(dslv: DragSortListView, dragHandleId: Int, dragInitMode: Int, removeMode: Int, clickRemoveId: Int) :
        this(dslv) {
        mDragHandleId = dragHandleId
        mDragInitMode = dragInitMode
        mRemoveMode = removeMode
        mClickRemoveId = clickRemoveId
    }

    constructor(
        dslv: DragSortListView,
        dragHandleId: Int,
        dragInitMode: Int,
        removeMode: Int,
        clickRemoveId: Int,
        flingHandleId: Int
    ) : this(dslv) {
        mDragHandleId = dragHandleId
        mDragInitMode = dragInitMode
        mRemoveMode = removeMode
        mClickRemoveId = clickRemoveId
        mFlingHandleId = flingHandleId
    }

    fun getDragInitMode(): Int {
        return mDragInitMode
    }

    fun setDragInitMode(mode: Int) {
        mDragInitMode = mode
    }

    fun setSortEnabled(enabled: Boolean) {
        mSortEnabled = enabled
    }

    fun isSortEnabled(): Boolean {
        return mSortEnabled
    }

    fun setRemoveMode(mode: Int) {
        mRemoveMode = mode
    }

    fun getRemoveMode(): Int {
        return mRemoveMode
    }

    fun setRemoveEnabled(enabled: Boolean) {
        mRemoveEnabled = enabled
    }

    fun isRemoveEnabled(): Boolean {
        return mRemoveEnabled
    }

    fun setDragHandleId(id: Int) {
        mDragHandleId = id
    }

    fun setFlingHandleId(id: Int) {
        mFlingHandleId = id
    }

    fun setClickRemoveId(id: Int) {
        mClickRemoveId = id
    }

    fun startDrag(position: Int, deltaX: Int, deltaY: Int): Boolean {
        var dragFlags = 0
        if (mSortEnabled && !mIsRemoving) {
            dragFlags = dragFlags or DragSortListView.DRAG_POS_Y or DragSortListView.DRAG_NEG_Y
        }
        if (mRemoveEnabled && mIsRemoving) {
            dragFlags = dragFlags or DragSortListView.DRAG_POS_X
            dragFlags = dragFlags or DragSortListView.DRAG_NEG_X
        }

        mDragging = mDslv.startDrag(position - mDslv.headerViewsCount, dragFlags, deltaX, deltaY)
        return mDragging
    }

    override fun onTouch(v: View, ev: MotionEvent): Boolean {
        if (!mDslv.isDragEnabled || mDslv.listViewIntercepted()) {
            return false
        }

        mDetector.onTouchEvent(ev)
        if (mRemoveEnabled && mDragging && mRemoveMode == FLING_REMOVE) {
            mFlingRemoveDetector.onTouchEvent(ev)
        }

        val action = ev.action and MotionEvent.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mCurrX = ev.x.toInt()
                mCurrY = ev.y.toInt()
            }
            MotionEvent.ACTION_UP -> {
                if (mRemoveEnabled && mIsRemoving) {
                    val x = if (mPositionX >= 0) mPositionX else -mPositionX
                    val removePoint = mDslv.width / 2
                    if (x > removePoint) {
                        mDslv.stopDragWithVelocity(true, 0f)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                mIsRemoving = false
                mDragging = false
            }
        }

        return false
    }

    override fun onDragFloatView(floatView: View, position: Point, touch: Point) {
        if (mRemoveEnabled && mIsRemoving) {
            mPositionX = position.x
        }
    }

    fun startDragPosition(ev: MotionEvent): Int {
        return dragHandleHitPosition(ev)
    }

    fun startFlingPosition(ev: MotionEvent): Int {
        return if (mRemoveMode == FLING_REMOVE) flingHandleHitPosition(ev) else MISS
    }

    fun dragHandleHitPosition(ev: MotionEvent): Int {
        return viewIdHitPosition(ev, mDragHandleId)
    }

    fun flingHandleHitPosition(ev: MotionEvent): Int {
        return viewIdHitPosition(ev, mFlingHandleId)
    }

    fun viewIdHitPosition(ev: MotionEvent, id: Int): Int {
        val x = ev.x.toInt()
        val y = ev.y.toInt()

        val touchPos = mDslv.pointToPosition(x, y)

        val numHeaders = mDslv.headerViewsCount
        val numFooters = mDslv.footerViewsCount
        val count = mDslv.count

        if (touchPos != AdapterView.INVALID_POSITION && touchPos >= numHeaders && touchPos < (count - numFooters)) {
            val item = mDslv.getChildAt(touchPos - mDslv.firstVisiblePosition) ?: return MISS
            val rawX = ev.rawX.toInt()
            val rawY = ev.rawY.toInt()

            val dragBox = if (id == 0) item else item.findViewById<View>(id) ?: return MISS
            dragBox.getLocationOnScreen(mTempLoc)

            if (rawX > mTempLoc[0] && rawY > mTempLoc[1] && rawX < mTempLoc[0] + dragBox.width && rawY < mTempLoc[1] + dragBox.height) {
                mItemX = item.left
                mItemY = item.top
                return touchPos
            }
        }

        return MISS
    }

    override fun onDown(e: MotionEvent): Boolean {
        if (mRemoveEnabled && mRemoveMode == CLICK_REMOVE) {
            mClickRemoveHitPos = viewIdHitPosition(e, mClickRemoveId)
        }

        mHitPos = startDragPosition(e)
        if (mHitPos != MISS && mDragInitMode == ON_DOWN) {
            startDrag(mHitPos, e.x.toInt() - mItemX, e.y.toInt() - mItemY)
        }

        mIsRemoving = false
        mCanDrag = true
        mPositionX = 0
        mFlingHitPos = startFlingPosition(e)

        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        if (e1 == null) return false
        val x1 = e1.x.toInt()
        val y1 = e1.y.toInt()
        val x2 = e2.x.toInt()
        val y2 = e2.y.toInt()
        val deltaX = x2 - mItemX
        val deltaY = y2 - mItemY

        if (mCanDrag && !mDragging && (mHitPos != MISS || mFlingHitPos != MISS)) {
            if (mHitPos != MISS) {
                if (mDragInitMode == ON_DRAG && Math.abs(y2 - y1) > mTouchSlop && mSortEnabled) {
                    startDrag(mHitPos, deltaX, deltaY)
                } else if (mDragInitMode != ON_DOWN && Math.abs(x2 - x1) > mTouchSlop && mRemoveEnabled) {
                    mIsRemoving = true
                    startDrag(mFlingHitPos, deltaX, deltaY)
                }
            } else if (mFlingHitPos != MISS) {
                if (Math.abs(x2 - x1) > mTouchSlop && mRemoveEnabled) {
                    mIsRemoving = true
                    startDrag(mFlingHitPos, deltaX, deltaY)
                } else if (Math.abs(y2 - y1) > mTouchSlop) {
                    mCanDrag = false
                }
            }
        }
        return false
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        if (mRemoveEnabled && mRemoveMode == CLICK_REMOVE) {
            if (mClickRemoveHitPos != MISS) {
                mDslv.removeItem(mClickRemoveHitPos - mDslv.headerViewsCount)
            }
        }
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        // do nothing
    }

    override fun onLongPress(e: MotionEvent) {
        if (mHitPos != MISS && mDragInitMode == ON_LONG_PRESS) {
            mDslv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            startDrag(mHitPos, mCurrX - mItemX, mCurrY - mItemY)
        }
    }
}

