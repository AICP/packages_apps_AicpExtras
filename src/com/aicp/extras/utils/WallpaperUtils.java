/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.aicp.extras.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;

import androidx.palette.graphics.Palette;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallpaperUtils {

    public interface OnColorsExtractedListener {
        void onColorsExtracted(Palette palette);
    }

    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static void extractWallpaperColors(Context context, OnColorsExtractedListener listener) {
        sExecutor.execute(() -> {
            try {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                Drawable drawable = wallpaperManager.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    Palette palette = Palette.from(bitmap).generate();
                    sHandler.post(() -> listener.onColorsExtracted(palette));
                } else {
                    sHandler.post(() -> listener.onColorsExtracted(null));
                }
            } catch (Exception e) {
                sHandler.post(() -> listener.onColorsExtracted(null));
            }
        });
    }

    public static Drawable getWall(Context context, boolean z) {
        WallpaperManager instance = WallpaperManager.getInstance(context);
        ParcelFileDescriptor wallpaperFile = instance.getWallpaperFile(z ? 2 : 1);
        if (wallpaperFile == null) {
            return instance.getDrawable();
        }
        Bitmap createScaledBitmap = Bitmap.createScaledBitmap(
            BitmapFactory.decodeFileDescriptor(wallpaperFile.getFileDescriptor()), 
            1080, 1080, true);
        try {
            wallpaperFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BitmapDrawable(context.getResources(), createScaledBitmap);
    }

    public static boolean isLiveWall(Context context) {
        return WallpaperManager.getInstance(context).getWallpaperInfo() != null;
    }
} 
