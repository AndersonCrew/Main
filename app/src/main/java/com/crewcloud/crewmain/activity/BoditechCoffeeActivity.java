package com.crewcloud.crewmain.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BoditechCoffeeActivity extends BaseActivity {

    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boditech_coffee);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.boditech);
        toolbar.setNavigationIcon(R.drawable.nav_back_ic);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final String userId = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentUserID();
        mWebView = (WebView) findViewById(R.id.webView);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(mWebView, url);

                Log.d("A", "A");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d("A", "A");
            }
        });

        String url = "http://cafe.boditech.co.kr/auth/login";
        String postData = null;
        postData = "id=kis1";

        mWebView.postUrl(url, postData.getBytes());
    }
}
