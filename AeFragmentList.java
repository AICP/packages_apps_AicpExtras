/*
 * Copyright (C) 2019 Android Ice Cold Project
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

/* xx0xx */

package com.aicp.extras.search;

import com.aicp.extras.R;

/* xx0xx */
public class AeFragmentList {

    public static class AeFragmentInfo {
        public final String fragmentClass;
        public final String key;
        public final int title;
        public final int summary;
        public final int xmlRes;
        public AeFragmentInfo(String fragmentClass, String key, int title, int summary, int xmlRes) {
            this.fragmentClass = fragmentClass;
            this.key = key;
            this.title = title;
            this.summary = summary;
            this.xmlRes = xmlRes;
        }
    }

    /* xx0xx */
    public static final AeFragmentInfo[] FRAGMENT_LIST = {/* xx1xx */
    };
}
