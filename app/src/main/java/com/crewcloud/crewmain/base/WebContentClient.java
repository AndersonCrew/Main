package com.crewcloud.crewmain.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.MailTo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.crewcloud.crewmain.activity.WebViewActivity;


public class WebContentClient extends WebViewClient {

    private WebViewActivity ParentActivity = null;
    private ProgressBar mProgressBar;

    public WebContentClient(WebViewActivity activity, ProgressBar progressBar) {
        this.ParentActivity = activity;
        this.mProgressBar = progressBar;
    }

    private Intent newEmailIntent(Context context, String address, String subject, String body, String cc) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { address });
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(url));
            ParentActivity.startActivity(intent);
            return true;
        }else if (url.startsWith("mailto:")) {
            MailTo mt = MailTo.parse(url);
            Intent email = newEmailIntent(ParentActivity, mt.getTo(), mt.getSubject(), mt.getBody(), mt.getCc());
            ParentActivity.startActivity(Intent.createChooser(email, "Choose an Email client :"));

            return true;

        } else if (url.startsWith("logout:")) {
        }else {
            view.loadUrl(url);
        }

        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        mProgressBar.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        handler.proceed() ;
    }

}