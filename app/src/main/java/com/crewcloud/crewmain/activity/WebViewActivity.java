package com.crewcloud.crewmain.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.base.WebContentChromeClient;
import com.crewcloud.crewmain.base.WebContentClient;
import com.crewcloud.crewmain.util.DeviceUtilities;
import com.crewcloud.crewmain.util.PreferenceUtilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Dazone on 6/26/2017.
 */

public class WebViewActivity extends BaseActivity {

    @Bind(R.id.image)
    ImageView ivImage;

    @Bind(R.id.webview)
    WebView webView;

    @Bind(R.id.progress_bar)
    ProgressBar progress_bar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_activity);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        toolbar.setNavigationIcon(R.drawable.nav_back_ic);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("AAA");
        if (url != null) {
//            if (url.equals(Statics.URL_ALL_BOARD)) {
//                webView.setVisibility(View.GONE);
//                ivImage.setVisibility(View.VISIBLE);
//                Picasso.with(this).load(R.mipmap.allboad).into(ivImage);
//                progress_bar.setVisibility(View.GONE);
//            } else {
                webView.setVisibility(View.VISIBLE);
                ivImage.setVisibility(View.GONE);
//
                loadWeb(url);
//            }
        }


    }

    private void loadWeb(final String url) {
        WebSettings webSettings = webView.getSettings();

        webSettings.setAppCacheEnabled(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setSaveFormData(false);
        webSettings.setSupportZoom(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);

        webView.setWebChromeClient(new WebContentChromeClient());
        webView.setWebViewClient(new WebContentClient(WebViewActivity.this, progress_bar));

        webView.setVerticalScrollBarEnabled(true);
        webView.setHorizontalScrollBarEnabled(true);

        mFileDownloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        webView.setDownloadListener(mDownloadListener);

        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

        String domain = preferenceUtilities.getCurrentCompanyDomain();

        CookieManager.getInstance().setCookie("http://" + domain, "skey0=" + preferenceUtilities.getCurrentMobileSessionId());
        CookieManager.getInstance().setCookie("http://" + domain, "skey1=" + "123123123123132");
        CookieManager.getInstance().setCookie("http://" + domain, "skey2=" + DeviceUtilities.getLanguageCode());
        CookieManager.getInstance().setCookie("http://" + domain, "skey3=" + preferenceUtilities.getCurrentCompanyNo());

        webView.loadUrl(url);
    }
    private DownloadManager mFileDownloadManager = null;
    private final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile("attachment\\s*;\\s*filename\\s*=\\s*\"*([^\"]*)\"*");

    private DownloadListener mDownloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            String fileName = parseContentDisposition(contentDisposition);
            Uri uriToDownload = Uri.parse(url);
            DownloadManager.Request fileDownloadRequest = new DownloadManager.Request(uriToDownload);
            fileDownloadRequest.setTitle(fileName);
            fileDownloadRequest.setDescription("커뮤니티 첨부파일 다운로드");
            fileDownloadRequest.setDestinationInExternalPublicDir("/Download", fileName);
            fileDownloadRequest.setVisibleInDownloadsUi(true);
            fileDownloadRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            Environment.getExternalStoragePublicDirectory("/Download").mkdir();
            mFileDownloadManager.enqueue(fileDownloadRequest);

        }

        private String parseContentDisposition(String contentDisposition) {
            try {
                Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
                if (m.find()) {
                    return java.net.URLDecoder.decode(m.group(1), "UTF-8");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
