package com.crewcloud.crewmain.module.changepass;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.Request;
import com.crewcloud.crewmain.Constants;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.base.BasePresenter;
import com.crewcloud.crewmain.datamodel.ErrorDto;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.Util;
import com.crewcloud.crewmain.util.WebServiceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dazone on 5/11/2017.
 */

public class ChangePassPresenterImp extends BasePresenter<ChangePassPresenter.view> implements ChangePassPresenter.presenter {

    private Context context;

    public ChangePassPresenterImp(Context context) {
        this.context = context;
    }

    @Override
    public void ChangePass(String oldPass, final String newPass) {
        final String sessionId = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId();
        long timeZoneOffset = Util.getTimeOffsetInMinute();

        final String url = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + Constants.URL_CHANGE_PASS;
        Map<String, String> params = new HashMap<>();
        params.put("languageCode", Util.getPhoneLanguage());
        params.put("sessionId", sessionId);
        params.put("timeZoneOffset", "" + timeZoneOffset);
        params.put("originalPassword", oldPass);
        params.put("newPassword", newPass);
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String newSessionId = jsonObject.getString("newSessionID");
                    PreferenceUtilities preference = CrewCloudApplication.getInstance().getPreferenceUtilities();
                    preference.setCurrentMobileSessionId(newSessionId);
                    preference.setPass(newPass);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                getView().ChangePassSuccess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                String messageDto = error.getMessage();
                getView().ChangePassError(messageDto);
            }
        });

    }

    @Override
    public void CheckPass(String oldPass, String newPass, String confirmPass) {
        if (TextUtils.isEmpty(oldPass)) {
            getView().ChangePassError("You can't leave this empty current password");
        } else if (TextUtils.isEmpty(newPass)) {
            getView().ChangePassError("You can't leave this empty new password");
        } else if (TextUtils.isEmpty(confirmPass)) {
            getView().ChangePassError("These passwords don't match. Try again?");
        } else if (!newPass.equals(confirmPass)) {
            getView().ChangePassError("These passwords don't match. Try again?");
        } else {
            ChangePass(oldPass, newPass);
        }
    }
}
