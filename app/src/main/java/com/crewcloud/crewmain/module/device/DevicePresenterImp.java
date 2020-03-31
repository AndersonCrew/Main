package com.crewcloud.crewmain.module.device;

import com.android.volley.Request;
import com.crewcloud.crewmain.Constants;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.activity.BaseActivity;
import com.crewcloud.crewmain.base.BasePresenter;
import com.crewcloud.crewmain.datamodel.ErrorDto;
import com.crewcloud.crewmain.util.TimeUtils;
import com.crewcloud.crewmain.util.Util;
import com.crewcloud.crewmain.util.WebServiceManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Dazone on 7/25/2017.
 */

public class DevicePresenterImp extends BasePresenter<DevicePresenter.view> implements DevicePresenter.presenter {

    BaseActivity activity;

    public DevicePresenterImp(BaseActivity activity) {
        this.activity = activity;
    }

    @Override
    public void insertDevice(String regId) {
        final String url = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + Constants.URL_INSERT_DEVICE;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        params.put("deviceID", regId);
        params.put("osVersion", android.os.Build.VERSION.RELEASE);
        params.put("notificationOptions", "");
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                getView().onSucess();
            }

            @Override
            public void onFailure(ErrorDto error) {
                getView().onError();
            }
        });
    }

    @Override
    public void deleteDevice(String regID) {
        final String url = CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentServiceDomain() + Constants.URL_DELETE_DEVICE;
        Map<String, String> params = new HashMap<>();
        params.put("sessionId", "" + CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        params.put("languageCode", Locale.getDefault().getLanguage().toUpperCase());
        params.put("timeZoneOffset", TimeUtils.getTimezoneOffsetInMinutes());
        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
            }

            @Override
            public void onFailure(ErrorDto error) {
            }
        });
    }
}
