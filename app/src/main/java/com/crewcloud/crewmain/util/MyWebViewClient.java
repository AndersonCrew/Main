package com.crewcloud.crewmain.util;

import android.app.Dialog;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


import com.crewcloud.crewmain.CrewCloudApplication;

import static android.content.ContentValues.TAG;

/**
 * Created by Dazone on 6/26/2017.
 */

public class MyWebViewClient extends WebViewClient {
    String url;

    public MyWebViewClient(String url) {
        this.url = url;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    int a = 0;

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.d(TAG, "onPageFinished");
        String userName = CrewCloudApplication.getInstance().getPreferenceUtilities().getUserId();
        String password = CrewCloudApplication.getInstance().getPreferenceUtilities().getPass();
        if (a < 2) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                view.evaluateJavascript("javascript:document.getElementById('tbUserID').value ='"
                        + userName
                        + "';javascript:document.getElementById('tbPassword').value = '"
                        + password + "';javascript:document.getElementById('btnLogin').click();", null);
            } else {
                view.loadUrl("javascript:document.getElementById('tbUserID').value = '"
                        + userName + "';javascript:document.getElementById('tbPassword').value = '"
                        + password + "';javascript:document.getElementById('btnLogin').click();");
            }
            a++;
        } else {
//            finshedListener.onfinished();
            view.loadUrl(this.url);

        }
    }

//    public interface FinishedListener {
//        void onfinished();
//    }
//
//    private FinishedListener finshedListener;
//
//    public void setOnFinishedListener(FinishedListener finshedListener) {
//        this.finshedListener = finshedListener;
//    }
}