package com.crewcloud.crewmain.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.datamodel.Login_v2_Result;
import com.crewcloud.crewmain.module.device.DevicePresenter;
import com.crewcloud.crewmain.module.device.DevicePresenterImp;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.SoftKeyboardDetectorView;
import com.crewcloud.crewmain.util.Statics;
import com.crewcloud.crewmain.util.Util;
import com.crewcloud.crewmain.util.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import me.leolin.shortcutbadger.ShortcutBadger;


public class LoginActivity extends BaseActivity {
    private ImageView img_login_logo;
    private TextView tv_login_logo_text;
    private EditText login_edt_server, login_edt_username, login_edt_password;
    private RelativeLayout login_btn_login;
    public PreferenceUtilities mPrefs;
    private boolean mFirstLogin = true;
    private String mInputUsername, mInputPassword;
    private String mCompanyName = "_woori";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


//        if (checkPermissions()) {
//            Thread thread = new Thread(new LoginActivity.UpdateRunnable());
//            thread.setDaemon(true);
//            thread.start();
//        } else {
//            setPermissions();
//        }

        mPrefs = CrewCloudApplication.getInstance().getPreferenceUtilities();

        init();

        final SoftKeyboardDetectorView softKeyboardDetectorView = new SoftKeyboardDetectorView(this);
        addContentView(softKeyboardDetectorView, new FrameLayout.LayoutParams(-1, -1));

        softKeyboardDetectorView.setOnShownKeyboard(new SoftKeyboardDetectorView.OnShownKeyboardListener() {
            @Override
            public void onShowSoftKeyboard() {
                if (img_login_logo != null) {
                    img_login_logo.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv_login_logo_text.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    tv_login_logo_text.setLayoutParams(params);
                    tv_login_logo_text.setText(Util.getString(R.string.app_name));
                }
            }
        });

        softKeyboardDetectorView.setOnHiddenKeyboard(new SoftKeyboardDetectorView.OnHiddenKeyboardListener() {
            @Override
            public void onHiddenSoftKeyboard() {
                if (img_login_logo != null) {
                    img_login_logo.setVisibility(View.VISIBLE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv_login_logo_text.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    tv_login_logo_text.setLayoutParams(params);
                    tv_login_logo_text.setText(Util.getString(R.string.app_name));
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
    }

    private void init() {
        try {
            ShortcutBadger.applyCount(this, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        img_login_logo = (ImageView) findViewById(R.id.img_login_logo);
        tv_login_logo_text = (TextView) findViewById(R.id.tv_login_logo_text);
        login_edt_username = (EditText) findViewById(R.id.login_edt_username);
        login_edt_password = (EditText) findViewById(R.id.login_edt_password);
        login_edt_server = (EditText) findViewById(R.id.login_edt_server);

        login_edt_username.setPrivateImeOptions("defaultInputmode=english;");
        login_edt_server.setPrivateImeOptions("defaultInputmode=english;");

        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
        login_edt_password.setText(preferenceUtilities.getPass());
        login_edt_server.setText(preferenceUtilities.getDomain());
        login_edt_username.setText(preferenceUtilities.getUserId());

        login_edt_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "");
                if (!s.toString().equals(result)) {
                    login_edt_username.setText(result);
                    login_edt_username.setSelection(result.length());
                }
            }
        });

        login_edt_server.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "");

                if (!s.toString().equals(result)) {
                    login_edt_server.setText(result);
                    login_edt_server.setSelection(result.length());
                }
            }
        });

        login_edt_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login_btn_login.callOnClick();
                }

                return false;
            }
        });

        login_btn_login = (RelativeLayout) findViewById(R.id.login_btn_login);

        if (login_btn_login != null) {
            login_btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInputUsername = login_edt_username.getText().toString();
                    mInputPassword = login_edt_password.getText().toString();
                    server_site = login_edt_server.getText().toString();

                    if (TextUtils.isEmpty(checkStringValue(server_site, mInputUsername, mInputPassword))) {
                        server_site = getServerSite(server_site);
                        String company_domain = server_site;

                        if (!company_domain.startsWith("http")) {
                            server_site = "http://" + server_site;
                        }

                        String temp_server_site = server_site;

                        if (temp_server_site.contains(".bizsw.co.kr")) {
                            temp_server_site = "http://www.bizsw.co.kr:8080";
                        } else {
                            if (temp_server_site.contains("crewcloud")) {
                                temp_server_site = "http://www.crewcloud.net";
                            }
                        }

//                        showProgressDialog();

                        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

                        preferenceUtilities.setCurrentServiceDomain(temp_server_site); // Domain
                        preferenceUtilities.setCurrentCompanyDomain(company_domain); // group ID

                        new WebClientAsync_Login_v2(company_domain, temp_server_site).execute();
                    } else {
                        displayAddAlertDialog(getString(R.string.app_name), checkStringValue(server_site, mInputUsername, mInputPassword), getString(R.string.string_ok), null,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }, null);
                    }
                }
            });
        }
    }

    private String checkStringValue(String server_site, String username, String password) {
        String result = "";

        if (TextUtils.isEmpty(server_site)) {
            result += getString(R.string.string_server_site);
        }

        if (TextUtils.isEmpty(username)) {
            if (TextUtils.isEmpty(result)) {
                result += getString(R.string.login_username);
            } else {
                result += ", " + getString(R.string.login_username);
            }
        }

        if (TextUtils.isEmpty(password)) {
            if (TextUtils.isEmpty(result)) {
                result += getString(R.string.login_password);
            } else {
                result += ", " + getString(R.string.login_password);
            }
        }

        if (TextUtils.isEmpty(result)) {
            return result;
        } else {
            return result + " " + getString(R.string.login_empty_input);
        }
    }

    private String getServerSite(String server_site) {
        String[] domains = server_site.split("[.]");
        if (server_site.contains(".bizsw.co.kr") && !server_site.contains("8080")) {
            return server_site.replace(".bizsw.co.kr", ".bizsw.co.kr:8080");
        }

        if (domains.length == 1) {
            return domains[0] + ".crewcloud.net";
        } else {
            return server_site;
        }
    }


    // ------------------------------------------------------------------------------------------------

    private class WebClientAsync_Login_v2 extends AsyncTask<Void, Void, Void> {
        private String mCompanyDomain, mTempServerSite;
        private boolean mIsSuccess, mIsFailed;

        private boolean mIsLogin = true;
        private String mErrorMessage;

        private WebClientAsync_Login_v2(String companyDomain, String tempServerSite) {
            mCompanyDomain = companyDomain;
            mTempServerSite = tempServerSite;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            WebClient.Login_v2(Util.getPhoneLanguage(), Util.getTimeOffsetInMinute(), mCompanyDomain,
                    mInputUsername, mInputPassword, "Android " + android.os.Build.VERSION.RELEASE, mTempServerSite, new WebClient.OnWebClientListener() {
                        @Override
                        public void onSuccess(JsonNode jsonNode) {
                            mIsSuccess = true;
                            mIsFailed = false;

                            try {
                                if (jsonNode.get("success").asInt() == 0) {
                                    mIsLogin = false;
                                    mErrorMessage = jsonNode.get("error").get("message").asText();
                                } else {
                                    ObjectMapper mapper = new ObjectMapper();
                                    String dataJson = jsonNode.get("data").toString();

                                    Login_v2_Result result = mapper.readValue(dataJson, new TypeReference<Login_v2_Result>() {
                                    });
                                    result.prefs.putUserData(dataJson);
                                    result.prefs.setCurrentMobileSessionId(result.session);
                                    result.prefs.setCurrentUserIsAdmin(result.PermissionType);
                                    result.prefs.setCurrentCompanyNo(result.CompanyNo);
                                    result.prefs.setCurrentUserNo(result.Id);
                                    result.prefs.setCurrentUserID(result.userID);
                                    result.prefs.setAvatar(result.avatar);
                                    result.prefs.setEmail(result.MailAddress);
                                    result.prefs.setUserId(result.userID);
                                    result.prefs.setFullName(result.FullName);
                                    result.prefs.setCurrentCompanyName(result.NameCompany);
                                    result.prefs.setPass(mInputPassword);
                                    result.prefs.setDomain(mCompanyDomain);
                                    result.prefs.setCellPhone(result.CellPhone);
                                    result.prefs.setCompanyPhone(result.CompanyPhone);
                                    result.prefs.setBirthday(result.BirthDate);
                                    result.prefs.setEntranceDate(result.EntranceDate);

                                    CrewCloudApplication.getInstance().getPreferenceUtilities().setCurrentCompanyName(result.NameCompany);

                                    if (!TextUtils.isEmpty(server_site)) {
                                        CrewCloudApplication.getInstance().getPreferenceUtilities().setCurrentServiceDomain(server_site);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mIsSuccess = false;
                                mIsFailed = true;
                                mIsLogin = false;
                            }
                        }

                        @Override
                        public void onFailure() {
                            mIsSuccess = false;
                            mIsFailed = true;
                            mIsLogin = false;
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissProgressDialog();
            if (!mIsFailed && !mIsLogin) {
                displayAddAlertDialog(getString(R.string.app_name), mErrorMessage, getString(R.string.string_ok), null,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }, null);

                dismissProgressDialog();
            } else {
                if (mIsSuccess) {
                    Intent newIntent = new Intent(LoginActivity.this, MainActivityV2.class);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(newIntent);
                    finish();
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else if (mIsFailed) {
                    if (mFirstLogin) {
                        dismissProgressDialog();

                        mFirstLogin = false;
                        init();
                    } else {
                        dismissProgressDialog();

                        displayAddAlertDialog(getString(R.string.app_name), getString(R.string.connection_falsed), getString(R.string.string_ok), null,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }, null);
                    }
                }
            }
        }
    }
}