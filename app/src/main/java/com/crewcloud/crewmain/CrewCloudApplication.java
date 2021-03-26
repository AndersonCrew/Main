package com.crewcloud.crewmain;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.Statics;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CrewCloudApplication extends Application {
    private static CrewCloudApplication mInstance;
    private static PreferenceUtilities mPreferenceUtilities;
    private RequestQueue mRequestQueue;

    public CrewCloudApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handleSSLHandshake();
        mInstance = this;
    }

    /**
     * Enables https connections
     */
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    public static synchronized CrewCloudApplication getInstance() {
        return mInstance;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setRetryPolicy(new DefaultRetryPolicy(Statics.REQUEST_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? "Application" : tag);
        getRequestQueue().add(req);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public synchronized PreferenceUtilities getPreferenceUtilities() {
        if (mPreferenceUtilities == null) {
            mPreferenceUtilities = new PreferenceUtilities();
        }

        return mPreferenceUtilities;
    }
}