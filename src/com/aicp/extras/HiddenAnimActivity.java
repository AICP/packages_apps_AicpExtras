package com.aicp.extras;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aicp.extras.BaseActivity;
import com.aicp.extras.view.GifView;

public class HiddenAnimActivity extends BaseActivity {

    private GifView gifView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animgif_layout);

        gifView = (GifView) findViewById(R.id.gif_view);
        gifView.setGifAssetPath("yoga.gif");
        gifView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HiddenAnimActivity.this);
                builder.setPositiveButton(R.string.hidden_anim_more_nice,
                        new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                });
                final AlertDialog dialog = builder.create();
                LayoutInflater inflater = getLayoutInflater();
                View dialogLayout = inflater.inflate(R.layout.hidden_img_layout, null);
                dialog.setView(dialogLayout);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

                dialog.show();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface d) {
                        ImageView image = (ImageView) dialog.findViewById(R.id.hidden_img);
                        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                                R.drawable.aicp_hidden);
                        float imageWidthInPX = (float)image.getWidth();

                        LinearLayout.LayoutParams layoutParams =
                                new LinearLayout.LayoutParams(Math.round(imageWidthInPX),
                                Math.round(imageWidthInPX * (float)icon.getHeight() /
                                (float)icon.getWidth()));
                        image.setLayoutParams(layoutParams);
                    }
                });
                return true;
            }
        });
    }
}
