package com.stockpin.bubble;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

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

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;


public class BubbleService extends Service implements FloatingViewListener {

    SharedPreferences settings;
    public static FloatingViewManager bubbleViewManager;

    private final CharSequence channelName = "Stock Pin Notifications";
    private final String description = "Stock Notification Pin";
    private final int importance = NotificationManager.IMPORTANCE_DEFAULT;
    private final String NOTIFICATION_ID = "com.stockpin.notifications";

    Notification.Builder builder;
    NotificationManager notificationManager;
    NotificationChannel channel;

    ImageView bubble;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //Do nothing if Manager already exists
        if (bubbleViewManager != null) {
            return START_STICKY;
        }


        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        final LayoutInflater inflater = LayoutInflater.from(this);
        final RelativeLayout iconView = (RelativeLayout) inflater.inflate(R.layout.bubble_layout, null, false);

        //Set bubble color
        bubble = iconView.findViewById(R.id.bubbleImage);

        settings = getApplicationContext().getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        int bubbleColor = settings.getInt("bubbleColor", 1);
        switch (bubbleColor) {
            case 1:
                bubble.setImageResource(R.drawable.bubble_icon);break;
            case 2:
                bubble.setImageResource(R.drawable.red_bubble_icon);break;
            case 3:
                bubble.setImageResource(R.drawable.gold_bubble_icon);break;
            case 4:
                bubble.setImageResource(R.drawable.green_bubble_icon);break;
            case 5:
                bubble.setImageResource(R.drawable.purple_bubble_icon);break;
            case 6:
                bubble.setImageResource(R.drawable.black_bubble_icon);break;
        }

        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show bubble popup
                Intent target = new Intent(getApplicationContext(), bubblePopup.class);
                target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                getApplicationContext().startActivity(target);
            }
        });

        //Initiate bubble view manager
        bubbleViewManager = new FloatingViewManager(this, this);
        bubbleViewManager.setFixedTrashIconImage(R.mipmap.bubble_trash);

        bubbleViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra("cutout_safe_area")); //Intent key (Cutout safe area)
        final FloatingViewManager.Options options = new FloatingViewManager.Options();

        //Settings
        final int offset = (int) (48 + 8 * metrics.density);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            options.overMargin = (int) (9 * metrics.density);
            options.floatingViewY = (int) (metrics.heightPixels * 0.65 - offset);
            options.shape = FloatingViewManager.SHAPE_CIRCLE;
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN;
        } else {
            options.overMargin = (int) (9 * metrics.density);
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN;
        }


        loadDynamicOptions();

        bubbleViewManager.addViewToWindow(iconView, options);


        //Create foreground notification
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //Create channel object
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        Random rand = new Random(); //instance of random class
        int UNIQUE_ID = rand.nextInt(Integer.MAX_VALUE);

        Intent target = new Intent(getApplicationContext(), MainActivity.class);

        PendingIntent bubbleIntent = PendingIntent.getActivity(getApplicationContext(), UNIQUE_ID, target, PendingIntent.FLAG_UPDATE_CURRENT);

        //Add notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), channel.getId());
            notificationManager.createNotificationChannel(channel);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }

        builder.setSmallIcon(R.drawable.ic_notify)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.bubble_icon))
                .setContentTitle("Stock Pin Service")
                .setContentText("service is running")
                .setOngoing(true)
                .setAutoCancel(false)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(bubbleIntent);

        Intent stop = new Intent(getApplicationContext(), bubbleReciever.class);
        stop.putExtra("STOP", "stopService");
        PendingIntent stopTheService = PendingIntent.getBroadcast(getApplicationContext(), 0, stop, 0);
        //Add action button
        builder.addAction(Color.TRANSPARENT, "Stop", stopTheService);


        notificationManager.cancelAll();

        startForeground(UNIQUE_ID, builder.build());

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onFinishFloatingView() {
        //Minimize bubble in notifications shade when removed
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        //Create channel object
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_ID, channelName, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        ArrayList<StockStorage> temp = retrieveStockStore(getApplicationContext());
        if (temp != null && temp.size() > 0) { //create minimize if stocks are greater than 0
            Random rand = new Random(); //instance of random class
            int UNIQUE_ID = rand.nextInt(Integer.MAX_VALUE);

            Intent target = new Intent(getApplicationContext(), bubbleReciever.class);

            PendingIntent bubbleIntent = PendingIntent.getBroadcast(getApplicationContext(), UNIQUE_ID, target, PendingIntent.FLAG_UPDATE_CURRENT);

            //Add notification channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(getApplicationContext(), channel.getId());
                notificationManager.createNotificationChannel(channel);
            } else {
                builder = new Notification.Builder(getApplicationContext());
            }

            builder.setSmallIcon(R.drawable.ic_notify)
                    .setContentTitle("Stock Pin Minimized")
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.bubble_icon))
                    .setContentText("Tap to expand")
                    .setAutoCancel(true)
                    .setContentIntent(bubbleIntent);

            Intent openApp = new Intent(getApplicationContext(), MainActivity.class);
            openApp.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            PendingIntent open = PendingIntent.getActivity(getApplicationContext(), UNIQUE_ID - 1, openApp, PendingIntent.FLAG_UPDATE_CURRENT);
            //Add action button
            builder.addAction(Color.TRANSPARENT, "Open App", open);

            SharedPreferences settingsPreferences = getApplicationContext().getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
            int permissionInt = settingsPreferences.getInt("permission", -1);

            notificationManager.cancelAll();
            if (isNotificationsEnabled(getApplicationContext(), NOTIFICATION_ID) && permissionInt == 1) {
                notificationManager.notify(UNIQUE_ID, builder.build());
            }
        }
        stopSelf();
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) { }

    private void loadDynamicOptions() {

        SharedPreferences settingsPreferences = getApplicationContext().getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);

        if(settingsPreferences.getBoolean("hideOnFull", false)) {
            bubbleViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_FULLSCREEN);
        } else {
            bubbleViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        }

    }

    private void destroy() {
        if (bubbleViewManager != null) {
            bubbleViewManager.removeAllViewToWindow();
            bubbleViewManager = null;
        }
    }

    public ArrayList<StockStorage> retrieveStockStore(Context context) {
        ArrayList<StockStorage> stockStore;
        SharedPreferences stockPreferences = context.getSharedPreferences("com.stockpin.stocks", MODE_PRIVATE);
        String serializedObject = stockPreferences.getString("stockData", null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<StockStorage>>(){}.getType();
            stockStore = gson.fromJson(serializedObject, type);
        } else {
            stockStore = new ArrayList<>();
        }
        return stockStore;
    }

    /**
     * Checks if notifications are enabled
     *
     * @param context
     * @param channelId
     * @return
     */
    public boolean isNotificationsEnabled(Context context, @Nullable String channelId){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(!TextUtils.isEmpty(channelId)) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = manager.getNotificationChannel(channelId);
                return channel.getImportance() != NotificationManager.IMPORTANCE_NONE  && NotificationManagerCompat.from(context).areNotificationsEnabled();
            }
            return false;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }
}
