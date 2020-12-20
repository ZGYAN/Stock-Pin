package com.stockpin.bubble;

import android.app.ActivityManager;
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
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stockpin.stockpinapp.MainActivity;
import com.stockpin.stockpinapp.NetworkRequest;
import com.stockpin.stockpinapp.R;
import com.stockpin.stockpinapp.StockStorage;
import com.stockpin.stockpinapp.bubblePopup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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

    private final CharSequence channelNameAlert = "Stock Pin Alerts";
    private final String descriptionAlert = "Stock Notification Pin Alerts";
    private final String NOTIFICATION_IDAlert = "com.stockpin.notifications.alerts";

    Notification.Builder builderAlert;
    NotificationManager notificationManagerAlert;
    NotificationChannel channelAlert;

    CardView card;
    ImageView bubble;
    ImageView imgAnim, imgAnim2;

    Handler pulseHandler;
    int pulseCounts;
    NetworkRequest netRequest;
    Timer myTimer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        myTimer = new Timer();

        //Do nothing if Manager already exists
        if (bubbleViewManager != null) {
            return START_STICKY;
        }



        pulseHandler = new Handler();
        pulseCounts = 0;
        netRequest = new NetworkRequest(getApplicationContext());

        final DisplayMetrics metrics = new DisplayMetrics();
        final WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        LayoutInflater inflater = LayoutInflater.from(this);
        RelativeLayout iconView = (RelativeLayout) inflater.inflate(R.layout.bubble_layout, null, false);

        card = iconView.findViewById(R.id.bubbleCard);
        card.setCardBackgroundColor(Color.TRANSPARENT);
        card.setCardElevation(0);
        //Set bubble color
        bubble = iconView.findViewById(R.id.bubbleImage);
        imgAnim = iconView.findViewById(R.id.imgAnim);
        imgAnim2 = iconView.findViewById(R.id.imgAnim2);


        settings = getApplicationContext().getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        int bubbleColor = settings.getInt("bubbleColor", 1);
        switch (bubbleColor) {
            case 1:
                bubble.setImageResource(R.drawable.bubble_icon);
                break;
            case 2:
                bubble.setImageResource(R.drawable.red_bubble_icon);
                break;
            case 3:
                bubble.setImageResource(R.drawable.gold_bubble_icon);
                break;
            case 4:
                bubble.setImageResource(R.drawable.green_bubble_icon);
                break;
            case 5:
                bubble.setImageResource(R.drawable.purple_bubble_icon);
                break;
            case 6:
                bubble.setImageResource(R.drawable.black_bubble_icon);
                break;
        }

        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Show bubble popup
                Intent target = new Intent(getApplicationContext(), bubblePopup.class);
                target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                getApplicationContext().startActivity(target);
                stopPulsing();
            }
        });


        //Initiate bubble view manager
        bubbleViewManager = new FloatingViewManager(this, this);
        bubbleViewManager.setFixedTrashIconImage(R.mipmap.bubble_trash);

        bubbleViewManager.setSafeInsetRect((Rect) intent.getParcelableExtra("cutout_safe_area")); //Intent key (Cutout safe area)
        final FloatingViewManager.Options options = new FloatingViewManager.Options();

        //Settings
        final int offset = (int) (48 + 8 * metrics.density);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            options.overMargin = (int) (30 * metrics.density);
            options.floatingViewY = (int) (metrics.heightPixels * 0.65 - offset);
            options.shape = FloatingViewManager.SHAPE_CIRCLE;
            options.moveDirection = FloatingViewManager.MOVE_DIRECTION_THROWN;
        } else {
            options.overMargin = (int) (30 * metrics.density);
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

        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkStockAlerts();
            }
        },10000,   1000*60*15); //15 minute intervals

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
    public void onTouchFinished(boolean isFinishing, int x, int y) {
    }

    private void loadDynamicOptions() {

        SharedPreferences settingsPreferences = getApplicationContext().getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);

        if (settingsPreferences.getBoolean("hideOnFull", false)) {
            bubbleViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_HIDE_FULLSCREEN);
        } else {
            bubbleViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        }

    }

    private void destroy() {
        myTimer.cancel();
        if (bubbleViewManager != null) {
            bubbleViewManager.removeAllViewToWindow();
            bubbleViewManager = null;
        }
    }


    private void startPulsing() {
        this.runnableAnimate.run();
    }

    private void stopPulsing() {
        this.pulseHandler.removeCallbacks(runnableAnimate);
        pulseCounts = 0;
    }


    private Runnable runnableAnimate = new Runnable() {
        @Override
        public void run() {

            imgAnim.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    imgAnim.setScaleX(1f);
                    imgAnim.setScaleY(1f);
                    imgAnim.setAlpha(1f);
                    pulseCounts = pulseCounts + 1;
                    if (pulseCounts > 5) {
                        stopPulsing();
                    }
                }
            });

            imgAnim2.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(600).withEndAction(new Runnable() {
                @Override
                public void run() {
                    imgAnim2.setScaleX(1f);
                    imgAnim2.setScaleY(1f);
                    imgAnim2.setAlpha(1f);
                }
            });


            pulseHandler.postDelayed(runnableAnimate, 1800);

        }
    };


    public void saveStockStore(ArrayList<StockStorage> stockStore) {
        SharedPreferences stockPreferences = getApplicationContext().getSharedPreferences("com.stockpin.stocks", MODE_PRIVATE);
        SharedPreferences.Editor editor = stockPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(stockStore);
        editor.putString("stockData", json);
        editor.commit();
    }

    public ArrayList<StockStorage> retrieveStockStore(Context context) {
        ArrayList<StockStorage> stockStore;
        SharedPreferences stockPreferences = context.getSharedPreferences("com.stockpin.stocks", MODE_PRIVATE);
        String serializedObject = stockPreferences.getString("stockData", null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<StockStorage>>() {
            }.getType();
            stockStore = gson.fromJson(serializedObject, type);
        } else {
            stockStore = new ArrayList<>();
        }
        return stockStore;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    public void checkStockAlerts() {
        if (isMyServiceRunning(BubbleService.class) && settings.getBoolean("isAlerting", true)) {

            //All list are in parallel
            final ArrayList<StockStorage> stockList = retrieveStockStore(getApplicationContext());

            if (netRequest.isConnected() && stockList.size() > 0) {

                String symbols = "";
                for (int i = 0; i < stockList.size(); i++) {
                    symbols = symbols + stockList.get(i).getTicker() + ",";
                }
                symbols = symbols.substring(0, symbols.length() - 1);


                //Initialize new RequestQueue instance
                RequestQueue stockRequestQueue = Volley.newRequestQueue(this);
                //JsonArray Request instance
                JsonObjectRequest stockRequest = new JsonObjectRequest(Request.Method.GET, netRequest.getQuote(symbols), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        ArrayList<Double> stockPercentChangeList = new ArrayList<>();
                        ArrayList<Double> stockPriceList = new ArrayList<>();
                        String marketState = "";

                        //Check for API error
                        boolean apiError = false;
                        try {
                            JSONArray stockStatus = response.getJSONObject("quoteResponse").getJSONArray("result");
                            if (stockStatus.length() == 0) { //no results returned
                                apiError = true;
                            }
                        } catch (Exception e) {
                            apiError = false;
                            e.printStackTrace();
                        }

                        //extract quote information
                        if (!apiError) {
                            try {

                                JSONArray stocks = response.getJSONObject("quoteResponse").getJSONArray("result");

                                DecimalFormat unitFormat;
                                unitFormat = new DecimalFormat("###,###,###,###,###,###.##");
                                unitFormat.setDecimalSeparatorAlwaysShown(true);
                                unitFormat.setMinimumFractionDigits(2);

                                for (int i = 0; i < stocks.length(); i++) {

                                    double stockPrice, stockPercentChange;

                                    JSONObject stock = stocks.getJSONObject(i);
                                    try { marketState = stock.getString("marketState"); } catch (Exception e) { marketState = ""; }
                                    if(marketState.equals("POSTPOST") || marketState.contains("CLOSED") || marketState.equals("PREPRE")) {
                                        marketState = "Market Closed";
                                    } else if(marketState.contains("REGULAR")) {
                                        marketState = "Market Open";
                                    } else if(marketState.equals("PRE")) {
                                        marketState = "Pre-Market";
                                    } else if(marketState.equals("POST")) {
                                        marketState = "After Hours";
                                    }  else {
                                        marketState = "";
                                    }

                                    if(marketState.contains("Closed")) {
                                        //stop alerts
                                        return; //do not continue with alert checks
                                    } else {
                                        if (marketState.equals("Market Open")) {
                                            try {
                                                stockPrice = stock.getDouble("regularMarketPrice");
                                                stockPercentChange = stock.getDouble("regularMarketChangePercent");
                                                stockPercentChangeList.add(stockPercentChange);
                                                stockPriceList.add(stockPrice);
                                            } catch (JSONException j) {
                                                stockPrice = 0;
                                                stockPercentChange = 0;
                                                stockPercentChangeList.add(stockPercentChange);
                                                stockPriceList.add(stockPrice);
                                            }
                                        } else if(marketState.equals("Pre-Market")) {
                                            try {
                                                stockPrice = stock.getDouble("preMarketPrice");
                                                stockPercentChange = stock.getDouble("preMarketChangePercent");
                                                stockPercentChangeList.add(stockPercentChange);
                                                stockPriceList.add(stockPrice);
                                            } catch (JSONException j) {
                                                stockPrice = 0;
                                                stockPercentChange = 0;
                                                stockPercentChangeList.add(stockPercentChange);
                                                stockPriceList.add(stockPrice);
                                            }
                                        } else if (marketState.equals("After Hours")) {
                                            try {
                                                stockPrice = stock.getDouble("postMarketPrice");
                                                stockPercentChange = stock.getDouble("postMarketChangePercent");
                                                stockPercentChangeList.add(stockPercentChange);
                                                stockPriceList.add(stockPrice);
                                            } catch (JSONException j) {
                                                stockPrice = 0;
                                                stockPercentChange = 0;
                                                stockPercentChangeList.add(stockPercentChange);
                                                stockPriceList.add(stockPrice);
                                            }
                                        }
                                    }
                                }

                                //Find to biggest percent change
                                int biggestPercentIndex = 0;
                                for (int i = 0; i < stockPercentChangeList.size(); i++) {
                                    double percent = stockPercentChangeList.get(i);
                                    if(Math.abs(percent) > Math.abs(stockPercentChangeList.get(biggestPercentIndex)))
                                        biggestPercentIndex = i;
                                }
                                String ticker = stockList.get(biggestPercentIndex).getTicker();
                                String percentString = unitFormat.format(stockPercentChangeList.get(biggestPercentIndex)) + "%";
                                String priceString = "$" + unitFormat.format(stockPriceList.get(biggestPercentIndex));

                                //Notify user of important changes

                                boolean isAlert = false;
                                //Stock is up
                                if (stockPercentChangeList.get(biggestPercentIndex) >= 10) { //send notification and pulse
                                    String statement = "📈 " + ticker + " is up " + percentString + " to " + priceString;
//                                    Toast.makeText(getApplicationContext(), statement, Toast.LENGTH_SHORT).show();
                                    notifyAlert(statement);
                                    isAlert = true;
                                    startPulsing();
                                } else if (stockPercentChangeList.get(biggestPercentIndex) >= 5) { //send pulse
                                    String statement = "📈 " + ticker + " is up " + percentString + " to " + priceString;
//                                    Toast.makeText(getApplicationContext(), statement, Toast.LENGTH_SHORT).show();
                                    notifyAlert(statement);
                                    isAlert = true;
                                    startPulsing();
                                }
                                //Stock is down
                                if (stockPercentChangeList.get(biggestPercentIndex) <= -10) { //send notification and pulse
                                    String statement = "📉 " + ticker + " is down " + percentString + " to " + priceString;
//                                    Toast.makeText(getApplicationContext(), statement, Toast.LENGTH_SHORT).show();
                                    notifyAlert(statement);
                                    isAlert = true;
                                    startPulsing();
                                } else if (stockPercentChangeList.get(biggestPercentIndex) <= -5) { //send pulse
                                    String statement = "📉 " + ticker + " is down " + percentString + " to " + priceString;
//                                    Toast.makeText(getApplicationContext(), statement, Toast.LENGTH_SHORT).show();
                                    notifyAlert(statement);
                                    isAlert = true;
                                    startPulsing();
                                }

                                if (isAlert) { //update stock store focus
                                    ArrayList<StockStorage> newStockList = retrieveStockStore(getApplicationContext());
                                    for (int i = 0; i < newStockList.size(); i++) {
                                        newStockList.get(i).setFocus(false);
                                    }
                                    newStockList.get(biggestPercentIndex).setFocus(true);
                                    saveStockStore(newStockList);
                                }

                            } catch (JSONException e) { }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                //Add JsonArray request to request queue
                stockRequestQueue.add(stockRequest);

                //No connection
            } else { }

        }
    }


        /**
         * Checks if notifications are enabled
         *
         * @param context
         * @param channelId
         * @return
         */
        public boolean isNotificationsEnabled (Context context, @Nullable String channelId){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!TextUtils.isEmpty(channelId)) {
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    NotificationChannel channel = manager.getNotificationChannel(channelId);
                    return channel.getImportance() != NotificationManager.IMPORTANCE_NONE && NotificationManagerCompat.from(context).areNotificationsEnabled();
                }
                return false;
            } else {
                return NotificationManagerCompat.from(context).areNotificationsEnabled();
            }
        }

    public void notifyAlert(String statement) {

        if (isMyServiceRunning(BubbleService.class)) {

            notificationManagerAlert = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

            //Add notification channel if necessary
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                channelAlert = new NotificationChannel(NOTIFICATION_IDAlert, channelNameAlert, NotificationManager.IMPORTANCE_DEFAULT);
                channelAlert.setDescription(descriptionAlert);
                notificationManagerAlert.createNotificationChannel(channelAlert);
                builderAlert = new Notification.Builder(getApplicationContext(), channelAlert.getId());
            } else {
                builderAlert = new Notification.Builder(getApplicationContext());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManagerAlert.createNotificationChannel(channelAlert);
            }


            Random rand = new Random(); //instance of random class
            int UNIQUE_ID = rand.nextInt(Integer.MAX_VALUE);

            Intent target = new Intent(getApplicationContext(), bubblePopup.class);

            PendingIntent bubbleIntent = PendingIntent.getActivity(getApplicationContext(), UNIQUE_ID, target, PendingIntent.FLAG_UPDATE_CURRENT);

            builderAlert.setSmallIcon(R.drawable.ic_notify)
                    .setContentTitle("Stock Pin Alert")
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.bubble_icon))
                    .setContentText(statement)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setContentIntent(bubbleIntent);

            notificationManagerAlert.cancelAll();
            if (isNotificationsEnabled(getApplicationContext(), NOTIFICATION_IDAlert)) {
                notificationManagerAlert.notify(UNIQUE_ID, builderAlert.build());
            }
        }

    }


}
