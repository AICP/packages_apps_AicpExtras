package com.aicp.extras;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class HiddenAnimActivityDialog {

    Activity activity;
    Dialog dialog;
    public HiddenAnimActivityDialog(Activity activity) {
        this.activity = activity;
    }

    public void showDialog() {

        dialog  = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.hiddenactivity_dialog_layout);

        final ImageView imgView = dialog.findViewById(R.id.imageView_1);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_cyclic);

        GlideDrawableImageViewTarget imgViewTarget = new GlideDrawableImageViewTarget(imgView);
        Glide.with(activity)
                .load("https://i.imgur.com/ZkBoyNA.jpg")
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

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
