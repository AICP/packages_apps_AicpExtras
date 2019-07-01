package com.aicp.extras;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.aicp.extras.BaseActivity;
import com.aicp.extras.HiddenAnimActivityDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class HiddenAnimActivity extends BaseActivity {

    HiddenAnimActivityDialog hiddenAnimActivityDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hiddenactivity_main_layout);

        hiddenAnimActivityDialog = new HiddenAnimActivityDialog(this);

        final ImageView imgView = (ImageView) findViewById(R.id.imageView_1);

        GlideDrawableImageViewTarget imgViewTarget = new GlideDrawableImageViewTarget(imgView);
        Glide.with(this)
            .load("https://i.postimg.cc/d3Ksvcfk/c64520bbaf7e0b14aa77ae1d77571196.gif")
            .placeholder(R.drawable.glide_loading)
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
