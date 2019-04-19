package com.aicp.extras;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

public class HiddenImgDialog {

    Activity activity;
    Dialog dialog;
    public HiddenImgDialog(Activity activity) {
        this.activity = activity;
    }

    public void showDialog() {

        dialog  = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.hiddenimgdialog_layout);

        ImageView imgView = dialog.findViewById(R.id.hiddenimg_view);

        GlideDrawableImageViewTarget imgViewTarget = new GlideDrawableImageViewTarget(imgView);
        Glide.with(activity)
                .load("https://media.giphy.com/media/WXyAuetkvy45q/giphy.gif")
                .placeholder(R.drawable.glide_loading)
                .error(R.drawable.glide_error)
                .into(imgViewTarget);

        dialog.show();
    }

    public void hideDialog(){
        dialog.dismiss();
    }

}
