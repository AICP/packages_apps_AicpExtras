package com.aicp.extras;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.aicp.extras.BaseActivity;
import com.aicp.extras.HiddenImgDialog;
import com.bumptech.glide.Glide;

public class HiddenAnimActivity extends BaseActivity {

	HiddenImgDialog hiddenImgDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hiddenactivity_layout);

        hiddenImgDialog = new HiddenImgDialog(this);

        ImageView gifView = (ImageView) findViewById(R.id.hiddengif_view);
        Glide.with(this)
            .load("https://media.giphy.com/media/WXyAuetkvy45q/giphy.gif")
            .placeholder(R.drawable.glide_loading)
            .error(R.drawable.glide_error)
            .into(gifView);

        gifView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
            	hiddenImgDialog.showDialog();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hiddenImgDialog.hideDialog();
                    }
                }, 7000);

                return true;
            }
        });
    }
}
