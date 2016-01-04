package com.lordclockan.aicpextras.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;

public class GifWebView extends WebView {

    public GifWebView(Context context) {
        super(context);
        getSettings().setJavaScriptEnabled(true);
    }

    public GifWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getSettings().setJavaScriptEnabled(true);
    }

    public void setGifAssetPath(String pPath) {
        String baseUrl = pPath.substring(0, pPath.lastIndexOf("/") + 1);
        String fileName = pPath.substring(pPath.lastIndexOf("/")+1);
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<html><head><body bgcolor=\"#455A64\"><style type='text/css'>body{margin:auto auto;text-align:center;} img{width:100%25;} </style>");
        strBuilder.append("</head><body>");
        strBuilder.append("<img src=\"" + fileName + "\" width=\"100%\" /></body></html>");
        String data = strBuilder.toString();
        Log.d(this.getClass().getName(), "data: " + data);
        Log.d(this.getClass().getName(), "base url: " + baseUrl);
        Log.d(this.getClass().getName(), "file name: " + fileName);
        loadDataWithBaseURL(baseUrl, data, "text/html", "utf-8", null);
    }
}
