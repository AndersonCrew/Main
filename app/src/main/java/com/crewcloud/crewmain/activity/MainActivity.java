package com.crewcloud.crewmain.activity;

import android.app.ProgressDialog;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crewcloud.crewmain.BuildConfig;
import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.Constants;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.datamodel.Application;
import com.crewcloud.crewmain.datamodel.ApprovalDocument;
import com.crewcloud.crewmain.datamodel.LoginDto;
import com.crewcloud.crewmain.datamodel.Mail;
import com.crewcloud.crewmain.datamodel.NoticeDocument;
import com.crewcloud.crewmain.datamodel.ScheduleDocument;
import com.crewcloud.crewmain.util.DeviceUtilities;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apmem.tools.layouts.FlowLayout;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import me.leolin.shortcutbadger.ShortcutBadger;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    Application application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
            }
        });

        if (!TextUtils.isEmpty(loginResult.FullName)) {
            tv_name.setText(loginResult.FullName);
        }

        TextView tv_email = header.findViewById(R.id.tv_email);

        if (!TextUtils.isEmpty(loginResult.NameCompany)) {
            tv_email.setText(loginResult.NameCompany);
        }

        findViewsOfActivity();

        new WebClientAsync_GetEnabledApplications().execute();
    }

    private boolean mIsBackPressed = false;

    private static class ActivityHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private ActivityHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                activity.setBackPressed(false);
            }
        }
    }

    private final ActivityHandler mActivityHandler = new ActivityHandler(this);

    public void setBackPressed(boolean isBackPressed) {
        mIsBackPressed = isBackPressed;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.start_drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (!mIsBackPressed) {
                Toast.makeText(this, R.string.mainActivity_message_exit, Toast.LENGTH_SHORT).show();
                mIsBackPressed = true;
                mActivityHandler.sendEmptyMessageDelayed(0, 2000);
            } else {
                finish();
            }
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int idOfMenuItem = item.getItemId();

        if (idOfMenuItem == R.id.nav_menu_item_logout) {
            new WebClientAsync_Logout_v2().execute();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.start_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private FlowLayout fl_enabled_applications;
    private RelativeLayout rl_approval_documents, rl_unread_mails, rl_schedule, rl_notice;
    private LinearLayout ll_approval_documents, ll_unread_mails, ll_schedule, ll_notice;

    private void findViewsOfActivity() {
        fl_enabled_applications = (FlowLayout) findViewById(R.id.fl_enabled_applications);

        rl_approval_documents = (RelativeLayout) findViewById(R.id.rl_approval_documents);
        rl_unread_mails = (RelativeLayout) findViewById(R.id.rl_unread_mails);
        rl_schedule = (RelativeLayout) findViewById(R.id.rl_schedule);
        rl_notice = (RelativeLayout) findViewById(R.id.rl_notice);

        ll_approval_documents = (LinearLayout) findViewById(R.id.ll_approval_documents);
        ll_unread_mails = (LinearLayout) findViewById(R.id.ll_unread_mails);
        ll_schedule = (LinearLayout) findViewById(R.id.ll_schedule);
        ll_notice = (LinearLayout) findViewById(R.id.ll_notice);
    }

    private void setListOfApplications() {
        fl_enabled_applications.removeAllViews();

        LayoutInflater layoutInflater = getLayoutInflater();
        LinearLayout ll_inflate_layout_app_item;
        ImageView iv_inflate_layout_app_item_icon;
        TextView tv_inflate_layout_app_item_name;
        TextView tvBadge;
        Application happyCall = new Application();
        happyCall.setProjectCode("HappyCall");
        happyCall.setPackageName("http://wine.woorihom.com/UI/MobileHappyCall/");
        happyCall.setApplicationName("해피콜");
        mListOfApplications.add(happyCall);
        Application board = new Application();
        board.setProjectCode("OA");
        board.setPackageName("http://wine.woorihom.com/UI/mobileallboard");
        board.setApplicationName("게시판");
        mListOfApplications.add(board);

        for (Application application : mListOfApplications) {
            if (TextUtils.isEmpty(application.PackageName)) {
                continue;
            }

            ll_inflate_layout_app_item = (LinearLayout) layoutInflater.inflate(R.layout.inflate_layout_app_item, fl_enabled_applications, false);
            fl_enabled_applications.addView(ll_inflate_layout_app_item);

            ll_inflate_layout_app_item.setTag(application);
            ll_inflate_layout_app_item.setOnClickListener(this);

            iv_inflate_layout_app_item_icon = ll_inflate_layout_app_item.findViewById(R.id.iv_inflate_layout_app_item_icon);
            tv_inflate_layout_app_item_name = ll_inflate_layout_app_item.findViewById(R.id.tv_inflate_layout_app_item_name);
            tvBadge = ll_inflate_layout_app_item.findViewById(R.id.badge_notification_3);

            switch (application.ProjectCode) {
                case "_EAPP": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.approval);
                    rl_approval_documents.setVisibility(View.VISIBLE);
                    ll_approval_documents.setVisibility(View.VISIBLE);

                    new WebClientAsync_GetApprovalList().execute();

                    break;
                }

                case "Mail3": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.mail);
                    rl_unread_mails.setVisibility(View.VISIBLE);
                    ll_unread_mails.setVisibility(View.VISIBLE);

                    new WebClientAsync_GetUnreadMails().execute();

                    break;
                }

                case "Schedule": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.schedule);
                    rl_schedule.setVisibility(View.VISIBLE);
                    ll_schedule.setVisibility(View.VISIBLE);

                    new WebClientAsync_GetScheduleList().execute();

                    break;
                }

                case "DDay": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.dday);
                    break;
                }

                case "OA": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_allboad);
                    break;
                }
                case "HappyCall": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.ic_happy_call);
                    break;
                }
                case "Board": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.community);
                    break;
                }

                case "Notice": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.notice);
                    rl_notice.setVisibility(View.VISIBLE);
                    ll_notice.setVisibility(View.VISIBLE);

                    new WebClientAsync_GetNotices().execute();

                    break;
                }

                case "Contacts": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.contact);
                    break;
                }

                case "CrewChat": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.chatting);
                    if (!application.totalUnreadCount.equals("0")) {
                        ShortcutBadger.applyCount(this, Integer.parseInt(application.totalUnreadCount));
                        tvBadge.setVisibility(View.VISIBLE);
                        tvBadge.setText(application.totalUnreadCount);
                    }
                    break;
                }

                case "WorkingTime": {
                    iv_inflate_layout_app_item_icon.setImageResource(R.drawable.time_card);
                    break;
                }

                default: {
                    break;
                }
            }
            tv_inflate_layout_app_item_name.setText(application.ApplicationName);
        }
    }

    private void setListOfApprovalDocuments() {
        ll_approval_documents.removeAllViews();

        LayoutInflater layoutInflater = getLayoutInflater();
        RelativeLayout rl_inflate_layout_approval_document_item;
        TextView tv_inflate_layout_approval_document_item_type, tv_inflate_layout_approval_document_item_title, tv_inflate_layout_approval_document_item_date;

        for (ApprovalDocument document : mListOfApprovalDocuments) {
            rl_inflate_layout_approval_document_item = (RelativeLayout) layoutInflater.inflate(R.layout.inflate_layout_approval_document_item, ll_approval_documents, false);
            ll_approval_documents.addView(rl_inflate_layout_approval_document_item);

            tv_inflate_layout_approval_document_item_type = rl_inflate_layout_approval_document_item.findViewById(R.id.tv_inflate_layout_approval_document_item_type);
            tv_inflate_layout_approval_document_item_title = rl_inflate_layout_approval_document_item.findViewById(R.id.tv_inflate_layout_approval_document_item_title);
            tv_inflate_layout_approval_document_item_date = rl_inflate_layout_approval_document_item.findViewById(R.id.tv_inflate_layout_approval_document_item_date);

            tv_inflate_layout_approval_document_item_type.setText("[" + document.AccessName + "]");
            tv_inflate_layout_approval_document_item_title.setText(document.Title);
            tv_inflate_layout_approval_document_item_date.setText(document.RegDate);
        }
    }

    private void setListOfSchedule() {
        ll_schedule.removeAllViews();

        LayoutInflater layoutInflater = getLayoutInflater();
        RelativeLayout rl_inflate_layout_schedule_item;
        TextView tv_inflate_layout_unread_mail_item_name; // tv_inflate_layout_unread_mail_item_date;

        for (ScheduleDocument scheduleDocument : mListOfScheduleDocuments) {
            rl_inflate_layout_schedule_item = (RelativeLayout) layoutInflater.inflate(R.layout.inflate_layout_schedule_item, ll_schedule, false);
            ll_schedule.addView(rl_inflate_layout_schedule_item);

            tv_inflate_layout_unread_mail_item_name = rl_inflate_layout_schedule_item.findViewById(R.id.tv_inflate_layout_unread_mail_item_name);
            tv_inflate_layout_unread_mail_item_name.setText(scheduleDocument.Title);
        }
    }

    private void setListOfNotice() {
        ll_notice.removeAllViews();

        LayoutInflater layoutInflater = getLayoutInflater();
        RelativeLayout rl_inflate_layout_schedule_item;
        TextView tv_inflate_layout_unread_mail_item_name, tv_inflate_layout_unread_mail_item_date;

        for (NoticeDocument noticeDocument : mListOfNotices) {
            rl_inflate_layout_schedule_item = (RelativeLayout) layoutInflater.inflate(R.layout.inflate_layout_schedule_item, ll_notice, false);
            ll_notice.addView(rl_inflate_layout_schedule_item);

            tv_inflate_layout_unread_mail_item_name = rl_inflate_layout_schedule_item.findViewById(R.id.tv_inflate_layout_unread_mail_item_name);
            tv_inflate_layout_unread_mail_item_date = rl_inflate_layout_schedule_item.findViewById(R.id.tv_inflate_layout_unread_mail_item_date);

            tv_inflate_layout_unread_mail_item_name.setText(noticeDocument.Title);
            tv_inflate_layout_unread_mail_item_date.setText(noticeDocument.DivisionName);
        }
    }

    private void setListOfUnreadMails() {
        ll_unread_mails.removeAllViews();

        LayoutInflater layoutInflater = getLayoutInflater();
        RelativeLayout rl_inflate_layout_unread_mail_item;
        TextView tv_inflate_layout_unread_mail_item_name, tv_inflate_layout_unread_mail_item_date;

        for (Mail mail : mListOfUnreadMails) {
            rl_inflate_layout_unread_mail_item = (RelativeLayout) layoutInflater.inflate(R.layout.inflate_layout_unread_mail_item, ll_unread_mails, false);
            ll_unread_mails.addView(rl_inflate_layout_unread_mail_item);
            rl_inflate_layout_unread_mail_item.setTag(mail);
            rl_inflate_layout_unread_mail_item.setOnClickListener(this);

            tv_inflate_layout_unread_mail_item_name = rl_inflate_layout_unread_mail_item.findViewById(R.id.tv_inflate_layout_unread_mail_item_name);
                tv_inflate_layout_unread_mail_item_date = rl_inflate_layout_unread_mail_item.findViewById(R.id.tv_inflate_layout_unread_mail_item_date);

            tv_inflate_layout_unread_mail_item_name.setText(mail.Title);
            tv_inflate_layout_unread_mail_item_date.setText(mail.RegDate);
        }
    }

    private List<Application> mListOfApplications;
    private List<ApprovalDocument> mListOfApprovalDocuments;
    private List<ScheduleDocument> mListOfScheduleDocuments;
    private List<Mail> mListOfUnreadMails;
    private List<NoticeDocument> mListOfNotices;

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
                                        application.totalUnreadCount = appNode.get("totalUnreadCount").asText();

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
            ShortcutBadger.applyCount(getApplicationContext(),Integer.parseInt(application.totalUnreadCount));
            setListOfApplications();
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

            if (!mIsFailed && mIsSuccess) {
                setListOfApprovalDocuments();
            }
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

    private class Async_DownloadApkFile extends AsyncTask<Void, Void, Void> {
        private String mApkFileName;
        private final WeakReference<MainActivity> mWeakActivity;
        private ProgressDialog mProgressDialog = null;

        private Async_DownloadApkFile(MainActivity activity, String apkFileName) {
            mWeakActivity = new WeakReference<>(activity);
            mApkFileName = apkFileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            MainActivity activity = mWeakActivity.get();

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
                URL apkUrl = new URL(Constants.ROOT_URL_ANDROID + Constants.PACKGE + mApkFileName + ".apk");
                urlConnection = (HttpURLConnection) apkUrl.openConnection();
                inputStream = urlConnection.getInputStream();
                bufferedInputStream = new BufferedInputStream(inputStream);

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + "_new.apk";
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

            MainActivity activity = mWeakActivity.get();

            if (activity != null) {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download/" + mApkFileName + "_new.apk";

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

    private class WebClientAsync_Logout_v2 extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

            WebClient.Logout_v2(preferenceUtilities.getCurrentMobileSessionId(),
                    preferenceUtilities.getDomain(), new WebClient.OnWebClientListener() {
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

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        int idOfView = view.getId();

        if (idOfView == R.id.ll_inflate_layout_app_item) {
            final Application application = (Application) view.getTag();
            String packageName = application.PackageName;


            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

            if (intent == null) {
                if (application.getProjectCode().equals("OA") || application.getProjectCode().equals("HappyCall")) {
                    Intent browserIntent = new Intent(this, WebViewActivity.class);
                    browserIntent.putExtra("AAA", application.getPackageName());
                    startActivity(browserIntent);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.mailActivity_message_install_apk, application.ApplicationName));
                    builder.setNegativeButton(R.string.common_alert_dialog_no, null);
                    builder.setPositiveButton(R.string.common_alert_dialog_install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String apkFileName = "";

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
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewchat"));
                                startActivity(intent);
                            } else if (apkFileName.equals("CrewTimeCard")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "timecard.dazone.com.dazonetimecard"));
                                startActivity(intent);
                            } else if (apkFileName.equals("CrewDday")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewdday"));
                                startActivity(intent);
                            } else if (apkFileName.equals("CrewMail")) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id=" + "com.dazone.crewemail"));
                                startActivity(intent);
                            } else {
                                new Async_DownloadApkFile(MainActivity.this, apkFileName).execute();
                            }
                        }
                    });

                    builder.show();
                }
            } else {
                startActivity(intent);
            }
        }
    }

}