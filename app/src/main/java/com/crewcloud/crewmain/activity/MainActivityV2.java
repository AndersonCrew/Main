package com.crewcloud.crewmain.activity;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crewcloud.crewmain.BuildConfig;
import com.crewcloud.crewmain.Constants;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.adapter.ApplicationAdapter;
import com.crewcloud.crewmain.adapter.ApprovalAdapter;
import com.crewcloud.crewmain.adapter.MenuAdapter;
import com.crewcloud.crewmain.adapter.NoticeAdapter;
import com.crewcloud.crewmain.adapter.ScheduleAdapter;
import com.crewcloud.crewmain.adapter.UnreadMailAdapter;
import com.crewcloud.crewmain.datamodel.Application;
import com.crewcloud.crewmain.datamodel.ApprovalDocument;
import com.crewcloud.crewmain.datamodel.ErrorDto;
import com.crewcloud.crewmain.datamodel.LeftMenu;
import com.crewcloud.crewmain.datamodel.Login_v2_Result;
import com.crewcloud.crewmain.datamodel.Mail;
import com.crewcloud.crewmain.datamodel.NoticeDocument;
import com.crewcloud.crewmain.datamodel.ScheduleDocument;
import com.crewcloud.crewmain.module.device.DevicePresenter;
import com.crewcloud.crewmain.module.device.DevicePresenterImp;
import com.crewcloud.crewmain.util.DeviceUtilities;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.Statics;
import com.crewcloud.crewmain.util.Util;
import com.crewcloud.crewmain.util.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.leolin.shortcutbadger.ShortcutBadger;

import static com.crewcloud.crewmain.util.Util.compareVersionNames;
import static com.crewcloud.crewmain.util.Util.getApplicationName;

/**
 * Created by Dazone on 8/22/2017.
 */

public class MainActivityV2 extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, DevicePresenter.view {

    @Bind(R.id.rv_notice)
    RecyclerView rv_notice;

    @Bind(R.id.rv_approval)
    RecyclerView rv_approval;

    @Bind(R.id.rv_unread_mail)
    RecyclerView rv_unread_mail;

    @Bind(R.id.rv_schedule)
    RecyclerView rv_schedule;

    @Bind(R.id.fl_enabled_applications)
    RecyclerView rvApplication;

    ApplicationAdapter adapter;
    ApprovalAdapter approvalAdapter;
    NoticeAdapter noticeAdapter;
    ScheduleAdapter scheduleAdapter;
    UnreadMailAdapter unreadMailAdapter;
    MenuAdapter menuadapter;
    private Context context;
    private DevicePresenterImp devicePresenter;
    private GoogleCloudMessaging gcm;
    private String regID;
    private String msg = "";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final ActivityHandler mActivityHandler = new ActivityHandler(this);
    public static String urlDownload = "";
    private List<LeftMenu> lstMenu = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_v2);

        ButterKnife.bind(this);

        devicePresenter = new DevicePresenterImp(this);
        devicePresenter.attachView(this);
        createGMC();
        adapter = new ApplicationAdapter(this);
        approvalAdapter = new ApprovalAdapter(this);
        noticeAdapter = new NoticeAdapter(this);
        scheduleAdapter = new ScheduleAdapter(this);
        unreadMailAdapter = new UnreadMailAdapter(this);
        menuadapter = new MenuAdapter(this);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.start_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.start_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

        if (preferenceUtilities.getCurrentCompanyDomain().contains("wine.woorihom.com")) {
            setTitle("우리관리 그룹웨어");
        } else {
            setTitle(preferenceUtilities.getCurrentCompanyName());
        }

        Login_v2_Result loginResult = new Gson().fromJson(preferenceUtilities.getUserData(), Login_v2_Result.class);
        TextView tv_name = (TextView) header.findViewById(R.id.tv_name);
        ImageView ivSetting = (ImageView) header.findViewById(R.id.iv_setting);
        RecyclerView rvMenu = (RecyclerView) header.findViewById(R.id.menu);

//        lstMenu.add(new LeftMenu("전사공지"));
//        lstMenu.add(new LeftMenu("본사공지"));
//        lstMenu.add(new LeftMenu("그룹공지"));
//
//        rvMenu.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
//        rvMenu.setAdapter(menuadapter);
//        menuadapter.addAll(lstMenu);

        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivityV2.this, SettingActivity.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        if (!TextUtils.isEmpty(loginResult.FullName)) {
            tv_name.setText(loginResult.FullName);
        }

        TextView tv_email = (TextView) header.findViewById(R.id.tv_email);
        ImageView iv_avatar = (ImageView) header.findViewById(R.id.iv_avatar);

        if (!TextUtils.isEmpty(loginResult.avatar)) {
            PreferenceUtilities prefUtils = CrewCloudApplication.getInstance().getPreferenceUtilities();
            String serviceDomain = prefUtils.getCurrentServiceDomain();
            String avatar = prefUtils.getAvatar();
            String newAvatar = avatar.replaceAll("\"", "");
            String mUrl = serviceDomain + newAvatar;
            Picasso.with(this).load(mUrl).into(iv_avatar);
        }

        if (!TextUtils.isEmpty(loginResult.MailAddress)) {
            tv_email.setText(loginResult.MailAddress);
        }

        initView();

        new WebClientAsync_checkVersion("CrewMain").execute();

    }

    private void checkVersion(String nameApp) {
        WebClient.checkVersionUpdate(this, new WebClient.OnWebClientListener() {
            @Override
            public void onSuccess(JsonNode jsonNode) {
                String dataJson = jsonNode.get("version").textValue();
                urlDownload = jsonNode.get("packageUrl").textValue();
                if (!urlDownload.equals("")) {
                    Thread thread = new Thread(new UpdateRunnable(dataJson));
                    thread.setDaemon(true);
                    thread.start();
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // runs on UI thread
                            Toast.makeText(getApplicationContext(), R.string.can_not_check_version, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure() {

            }
        }, nameApp);
    }

    private class UpdateRunnable implements Runnable {
        String version = "";

        public UpdateRunnable(String version) {
            this.version = version;
        }

        @Override
        public void run() {
            try {
               /* URL txtUrl = new URL("http://www.crewcloud.net/Android/Version/CrewMain.txt");
                HttpURLConnection urlConnection = (HttpURLConnection) txtUrl.openConnection();

                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String serverVersion = bufferedReader.readLine();
                inputStream.close();
*/
                String appVersion = BuildConfig.VERSION_NAME;
                if (compareVersionNames(appVersion, version) == -1) {
                    mActivityHandler.sendEmptyMessage(Constants.ACTIVITY_HANDLER_START_UPDATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ActivityHandler extends Handler {
        private final WeakReference<MainActivityV2> mWeakActivity;

        private ActivityHandler(MainActivityV2 activity) {
            mWeakActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivityV2 activity = mWeakActivity.get();

            if (activity != null) {
                if (msg.what == Constants.ACTIVITY_HANDLER_NEXT_ACTIVITY) {
                    /* startApplication();*/
                } else if (msg.what == Constants.ACTIVITY_HANDLER_START_UPDATE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.string_update_content);

                    builder.setPositiveButton(R.string.login_button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Async_DownloadApkFileCheckVersion(MainActivityV2.this, "CrewMain").execute();
                            //new Async_DownloadApkFileCheckVersion(MainActivityV2.this, getApplicationName(getApplicationContext())).execute();
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton(R.string.login_button_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            /*  startApplication();*/
                        }
                    });
                    try {
                        AlertDialog dialog = builder.create();
                        dialog.setCancelable(false);
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class Async_DownloadApkFile extends AsyncTask<Void, Void, Void> {
        private String mApkFileName;
        private final WeakReference<MainActivityV2> mWeakActivity;
        private ProgressDialog mProgressDialog = null;

        private Async_DownloadApkFile(MainActivityV2 activity, String apkFileName) {
            mWeakActivity = new WeakReference<>(activity);
            mApkFileName = apkFileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MainActivityV2 activity = mWeakActivity.get();

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
                // URL apkUrl = new URL("http://www.crewcloud.net/Android/Package/CrewMain.apk");
                URL apkUrl = new URL(urlDownload);
                // URL apkUrl = new URL(Constants.ROOT_URL_ANDROID + Constants.PACKGE + mApkFileName + ".apk");
//                URL apkUrl = new URL("http://www.crewcloud.net/Android/Package/CrewMain.apk");
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

            MainActivityV2 activity = mWeakActivity.get();

            if (activity != null) {
//                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";

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

    private class Async_DownloadApkFileCheckVersion extends AsyncTask<Void, Void, Void> {
        private String mApkFileName;
        private final WeakReference<MainActivityV2> mWeakActivity;
        private ProgressDialog mProgressDialog = null;

        private Async_DownloadApkFileCheckVersion(MainActivityV2 activity, String apkFileName) {
            mWeakActivity = new WeakReference<>(activity);
            mApkFileName = apkFileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MainActivityV2 activity = mWeakActivity.get();

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
//                URL apkUrl = new URL("http://www.crewcloud.net/Android/Package/CrewMain.apk");
                URL apkUrl = new URL(urlDownload);
                // URL apkUrl = new URL(Constants.ROOT_URL_ANDROID + Constants.PACKGE + mApkFileName + ".apk");
//                URL apkUrl = new URL("http://www.crewcloud.net/Android/Package/CrewMain.apk");
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

            MainActivityV2 activity = mWeakActivity.get();

            if (activity != null) {
//                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + ".apk";

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

    private class WebClientAsync_checkVersion extends AsyncTask<Void, Void, Void> {

        String appName;

        private WebClientAsync_checkVersion(String finalApkFileName) {
            this.appName = finalApkFileName;
        }

        @Override
        protected Void doInBackground(Void... params) {

            checkVersion(appName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    private void initView() {
        rvApplication.setLayoutManager(new GridLayoutManager(this, 4));
        rv_approval.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_unread_mail.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_notice.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_schedule.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        rvApplication.setAdapter(adapter);
        rv_approval.setAdapter(approvalAdapter);
        rv_schedule.setAdapter(scheduleAdapter);
        rv_notice.setAdapter(noticeAdapter);
        rv_unread_mail.setAdapter(unreadMailAdapter);


        adapter.setOnClickLitener(new ApplicationAdapter.onClickItemListener() {
            @Override
            public void onClickItem(final Application application) {
                otherApp(application);
            }
        });
        new WebClientAsync_GetEnabledApplications().execute();
        new WebClientAsync_GetApprovalList().execute();
        new WebClientAsync_GetScheduleList().execute();
        new WebClientAsync_GetNotices().execute();
        new WebClientAsync_GetUnreadMails().execute();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    Application application;

    private List<Application> mListOfApplications;
    private List<ApprovalDocument> mListOfApprovalDocuments;
    private List<ScheduleDocument> mListOfScheduleDocuments;
    private List<Mail> mListOfUnreadMails;
    private List<NoticeDocument> mListOfNotices;

    @Override
    public void onSucess() {
        Log.wtf("INSERT_DEVICE", "Success");

    }

    @Override
    public void onError() {
        Log.wtf("INSERT_DEVICE", "Error");
    }

    private class WebClientAsync_GetEnabledApplications extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetEnabledApplications(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsFailed = false;

                            try {
                                mIsSuccess = (jsonNode.get("success").asInt() == 1);

                                if (mIsSuccess) {
                                    JsonNode data = jsonNode.get("data");
                                    int length = data.size();

                                    mListOfApplications = new ArrayList<>();
                                    // = new Application();
                                    //application.ApplicationNo = 0;
                                    //application.ProjectCode = "CrewChat";
                                    //application.ApplicationName = getString(R.string.app_name_crewchat);
                                    //application.PackageName = "com.dazone.crewchat";

                                    //mListOfApplications.add(application);

                                    JsonNode appNode;

                                    for (int i = 0; i < length; i++) {
                                        appNode = data.get(i);

                                        application = new Application();
                                        mListOfApplications.add(application);

                                        application.ApplicationNo = appNode.get("ApplicationNo").asInt();
                                        application.ProjectCode = appNode.get("ProjectCode").asText();
                                        application.ApplicationName = appNode.get("ApplicationName").asText();
                                        application.PackageName = appNode.get("PackageName").asText();
                                        if (appNode.get("totalUnreadCount") != null) {
                                            application.totalUnreadCount = appNode.get("totalUnreadCount").asText();
                                        } else {
                                            application.totalUnreadCount = "";
                                        }

                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mIsSuccess = false;
                            }
                        }

                        @Override
                        public void onFailure() {
                            mIsFailed = true;
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
            if (!TextUtils.isEmpty(application.totalUnreadCount)) {
                ShortcutBadger.applyCount(getApplicationContext(), Integer.parseInt(application.totalUnreadCount));
            }
            List<Application> lstApp = new ArrayList<>();
            for (Application app : mListOfApplications) {
                if (!TextUtils.isEmpty(app.PackageName)) {
                    lstApp.add(app);
                }

            }
            adapter.addAll(lstApp);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Application happyCall = new Application();
//            happyCall.setProjectCode("HappyCall");
//            happyCall.setPackageName("http://wine.woorihom.com/UI/MobileHappyCall/");
//            happyCall.setApplicationName("해피콜");
//            mListOfApplications.add(happyCall);L
//            Application board = new Application();
//            board.setProjectCode("OA");
//            board.setPackageName("http://wine.woorihom.com/UI/mobileallboard");
//            board.setApplicationName("게시판");
//            mListOfApplications.add(board);

        }
    }

    private class WebClientAsync_GetApprovalList extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetApprovalList(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    10, "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsFailed = false;

                            try {
                                if (jsonNode.get("success").asInt() != 1) {
                                    mIsSuccess = false;
                                    return;
                                }

                                mIsSuccess = true;

                                JsonNode dataNode = jsonNode.get("data");
                                int length = dataNode.size();

                                mListOfApprovalDocuments = new ArrayList<>();
                                ApprovalDocument approvalDocument;

                                JsonNode itemNode;

                                for (int i = 0; i < length; i++) {
                                    itemNode = dataNode.get(i);

                                    approvalDocument = new ApprovalDocument();
                                    approvalDocument.ID = itemNode.get("ID").asLong();
                                    approvalDocument.DocumentID = itemNode.get("DocumentID").asLong();
                                    approvalDocument.Title = itemNode.get("Title").asText();
                                    approvalDocument.RegDate = itemNode.get("RegDate").asText();
                                    approvalDocument.WriterName = itemNode.get("WriterName").asText();
                                    approvalDocument.AccessName = itemNode.get("AccessName").asText();

                                    mListOfApprovalDocuments.add(approvalDocument);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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

            if (!mIsFailed && mIsSuccess) {
                setListOfApprovalDocuments();
            }
        }
    }

    private void setListOfApprovalDocuments() {

        approvalAdapter.addAll(mListOfApprovalDocuments);
        approvalAdapter.setOnClickItem(new ApprovalAdapter.onClickItemListener() {
            @Override
            public void onClick(int position) {

                Application application = new Application();
                application.setPackageName("com.crewcloud.apps.crewapproval");
                application.setApplicationName("Approval");
                application.setProjectCode("_EAPP");
                application.setApplicationNo(13);
                otherListApp(application, String.valueOf(mListOfApprovalDocuments.get(position).ID));
            }
        });
//        ll_approval_documents.removeAllViews();
//
//        LayoutInflater layoutInflater = getLayoutInflater();
//        RelativeLayout rl_inflate_layout_approval_document_item;
//        TextView tv_inflate_layout_approval_document_item_type, tv_inflate_layout_approval_document_item_title, tv_inflate_layout_approval_document_item_date;
//
//        for (ApprovalDocument document : mListOfApprovalDocuments) {
//            rl_inflate_layout_approval_document_item = (RelativeLayout) layoutInflater.inflate(R.layout.inflate_layout_approval_document_item, ll_approval_documents, false);
//            ll_approval_documents.addView(rl_inflate_layout_approval_document_item);
//
//            tv_inflate_layout_approval_document_item_type = (TextView) rl_inflate_layout_approval_document_item.findViewById(R.id.tv_inflate_layout_approval_document_item_type);
//            tv_inflate_layout_approval_document_item_title = (TextView) rl_inflate_layout_approval_document_item.findViewById(R.id.tv_inflate_layout_approval_document_item_title);
//            tv_inflate_layout_approval_document_item_date = (TextView) rl_inflate_layout_approval_document_item.findViewById(R.id.tv_inflate_layout_approval_document_item_date);
//
//            tv_inflate_layout_approval_document_item_type.setText("[" + document.AccessName + "]");
//            tv_inflate_layout_approval_document_item_title.setText(document.Title);
//            tv_inflate_layout_approval_document_item_date.setText(document.RegDate);
//        }
    }

    private void setListOfSchedule() {
        scheduleAdapter.addAll(mListOfScheduleDocuments);
        scheduleAdapter.setOnClickItem(new ScheduleAdapter.onClickItemListener() {
            @Override
            public void onClick(int position) {

                Application application = new Application();
                application.setPackageName("com.dazone.crewschedule");
                application.setApplicationName("Schedule");
                application.setProjectCode("Schedule");
                application.setApplicationNo(4);
                otherListApp(application, String.valueOf(mListOfScheduleDocuments.get(position).ScheduleNo));
            }
        });

    }

    private void setListOfNotice() {
        noticeAdapter.addAll(mListOfNotices);
        noticeAdapter.setOnClickItem(new NoticeAdapter.onClickItemListener() {
            @Override
            public void onClick(int position) {
                Application application = new Application();
                application.setPackageName("com.crewcloud.apps.crewnotice");
                application.setApplicationName("Notice");
                application.setProjectCode("Notice");
                application.setApplicationNo(9);
                otherListApp(application, String.valueOf(mListOfNotices.get(position).NoticeNo));
            }
        });
    }

    private void setListOfUnreadMails() {
        unreadMailAdapter.addAll(mListOfUnreadMails);
        unreadMailAdapter.setOnClickItem(new UnreadMailAdapter.onClickItemListener() {
            @Override
            public void onClick(int position) {
                Application application = new Application();
                application.setPackageName("com.crewcloud.apps.crewMail");
                application.setApplicationName("eMail");
                application.setProjectCode("Mail3");
                application.setApplicationNo(2);
                otherListApp(application, String.valueOf(mListOfUnreadMails.get(position).MailNo));
            }
        });
    }

    private class WebClientAsync_GetScheduleList extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            String startDay = sdf.format(c.getTime());
            String endDay = sdf.format(c.getTime());
            int scheduleType = 0;
            WebClient.GetScheduleList(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    startDay, endDay, scheduleType, "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsFailed = false;

                            try {
                                if (jsonNode.get("success").asInt() != 1) {
                                    mIsSuccess = false;
                                    return;
                                }

                                mIsSuccess = true;

                                JsonNode dataNode = jsonNode.get("data");
                                int length = dataNode.size();

                                mListOfScheduleDocuments = new ArrayList<>();

                                JsonNode itemNode;

                                for (int i = 0; i < length; i++) {
                                    itemNode = dataNode.get(i);
                                    List<ScheduleDocument> listSchedule;
                                    Gson gson = new GsonBuilder().create();
                                    listSchedule = gson.fromJson(itemNode.toString(), new TypeToken<List<ScheduleDocument>>() {
                                    }.getType());

                                    if (!listSchedule.isEmpty()) {
//                                    scheduleDocument.ScheduleNo = itemNode.get("ScheduleNo").asLong();
//                                    scheduleDocument.CalendarNo = itemNode.get("CalendarNo").asLong();
//                                    scheduleDocument.Title = itemNode.get("Title").asText();
//                                    scheduleDocument.CalendarType = itemNode.get("CalendarType").asInt();
//                                    scheduleDocument.DivisionNo = itemNode.get("DivisionNo").asInt();
//                                    scheduleDocument.CalendarColor = itemNode.get("CalendarColor").asText();
//                                    scheduleDocument.StartTime = itemNode.get("StartTime").asText();
//                                    scheduleDocument.EndTime = itemNode.get("EndTime").asText();
//                                    mListOfScheduleDocuments.add(scheduleDocument);
//                                        for (int j = 0; j < listSchedule.size(); j++) {
//                                            ScheduleDocument scheduleDocument = listSchedule.get(i);
//                                            mListOfScheduleDocuments.add(scheduleDocument);
                                        mListOfScheduleDocuments.addAll(listSchedule);
//                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
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

            if (!mIsFailed && mIsSuccess) {
                setListOfSchedule();
            }
        }
    }

    private class WebClientAsync_GetUnreadMails extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetUnreadMails(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    10, "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsFailed = false;

                            try {
                                mIsSuccess = (jsonNode.get("success").asInt() == 1);

                                if (mIsSuccess) {
                                    JsonNode data = jsonNode.get("data");
                                    int length = data.size();

                                    mListOfUnreadMails = new ArrayList<>();
                                    Mail mail;
                                    JsonNode appNode;

                                    for (int i = 0; i < length; i++) {
                                        appNode = data.get(i);

                                        mail = new Mail();
                                        mListOfUnreadMails.add(mail);
                                        mail.MailNo = appNode.get("MailNo").asLong();
                                        mail.BoxNo = appNode.get("BoxNo").asLong();
                                        mail.BoxName = appNode.get("BoxName").asText();
                                        mail.Title = appNode.get("Title").asText();
                                        mail.RegDate = appNode.get("RegDate").asText();
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mIsSuccess = false;
                            }
                        }

                        @Override
                        public void onFailure() {
                            mIsFailed = true;
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!mIsFailed && mIsSuccess) {
                setListOfUnreadMails();
            }
        }
    }

    private class WebClientAsync_GetNotices extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetNoticeList(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    10, "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsFailed = false;

                            try {
                                mIsSuccess = (jsonNode.get("success").asInt() == 1);

                                if (mIsSuccess) {
                                    JsonNode data = jsonNode.get("data");
                                    int length = data.size();

                                    mListOfNotices = new ArrayList<>();
                                    NoticeDocument notice;
                                    JsonNode appNode;

                                    for (int i = 0; i < length; i++) {
                                        appNode = data.get(i);

                                        notice = new NoticeDocument();
//                                        mListOfNotices.add(notice);
                                        notice.NoticeNo = Long.parseLong(appNode.get("NoticeNo").asText());
                                        notice.Title = appNode.get("Title").asText();
                                        notice.DivisionName = appNode.get("DivisionName").asText();
                                        mListOfNotices.add(notice);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mIsSuccess = false;
                            }
                        }

                        @Override
                        public void onFailure() {
                            mIsFailed = true;
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!mIsFailed && mIsSuccess) {
                setListOfNotice();
            }
        }
    }

    // ----------------------------------------------------------------------------------------------


/*    private class WebClientAsync_Logout_v2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.Logout_v2(preferenceUtilities.getCurrentMobileSessionId(),
                    "http://" + preferenceUtilities.getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                        }

                        @Override
                        public void onFailure() {
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
            preferenceUtilities.setCurrentMobileSessionId("");
            preferenceUtilities.setCurrentCompanyNo(0);

            Intent intent = new Intent(MainActivityV2.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }*/

    // ----------------------------------------------------------------------------------------------

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void otherApp(final Application application) {
        String packageName = application.PackageName;
     /*   PackageManager pm = context.getPackageManager();
        boolean installed = isPackageInstalled("gw.se-won.co.kr");
        if(installed) {
            //This intent will help you to launch if the package is already installed
            Intent LaunchIntent = getPackageManager() .getLaunchIntentForPackage("com.Ch.Example.pack");
            startActivity(LaunchIntent); } else {

        }*/

        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);


      /*  if (mTempDomain.equals(domainSeverLogin_1)) {

        } else {*/
        if (intent == null) {
            if (application.getProjectCode().equals("OA") || application.getProjectCode().equals("HappyCall")) {
                Intent browserIntent = new Intent(MainActivityV2.this, WebViewActivity.class);
                browserIntent.putExtra("AAA", application.getPackageName());
                startActivity(browserIntent);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityV2.this);
                builder.setMessage(getString(R.string.mailActivity_message_install_apk, application.ApplicationName));
                builder.setNegativeButton(R.string.common_alert_dialog_no, null);
                builder.setPositiveButton(R.string.common_alert_dialog_install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String apkFileName = "";
                        //check if domain sewon
                        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
                        String mTempDomain = "" + preferenceUtilities.getCurrentCompanyDomain();
                        String domainSeverLogin_1 = "gw.se-won.co.kr";
                        switch (application.ProjectCode) {
                            case "_EAPP":
                                apkFileName = "CrewApproval";
                                break;
                            case "Mail3":
                                apkFileName = "CrewMail";
                                break;
                            case "Schedule":
                                apkFileName = "CrewSchedule";
                                break;
                            case "DDay":
                                apkFileName = "CrewDday";
                                break;
                            case "Board":
                                apkFileName = "CrewBoard";
                                break;
                            case "Notice":
                                apkFileName = "CrewNotice";
                                break;
                            case "Contacts":
                                apkFileName = "CrewContacts";
                                break;
                            case "CrewChat":
                                apkFileName = "CrewChat";
                                break;
                            case "WorkingTime":
                                apkFileName = "CrewTimeCard";
                                break;
                        }

                        if (apkFileName.equals("CrewChat")) {
                            if (mTempDomain.contains("crewcloud.net") || mTempDomain.contains("core")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewchat"));
                                startActivity(intent);
                            } else {
                                final String finalApkFileName = apkFileName;
                                new WebClientAsync_download(finalApkFileName).execute();
                            }
                        } else if (apkFileName.equals("CrewTimeCard")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=" + "timecard.dazone.com.dazonetimecard"));
                            startActivity(intent);
                        } else if (apkFileName.equals("CrewDday")) {
                            if (mTempDomain.contains("crewcloud.net") || mTempDomain.contains("core")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewdday"));
                                startActivity(intent);
                            }else {
                                final String finalApkFileName = apkFileName;
                                new WebClientAsync_download(finalApkFileName).execute();
                            }
                        } else if (apkFileName.equals("CrewMail")) {
                            if (mTempDomain.contains("crewcloud.net") || mTempDomain.contains("core")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewemail"));
                                startActivity(intent);
                            } else {
                                final String finalApkFileName = apkFileName;
                                new WebClientAsync_download(finalApkFileName).execute();
                            }
                        } else {
                            // new Async_DownloadApkFile(MainActivityV2.this, apkFileName).execute();
                            // checkVersion();
                            final String finalApkFileName = apkFileName;
                            new WebClientAsync_download(finalApkFileName).execute();

                        }

                    }
                });

                builder.show();
            }

        } else {

            startActivity(intent);
        }
        // }

    }

    private class WebClientAsync_download extends AsyncTask<Void, Void, Void> {
        String apkFileName;

        private WebClientAsync_download() {

        }

        public WebClientAsync_download(String finalApkFileName) {
            this.apkFileName = finalApkFileName;
        }

        @Override
        protected Void doInBackground(Void... params) {

            WebClient.checkVersionUpdate(getApplicationContext(), new WebClient.OnWebClientListener() {
                @Override
                public void onSuccess(JsonNode jsonNode) {
                    try {
                        String dataJson = jsonNode.get("version").textValue();
                        urlDownload = jsonNode.get("packageUrl").textValue();
                        if (!urlDownload.equals("")) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    // runs on UI thread
                                    new Async_DownloadApkFile(MainActivityV2.this, apkFileName).execute();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    // runs on UI thread
                                    Toast.makeText(getApplicationContext(), R.string.file_not_found_error, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    } catch (Exception e) {
                        String json = jsonNode.get("data").textValue();
                        try {
                            JSONObject obj = new JSONObject(jsonNode.toString());
                            JSONObject objdata = new JSONObject(obj.get("data").toString());
                            String dataJson = objdata.getString("version");
                            urlDownload = objdata.getString("packageUrl");
                            if (!urlDownload.equals("")) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        // runs on UI thread
                                        new Async_DownloadApkFile(MainActivityV2.this, apkFileName).execute();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        // runs on UI thread
                                        Toast.makeText(getApplicationContext(), R.string.file_not_found_error, Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }


                }

                @Override
                public void onFailure() {

                }
            }, apkFileName);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

    private void otherListApp(final Application application, String no) {
        String packageName = application.PackageName;


        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

        if (intent == null) {
            if (application.getProjectCode().equals("OA") || application.getProjectCode().equals("HappyCall")) {
                Intent browserIntent = new Intent(MainActivityV2.this, WebViewActivity.class);
                browserIntent.putExtra("AAA", application.getPackageName());
                startActivity(browserIntent);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityV2.this);
                builder.setMessage(getString(R.string.mailActivity_message_install_apk, application.ApplicationName));
                builder.setNegativeButton(R.string.common_alert_dialog_no, null);
                builder.setPositiveButton(R.string.common_alert_dialog_install, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String apkFileName = "";
                        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
                        String mTempDomain = "" + preferenceUtilities.getCurrentCompanyDomain();
                        switch (application.ProjectCode) {
                            case "_EAPP":
                                apkFileName = "CrewApproval";
                                break;
                            case "Mail3":
                                apkFileName = "CrewMail";
                                break;
                            case "Schedule":
                                apkFileName = "CrewSchedule";
                                break;
                            case "DDay":
                                apkFileName = "CrewDday";
                                break;
                            case "Board":
                                apkFileName = "CrewBoard";
                                break;
                            case "Notice":
                                apkFileName = "CrewNotice";
                                break;
                            case "Contacts":
                                apkFileName = "CrewContacts";
                                break;
                            case "CrewChat":
                                apkFileName = "CrewChat";
                                break;
                            case "WorkingTime":
                                apkFileName = "CrewTimeCard";
                                break;
                        }

                        if (apkFileName.equals("CrewChat")) {
                            if (mTempDomain.contains("core.crewcloud.net") || mTempDomain.contains("core")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewchat"));
                                startActivity(intent);
                            } else {
                                final String finalApkFileName = apkFileName;
                                new WebClientAsync_download(finalApkFileName).execute();
                            }
                        } else if (apkFileName.equals("CrewTimeCard")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=" + "timecard.dazone.com.dazonetimecard"));
                            startActivity(intent);
                        } else if (apkFileName.equals("CrewDday")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewdday"));
                            startActivity(intent);
                        } else if (apkFileName.equals("CrewMail")) {
                            if (mTempDomain.contains("core.crewcloud.net") || mTempDomain.contains("core")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewemail"));
                                startActivity(intent);
                            } else {
                                final String finalApkFileName = apkFileName;
                                new WebClientAsync_download(finalApkFileName).execute();
                            }
                        } else {
                            //  new Async_DownloadApkFile(MainActivityV2.this, apkFileName).execute();
                            final String finalApkFileName = apkFileName;
                            new WebClientAsync_download(finalApkFileName).execute();
                        }
                    }
                });

                builder.show();
            }
        } else {
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, no);
            intent.setType("text/plain");
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

    }

    private void createGMC() {
        context = getApplicationContext();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regID = new PreferenceUtilities().getGCMregistrationid();
            if (regID.isEmpty()) {
                registerInBackground();
            } else {
                devicePresenter.insertDevice(regID);
            }
        } else {
            dismissProgressDialog();
//            Intent newIntent = new Intent(this, MainActivityV2.class);
//            newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            startActivity(newIntent);
//            callActivity(MainActivityV2.class);
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private void registerInBackground() {
        new register().execute("");
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.d("TAG", "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public class register extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                regID = gcm.register(Statics.GOOGLE_SENDER_ID_MAIL);
                msg = "Device registered, registration ID=" + regID;
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            new PreferenceUtilities().setGCMregistrationid(regID);
//            callActivity(MainActivityV2.class);
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//            finish();
            devicePresenter.insertDevice(regID);

        }

    }


}
