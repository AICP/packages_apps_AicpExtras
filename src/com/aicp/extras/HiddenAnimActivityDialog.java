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
        dialog = new Dialog(activity, R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.hiddenactivity_dialog_layout);

        final ImageView imgView = dialog.findViewById(R.id.imageView_1);

        GlideDrawableImageViewTarget imgViewTarget = new GlideDrawableImageViewTarget(imgView);
        Glide.with(activity)
                .load("https://i.postimg.cc/gJ1wZ7Tm/f44e770c62e391996824b09991bc7be6.jpg")
                .placeholder(R.drawable.glide_loading)
                .error(R.drawable.glide_error)
                .into(imgViewTarget);

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
