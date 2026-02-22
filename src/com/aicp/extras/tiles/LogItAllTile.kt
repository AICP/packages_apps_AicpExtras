/*
 * Copyright (C) 2022-2026 The Android Ice Cold Project
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
package com.aicp.extras.tiles

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.aicp.extras.R
import com.aicp.extras.SettingsActivity
import com.aicp.extras.utils.Util

class LogItAllTile : TileService() {

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onTileAdded() {
        super.onTileAdded()
        checkRootAndSetState()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
    }

    override fun onStartListening() {
        super.onStartListening()
        checkRootAndSetState()
    }

    override fun onStopListening() {
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        val logItAllIntent = Intent(this, SettingsActivity::class.java).apply {
            putExtra(":android:show_fragment", "com.aicp.extras.fragments.LogIt")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivityAndCollapse(logItAllIntent)
    }

    private fun checkRootAndSetState() {
        if (!Util.hasSu()) {
            qsTile.state = Tile.STATE_UNAVAILABLE
            qsTile.subtitle = getString(R.string.no_root_title)
            qsTile.updateTile()
        }
    }
}

