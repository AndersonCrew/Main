package com.crewcloud.crewmain.util;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crewcloud.crewmain.CrewCloudApplication;
import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.datamodel.Application;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Util {
    public static String getString(int stringID) {
        return CrewCloudApplication.getInstance().getApplicationContext().getResources().getString(stringID);
    }

    public static long getTimeOffsetInMinute() {
        return TimeUnit.MINUTES.convert(getTimeOffsetInMillis(), TimeUnit.MILLISECONDS);
    }

    private static long getTimeOffsetInMillis() {
        Calendar mCalendar = new GregorianCalendar();
        TimeZone mTimeZone = mCalendar.getTimeZone();

        return mTimeZone.getRawOffset();
    }

    public static String displayTimeWithoutOffset(String timeString) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(Statics.DATE_FORMAT_YYYY_MM_DD, Locale.getDefault());
            return formatter.format(new Date(getTime(timeString)));
        } catch (Exception e) {
            return "";
        }
    }

    public static String formatYear(String birthDate) {
        String result = "";
        try {
            String timeString;
            timeString = birthDate.substring(birthDate.indexOf('(') + 1, birthDate.indexOf('+'));
            Date date = new Date(Long.parseLong(timeString));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy");
            result = simpleDateFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static long getTime(String timeString) {
        try {
            long time;

            if (timeString.contains("(")) {
                timeString = timeString.replace("/Date(", "");
                int plusIndex = timeString.indexOf("+");
                int minusIndex = timeString.indexOf("-");
                if (plusIndex != -1) {
                    time = Long.valueOf(timeString.substring(0, plusIndex));
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(time);
                    cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(timeString.substring(plusIndex + 1, plusIndex + 3)));
                    cal.add(Calendar.MINUTE, Integer.parseInt(timeString.substring(plusIndex + 3, plusIndex + 5)));
                    Calendar tCal = Calendar.getInstance();
                    tCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                    time = tCal.getTimeInMillis();
                } else if (minusIndex != -1) {
                    time = Long.valueOf(timeString.substring(0, minusIndex));
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(time);
                    cal.setTimeZone(TimeZone.getDefault());
                    cal.add(Calendar.HOUR_OF_DAY, -Integer.parseInt(timeString.substring(minusIndex + 1, minusIndex + 3)));
                    cal.add(Calendar.MINUTE, -Integer.parseInt(timeString.substring(minusIndex + 3, minusIndex + 5)));
                    Calendar tCal = Calendar.getInstance();
                    tCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
                    time = tCal.getTimeInMillis();
                } else {
                    time = Long.valueOf(timeString.substring(0, timeString.indexOf(")")));
                }
            } else {
                time = Long.valueOf(timeString);
            }

            return time;
        } catch (Exception e) {
            Log.d("lchTest", e.toString());
            return 0;
        }
    }

    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public static int compareVersionNames(String oldVersionName, String newVersionName) {
        int res = 0;

        String[] oldNumbers = oldVersionName.split("\\.");
        String[] newNumbers = newVersionName.split("\\.");

        // To avoid IndexOutOfBounds
        int maxIndex = Math.min(oldNumbers.length, newNumbers.length);

        for (int i = 0; i < maxIndex; i++) {
            int oldVersionPart = Integer.valueOf(oldNumbers[i]);
            int newVersionPart = Integer.valueOf(newNumbers[i]);

            if (oldVersionPart < newVersionPart) {
                res = -1;
                break;
            } else if (oldVersionPart > newVersionPart) {
                res = 1;
                break;
            }
        }

        // If versions are the same so far, but they have different length...
        if (res == 0 && oldNumbers.length != newNumbers.length) {
            res = (oldNumbers.length > newNumbers.length) ? 1 : -1;
        }

        return res;
    }

    public static String getPhoneLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static void oneButtonAlertDialog(final Activity context, String title, String message, String okButton) {
        // Build an AlertDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);

        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_common, null);

        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        // Get the custom alert dialog view widgets reference
        Button btn_positive = dialogView.findViewById(R.id.btn_yes);
        Button btn_negative = dialogView.findViewById(R.id.btn_no);
        TextView txtTitle = dialogView.findViewById(R.id.txt_dialog_title);
        TextView txtContent = dialogView.findViewById(R.id.txt_dialog_content);

        btn_negative.setVisibility(View.GONE);
        btn_positive.setText(okButton);

        txtTitle.setText(title);
        txtContent.setText(message);

        final android.app.AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        // Display the custom alert dialog on interface
        if (!context.isFinishing()) {
            dialog.show();
        }
    }

    public interface OnAlertDialogViewClickEvent {
        void onOkClick(DialogInterface alertDialog);

        void onCancelClick();
    }

    public static void customAlertDialog(final Activity context, String title, String message, String okButton, String noButton, final OnAlertDialogViewClickEvent clickEvent) {
        // Build an AlertDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = context.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_common, null);

        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        // Get the custom alert dialog view widgets reference
        Button btn_positive = dialogView.findViewById(R.id.btn_yes);
        Button btn_negative = dialogView.findViewById(R.id.btn_no);
        TextView txtTitle = dialogView.findViewById(R.id.txt_dialog_title);
        TextView txtContent = dialogView.findViewById(R.id.txt_dialog_content);

        btn_negative.setText(noButton);
        btn_positive.setText(okButton);
        txtTitle.setText(title);
        txtContent.setText(message);

        // Create the alert dialog
        final android.app.AlertDialog dialog = builder.create();

        btn_positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickEvent != null) {
                    clickEvent.onOkClick(dialog);
                }
                dialog.dismiss();
            }
        });

        // Set negative/no button click listener
        btn_negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the alert dialog
                if (clickEvent != null) {
                    clickEvent.onCancelClick();
                }
                dialog.cancel();
            }
        });

        dialog.show();
    }

    public static String setServerSite(String domain) {
        String[] domains = domain.split("[.]");
        if (domain.contains(".bizsw.co.kr") && !domain.contains("8080")) {
            domain =  domain.replace(".bizsw.co.kr", ".bizsw.co.kr:8080");
        }

        if (domains.length == 1) {
            domain = domains[0] + ".crewcloud.net";
        }

        if(domain.startsWith("http://") || domain.startsWith("https://")){
            domain = domain.startsWith("http://") ? domain.replace("http://", ""): domain.startsWith("https://") ? domain.replace("https://", ""): domain;
        }

        String head = CrewCloudApplication.getInstance().getPreferenceUtilities().getBooleanValue(Constants.HAS_SSL, false) ? "https://" : "http://";
        String domainCompany = head + domain;
        CrewCloudApplication.getInstance().getPreferenceUtilities().putStringValue(Constants.DOMAIN, domainCompany);
        CrewCloudApplication.getInstance().getPreferenceUtilities().putStringValue(Constants.COMPANY_NAME, domain);
        return domainCompany;
    }

    public static boolean checkContainApp(List<Application> list, String projectCode) {
        for(Application application : list) {
            if(application.getProjectCode().equals(projectCode)) {
                return true;
            }
        }
        return false;
    }
}