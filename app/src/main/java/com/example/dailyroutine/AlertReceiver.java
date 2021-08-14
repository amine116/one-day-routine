package com.example.dailyroutine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Objects;

public class AlertReceiver extends BroadcastReceiver {

    public final static int NOT_START_ID = 1;
    public static final String CH_NAME = "chanel_start",
            CH_START_ID = "starting_ID", CH_DESC = "description";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManagerCompat nManager = NotificationManagerCompat.from(context);

        NotificationCompat.Builder not = new NotificationCompat.Builder(context, CH_START_ID)
                .setSmallIcon(R.drawable.ic_add_play_list)
                .setContentTitle(intent.getStringExtra("TITLE"))
                .setContentText(intent.getStringExtra("MESSAGES"))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(intent.getStringExtra("LONG_TEXT")))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        not.setAutoCancel(true);

        nManager.notify(NOT_START_ID, not.build());
    }
}