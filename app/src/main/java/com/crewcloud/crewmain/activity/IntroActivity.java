package com.crewcloud.crewmain.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.crewcloud.crewmain.BuildConfig;
import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.Constants;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.util.DeviceUtilities;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.WebClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

public class IntroActivity extends AppCompatActivity {

    private String mCompanyName = "_woori";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_intro);


        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }

        if (checkPermissions()) {
            /*Thread thread = new Thread(new UpdateRunnable());
            thread.setDaemon(true);
            thread.start();*/
            startApplication();
        } else {
            setPermissions();
        }
    }

    private final int MY_PERMISSIONS_REQUEST_CODE = 1;

    private boolean checkPermissions() {
        // android.permission.INTERNET
        // android.permission.WRITE_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    private void setPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.INTERNET,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE
        }, MY_PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSIONS_REQUEST_CODE) {
            return;
        }

        boolean isGranted = true;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }

        if (isGranted) {
            startApplication();
        } else {
            Toast.makeText(this, R.string.introActivity_message_permission_denied, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startApplication() {
        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

        if (!TextUtils.isEmpty(preferenceUtilities.getCurrentMobileSessionId())) {
            new WebClientAsync_CheckSessionUser_v2().execute();
        } else {
            preferenceUtilities.setCurrentMobileSessionId("");
            preferenceUtilities.setCurrentCompanyNo(0);

            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            finish();
        }
    }

    // ----------------------------------------------------------------------------------------------

    private class ActivityHandler extends Handler {
        private final WeakReference<IntroActivity> mWeakActivity;

        private ActivityHandler(IntroActivity activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final IntroActivity activity = mWeakActivity.get();

            if (activity != null) {
                if (msg.what == Constants.ACTIVITY_HANDLER_NEXT_ACTIVITY) {
                    startApplication();
                } else if (msg.what == Constants.ACTIVITY_HANDLER_START_UPDATE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.string_update_content);

                    builder.setPositiveButton(R.string.login_button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Async_DownloadApkFile(IntroActivity.this, "CrewMain").execute();
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton(R.string.login_button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startApplication();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.setCancelable(false);
                    dialog.show();
                }
            }
        }
    }

    private final ActivityHandler mActivityHandler = new ActivityHandler(this);

    private class UpdateRunnable implements Runnable {
        @Override
        public void run() {
            try {
                URL txtUrl = new URL("http://www.crewcloud.net/Android/Version/CrewMain.txt");
                HttpURLConnection urlConnection = (HttpURLConnection) txtUrl.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String serverVersion = bufferedReader.readLine();
                inputStream.close();

                String appVersion = BuildConfig.VERSION_NAME;

                if (serverVersion.equals(appVersion)) {
                    mActivityHandler.sendEmptyMessageDelayed(Constants.ACTIVITY_HANDLER_NEXT_ACTIVITY, 1000);
                } else {
                    mActivityHandler.sendEmptyMessage(Constants.ACTIVITY_HANDLER_START_UPDATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Async_DownloadApkFile extends AsyncTask<Void, Void, Void> {
        private String mApkFileName;
        private final WeakReference<IntroActivity> mWeakActivity;
        private ProgressDialog mProgressDialog = null;

        private Async_DownloadApkFile(IntroActivity activity, String apkFileName) {
            mWeakActivity = new WeakReference<>(activity);
            mApkFileName = apkFileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            IntroActivity activity = mWeakActivity.get();

            if (activity != null) {
                mProgressDialog = new ProgressDialog(activity);
                mProgressDialog.setMessage(getString(R.string.mailActivity_message_download_apk));
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            BufferedInputStream bufferedInputStream = null;
            FileOutputStream fileOutputStream = null;

            try {
                //URL apkUrl = new URL("http://www.crewcloud.net/Android/Package/CrewMain__woori.apk");
               URL apkUrl = new URL("http://www.crewcloud.net/Android/Package/CrewMain.apk");
                urlConnection = (HttpURLConnection) apkUrl.openConnection();
                inputStream = urlConnection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";
                fileOutputStream = new FileOutputStream(filePath);

                byte[] buffer = new byte[4096];
                int readCount;

                while (true) {
                    readCount = bufferedInputStream.read(buffer);
                    if (readCount == -1) {
                        break;
                    }

                    fileOutputStream.write(buffer, 0, readCount);
                    fileOutputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (bufferedInputStream != null) {
                    try {
                        bufferedInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (urlConnection != null) {
                    try {
                        urlConnection.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            IntroActivity activity = mWeakActivity.get();

            if (activity != null) {
               String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";
              //  String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + "_woori.apk";

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(apkUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    activity.startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                    activity.startActivity(intent);
                }
            }

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    private class WebClientAsync_CheckSessionUser_v2 extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;
        private String mCompanyName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.CheckSessionUser_v2(DeviceUtilities.getLanguageCode(),
                    DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    "http://" + preferenceUtilities.getCurrentCompanyDomain(),
                    new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsFailed = false;

                            try {
                                mIsSuccess = (jsonNode.get("success").asInt() == 1);
                                mCompanyName = jsonNode.get("data").get("NameCompany").asText();
                            } catch (Exception e) {
                                e.printStackTrace();
                                mIsSuccess = false;
                            }
                        }

                        @Override
                        public void onFailure() {
                            mIsFailed = true;
                            mIsSuccess = false;
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mIsFailed) {
                Toast.makeText(IntroActivity.this, R.string.loginActivity_message_wrong_server_site, Toast.LENGTH_LONG).show();
                finish();
            } else {
                PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

                if (mIsSuccess) {
                    preferenceUtilities.setCurrentCompanyName(mCompanyName);

                    Intent intent = new Intent(IntroActivity.this, MainActivityV2.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                } else {
                    preferenceUtilities.setCurrentMobileSessionId("");
                    preferenceUtilities.setCurrentCompanyNo(0);

                    Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }
        }
    }
}