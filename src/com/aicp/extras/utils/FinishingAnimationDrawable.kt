/*
 * Copyright (C) 2017-2026 AICP
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

import android.graphics.drawable.AnimationDrawable

/**
 * AnimationDrawable with OnAnimationFinishedListener.
 * Substitute to frameworks patch https://gerrit.aicp-rom.com/#/c/34280/
 */
class FinishingAnimationDrawable : AnimationDrawable() {

    private var onAnimationFinishedListener: OnAnimationFinishedListener? = null

    override fun selectDrawable(index: Int): Boolean {
        val result = super.selectDrawable(index)
        if (index == numberOfFrames - 1) {
            onAnimationFinishedListener?.onAnimationFinished()
        }
        return result
    }

    override fun start() {
        super.start()
        if (numberOfFrames == 0) {
            onAnimationFinishedListener?.onAnimationFinished()
        }
    }

    interface OnAnimationFinishedListener {
        fun onAnimationFinished()
    }

    fun setOnAnimationFinishedListener(listener: OnAnimationFinishedListener?) {
        onAnimationFinishedListener = listener
    }
}
