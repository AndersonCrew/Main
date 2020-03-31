package com.crewcloud.crewmain.activity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.datamodel.BelongDepartmentDTO;
import com.crewcloud.crewmain.datamodel.Login_v2_Result;
import com.crewcloud.crewmain.datamodel.UserDetailDto;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.TimeUtils;
import com.crewcloud.crewmain.util.Util;
import com.crewcloud.crewmain.util.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Dazone on 6/22/2017.
 */

public class MyProfileActivity extends BaseActivity {
    @Bind(R.id.activity_new_profile_iv_avatar)
    CircleImageView ivAvatar;

    @Bind(R.id.activity_new_profile_tv_name)
    TextView tvName;
    @Bind(R.id.activity_new_profile_tv_phone_com)
    TextView tvCompanyPhone;
    @Bind(R.id.activity_new_profile_date_of_emloyment_value)
    TextView tvEntranceDate;
    @Bind(R.id.activity_new_profile_date_of_birth_value)
    TextView tvBirthday;
    @Bind(R.id.activity_new_profile_tv_depart_position)
    TextView tvDepartPositionName;

    @Bind(R.id.activity_new_profile_tv_company_name)
    TextView tvCompanyName;

    @Bind(R.id.activity_new_profile_tv_company_id)
    TextView tvCompanyId;

    @Bind(R.id.activity_new_profile_tv_persion_id)
    TextView tvPersionId;

    @Bind(R.id.activity_new_profile_tv_email)
    TextView tvEmail;

    @Bind(R.id.activity_new_profile_tv_pass)
    TextView tvPass;

    @Bind(R.id.activity_new_profile_tv_phone)
    TextView tvPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile_activity);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.myColor_PrimaryDark));
        }
        ButterKnife.bind(this);
        new WebClientAsync_checkVersion().execute();
    }

    private class WebClientAsync_checkVersion extends AsyncTask<Void, Void, Void> {
        UserDetailDto userDto;
        @Override
        protected Void doInBackground(Void... params) {
            WebClient.getUser("http://" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentCompanyDomain(), new WebClient.OnWebClientListener() {
                @Override
                public void onSuccess(JsonNode jsonNode) {
                    try {
                        if (jsonNode.get("success").asInt() == 0) {

                        } else {
                            String dataJson = jsonNode.get("data").toString();
                            Gson gson = new Gson();
                             userDto = gson.fromJson(dataJson, UserDetailDto.class);


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
            fillData(userDto);
        }
    }


    private void fillData(UserDetailDto userDetailDto) {
        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();
        String companyName = preferenceUtilities.getCurrentCompanyName();
        String avatar = preferenceUtilities.getAvatar();
        tvName.setText(userDetailDto.getName());
        tvEmail.setText(userDetailDto.getMailAddress());
        tvPass.setText(userDetailDto.getPassword() + "pass");
        tvCompanyName.setText(companyName);
        tvCompanyId.setText(preferenceUtilities.getCurrentCompanyDomain());
        tvPersionId.setText(preferenceUtilities.getUserId());
        tvPhone.setText(userDetailDto.getCellPhone());
        tvCompanyPhone.setText(userDetailDto.getCompanyPhone());
        tvEntranceDate.setText(Util.displayTimeWithoutOffset(userDetailDto.getEntranceDate()));
        tvBirthday.setText(Util.displayTimeWithoutOffset(userDetailDto.getBirthDate()));
        String strPositionName = "";
        String belongToDepartment = "";
        ArrayList<BelongDepartmentDTO> listBelong = userDetailDto.getBelongs();

        for (BelongDepartmentDTO belongDepartmentDTOs : listBelong) {
            belongToDepartment += listBelong.indexOf(belongDepartmentDTOs) == listBelong.size() - 1 ?
                    belongDepartmentDTOs.getDepartName() + " / " + belongDepartmentDTOs.getPositionName() + " / " + belongDepartmentDTOs.getDutyName() :
                    belongDepartmentDTOs.getDepartName() + " / " + belongDepartmentDTOs.getPositionName() + " / " + belongDepartmentDTOs.getDutyName() + "<br>";
            if (belongDepartmentDTOs.isDefault()) {
                strPositionName = belongDepartmentDTOs.getDepartName() + " / " + belongDepartmentDTOs.getPositionName() + " / " + belongDepartmentDTOs.getDutyName();
            }
        }
        tvDepartPositionName.setText(strPositionName);
        if (!TextUtils.isEmpty(avatar)) {
            Picasso.with(this).load(CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + avatar)
                    .placeholder(R.mipmap.avatar_default).into(ivAvatar);
        }
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.btn_back)
    public void onClickBack() {
        finish();
    }

    @OnClick(R.id.change_pass)
    public void changePass() {
        BaseActivity.Instance.callActivity(ChangePasswordActivity.class);
    }

}


