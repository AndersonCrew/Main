package com.crewcloud.crewmain.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.crewcloud.crewmain.Constants;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Base64;

public class BoditechCoffeeActivity extends BaseActivity {

    private WebView mWebView;
    private String projectCode = Constants.PROJECT_CODE_COFFEE;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boditech_coffee);
        projectCode = getIntent().getStringExtra(Constants.TYPE_PROJECT_CODE_BODITECH);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(projectCode.equals(Constants.PROJECT_CODE_COFFEE) ? R.string.boditech : R.string.pms);
        toolbar.setNavigationIcon(R.drawable.nav_back_ic);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final String userId = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentUserID();
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
            }
        });


        if(projectCode.equals(Constants.PROJECT_CODE_COFFEE)) {
            String url = "http://cafe.boditech.co.kr/auth/login";
            String postData = null;
            postData = "id=" + userId;

            mWebView.postUrl(url, postData.getBytes());
        } else {
            String url = "http://pms.boditech.co.kr/sso?id=" + Base64.getEncoder().encodeToString(userId.getBytes());
            mWebView.loadUrl(url);
        }


    }
}
