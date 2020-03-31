package com.crewcloud.crewmain.util;

public class Statics {
    public static final int REQUEST_TIMEOUT_MS = 15000;
    public static final String PREFS_KEY_SESSION_ERROR = "session_error";
    public static final boolean WRITEHTTPREQUEST = true;
    public static final String URL_HAS_APPLICATION = "/UI/WebService/WebServiceCenter.asmx/HasApplication_v2";

    public static final String URL_GET_USER_INFO = "/UI/MobileWorkingTime/WorkingTime_Service.asmx/GetUserInfo";
    public static final String URL_REG_GCM_ID = "/UI/WebService/GCMWebService.asmx/Note_GCMUpdateRegID";
    public static final String URL_CHECK_SESSION = "/UI/WebService/WebServiceCenter.asmx/CheckSessionUser_v2";
    public static final String URL_ALL_BOARD = "http://wine.woorihom.com/UI/Board";
    public static final String ACTION_RECEIVER_NOTIFICATION = "receiver_notification";
    public static final String GCM_DATA_NOTIFICATOON = "gcm_data_notificaiton";

    //final public static String GOOGLE_SENDER_ID_MAIL = "537469459942";//AIzaSyAioHyUAEvdJ4GRDJEkJoxZVnCWMdOJezs
    final public static String GOOGLE_SENDER_ID_MAIL = "268267203529";

    /**
     * KEY PREFERENCES
     */
    public static final String KEY_PREFERENCES_PIN = "KEY_PREFERENCES_PIN";
    public static final String KEY_PREFERENCES_ADJUST_TO_SCREEN_WIDTH = "KEY_PREFERENCES_ADJUST_TO_SCREEN_WIDTH";
    public static final String KEY_PREFERENCES_NOTIFICATION_NEW_MAIL = "KEY_PREFERENCES_NOTIFICATION_NEW_MAIL";
    public static final String KEY_PREFERENCES_NOTIFICATION_SOUND = "KEY_PREFERENCES_NOTIFICATION_SOUND";
    public static final String KEY_PREFERENCES_NOTIFICATION_VIBRATE = "KEY_PREFERENCES_NOTIFICATION_VIBRATE";
    public static final String KEY_PREFERENCES_NOTIFICATION_TIME = "KEY_PREFERENCES_NOTIFICATION_TIME";
    public static final String KEY_PREFERENCES_NOTIFICATION_TIME_TO_TIME = "KEY_PREFERENCES_NOTIFICATION_TIME_TO_TIME";
    public static final String KEY_PREFERENCES_NOTIFICATION_TIME_FROM_TIME = "KEY_PREFERENCES_NOTIFICATION_TIME_FROM_TIME";
    public static String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
}
