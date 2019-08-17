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


package com.aicp.extras;

import androidx.preference.Preference;

public class PreferenceMultiClickHandler implements Preference.OnPreferenceClickListener {

    private int mRequiredHits;
    private int mAllowedBreak;

    private int mHits = 0;
    private long mLastHit = -1L;

    private Runnable mRunnable;

    public PreferenceMultiClickHandler(Runnable runnable) {
        this(runnable, 3);
    }

    public PreferenceMultiClickHandler(Runnable runnable, int requiredHits) {
        this(runnable, requiredHits, 500);
    }

    public PreferenceMultiClickHandler(Runnable runnable, int requiredHits, int allowedBreak) {
        mRunnable = runnable;
        mRequiredHits = requiredHits;
        mAllowedBreak = allowedBreak;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        long now = System.currentTimeMillis();
        if (now - mLastHit > mAllowedBreak) {
            mHits = 0;
        }
        mLastHit = now;
        mHits++;
        if (mHits == mRequiredHits) {
            mHits = 0;
            mRunnable.run();
        }
        return true;
    }
}
