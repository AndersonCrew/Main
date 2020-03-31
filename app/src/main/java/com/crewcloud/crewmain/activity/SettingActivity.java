package com.crewcloud.crewmain.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.crewcloud.crewmain.BuildConfig;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.Util;
import com.crewcloud.crewmain.util.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.picasso.Picasso;

/**
 * Created by Hung Dinh on 7/9/2016.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    ImageView img_avatar;
    LinearLayout ln_profile, ln_logout, ln_about;
    public PreferenceUtilities prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_page_layout);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.setings);
        toolbar.setNavigationIcon(R.drawable.nav_back_ic);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingActivity.this, MainActivityV2.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        ln_profile = (LinearLayout) findViewById(R.id.ln_profile);
        ln_logout = (LinearLayout) findViewById(R.id.ln_logout);
        ln_about = (LinearLayout) findViewById(R.id.ln_about);
        ln_profile.setOnClickListener(this);
        ln_about.setOnClickListener(this);
        ln_logout.setOnClickListener(this);
        PreferenceUtilities prefUtils = CrewCloudApplication.getInstance().getPreferenceUtilities();
        String serviceDomain = prefUtils.getCurrentServiceDomain();
        String avatar = prefUtils.getAvatar();
        String newAvatar = avatar.replaceAll("\"", "");
        String mUrl = serviceDomain + newAvatar;


//        ImageLoader imageLoader = ImageLoader.getInstance();
        img_avatar = (ImageView) findViewById(R.id.iv_avatar);
        Picasso.with(this).load(mUrl).into(img_avatar);
    }

    @Override
    public void onClick(View v) {
        if (v == ln_profile) {
//            Intent intent = new Intent(SettingActivity.this, LogoutActivity.class);
//            startActivity(intent);
//            BaseActivity.Instance.gotoInfor(MyProfileActivity.class);
            Intent newIntent = new Intent(this, MyProfileActivity.class);
            startActivity(newIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (v == ln_logout) {
           /* AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this)
                    .setMessage(R.string.are_you_sure_loguot)
                    .setPositiveButton(Util.getString(R.string.auto_login_button_yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            new WebClientAsync_Logout_v2().execute();
                        }
                    })
                    .setNegativeButton(Util.getString(R.string.auto_login_button_no), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();*/
            logoutV2();
        } else if (v == ln_about) {
        /*    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.about));

            String versionName = BuildConfig.VERSION_NAME;

//            String lastest_version = getResources().getString(R.string.lastest_version) + " " + prefs.getSERVER_VERSION();
//        String msg = user_version + "\n\n" + lastest_version;
            String msg = getResources().getString(R.string.user_version) + " " + versionName;
            builder.setMessage(msg);

            builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            android.support.v7.app.AlertDialog dialog = builder.create();
            dialog.show();
            Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (b != null) {
                b.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }*/
            showInfoV2();
        }
    }
    private void showInfoV2() {
        String versionName = BuildConfig.VERSION_NAME;
        String user_version = getResources().getString(R.string.user_version) + " " + versionName;
        Util.oneButtonAlertDialog(this, getResources().getString(R.string.about), user_version, getResources().getString(R.string.confirm));
    }
    private void logoutV2() {
        Util.customAlertDialog(this, getResources().getString(R.string.app_name),Util.getString(R.string.are_you_sure_loguot), Util.getString(R.string.auto_login_button_yes), Util.getString(R.string.auto_login_button_no), new Util.OnAlertDialogViewClickEvent() {
            @Override
            public void onOkClick(DialogInterface alertDialog) {
                new WebClientAsync_Logout_v2().execute();
            }

            @Override
            public void onCancelClick() {
            }
        });
    }
    public void deletedAndroidDevice() {

//        HttpOauthRequest.getInstance().deleteAndroidDevice(new BaseHTTPCallBack() {
//            @Override
//            public void onHTTPSuccess() {
//                logout();
//            }
//
//            @Override
//            public void onHTTPFail(ErrorDto errorDto) {
//                logout();
//            }
//        });
    }

    public void logout() {
//        HttpOauthRequest.getInstance().logout(new BaseHTTPCallBack() {
//            @Override
//            public void onHTTPSuccess() {
//                prefs = CrewCloudApplication.getInstance().getPreferenceUtilities();
//                prefs.clearLogin();
//                prefs.putaccesstoken("");
////                    prefs.putBooleanValue(Statics.PREFS_KEY_SESSION_ERROR, true);
//                Intent newIntent = new Intent(SettingActivity.this, LoginActivity.class);
//                newIntent.putExtra("count_id", 1);
//                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(newIntent);
//                finish();
//            }
//
//            @Override
//            public void onHTTPFail(ErrorDto errorDto) {
//
//            }
//        });
    }

    private class WebClientAsync_Logout_v2 extends AsyncTask<Void, Void, Void> {
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
            preferenceUtilities.clearLogin();

            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);


        }
    }
}
