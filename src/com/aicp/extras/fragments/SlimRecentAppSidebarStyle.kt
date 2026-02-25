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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aicp.extras.fragments

import com.aicp.extras.BaseSettingsFragment
import com.aicp.extras.R

class SlimRecentAppSidebarStyle : BaseSettingsFragment() {

    override fun getPreferenceResource(): Int {
        return R.xml.slim_recent_app_sidebar_style
    }
}

