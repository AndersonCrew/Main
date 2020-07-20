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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_page_layout);
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

        img_avatar = (ImageView) findViewById(R.id.iv_avatar);
        Picasso.with(this).load(mUrl).into(img_avatar);
    }

    @Override
    public void onClick(View v) {
        if (v == ln_profile) {
            Intent newIntent = new Intent(this, MyProfileActivity.class);
            startActivity(newIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (v == ln_logout) {
            logoutV2();
        } else if (v == ln_about) {
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
            preferenceUtilities.clearLogin();

            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);


        }
    }
}
