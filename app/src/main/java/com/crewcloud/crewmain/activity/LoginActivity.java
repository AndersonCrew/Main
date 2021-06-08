package com.crewcloud.crewmain.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.callbacks.LoginCallBack;
import com.crewcloud.crewmain.datamodel.ErrorDto;
import com.crewcloud.crewmain.datamodel.LoginDto;
import com.crewcloud.crewmain.util.Constants;
import com.crewcloud.crewmain.util.ICheckSSL;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.SoftKeyboardDetectorView;
import com.crewcloud.crewmain.util.Util;
import com.crewcloud.crewmain.util.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import org.json.JSONObject;

import me.leolin.shortcutbadger.ShortcutBadger;


public class LoginActivity extends BaseActivity {
    private ImageView imgLoginLogo;
    private TextView tvLoginLogoText;
    private EditText etDomain, etUsername, etPassword;
    private RelativeLayout btnLogin;
    public PreferenceUtilities mPrefs;
    private boolean mFirstLogin = true;
    private String mInputUsername, mInputPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPrefs = CrewCloudApplication.getInstance().getPreferenceUtilities();
        init();

        final SoftKeyboardDetectorView softKeyboardDetectorView = new SoftKeyboardDetectorView(this);
        addContentView(softKeyboardDetectorView, new FrameLayout.LayoutParams(-1, -1));

        softKeyboardDetectorView.setOnShownKeyboard(new SoftKeyboardDetectorView.OnShownKeyboardListener() {
            @Override
            public void onShowSoftKeyboard() {
                if (imgLoginLogo != null) {
                    imgLoginLogo.setVisibility(View.GONE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvLoginLogoText.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    tvLoginLogoText.setLayoutParams(params);
                    tvLoginLogoText.setText(Util.getString(R.string.app_name));
                }
            }
        });

        softKeyboardDetectorView.setOnHiddenKeyboard(new SoftKeyboardDetectorView.OnHiddenKeyboardListener() {
            @Override
            public void onHiddenSoftKeyboard() {
                if (imgLoginLogo != null) {
                    imgLoginLogo.setVisibility(View.VISIBLE);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvLoginLogoText.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                    tvLoginLogoText.setLayoutParams(params);
                    tvLoginLogoText.setText(Util.getString(R.string.app_name));
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
        imgLoginLogo = (ImageView) findViewById(R.id.img_login_logo);
        tvLoginLogoText = (TextView) findViewById(R.id.tv_login_logo_text);
        etUsername = (EditText) findViewById(R.id.login_edt_username);
        etPassword = (EditText) findViewById(R.id.login_edt_password);
        etDomain = (EditText) findViewById(R.id.login_edt_server);

        etUsername.setPrivateImeOptions("defaultInputmode=english;");
        etDomain.setPrivateImeOptions("defaultInputmode=english;");

        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
        /** Check save old domain*/
        if (!preferenceUtilities.getStringValue("domain", "").isEmpty()) {
            preferenceUtilities.putStringValue(Constants.COMPANY_NAME, preferenceUtilities.getStringValue("domain", ""));
        }

        etPassword.setText(preferenceUtilities.getPass());
        etDomain.setText(preferenceUtilities.getStringValue(Constants.COMPANY_NAME, ""));
        etUsername.setText(preferenceUtilities.getUserId());

        etUsername.addTextChangedListener(new TextWatcher() {
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
                    etUsername.setText(result);
                    etUsername.setSelection(result.length());
                }
            }
        });

        etDomain.addTextChangedListener(new TextWatcher() {
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
                    etDomain.setText(result);
                    etDomain.setSelection(result.length());
                }
            }
        });

        etPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnLogin.callOnClick();
                }

                return false;
            }
        });

        btnLogin = (RelativeLayout) findViewById(R.id.login_btn_login);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mInputUsername = etUsername.getText().toString();
                    mInputPassword = etPassword.getText().toString();
                    final String domain = etDomain.getText().toString();

                    Util.setServerSite(domain);
                    if (TextUtils.isEmpty(checkStringValue(domain, mInputUsername, mInputPassword))) {
                        showProgressDialog();
                        WebClient.checkSSL(new ICheckSSL() {
                            @Override
                            public void hasSSL(boolean hasSSL) {
                                Util.setServerSite(domain);
                                new WebClientAsync_Login_v2().execute();
                            }

                            @Override
                            public void checkSSLError(ErrorDto errorData) {
                                dismissProgressDialog();
                                Toast.makeText(LoginActivity.this, "Cannot check ssl this domain!", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        displayAddAlertDialog(getString(R.string.app_name), checkStringValue(domain, mInputUsername, mInputPassword), getString(R.string.string_ok), null,
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

    private class WebClientAsync_Login_v2 extends AsyncTask<Void, Void, Void> {
        private boolean mIsSuccess, mIsFailed;

        private boolean mIsLogin = true;
        private String mErrorMessage;

        private WebClientAsync_Login_v2() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected Void doInBackground(Void... params) {
            WebClient.Login_v2(Util.getPhoneLanguage(), Util.getTimeOffsetInMinute(),
                    mInputUsername, mInputPassword, "Android " + android.os.Build.VERSION.RELEASE, new LoginCallBack() {
                        @Override
                        public void onLoginSuccess(String response) {
                            mIsSuccess = true;
                            mIsFailed = false;

                            try {
                                JSONObject json = new JSONObject(response);
                                JSONObject jsonD = new JSONObject(json.getString("d"));

                                if (Integer.parseInt(jsonD.getString("success")) == 0) {
                                    mIsLogin = false;
                                    JSONObject jsonError = (JSONObject) jsonD.get("error");
                                    mErrorMessage = jsonError.getString("message");
                                } else {
                                    Gson gson = new Gson();
                                    LoginDto result = gson.fromJson(jsonD.getString("data"), LoginDto.class);

                                    result.prefs.putUserData(jsonD.getString("data"));
                                    result.prefs.setCurrentMobileSessionId(result.session);
                                    result.prefs.setCurrentCompanyNo(result.CompanyNo);
                                    result.prefs.setCurrentUserNo(result.Id);
                                    result.prefs.setCurrentUserID(result.userID);
                                    result.prefs.setAvatar(result.avatar);
                                    result.prefs.setUserId(result.userID);
                                    result.prefs.setPass(mInputPassword);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mIsSuccess = false;
                                mIsFailed = true;
                                mIsLogin = false;
                                mErrorMessage = e.getMessage();
                            }
                        }

                        @Override
                        public void onLoginFail() {
                            dismissProgressDialog();
                            mIsSuccess = false;
                            mIsFailed = true;
                            mIsLogin = false;
                            mErrorMessage = getResources().getString(R.string.connection_falsed);
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