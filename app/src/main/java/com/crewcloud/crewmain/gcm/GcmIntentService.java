package com.crewcloud.crewmain.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.crewcloud.crewmain.R;
import com.crewcloud.crewmain.activity.MainActivity;
import com.crewcloud.crewmain.datamodel.GCMData;
import com.crewcloud.crewmain.datamodel.NotificationBundleDto;
import com.crewcloud.crewmain.util.PreferenceUtilities;
import com.crewcloud.crewmain.util.Statics;
import com.crewcloud.crewmain.util.StaticsBundle;
import com.crewcloud.crewmain.util.TimeUtils;
import com.crewcloud.crewmain.util.Util;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import me.leolin.shortcutbadger.ShortcutBadger;

public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    private int Code = 0;

    /**
     * NOTIFICATION
     */
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                if (extras.containsKey("Code")) {
                    Code = Integer.parseInt(extras.getString("Code", "0"));

                    switch (Code) {
                        case 1:
                            Log.d("TAG", "Case 1 ###");
                            receiveCode1(extras);
                            break;
                    }
                }

            }
        } else {
            Log.d("TAG", "empty");
        }
    }

    private void receiveCode1(Bundle extras) {
        try {
            Log.d("TAG", extras.toString());
            NotificationBundleDto bundleDto = new Gson().fromJson(extras.getString("Data"), NotificationBundleDto.class);
            String title = extras.getString("Title");
            String fromName = extras.getString("FromName");
            String content = extras.getString("Content");
            String receivedDate = extras.getString("ReceivedDate");
            String toAddress = extras.getString("ToAddress");
            String mailNo = extras.getString("MailNo");
            String mailBoxNo = extras.getString("MailBoxNo");

            final long unreadCount = bundleDto.getUnreadTotalCount();

            ShortcutBadger.applyCount(this, (int) unreadCount); //for 1.1.4

            ShowNotification(title, fromName, content, receivedDate, toAddress, Long.parseLong(mailNo), mailBoxNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ShowNotification(String title, String fromName, String content, String receivedDate, String toAddress, long mailNo, String mailBoxNo) {

        long[] vibrate = new long[]{1000, 1000, 0, 0, 0};
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /** PendingIntent */
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(StaticsBundle.BUNDLE_MAIL_NO, mailNo);
        intent.putExtra(StaticsBundle.BUNDLE_MAIL_FROM_NOTIFICATION, true);
        intent.putExtra(StaticsBundle.BUNDLE_MAIL_FROM_NOTIFICATION_MAILBOX_NO, mailBoxNo);
        intent.putExtra(StaticsBundle.PREFS_KEY_ISREAD, false);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setTicker("The new mail has arrived.")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentTitle(fromName)
                        .setContentText(title)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        /** GET PREFERENCES */
        boolean isVibrate = new PreferenceUtilities().getBooleanValue(Statics.KEY_PREFERENCES_NOTIFICATION_VIBRATE, true);
        boolean isSound = new PreferenceUtilities().getBooleanValue(Statics.KEY_PREFERENCES_NOTIFICATION_SOUND, true);
        boolean isNewMail = new PreferenceUtilities().getBooleanValue(Statics.KEY_PREFERENCES_NOTIFICATION_NEW_MAIL, true);
        boolean isTime = new PreferenceUtilities().getBooleanValue(Statics.KEY_PREFERENCES_NOTIFICATION_TIME, true);
        String strFromTime = new PreferenceUtilities().getStringValue(Statics.KEY_PREFERENCES_NOTIFICATION_TIME_FROM_TIME, Util.getString(R.string.setting_notification_from_time));
        String strToTime = new PreferenceUtilities().getStringValue(Statics.KEY_PREFERENCES_NOTIFICATION_TIME_TO_TIME, Util.getString(R.string.setting_notification_to_time));


        if (isVibrate) {
            mBuilder.setVibrate(vibrate);
        }

        if (isSound) {
            mBuilder.setSound(soundUri);
        }

        NotificationCompat.BigTextStyle bigTextStyle
                = new NotificationCompat.BigTextStyle();

        /** STYLE BIG TEXT */
        String bigText = "<font color='#878787'>" + title + "</font>";
        if (!TextUtils.isEmpty(content.replaceAll("&nbsp;", "").trim())) {
            bigText = bigText + "<br/>" + content;
        }

        bigTextStyle.bigText(Html.fromHtml(bigText));
        bigTextStyle.setSummaryText(toAddress);
        mBuilder.setStyle(bigTextStyle);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = 999;
        Notification notification = mBuilder.build();

        //consider using setTicker of Notification.Builder
        if (isNewMail) {
            if (isTime) {
                if (TimeUtils.isBetweenTime(strFromTime, strToTime)) {
                    mNotificationManager.notify(notificationID, notification);
                    mNotificationManager.notify(notificationID, mBuilder.build());
                }
            } else {
                mNotificationManager.notify(notificationID, notification);
                mNotificationManager.notify(notificationID, mBuilder.build());
            }
        }
    }
}