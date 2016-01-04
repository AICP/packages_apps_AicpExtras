package com.lordclockan.aicpextras;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lordclockan.R;
import com.lordclockan.aicpextras.utils.GifWebView;

public class HiddenAnimActivity extends Activity {

    private GifWebView gifView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animgif_layout);

        gifView = (GifWebView) findViewById(R.id.gif_view);
        gifView.setGifAssetPath("file:///android_asset/yoga.gif");

        Snackbar snackbar = Snackbar
                .make(gifView, R.string.hidden_anim_activity_title, Snackbar.LENGTH_LONG)
                .setAction(R.string.hidden_anim_more_title, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                        }
                });

        snackbar.setActionTextColor(Color.RED);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
    }
}
