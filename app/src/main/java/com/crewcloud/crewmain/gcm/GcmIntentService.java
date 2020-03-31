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
            //Log.d(Statics.LOG_TAG, extras.toString());
            //Util.printLogs(extras.toString());

            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
//                sendNotification("Send error",extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
//                sendNotification("Deleted messages on server ",
//                        extras.toString());
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
            //Code = Integer.parseInt(extras.getString("Code"));
            String title = extras.getString("Title");
            String fromName = extras.getString("FromName");
            String content = extras.getString("Content");
            String receivedDate = extras.getString("ReceivedDate");
            String toAddress = extras.getString("ToAddress");
            String mailNo = extras.getString("MailNo");
            String mailBoxNo = extras.getString("MailBoxNo");

            System.out.println("aaaaaaaaa title " + title);
            System.out.println("aaaaaaaaa fromName " + fromName);
            System.out.println("aaaaaaaaa content " + content);
            System.out.println("aaaaaaaaa receivedDate " + receivedDate);
            System.out.println("aaaaaaaaa toAddress " + toAddress);
            System.out.println("aaaaaaaaa mailNo " + mailNo);
            System.out.println("aaaaaaaaa mailBoxNo " + mailBoxNo);
            final long unreadCount = bundleDto.getUnreadTotalCount();
            // Update unreadTotalCount to database in new thread, hihi

            Log.d("TAG", "unreadCount:" + unreadCount);

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
        // summary is displayed replacing the position of contentText
        // if summary is not set, contentInfo will not be displayed too
        bigTextStyle.setSummaryText(toAddress);
        // Moves the big view style object into the notification object.
        mBuilder.setStyle(bigTextStyle);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //mNotificationManager.cancelAll();
        // mId allows you to update the notification later on.
        int notificationID = 999;
        Notification notification = mBuilder.build();

        //consider using setTicker of Notification.Builder
//        notification.tickerText = "New Message";
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


    private void executeGCM(GCMData dto, Intent intent) {
        /*switch (BuildConfig.App_name)
        {
            case 0:
                if(dto.type ==1||dto.type==2)
                {
                    HttpRequest.getInstance().checLogin(null);
                }
                break;
            case 1:
                CrewGCMDatabaseHelper.updateGCMItem(dto);
                if(Global.isAppForeground)
                {
                    sendBroadcastToActivity(dto);
                }
                else
                {
                    new CrewPrefs().putBooleanValue(CrewStatics.RELOAD_MAIN_KEY,true);
                }
                break;
            case 2:
                if(dto.type ==1||dto.type==2)
                {
                    HttpRequest.getInstance().checLogin(null);
                }
                else {
                    Util.setBadge(DaZoneApplication.getInstance(), dto.count);
                }
                break;
            default:
                break;
        }

        if(dto.type!=500&&BuildConfig.App_name!=0) {
            sendNotification(dto);
        }
            GcmBroadcastReceiver.completeWakefulIntent(intent);*/
    }

    private void sendNotification(GCMData dto) {
        /*int icon_id;

        PendingIntent contentIntent;
        switch (BuildConfig.App_name)
        {
            case 0:
                icon_id = R.drawable.notifi_ic_timecard;
                contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, LoginActivity.class), 0);
                break;
            case 1:
                icon_id = R.drawable.notifi_ic_crewcloud;
                contentIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, LoginActivity.class), 0);
                break;
            default:

                Intent intent = new Intent(this, CrewNoteActivity.class);
                intent.putExtra(Statics.KEY_NOTE_NO, dto.NoteNo);
                intent.putExtra(Statics.KEY_NOTE_TITLE, dto.title);
                icon_id = R.drawable.notifi_ic_crewnote;
                contentIntent = PendingIntent.getActivity(this, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
        }
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(icon_id)
        .setContentTitle(dto.title)
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(dto.content))
        .setContentText(dto.content);
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(0, mBuilder.build());*/
    }

    // called to send data to Activity
    private void sendBroadcastToActivity(GCMData dto) {
        Intent intent = new Intent(Statics.ACTION_RECEIVER_NOTIFICATION);
        intent.putExtra(Statics.GCM_DATA_NOTIFICATOON, new Gson().toJson(dto));
        sendBroadcast(intent);
    }
}