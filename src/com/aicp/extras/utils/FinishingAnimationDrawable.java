/*
 * Copyright (C) 2017 AICP
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


package com.aicp.extras.utils;

import android.graphics.drawable.AnimationDrawable;

/**
 * AnimationDrawable with OnAnimationFinishedListener.
 * Substitute to frameworks patch https://gerrit.aicp-rom.com/#/c/34280/
 */
public class FinishingAnimationDrawable extends AnimationDrawable {

    private OnAnimationFinishedListener mOnAnimationFinishedListener;

    public boolean selectDrawable(int index) {
        boolean result = super.selectDrawable(index);
        if (index == getNumberOfFrames() - 1 && mOnAnimationFinishedListener != null) {
            mOnAnimationFinishedListener.onAnimationFinished();
        }
        return result;
    }

    public void start() {
        super.start();
        if (getNumberOfFrames() == 0 && mOnAnimationFinishedListener != null) {
            mOnAnimationFinishedListener.onAnimationFinished();
        }
    }

    public interface OnAnimationFinishedListener {
        void onAnimationFinished();
    }

    public void setOnAnimationFinishedListener(OnAnimationFinishedListener listener) {
        mOnAnimationFinishedListener = listener;
    }
}
