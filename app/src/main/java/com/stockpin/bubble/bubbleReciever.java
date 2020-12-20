package com.stockpin.bubble;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stockpin.stockpinapp.MainActivity;
import com.stockpin.stockpinapp.R;
import com.stockpin.stockpinapp.StockStorage;
import com.stockpin.stockpinapp.bubblePopup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class bubbleReciever extends BroadcastReceiver {

    private final CharSequence channelName = "Stock Pin Notifications";
    private final String description = "Stock Notification Pin";
    private final int importance = NotificationManager.IMPORTANCE_DEFAULT;
    private final String NOTIFICATION_ID = "com.stockpin.notifications";

    Notification.Builder builder;
    NotificationManager notificationManager;
    NotificationChannel channel;


    @Override
    public void onReceive(final Context context, Intent intent) {

        try {
            String stop = intent.getExtras().getString("STOP");
            if (stop != null && stop.equals("stopService")) {
                context.stopService(new Intent(context, BubbleService.class));
                return;
            }
        } catch (NullPointerException n) { }
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Create channel object
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_ID, channelName, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        //Add new bubble
        addNewBubble(context);
//        Intent openBubble = new Intent(context, bubblePopup.class);
//        openBubble.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(openBubble);

    }


    private void addNewBubble(final Context context) {
        try {
            if (!isMyServiceRunning(BubbleService.class, context)) {
                // launch service
                final Intent blowBubble = new Intent(context, BubbleService.class);
                try {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
                        context.startService(blowBubble);
                        Intent openBubble = new Intent(context, bubblePopup.class);
                        openBubble.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(openBubble);
                    } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                        Toast.makeText(context, "Permission Not Enabled", Toast.LENGTH_SHORT).show();
                        SharedPreferences settingsPreferences = context.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settingsPreferences.edit();
                        editor.putInt("permission", -1);
                        editor.commit();
                    }
                    else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        context.startService(blowBubble);
                        Intent openBubble = new Intent(context, bubblePopup.class);
                        openBubble.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(openBubble);
                    }

                } catch (Exception e) {
                    //unable to create bubble
                }
            }
        } catch (ReceiverCallNotAllowedException e) {
            //Open app if bubble cannot be created
            try {
                Intent openApp = new Intent(context, MainActivity.class);
                openApp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                context.startActivity(openApp);
            } catch (Exception n) {
                Toast.makeText(context, "Timed out", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
