package com.meeple.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.meeple.R;
import com.meeple.activity.DashBoardActivity;
import com.meeple.receiver.GCMBroadCastReceiver;

/**
 * Created by harry on 10/5/15.
 */
public class GCMNotificationIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;

    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCMNotificationIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

        String messageType = gcm.getMessageType(intent);

        if (extras != null) {
            if (!extras.isEmpty()) {
                if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
                        .equals(messageType)) {
                    sendNotification("Send error: " + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
                        .equals(messageType)) {
                    sendNotification("Deleted messages on server: "
                            + extras.toString());
                } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
                        .equals(messageType)) {

                    if ("USERLIST".equals(extras.get("SM"))) {
                        //update the userlist view
                        Intent userListIntent = new Intent("com.javapapers.android.gcm.chat.userlist");
                        String userList = extras.get("USERLIST").toString();
                        userListIntent.putExtra("USERLIST", userList);
                        sendBroadcast(userListIntent);
                    } else if ("CHAT".equals(extras.get("SM"))) {
                        Intent chatIntent = new Intent("com.javapapers.android.gcm.chat.chatmessage");
                        chatIntent.putExtra("CHATMESSAGE", extras.get("CHATMESSAGE").toString());
                        sendBroadcast(chatIntent);
                    }

                }
            }
        }
        GCMBroadCastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        notificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DashBoardActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("GCM XMPP Message")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}