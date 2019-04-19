package com.aicp.extras;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.aicp.extras.BaseActivity;
import com.aicp.extras.HiddenAnimActivityDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class HiddenAnimActivity extends BaseActivity {

	HiddenAnimActivityDialog hiddenAnimActivityDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hiddenactivity_main_layout);

        hiddenAnimActivityDialog = new HiddenImgDialog(this);

        final ImageView imgView = dialog.findViewById(R.id.imageView_1);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_cyclic);

        GlideDrawableImageViewTarget imgViewTarget = new GlideDrawableImageViewTarget(imgView);
        Glide.with(this)
            .load("https://imgur.com/download/fKZHIs6")
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            })
            .error(R.drawable.glide_error)
            .into(imgViewTarget);

        imgView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
            	hiddenAnimActivityDialog.showDialog();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hiddenAnimActivityDialog.hideDialog();
                    }
                }, 7000);

                return true;
            }
        });
    }
}
