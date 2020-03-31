package com.crewcloud.crewmain.util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Dazone on 7/25/2017.
 */

public class TimeUtils {

    public static String getTimezoneOffsetInMinutes() {
        TimeZone tz = TimeZone.getDefault();
        int offsetMinutes = tz.getRawOffset() / 60000;
        String sign = "";
        if (offsetMinutes < 0) {
            sign = "-";
            offsetMinutes = -offsetMinutes;
        }
        return sign + "" + offsetMinutes;
    }

    /**
     * CONVERT STRING TO TIME - NOTIFICATION SETTING
     */
    public static Calendar getTimeFromStr(String strTime) {
        String hours = strTime.substring(3, 5);
        String minutes = strTime.substring(6, 8);
        int intHours = Integer.parseInt(hours);
        int intMinutes = Integer.parseInt(minutes);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, intHours);
        calendar.set(Calendar.MINUTE, intMinutes);
        return calendar;
    }

    /**
     * CHECK TIME BETWEEN
     */
    public static boolean isBetweenTime(String strFromTime, String strToTime) {
        Calendar calendar = Calendar.getInstance();
        Calendar calFromTime = getTimeFromStr(strFromTime);
        Calendar calToTime = getTimeFromStr(strToTime);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int hourFromTime = calFromTime.get(Calendar.HOUR_OF_DAY);
        int minuteFromTime = calFromTime.get(Calendar.MINUTE);
        int hourToTime = calToTime.get(Calendar.HOUR_OF_DAY);
        int minuteToTime = calToTime.get(Calendar.MINUTE);
        if (hourFromTime == hour || hour == hourToTime) {
            if (hourFromTime == hour && hour == hourToTime) {
                if (minuteFromTime <= minute && minute <= minuteToTime) {
                    return true;
                } else {
                    return false;
                }
            } else if (hourFromTime == hour && hour != hourToTime) {
                if (minuteFromTime <= minute) {
                    return true;
                } else {
                    return false;
                }
            } else if (hourFromTime != hour && hour == hourToTime) {
                if (minute <= minuteToTime) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (hourFromTime < hour && hour < hourToTime) {
                return true;
            } else {
                return false;
            }
        }
    }
}
