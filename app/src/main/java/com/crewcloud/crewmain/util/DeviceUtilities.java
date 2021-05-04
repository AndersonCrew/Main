package com.crewcloud.crewmain.util;

import android.content.Context;


import com.crewcloud.crewmain.CrewCloudApplication;

import java.util.Locale;
import java.util.TimeZone;

public class DeviceUtilities {
    public static String getLanguageCode() {
        Context context = CrewCloudApplication.getInstance().getBaseContext();
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        switch (language) {
            case "ko":
                return "KO";
            case "vi":
                return "VN";
            case "zh":
                return "CH";
            case "ja":
                return "JP";
            default:
                return "EN";
        }
    }

    public static int getTimeZoneOffset() {
        return TimeZone.getDefault().getRawOffset() / 1000 / 60;
    }
}