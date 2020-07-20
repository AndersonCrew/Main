package com.crewcloud.crewmain.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.crewcloud.crewmain.CrewCloudApplication;


public class PreferenceUtilities {
    private SharedPreferences mPreferences;
    private final String KEY_CURRENT_COMPANY_NO = "currentCompanyNo";
    private final String USER_JSON_INFO = "user_json";
    private final String KEY_CURRENT_MOBILE_SESSION_ID = "currentMobileSessionId";
    private final String KEY_CURRENT_USER_ID = "currentUserID";
    private final String KEY_AVATAR = "avatar";
    private final String KEY_USER = "userID";
    private final String KEY_PASS = "pass";
    private final String KEY_CURRENT_USER_NO = "currentUserNo";
    private final String PREF_FLAG_GMC_ID = "aeSortType";

    public PreferenceUtilities() {
        mPreferences = CrewCloudApplication.getInstance().getApplicationContext().getSharedPreferences("CrewMain_Prefs", Context.MODE_PRIVATE);
    }

    public void putBooleanValue(String key, boolean value) {
        mPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    public String getStringValue(String key, String defaultValue) {
        return mPreferences.getString(key, defaultValue);
    }

    public void putStringValue(String KEY, String value) {
        mPreferences.edit().putString(KEY, value).apply();
    }

    public String getGCMregistrationid() {
        return mPreferences.getString(PREF_FLAG_GMC_ID, "");
    }

    public void setGCMregistrationid(String value) {
        mPreferences.edit().putString(PREF_FLAG_GMC_ID, value);
    }

    public String getCurrentServiceDomain() {
        return mPreferences.getString(Constants.DOMAIN, "");
    }

    public String getCurrentCompanyDomain() {
        return mPreferences.getString(Constants.COMPANY_NAME, "");
    }

    public void setCurrentCompanyNo(int companyNo) {
        mPreferences.edit().putInt(KEY_CURRENT_COMPANY_NO, companyNo).apply();
    }

    public int getCurrentCompanyNo() {
        return mPreferences.getInt(KEY_CURRENT_COMPANY_NO, 0);
    }

    public void setCurrentMobileSessionId(String sessionId) {
        mPreferences.edit().putString(KEY_CURRENT_MOBILE_SESSION_ID, sessionId).apply();
    }

    public String getCurrentCompanyName() {
        return mPreferences.getString(Constants.COMPANY_NAME, "");
    }

    public void setUserId(String userId) {
        mPreferences.edit().putString(KEY_USER, userId).apply();
    }

    public String getUserId() {
        return mPreferences.getString(KEY_USER, "");
    }

    public void setPass(String pass) {
        mPreferences.edit().putString(KEY_PASS, pass).apply();
    }

    public String getPass() {
        return mPreferences.getString(KEY_PASS, "");
    }

    public String getDomain() {
        return mPreferences.getString(Constants.DOMAIN, "");
    }

    public String getCurrentMobileSessionId() {
        return mPreferences.getString(KEY_CURRENT_MOBILE_SESSION_ID, "");
    }

    public void putUserData(String userDataJson) {
        mPreferences.edit().putString(USER_JSON_INFO, userDataJson).apply();
    }

    public String getUserData() {
        return mPreferences.getString(USER_JSON_INFO, "");
    }

    public void setCurrentUserID(String userID) {
        mPreferences.edit().putString(KEY_CURRENT_USER_ID, userID).apply();
    }

    public String getCurrentUserID() {
        return mPreferences.getString(KEY_CURRENT_USER_ID, "");
    }

    public void setAvatar(String avatar) {
        mPreferences.edit().putString(KEY_AVATAR, avatar).apply();
    }

    public String getAvatar() {
        return mPreferences.getString(KEY_AVATAR, "");
    }

    public int getCurrentUserNo() {
        return mPreferences.getInt(KEY_CURRENT_USER_NO, 0);
    }

    public void setCurrentUserNo(int userNo) {
        mPreferences.edit().putInt(KEY_CURRENT_USER_NO, userNo).apply();
    }

    public void setSessionError(boolean isError) {
        mPreferences.edit().putBoolean(Statics.PREFS_KEY_SESSION_ERROR, isError).apply();
    }

    public void clearLogin() {
        setCurrentMobileSessionId("");
        setCurrentCompanyNo(0);
        mPreferences.edit().remove(KEY_CURRENT_MOBILE_SESSION_ID).apply();
    }
}