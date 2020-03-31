package com.crewcloud.crewmain.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.crewcloud.crewmain.CrewCloudApplication;


public class PreferenceUtilities {
    private SharedPreferences mPreferences;
    private final String KEY_CURRENT_SERVICE_DOMAIN = "currentServiceDomain";
    private final String KEY_CURRENT_COMPANY_DOMAIN = "currentCompanyDomain";
    private final String KEY_CURRENT_COMPANY_ID = "currentCompanyID";
    private final String KEY_CURRENT_COMPANY_NO = "currentCompanyNo";
    private final String KEY_CURRENT_COMPANY_NAME = "currentCompanyName";
    private final String USER_JSON_INFO = "user_json";
    private final String KEY_CURRENT_MOBILE_SESSION_ID = "currentMobileSessionId";
    private final String KEY_CURRENT_USER_ID = "currentUserID";
    private final String KEY_AVATAR = "avatar";
    private final String KEY_USER = "userID";
    private final String KEY_PASS = "pass";
    private final String KEY_DOMAIN = "domain";
    private final String KEY_CELL_PHONE = "cellphone";
    private final String KEY_ENTRANCE_DAY = "entranceday";
    private final String KEY_COMPANY_PHONE = "companyphone";
    private final String KEY_BIRTHDAY = "birthday";
    private final String KEY_FULL_NAME = "FullName";
    private final String KEY_NAME_COMPANY = "NameCompany";
    private final String KEY_MAIL = "MailAddress";
    private final String KEY_CURRENT_USER_NO = "currentUserNo";
    private final String KEY_CURRENT_USER_IS_ADMIN = "currentUserIsAdmin";
    private final String INTRO_COUNT = "introCount";
    private final String AESORTTYPE = "aeSortType";
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

    public void setCurrentServiceDomain(String domain) {
        mPreferences.edit().putString(KEY_CURRENT_SERVICE_DOMAIN, domain).apply();
    }

    public String getCurrentServiceDomain() {
        return mPreferences.getString(KEY_CURRENT_SERVICE_DOMAIN, "");
    }

    public void setCurrentCompanyDomain(String domain) {
        mPreferences.edit().putString(KEY_CURRENT_COMPANY_DOMAIN, domain).apply();
    }

    public String getCurrentCompanyDomain() {
        return mPreferences.getString(KEY_CURRENT_COMPANY_DOMAIN, "");
    }

    public String getCompanyId() {
        return mPreferences.getString(KEY_CURRENT_COMPANY_ID, "");
    }

    public void setCurrentCompanyId(String companyId) {
        mPreferences.edit().putString(KEY_CURRENT_COMPANY_ID, companyId).apply();
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

    public void setCurrentCompanyName(String name) {
        mPreferences.edit().putString(KEY_CURRENT_COMPANY_NAME, name).apply();
    }

    public String getCurrentCompanyName() {
        return mPreferences.getString(KEY_CURRENT_COMPANY_NAME, "");
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

    public void setDomain(String domain) {
        mPreferences.edit().putString(KEY_DOMAIN, domain).apply();
    }

    public String getDomain() {
        return mPreferences.getString(KEY_DOMAIN, "");
    }

    public void setCellPhone(String cellPhone) {
        mPreferences.edit().putString(KEY_CELL_PHONE, cellPhone).apply();
    }

    public void setBirthday(String birthday) {
        mPreferences.edit().putString(KEY_BIRTHDAY, birthday).apply();
    }

    public String getCompanyPhone() {
        return mPreferences.getString(KEY_COMPANY_PHONE, "");
    }

    public String getEntranceDate() {
        return mPreferences.getString(KEY_ENTRANCE_DAY, "");
    }

    public String getBirthDay() {
        return mPreferences.getString(KEY_BIRTHDAY, "");
    }

    public void setEntranceDate(String entranceDay) {
        mPreferences.edit().putString(KEY_ENTRANCE_DAY, entranceDay).apply();
    }

    public void setCompanyPhone(String companyPhone) {
        mPreferences.edit().putString(KEY_COMPANY_PHONE, companyPhone).apply();
    }

    public String getCellPhone() {
        return mPreferences.getString(KEY_CELL_PHONE, "");
    }

    public void setFullName(String fullName) {
        mPreferences.edit().putString(KEY_FULL_NAME, fullName).apply();
    }

    public String getFullName() {
        return mPreferences.getString(KEY_FULL_NAME, "");
    }

    public void setEmail(String email) {
        mPreferences.edit().putString(KEY_MAIL, email).apply();
    }

    public String getEmail() {
        return mPreferences.getString(KEY_MAIL, "");
    }

    public String getCurrentMobileSessionId() {
        return mPreferences.getString(KEY_CURRENT_MOBILE_SESSION_ID, "");
    }

    public void putUserData(String userDataJson) {
        mPreferences.edit().putString(USER_JSON_INFO, userDataJson).apply();
    }

    public void removeUserData() {
        mPreferences.edit().remove(USER_JSON_INFO).apply();
    }

    public String getUserData() {
        return mPreferences.getString(USER_JSON_INFO, "");
    }

    // ----------------------------------------------------------------------------------------------

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

    public int getCurrentIsAdmin() {
        return mPreferences.getInt(KEY_CURRENT_USER_IS_ADMIN, 0);
    }

    public void setCurrentUserIsAdmin(int isAdmin) {
        mPreferences.edit().putInt(KEY_CURRENT_USER_IS_ADMIN, isAdmin).apply();
    }

    public void setSessionError(boolean isError) {
        mPreferences.edit().putBoolean(Statics.PREFS_KEY_SESSION_ERROR, isError).apply();
    }

    public boolean getSessionError() {
        return mPreferences.getBoolean(Statics.PREFS_KEY_SESSION_ERROR, false);
    }

    public void putaeSortType(int aeSortType) {
        mPreferences.edit().putInt(AESORTTYPE, aeSortType).apply();
    }

    public int getaeSortType() {
        return mPreferences.getInt(AESORTTYPE, 0);
    }

    public void clearLogin() {
        setCurrentServiceDomain("");
        setCurrentMobileSessionId("");
        setCurrentCompanyNo(0);
        mPreferences.edit().remove(KEY_CURRENT_MOBILE_SESSION_ID).apply();
    }
}