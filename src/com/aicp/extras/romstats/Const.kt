/*
 * Copyright (C) 2026 Android Ice Cold Project
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
package com.aicp.extras.romstats

object Const {
    const val TAG = "AicpStats"

    const val SKIP_CERTIFICATE_CHECK = false

    const val ANONYMOUS_OPT_IN = "pref_anonymous_opt_in"
    const val ANONYMOUS_OPT_OUT_PERSIST = "pref_anonymous_opt_out_persist"
    const val ANONYMOUS_FIRST_BOOT = "pref_anonymous_first_boot"
    const val ANONYMOUS_LAST_CHECKED = "pref_anonymous_checked_in"
    const val ANONYMOUS_LAST_REPORT_VERSION = "pref_anonymous_last_rep_version"
    const val ANONYMOUS_NEXT_ALARM = "pref_anonymous_next_alarm"

    const val ROMSTATS_REPORTING_MODE_NEW = 0
    const val ROMSTATS_REPORTING_MODE_OLD = 1
}

