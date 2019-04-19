package com.aicp.extras;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.aicp.extras.BaseActivity;
import com.bumptech.glide.Glide;

public class HiddenAnimActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hiddenactivity_layout);

        ImageView gifView = (ImageView) findViewById(R.id.hiddengif_view);
        Glide.with(this)
            .load("https://media.giphy.com/media/WXyAuetkvy45q/giphy.gif")
            .placeholder(R.drawable.glide_loading)
            .error(R.drawable.glide_error)
            .into(gifView);
    }
}
