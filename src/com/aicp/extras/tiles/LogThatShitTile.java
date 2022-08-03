/*
 * Copyright (C) 2022 The Android Ice Cold Project
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

package com.aicp.extras.tiles;

import android.content.Intent;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.aicp.extras.SettingsActivity;
import com.aicp.extras.utils.Util;
import com.aicp.extras.R;

public class LogThatShitTile extends TileService {
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        if(!Util.hasSu()) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().setSubtitle(this.getResources().getString(R.string.no_root_title));
            getQsTile().updateTile();
        }
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        if(!Util.hasSu()) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
            getQsTile().setSubtitle(this.getResources().getString(R.string.no_root_title));
            getQsTile().updateTile();
        }
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        Intent logThatShit = new Intent(this, SettingsActivity.class);
        logThatShit.putExtra(":android:show_fragment", "com.aicp.extras.fragments.LogIt");
        logThatShit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityAndCollapse(logThatShit);
    }
}
