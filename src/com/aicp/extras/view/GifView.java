package com.aicp.extras.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.lang.Math;

public class GifView extends View {

    private Context mContext;
    private Movie mMovie;
    private long mMovieStart;

    public GifView(Context context) {
        super(context);
        mContext = context;
    }

    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public GifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void setGifAssetPath(String filename) {
        InputStream is = null;
        try {
            is = mContext.getResources().getAssets().open(filename);
            mMovie = Movie.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMovie == null) {
            return;
        }

        long now = SystemClock.uptimeMillis();

        if (mMovieStart == 0) mMovieStart = now;

        int relTime;
        relTime = (int)((now - mMovieStart) % mMovie.duration());
        mMovie.setTime(relTime);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int movieWidth = mMovie.width();
        int movieHeight = mMovie.height();
        float scaleX = ((float) viewWidth) / movieWidth;
        float scaleY = ((float) viewHeight) / movieHeight;
        float scale = Math.min(scaleX, scaleY);
        canvas.scale(scale, scale);
        mMovie.draw(canvas, scale, scale);
        invalidate();
    }
}
