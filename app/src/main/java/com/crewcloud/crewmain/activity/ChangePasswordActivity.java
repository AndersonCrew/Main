package com.crewcloud.crewmain.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.module.changepass.ChangePassPresenter;
import com.crewcloud.crewmain.module.changepass.ChangePassPresenterImp;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Dazone on 6/22/2017.
 */

public class ChangePasswordActivity extends AppCompatActivity implements ChangePassPresenter.view {

    @Bind(R.id.fragment_change_pass_et_confirm_pass)
    EditText etConfirmPass;

    @Bind(R.id.fragment_change_pass_et_new_pass)
    EditText etNewPass;

    @Bind(R.id.fragment_change_pass_et_old_pass)
    EditText etOldPass;

    ChangePassPresenterImp changePassPresenterImp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        changePassPresenterImp = new ChangePassPresenterImp(this);
        changePassPresenterImp.attachView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        changePassPresenterImp.detachView();
    }

    @OnClick(R.id.btn_back)
    public void onClickback() {
        finish();
    }

    @OnClick(R.id.fragment_change_pass_btn_change_pass)
    public void onClickChangePass() {
        String oldPass = etOldPass.getText().toString();
        String newPass = etNewPass.getText().toString();
        String confirmPass = etConfirmPass.getText().toString();

        changePassPresenterImp.CheckPass(oldPass, newPass, confirmPass);
    }

    @Override
    public void ChangePassSuccess() {
        finish();
    }

    @Override
    public void ChangePassError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

    }
}
