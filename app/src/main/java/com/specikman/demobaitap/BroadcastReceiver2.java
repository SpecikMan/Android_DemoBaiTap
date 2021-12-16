package com.specikman.demobaitap;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BroadcastReceiver2 extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String answer = intent.getStringExtra("answer");
        String choose = intent.getStringExtra("choose");
        int notification_id = intent.getIntExtra("notification_id",-1);
        if(answer!=null && choose!=null){
            if(answer.equals(choose)){
                if(notification_id>0){
                    NotificationManager notificationManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(notification_id);
                }
                Toast.makeText(context, "Correct", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Incorrect", Toast.LENGTH_SHORT).show();
            }
        }
    }
}