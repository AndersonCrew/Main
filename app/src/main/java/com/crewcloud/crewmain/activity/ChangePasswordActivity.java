package com.crewcloud.crewmain.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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

    private EditText etConfirmPass, etNewPass, etOldPass;
    private ChangePassPresenterImp changePassPresenterImp;
    private Button btnChangePass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initViews();
        initControls();
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        changePassPresenterImp = new ChangePassPresenterImp(this);
        changePassPresenterImp.attachView(this);
    }

    private void initViews() {
        etConfirmPass = (EditText) findViewById(R.id.etConfirmPassword);
        etNewPass = (EditText) findViewById(R.id.etNewPassword);
        etOldPass = (EditText) findViewById(R.id.fragment_change_pass_et_old_pass);
        btnChangePass = (Button) findViewById(R.id.btnChangePass);
    }

    private void initControls() {
        btnChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPass = etOldPass.getText().toString();
                String newPass = etNewPass.getText().toString();
                String confirmPass = etConfirmPass.getText().toString();

                changePassPresenterImp.CheckPass(oldPass, newPass, confirmPass);
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        changePassPresenterImp.detachView();
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
