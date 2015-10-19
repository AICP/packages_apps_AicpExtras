package com.lordclockan.aicpextras;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by lord on 07.10.15..
 */
public class AboutFragment extends Fragment {

    View myView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.about_layout, container, false);
        addListenerOnGCommunity();
        return myView;
    }

    public void addListenerOnGCommunity() {

        TextView mGcommunty = (TextView) myView.findViewById(R.id.tvGplus);
        mGcommunty.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String url = "https://plus.google.com/communities/101008638920580274588";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        TextView mAicpDownloads = (TextView) myView.findViewById(R.id.tvAicpDownloads);
        mAicpDownloads.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String url = "http://dwnld.aicp-rom.com";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        TextView mAicpGerrit = (TextView) myView.findViewById(R.id.tvAicpGerrit);
        mAicpGerrit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String url = "http://gerrit.aicp-rom.com/#/q/status:open";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });
    }
}
