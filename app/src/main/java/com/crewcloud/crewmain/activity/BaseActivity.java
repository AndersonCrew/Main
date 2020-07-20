package com.crewcloud.crewmain.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crewcloud.crewmain.R;

import java.util.Locale;


public abstract class BaseActivity extends AppCompatActivity {

    public ActionBar mActionBar;
    protected Context mContext;
    public static BaseActivity Instance;
    private Dialog mProgressDialog;
    private boolean mIsExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        Instance = this;
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        enableHomeAction();
    }
    @Override
    public void onBackPressed() {
        if (!isTaskRoot()) {
            super.onBackPressed();
        } else {
            if (mIsExit) {
                super.onBackPressed();
            } else {
                this.mIsExit = true;
                String Str;
                if (Locale.getDefault().getLanguage().equals("vi")) {
                    Str = "Click thêm lần nữa ứng dụng sẽ được đóng";
                } else if (Locale.getDefault().getLanguage().equals("ko")) {
                    Str = "'뒤로'버튼을 한번 더 누르시면 종료됩니다.";
                } else {
                    Str = "Press back again to quit.";
                }
                Toast.makeText(this, Str, Toast.LENGTH_SHORT).show();
                myHandler.postDelayed(myRunnable, 2000);
            }
        }

    }

    private Handler myHandler = new Handler();
    private Runnable myRunnable = new Runnable() {
        public void run() {
            mIsExit = false;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Instance = this;
    }

    public void showProgressDialog() {
        if (null == mProgressDialog || !mProgressDialog.isShowing()) {
            mProgressDialog = new Dialog(mContext, R.style.ProgressCircleDialog);
            mProgressDialog.setTitle(getString(R.string.loading_content));
            mProgressDialog.setCancelable(false);
            mProgressDialog.setOnCancelListener(null);
            mProgressDialog.addContentView(new ProgressBar(mContext), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mProgressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void callActivity(Class cls) {
        Intent newIntent = new Intent(this, cls);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(newIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
    public void startSingleActivity(Class cls) {
        Intent newIntent = new Intent(this, cls);
        newIntent.putExtra("count_id", 1);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(newIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    protected void enableHomeAction() {
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(true);
            mActionBar.setDisplayShowCustomEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void displayAddAlertDialog(String title, String content, String positiveTitle, String negativeTitle, DialogInterface.OnClickListener positiveListener, DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(content).setCancelable(false).setPositiveButton(positiveTitle, positiveListener).setNegativeButton(negativeTitle, negativeListener);
        builder.create().show();
    }
}