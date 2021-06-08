package com.crewcloud.crewmain.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.callbacks.LoginCallBack;
import com.crewcloud.crewmain.datamodel.ErrorDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.crewcloud.crewmain.util.Util.getApplicationName;


public class WebClient {

    private final static ObjectMapper mJSONObjectMapper = new ObjectMapper();

    private static final String SERVICE_URL_LOGIN_V2 = "/UI/WebService/WebServiceCenter.asmx/Login_v2";
    private static final String URL_CHECK_UPDATE = "http://mobileupdate.crewcloud.net/WebServiceMobile.asmx/Mobile_Version";
    private static final String SERVICE_URL_LOGOUT_V2 = "/UI/WebService/WebServiceCenter.asmx/Logout_v2";
    private static final String SERVICE_URL_CHECK_SESSION_USER_V2 = "/UI/WebService/WebServiceCenter.asmx/CheckSessionUser_v2";
    private static final String SERVICE_URL_GET_ENABLED_APPLICATIONS = "/UI/WebService/WebServiceCenter.asmx/GetEnabledApplications";

    private static final String MAIL_URL_GET_APPROVAL_LIST = "/UI/_EAPP/Widget/EAWebService.asmx/GetApprovalList";
    private static final String MAIL_URL_GET_COMMUNITY_LIST = "/UI/MobileBoard/Handlers/MobileService.asmx/GetBoardContentList";
    private static final String MAIL_URL_GET_UNREAD_MAILS = "/UI/MobileMail3/MobileDataService.asmx/GetUnreadMails";
    private static final String MAIL_URL_GET_SCHEDULE_LIST = "/UI/MobileSchedule/MobileDataService.asmx/GetPeriodSchedules";
    private static final String MAIL_URL_GET_NOTICE_LIST = "/UI/MobileNotice/NoticeService.asmx/GetNotices";
    public static final String URL_GET_USER = "/UI/WebService/WebServiceCenter.asmx/GetUser";

    public interface OnWebClientListener {
        void onSuccess(JsonNode jsonNode);

        void onFailure();
    }

    private static String getJSONStringFromUrl(String methodUrl, Map<String, Object> params) {
        HttpURLConnection urlConnection = null;

        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            URL url = new URL(methodUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.addRequestProperty("Content-Type", "application/json");

            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(mJSONObjectMapper.writeValueAsString(params).getBytes());
            outputStream.flush();
            outputStream.close();

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String s;

                while ((s = bufferedReader.readLine()) != null) {
                    sb.append(s);
                }

                return sb.toString();
            }

            return "";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (urlConnection != null) {
                try {
                    urlConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

    public static void Login_v2(String languageCode, long timeZoneOffset, String userID, String password, String mobileOSVersion, LoginCallBack listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("timeZoneOffset", timeZoneOffset);
        mapParams.put("companyDomain", CrewCloudApplication.getInstance().getPreferenceUtilities().getStringValue(Constants.COMPANY_NAME, "") );
        mapParams.put("userID", userID);
        mapParams.put("password", password);
        mapParams.put("mobileOSVersion", mobileOSVersion);

        String result = getJSONStringFromUrl(CrewCloudApplication.getInstance().getPreferenceUtilities().getStringValue(Constants.DOMAIN, "") + SERVICE_URL_LOGIN_V2, mapParams);
        Log.d("ssssss", mapParams.toString());
        if (TextUtils.isEmpty(result)) {
            listener.onLoginFail();
        } else {
            try {
                listener.onLoginSuccess(result);
            } catch (Exception e) {
                e.printStackTrace();
                listener.onLoginFail();
            }
        }
    }

    public static void getUser(String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("sessionId", CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentMobileSessionId());
        mapParams.put("userNo", CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentUserNo());
        mapParams.put("timeZoneOffset", Util.getTimeOffsetInMinute());
        mapParams.put("languageCode", Util.getPhoneLanguage());

        String result = getJSONStringFromUrl(_domain + URL_GET_USER, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void checkVersionUpdate(Context context, OnWebClientListener listener, String nameApp) {
        Map<String, Object> params = new HashMap<>();
        PreferenceUtilities preferenceUtilities = CrewCloudApplication.getInstance().getPreferenceUtilities();

        String mTempDomain = "" + preferenceUtilities.getCurrentCompanyDomain();
        params.put("Domain", mTempDomain);
        params.put("MobileType", "Android");
        params.put("Applications", "" + nameApp);
        Log.d("sssDebug", params.toString());
        String result = getJSONStringFromUrl(URL_CHECK_UPDATE, params);
        Log.d("sssDebug", result);
        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void Logout_v2(String sessionId, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("sessionId", sessionId);

        String result = getJSONStringFromUrl(_domain + SERVICE_URL_LOGOUT_V2, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void CheckSessionUser_v2(String languageCode, int timeZoneOffset, String sessionId, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("timeZoneOffset", timeZoneOffset);
        mapParams.put("sessionId", sessionId);

        String result = getJSONStringFromUrl(_domain + SERVICE_URL_CHECK_SESSION_USER_V2, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void GetEnabledApplications(String languageCode, int timeZoneOffset, String sessionId, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("timeZoneOffset", timeZoneOffset);
        mapParams.put("sessionId", sessionId);

        String result = getJSONStringFromUrl(_domain + SERVICE_URL_GET_ENABLED_APPLICATIONS, mapParams);
        Log.d("ssssurrl", mapParams.toString() + "link" + _domain + SERVICE_URL_GET_ENABLED_APPLICATIONS);
        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void GetApprovalList(String languageCode, int timeZoneOffset, String sessionId, int countPerList, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("timeZoneOffset", timeZoneOffset);
        mapParams.put("sessionId", sessionId);
        mapParams.put("countperlist", countPerList);

        String result = getJSONStringFromUrl(_domain + MAIL_URL_GET_APPROVAL_LIST, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void
    GetCommunityList(String languageCode, String sessionId, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("countPerPage", "10");
        mapParams.put("sessionId", sessionId);
        mapParams.put("curentPage", "1");
        mapParams.put("boardNo", "0");
        mapParams.put("filterType", "1");

        String result = getJSONStringFromUrl(_domain + MAIL_URL_GET_COMMUNITY_LIST, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void GetScheduleList(String languageCode, int timeZoneOffset, String sessionId, String startDay, String endDay, int scheduleType, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("timeZoneOffset", timeZoneOffset);
        mapParams.put("sessionId", sessionId);
        mapParams.put("scheduleType", scheduleType);
        mapParams.put("StartDate", startDay);
        mapParams.put("EndDate", endDay);

        String result = getJSONStringFromUrl(_domain + MAIL_URL_GET_SCHEDULE_LIST, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void GetNoticeList(String languageCode, int timeZoneOffset, String sessionId, int viewCnt, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("timeZoneOffset", timeZoneOffset);
        mapParams.put("sessionId", sessionId);
        mapParams.put("viewCnt", viewCnt);
        String result = getJSONStringFromUrl(_domain + MAIL_URL_GET_NOTICE_LIST, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    public static void GetUnreadMails(String languageCode, int timeZoneOffset, String sessionId, int viewCount, String _domain, OnWebClientListener listener) {
        Map<String, Object> mapParams = new HashMap<>();
        mapParams.put("languageCode", languageCode);
        mapParams.put("timeZoneOffset", timeZoneOffset);
        mapParams.put("sessionId", sessionId);
        mapParams.put("viewCount", viewCount);

        String result = getJSONStringFromUrl(_domain + MAIL_URL_GET_UNREAD_MAILS, mapParams);

        if (TextUtils.isEmpty(result)) {
            listener.onFailure();
        } else {
            try {
                listener.onSuccess(mJSONObjectMapper.readTree(result).get("d"));
            } catch (Exception e) {
                e.printStackTrace();
                listener.onFailure();
            }
        }
    }

    /*Check SSL*/
    public static void checkSSL(final ICheckSSL checkSSL) {
        final String url = Config.URL_CHECK_SSL;
        Map<String, String> params = new HashMap<>();
        params.put("Domain", CrewCloudApplication.getInstance().getPreferenceUtilities().getCurrentCompanyDomain());
        params.put("Applications", "CrewApproval");

        WebServiceManager webServiceManager = new WebServiceManager();
        webServiceManager.doJsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new WebServiceManager.RequestListener<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean hasSSL = jsonObject.getBoolean("SSL");
                    CrewCloudApplication.getInstance().getPreferenceUtilities().putBooleanValue(Constants.HAS_SSL, hasSSL);
                    checkSSL.hasSSL(hasSSL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(ErrorDto error) {
                checkSSL.checkSSLError(error);
            }
        });
    }
}