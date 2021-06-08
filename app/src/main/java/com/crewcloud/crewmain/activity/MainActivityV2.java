package com.crewcloud.crewmain.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.crewcloud.crewmain.datamodel.Community;
import com.crewcloud.crewmain.datamodel.LoginDto;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
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

/**
 * Created by Dazone on 8/22/2017.
 */

public class MainActivityV2 extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, DevicePresenter.view {

    @Bind(R.id.rv_notice)
    RecyclerView rv_notice;

    @Bind(R.id.tvNotice)
    TextView tvNotice;

    @Bind(R.id.rv_approval)
    RecyclerView rv_approval;

    @Bind(R.id.textViewApproval)
    TextView tvApproval;

    @Bind(R.id.rv_unread_mail)
    RecyclerView rv_unread_mail;

    @Bind(R.id.tvMail)
    TextView tvMail;

    @Bind(R.id.rv_schedule)
    RecyclerView rv_schedule;

    @Bind(R.id.tvSchedule)
    TextView tvSchedule;

    @Bind(R.id.rvCommunity)
    RecyclerView rvCommunity;

    @Bind(R.id.tvCommunity)
    TextView tvCommunity;

    @Bind(R.id.fl_enabled_applications)
    RecyclerView rvApplication;

    ApplicationAdapter adapter;
    ApprovalAdapter approvalAdapter;
    NoticeAdapter noticeAdapter;
    NoticeAdapter communityAdapter;
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
        communityAdapter = new NoticeAdapter(this);
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

        LoginDto loginResult = new Gson().fromJson(preferenceUtilities.getUserData(), LoginDto.class);
        TextView tv_name = header.findViewById(R.id.tv_name);
        ImageView ivSetting = header.findViewById(R.id.iv_setting);

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

        TextView tv_email = header.findViewById(R.id.tv_email);
        ImageView iv_avatar = header.findViewById(R.id.iv_avatar);

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
                } else if (msg.what == Constants.ACTIVITY_HANDLER_START_UPDATE) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(R.string.string_update_content);

                    builder.setPositiveButton(R.string.login_button_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Async_DownloadApkFileCheckVersion(MainActivityV2.this, "CrewMain").execute();
                            dialog.dismiss();
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


        adapter.setOnClickListener(new ApplicationAdapter.onClickItemListener() {
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
        new WebClientAsync_GetCommunityList().execute();

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    Application application;

    private List<Application> mListOfApplications= new ArrayList<>();
    private List<ApprovalDocument> mListOfApprovalDocuments = new ArrayList<>();
    private List<ScheduleDocument> mListOfScheduleDocuments= new ArrayList<>();
    private List<Mail> mListOfUnreadMails= new ArrayList<>();
    private List<NoticeDocument> mListOfNotices= new ArrayList<>();
    private List<Community> mListCommunity= new ArrayList<>();

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
                    preferenceUtilities.getDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsFailed = false;

                            try {
                                mIsSuccess = (jsonNode.get("success").asInt() == 1);

                                if (mIsSuccess) {
                                    JsonNode data = jsonNode.get("data");
                                    int length = data.size();

                                    mListOfApplications = new ArrayList<>();

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

                if (CrewCloudApplication.getInstance().getPreferenceUtilities().getStringValue(Constants.COMPANY_NAME, "").equals(Constants.CHECK_DOMAIN_BODITECH)) {
                    Application coffee = new Application();
                    coffee.setProjectCode(Constants.PROJECT_CODE_COFFEE);
                    coffee.setPackageName(Constants.URL_COFFEE);
                    coffee.setApplicationName(getResources().getString(R.string.boditech));
                    lstApp.add(coffee);

                    Application pms = new Application();
                    pms.setProjectCode(Constants.PROJECT_CODE_PMS);
                    pms.setPackageName(Constants.URL_PMS);
                    pms.setApplicationName(getResources().getString(R.string.boditech));
                    lstApp.add(pms);
                }

                adapter.addAll(lstApp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class WebClientAsync_GetApprovalList extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetApprovalList(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    10, preferenceUtilities.getDomain(), new WebClient.OnWebClientListener() {
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

            setListOfApprovalDocuments();
        }
    }

    private class WebClientAsync_GetCommunityList extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetCommunityList(DeviceUtilities.getLanguageCode(), preferenceUtilities.getCurrentMobileSessionId(),
                     preferenceUtilities.getDomain(), new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {

                            try {
                                if (jsonNode.get("success").asInt() != 1) {
                                    return;
                                }

                                JsonNode dataNode = jsonNode.get("data");
                                Type listType = new TypeToken<ArrayList<Community>>(){}.getType();
                                List<Community> communities = new Gson().fromJson(dataNode.toString(), listType);
                                if(communities.size() > 0) {
                                    mListCommunity = new ArrayList<>();
                                    mListCommunity.addAll(communities);
                                }


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
            setListOfCommunity();
        }
    }

    private void setListOfApprovalDocuments() {
        if(Util.checkContainApp(adapter.getList(), Constants.PROJECT_CODE_APPROVAL)) {
            tvApproval.setVisibility(View.VISIBLE);
            rv_approval.setVisibility(View.VISIBLE);
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
        } else {
            tvApproval.setVisibility(View.GONE);
            rv_approval.setVisibility(View.GONE);
        }

    }

    private void setListOfSchedule() {
        if(Util.checkContainApp(adapter.getList(), Constants.PROJECT_CODE_SCHEDULE)) {
            tvSchedule.setVisibility(View.VISIBLE);
            rv_schedule.setVisibility(View.VISIBLE);
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
        } else {
            tvSchedule.setVisibility(View.GONE);
            rv_schedule.setVisibility(View.GONE);
        }


    }

    private void setListOfNotice() {
        if(Util.checkContainApp(adapter.getList(), Constants.PROJECT_CODE_NOTICE)) {
            tvNotice.setVisibility(View.VISIBLE);
            rv_notice.setVisibility(View.VISIBLE);
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
        } else {
            tvNotice.setVisibility(View.GONE);
            rv_notice.setVisibility(View.GONE);
        }

    }

    private void setListOfCommunity() {
        if(Util.checkContainApp(adapter.getList(), Constants.PROJECT_CODE_COMMUNITY)) {
            tvCommunity.setVisibility(View.VISIBLE);
            rvCommunity.setVisibility(View.VISIBLE);
            communityAdapter.addAllCommunity(mListCommunity);
            communityAdapter.setOnClickItem(new NoticeAdapter.onClickItemListener() {
                @Override
                public void onClick(int position) {
                    Application application = new Application();
                    application.setPackageName("com.crewcloud.apps.crewboard");
                    application.setApplicationName("Community");
                    application.setProjectCode(Constants.PROJECT_CODE_COMMUNITY);
                    application.setApplicationNo(9);
                    otherListApp(application, String.valueOf(mListCommunity.get(position).getBoardNo()));
                }
            });
            rvCommunity.setAdapter(communityAdapter);
        } else {
            tvCommunity.setVisibility(View.GONE);
            rvCommunity.setVisibility(View.GONE);
        }

    }

    private void setListOfUnreadMails() {
        if(Util.checkContainApp(adapter.getList(), Constants.PROJECT_CODE_MAIL)) {
            tvMail.setVisibility(View.VISIBLE);
            rv_unread_mail.setVisibility(View.VISIBLE);
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
        } else {
            tvMail.setVisibility(View.GONE);
            rv_unread_mail.setVisibility(View.GONE);
        }

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
                    startDay, endDay, scheduleType, preferenceUtilities.getDomain(), new WebClient.OnWebClientListener() {
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
                                        mListOfScheduleDocuments.addAll(listSchedule);
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

            setListOfSchedule();
        }
    }

    private class WebClientAsync_GetUnreadMails extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetUnreadMails(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    10, preferenceUtilities.getDomain(), new WebClient.OnWebClientListener() {
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

            setListOfUnreadMails();
        }
    }

    private class WebClientAsync_GetNotices extends AsyncTask<Void, Void, Void> {
        private boolean mIsFailed;
        private boolean mIsSuccess;

        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.GetNoticeList(DeviceUtilities.getLanguageCode(), DeviceUtilities.getTimeZoneOffset(), preferenceUtilities.getCurrentMobileSessionId(),
                    10, preferenceUtilities.getDomain(), new WebClient.OnWebClientListener() {
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

            setListOfNotice();
        }
    }

    // ----------------------------------------------------------------------------------------------

    private void otherApp(final Application application) {
        String packageName = application.PackageName;
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent == null) {
            if (application.getProjectCode().equals("OA") || application.getProjectCode().equals("HappyCall")) {
                Intent browserIntent = new Intent(MainActivityV2.this, WebViewActivity.class);
                browserIntent.putExtra("AAA", application.getPackageName());
                startActivity(browserIntent);
            } else {
                if(application.ProjectCode.equals(Constants.PROJECT_CODE_COFFEE) || application.ProjectCode.equals(Constants.PROJECT_CODE_PMS)) {
                    Intent intentBoditech = new Intent(MainActivityV2.this, BoditechCoffeeActivity.class);
                    intentBoditech.putExtra(Constants.TYPE_PROJECT_CODE_BODITECH, application.ProjectCode);
                    startActivity(intentBoditech);
                    return;
                }

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
                            case Constants.PROJECT_CODE_COFFEE:
                                apkFileName = Constants.PROJECT_CODE_COFFEE;
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
                            } else {
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
                        try {
                            JSONObject obj = new JSONObject(jsonNode.toString());
                            JSONObject objdata = new JSONObject(obj.get("data").toString());
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
                            case "Community":
                                apkFileName = "Community";
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
                        } else if (apkFileName.equals("Community")) {
                            if (mTempDomain.contains("core.crewcloud.net") || mTempDomain.contains("core")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewemail"));
                                startActivity(intent);
                            } else {
                                final String finalApkFileName = apkFileName;
                                new WebClientAsync_download(finalApkFileName).execute();
                            }
                        } else {
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
            devicePresenter.insertDevice(regID);
        }
    }
}
