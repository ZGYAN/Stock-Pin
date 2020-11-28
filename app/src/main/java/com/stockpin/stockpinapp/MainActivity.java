package com.stockpin.stockpinapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stockpin.bubble.BubbleService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    CustomAutoComplete search;
    ImageView main_img, dayStatus, menuButton;
    ImageButton error_refresh;
    TextView direction_text, marketStatus;
    TextView ticker, company, price, dollar, percent;
    TextView open, open_price, high, high_price, low, low_price;
    TextView vol, vol_unit, avg_vol, avg_vol_unit, market_cap, market_cap_unit;
    TextView recentTicker1, recentStock1, recentTicker2, recentStock2, recentTicker3, recentStock3;
    TextView atClosePrice, atClosePercent, atCloseChange, atCloseIndicator;
    TextView[] textViews = {direction_text, marketStatus, ticker, company, price, dollar, percent, open, open_price, high, high_price, low, low_price, vol, vol_unit, avg_vol, avg_vol_unit, market_cap, market_cap_unit};
    Integer[] ids = {R.id.direction_text, R.id.markets_status, R.id.ticker, R.id.company_name, R.id.price, R.id.dollar_change, R.id.percent_change, R.id.open_title, R.id.open_price, R.id.high_title, R.id.high_price, R.id.low_title, R.id.low_price, R.id.volume_title, R.id.volume_unit, R.id.avg_volume_title, R.id.average_volume_unit, R.id.marketCapTitle, R.id.marketCap_listing};
    CardView card1, card2, card3;
    Button pinButton;
    ConstraintLayout mainLayout;
    ScrollView mainScroll;
    Button enable_btn, disable_btn;
    Dialog permissionDialog;
    FrameLayout adFrame;

    ArrayList<String> stockList;
    ArrayList<StockStorage> stockStore;
    SharedPreferences recentPreferences;
    SharedPreferences settingsPreferences;
    NetworkRequest netRequest;
    Boolean infoBehavior;
    SuggestionAdapter searchAdapter;

    //Auto refresh components
    final int MARKET_HOURS_REFRESH_RATE = 5000;
    final int PREP_HOURS_REFRESH_RATE = 10000;
    final int OUTSIDE_HOURS_REFRESH_RATE = 20000;
    final Handler refreshHandler = new Handler();
    SwipeRefreshLayout refreshLayout;

    String state = "Market Closed";
    private final CharSequence channelName = "Stock Pin Notifications";
    private final String description = "Stock Notification Pin";
    private final String NOTIFICATION_ID = "com.stockpin.notifications";
    private final int importance = NotificationManager.IMPORTANCE_DEFAULT;
    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;

    Notification.Builder builder;
    NotificationManager notificationManager;
    NotificationChannel channel;

    private Vibrator vibrator;

    Toolbar toolbar;
    DrawerLayout sideBar;
    ActionBarDrawerToggle sideBarToggle;
    NavigationView navigationView;

    //Ads
    AdView banner;
    InterstitialAd fullscreenAd;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize elements
        connectViews();

        clearInfo();

        loadRecents();

        mainScroll = findViewById(R.id.mainScroll);
        mainScroll.fullScroll(ScrollView.FOCUS_UP);

        sideBarToggle = new ActionBarDrawerToggle(this, sideBar, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                refreshLayout.setEnabled(false);
                InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(search.getWindowToken(), 0);
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                refreshLayout.setEnabled(true);
                super.onDrawerClosed(drawerView);
            }
        };
        sideBarToggle.setDrawerSlideAnimationEnabled(true);
        sideBar.addDrawerListener(sideBarToggle);
        sideBarToggle.syncState();


        stockList = new ArrayList<>();
        searchAdapter = new SuggestionAdapter(getApplicationContext(), R.layout.search_list_item, stockList);
        search.setAdapter(searchAdapter);

        adFrame = findViewById(R.id.AdFrame);
        banner = new AdView(this);
        banner.setAdUnitId(getString(R.string.testBannerAd));
        adFrame.addView(banner);
        loadBanner();

        fullscreenAd = new InterstitialAd(this);
        fullscreenAd.setAdUnitId(getString(R.string.testFullscreenAd));


        infoBehavior = search.getText().length() == 0;
        netRequest = new NetworkRequest(this);


        //Initialize stock store
        stockStore = retrieveStockStore();

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);

        //Load Ads
        MobileAds.initialize(this);
        banner.loadAd(new AdRequest.Builder().build());
        fullscreenAd.loadAd(new AdRequest.Builder().build());

        fullscreenAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                fullscreenAd.loadAd(new AdRequest.Builder().build());
                super.onAdClosed();
            }
        });

        //Create channel object
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_ID, channelName, importance);
            channel.setDescription(description);
        }


        settingsPreferences = getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        int permissionInt = settingsPreferences.getInt("permission", -1);
        SharedPreferences.Editor editor = settingsPreferences.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionInt == 1 && !Settings.canDrawOverlays(MainActivity.this)) {
            editor.putInt("permission", -1);
            editor.commit();
        }

        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                settingsPreferences = getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
                int permissionInt = settingsPreferences.getInt("permission", -1);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(MainActivity.this) && permissionInt == -1) {
                    showPermissionDialog();

                } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && permissionInt == -1) {
                    showPermissionDialog();
                } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && permissionInt == 1) {
                    setAndShowBubble();
                }

                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(getApplicationContext()) && permissionInt == 1) {
                    //Show bubble
                    //Clear all notifications if any

                    setAndShowBubble();

                    //regular notification
                } else {

                    Random rand = new Random(); //instance of random class
                    int UNIQUE_ID = rand.nextInt(Integer.MAX_VALUE);

                    Intent target = new Intent(getApplicationContext(), popUpActivity.class);

                    String passTick = ticker.getText().toString();
                    target.putExtra("ticker", passTick);
                    target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

                    PendingIntent bubbleIntent = PendingIntent.getActivity(getApplicationContext(), UNIQUE_ID, target, PendingIntent.FLAG_UPDATE_CURRENT);

                    //Add notification channel if necessary
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        builder = new Notification.Builder(getApplicationContext(), channel.getId());
                    } else {
                        builder = new Notification.Builder(getApplicationContext());
                    }

                    builder.setSmallIcon(R.drawable.ic_notify)
                            .setContentTitle("(" + ticker.getText().toString() + ") " + company.getText().toString())
                            .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.bubble_icon))
                            .setContentText("Tap to get quote")
                            .setAutoCancel(false)
                            .setContentIntent(bubbleIntent);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationManager.createNotificationChannel(channel);
                    }


                    if (isNotificationsEnabled(getApplicationContext(), NOTIFICATION_ID)) {
                        notificationManager.notify(UNIQUE_ID, builder.build());
                        vibrator.vibrate(50);
                    } else {
                        Snackbar mySnackbar = Snackbar.make(mainLayout, "Notifications Disabled", Snackbar.LENGTH_LONG);
                        mySnackbar.setAction("App Settings", new SettingsListener());
                        mySnackbar.show();
                    }
                    //Show ad before displaying content periodically
                    if (fullscreenAd.isLoaded()) {
                        fullscreenAd.show();
                    } else {
                        fullscreenAd.loadAd(new AdRequest.Builder().build());
                    }
                }
            }
        });

        //////////UI ELEMENT BEHAVIOUR/////////

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (ticker.getVisibility() == View.VISIBLE) {
                    refreshLayout.setColorSchemeColors(Color.BLUE);
                    stopRepeating();
                    getStock(ticker.getText().toString(), true);
                    refreshLayout.setRefreshing(true);
                } else {
                    String searchString = search.getText().toString();
                    if (!netRequest.isConnected()) {
                        Snackbar.make(mainLayout, "No Connection", BaseTransientBottomBar.LENGTH_LONG).show();
                    } else if (searchString.contains("(") && searchString.contains(")")) {
                        getStock(extractTicker(searchString), false);
                    } else {
                        getStock(searchString, false);
                    }
                    refreshLayout.setRefreshing(true);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settingsMenuButton:
                        sideBar.closeDrawers();
                        Intent finishIntent = new Intent(getApplicationContext(), AppSettings.class);
                        startActivity(finishIntent);
                        break;
                    case R.id.aboutMenuButton:
                        sideBar.closeDrawers();
                        Intent toWatchLater = new Intent(getApplicationContext(), About.class);
                        startActivity(toWatchLater);
                        break;
                    case R.id.shareMenuButton:
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Stock Pin");
                            String shareMessage= "Pin your favorite stocks to your screen with STOCK PIN!!! Download it on Google Play:\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID ;
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "Share App"));
                        } catch(Exception e) {
                            //e.toString();
                        }
                        break;
                }
                return true;
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sideBar.openDrawer(GravityCompat.START);
            }
        });

        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if (search.getText().length() > 0) {
                        String tick = search.getText().toString().toUpperCase();
                        if (ticker.getVisibility() == View.VISIBLE && tick.equals(ticker.getText().toString())) {
                            stopRepeating();
                            getStock(tick, true);
                        } else {
                            if(tick.contains("(") && tick.contains(")")) {
                                if (tick.indexOf("(") < tick.indexOf(")")) {
                                    stopRepeating();
                                    getStock(extractTicker(tick), false);
                                } else {
                                    stopRepeating();
                                    getStock(tick, false);
                                }
                            } else {
                                stopRepeating();
                                getStock(tick, false);
                            }
                        }
                    }
                    search.clearFocus(); //dismiss search bar
                    InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        //List item click event handler
        search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String listVal = (String) parent.getItemAtPosition(position);

                String tick = extractTicker(listVal);

                if (ticker.getVisibility() == View.VISIBLE && tick.equals(ticker.getText().toString())) {
                    stopRepeating();
                    getStock(tick, true);
                } else {
                    stopRepeating();
                    getStock(tick, false);
                }

                search.clearFocus(); //dismiss search bar
                InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(search.getWindowToken(), 0);
            }
        });

        //Listen and responds to search bar changes
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentSymbol = search.getText().toString();
                if(currentSymbol.length() != 0) getStockList(currentSymbol);

                Drawable x = getResources().getDrawable(R.drawable.clear_x);
                Drawable info = getResources().getDrawable(R.drawable.ic_settings);
                Drawable search_btn = getResources().getDrawable(R.drawable.search_img);
                if (s.length() > 0) {
                    infoBehavior = false;
                    search.setCompoundDrawablesWithIntrinsicBounds(search_btn, null, x, null);
                } else {
                    infoBehavior = true;
                    search.setCompoundDrawablesWithIntrinsicBounds(search_btn, null, info, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //Listener for search bar element touches
        search.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (event.getRawX() >= (search.getRight() - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        if (infoBehavior) {
                            search.clearFocus();
                            InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            in.hideSoftInputFromWindow(search.getWindowToken(), 0);
                            startActivity(new Intent(getApplicationContext(), AppSettings.class));

                        } else {
                            search.setText(""); //clear button
                        }
                        return true;
                    }
                }
                return false;
            }
        });


        //Card view (quick access clicks)
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!netRequest.isConnected()) {
                    Snackbar.make(mainLayout, "No Connection", BaseTransientBottomBar.LENGTH_LONG).show();
                } else {
                    String tick = extractTicker(recentTicker1.getText().toString());
                    if (ticker.getVisibility() == View.VISIBLE && tick.equals(ticker.getText().toString())) {
                        stopRepeating();
                        getStock(tick, true);
                    } else {
                        stopRepeating();
                        getStock(tick, false);
                    }
                    search.setText("");
                }

            }
        });
        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!netRequest.isConnected()) {
                    Snackbar.make(mainLayout, "No Connection", BaseTransientBottomBar.LENGTH_LONG).show();
                } else {
                    String tick = extractTicker(recentTicker2.getText().toString());
                    if (ticker.getVisibility() == View.VISIBLE && tick.equals(ticker.getText().toString())) {
                        stopRepeating();
                        getStock(tick, true);
                    } else {
                        stopRepeating();
                        getStock(tick, false);
                    }
                    search.setText("");
                }
            }
        });
        card3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!netRequest.isConnected()) {
                    Snackbar.make(mainLayout, "No Connection", BaseTransientBottomBar.LENGTH_LONG).show();
                } else {
                    String tick = extractTicker(recentTicker3.getText().toString());

                    if (ticker.getVisibility() == View.VISIBLE && tick.equals(ticker.getText().toString())) {
                        stopRepeating();
                        getStock(tick, true);
                    } else {
                        stopRepeating();
                        getStock(tick, false);
                    }
                    search.setText("");
                }

            }
        });


        error_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchString = search.getText().toString();
                if (!netRequest.isConnected()) {
                    Snackbar.make(mainLayout, "No Connection", BaseTransientBottomBar.LENGTH_LONG).show();
                } else if (searchString.contains("(") && searchString.contains(")")) {
                    getStock(extractTicker(searchString), false);
                } else {
                    getStock(searchString, false);
                }
            }
        });
    }


    /**
     * Retrieves stock data and displays results on screen
     *
     * @param symbol
     */
    public void getStock(final String symbol, final boolean isRefresh) {

        if(isRefresh) {
            showInfo();
        } else {
            stopRepeating();
            clearInfo();
        }
        //Check connection status before making any stock request
        if (netRequest.isConnected()) {
            main_img.setImageResource(R.drawable.icon_trans);
            direction_text.setText(R.string.loading);
            //Initialize new RequestQueue instance
            RequestQueue stockRequestQueue = Volley.newRequestQueue(this);
            //JsonArray Request instance
            JsonObjectRequest stockRequest = new JsonObjectRequest(Request.Method.GET, netRequest.getQuote(symbol), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    //Check for API error
                    boolean apiError = false;
                    try {
                        JSONArray stockStatus = response.getJSONObject("quoteResponse").getJSONArray("result");
                        if(stockStatus.length() == 0) { //no results returned
                            apiError = true;
                        }
                    } catch (Exception e) {
                        apiError = false;
                        e.printStackTrace();
                    }

                    //extract quote information
                    if(!apiError) {
                        try {
                            JSONObject stock = response.getJSONObject("quoteResponse").getJSONArray("result").getJSONObject(0);

                            String priceString = "";
                            String dollarString = "";
                            String percentString = "";
                            String openString = "";
                            String highString = "";
                            String lowString = "";
                            String volumeString = "";
                            String avgVolString = "";
                            String marketCapString = "";
                            String marketState = "";

                            double stockPrice, stockDollarChange, stockPercentChange;
                            double closeStockPrice = -1, closeStockDollarChange = -1, closeStockPercentChange = -1;
                            double stockOpen = -1, stockHigh = -1, stockLow = -1;
                            long stockVolume = -1, stockAvgVolume = -1, stockMarketCap = -1;
                            String stockSymbol, stockName;

                            //Required values
                            stockPrice = stock.getDouble("regularMarketPrice");
                            stockDollarChange = stock.getDouble("regularMarketChange");
                            stockPercentChange = stock.getDouble("regularMarketChangePercent");
                            stockSymbol = stock.getString("symbol");

                            try { stockName = stock.getString("longName"); } catch (JSONException e) { stockName = stock.getString("shortName"); }
                            try { stockOpen = stock.getDouble("regularMarketOpen"); } catch (Exception e) {openString = "--"; }
                            try { stockHigh = stock.getDouble("regularMarketDayHigh");} catch (Exception e) {highString = "--"; }
                            try { stockLow = stock.getDouble("regularMarketDayLow"); } catch (Exception e) {lowString = "--"; }
                            try { stockVolume = stock.getLong("regularMarketVolume"); } catch (Exception e) {volumeString = "--"; }
                            try { stockAvgVolume = stock.getLong("averageDailyVolume3Month"); } catch (Exception e) {avgVolString = "--"; }
                            try { stockMarketCap = stock.getLong("marketCap"); } catch (Exception e) {marketCapString = "--"; }
                            try { marketState = stock.getString("marketState"); } catch (Exception e) { marketState = ""; }


                            if(marketState.contains("CLOSED") || marketState.equals("PREPRE") || marketState.equals("POSTPOST")) {
                                marketState = "Market Closed";
                            } else if(marketState.contains("REGULAR")) {
                                marketState = "Market Open";
                            } else if(marketState.equals("PRE")) {
                                marketState = "Pre-Market";
                            } else if(marketState.equals("POST")) {
                                marketState = "After Hours";
                            } else {
                                marketState = "";
                            }

                            //Set pre or post market data
                            if (marketState.equals("After Hours")) {
                                try {
                                    closeStockPrice = stockPrice;
                                    closeStockDollarChange = stockDollarChange;
                                    closeStockPercentChange = stockPercentChange;
                                    stockPrice = stock.getDouble("postMarketPrice");
                                    stockDollarChange = stock.getDouble("postMarketChange");
                                    stockPercentChange = stock.getDouble("postMarketChangePercent");
                                } catch (JSONException e) { }
                            } else if (marketState.equals("Pre-Market")) {
                                try {
                                    closeStockPrice = stockPrice;
                                    closeStockDollarChange = stockDollarChange;
                                    closeStockPercentChange = stockPercentChange;
                                    stockPrice = stock.getDouble("preMarketPrice");
                                    stockDollarChange = stock.getDouble("preMarketChange");
                                    stockPercentChange = stock.getDouble("preMarketChangePercent");
                                } catch (JSONException j) { }
                            }


                            DecimalFormat unitFormat;

                            if (stockPrice < 1) {
                                unitFormat = new DecimalFormat("###,###,###,###,###,###.###");
                                unitFormat.setDecimalSeparatorAlwaysShown(true);
                                unitFormat.setMinimumFractionDigits(3);
                            } else {
                                unitFormat = new DecimalFormat("###,###,###,###,###,###.##");
                                unitFormat.setDecimalSeparatorAlwaysShown(true);
                                unitFormat.setMinimumFractionDigits(2);
                            }
                            if (stockPercentChange > 0) { //Specify change
                                percentString = "(" + unitFormat.format(stockPercentChange) + "%)";
                                dollarString = "+" + unitFormat.format(stockDollarChange);
                            } else {
                                double stockPercentChangeVal = Math.abs(stockPercentChange);
                                percentString = "(" + unitFormat.format(stockPercentChangeVal) + "%)";
                                dollarString = unitFormat.format(stockDollarChange);
                            }
                            //At close check
                            if(marketState.equals("After Hours") || marketState.equals("Pre-Market")) {
                                if (closeStockPercentChange >= 0) { //Specify change
                                    String closePercent = "(" + unitFormat.format(closeStockPercentChange) + "%)";
                                    String closeDollarString = "+" + unitFormat.format(closeStockDollarChange);
                                    atCloseChange.setTextColor(getResources().getColor(R.color.green));
                                    atClosePercent.setTextColor(getResources().getColor(R.color.green));
                                    atClosePercent.setText(closePercent);
                                    atCloseChange.setText(closeDollarString);
                                } else {
                                    double stockPercentChangeVal = Math.abs(closeStockPercentChange);
                                    String closePercent = "(" + unitFormat.format(stockPercentChangeVal) + "%)";
                                    String closeDollarString  = unitFormat.format(closeStockDollarChange);
                                    atCloseChange.setTextColor(getResources().getColor(R.color.red));
                                    atClosePercent.setTextColor(getResources().getColor(R.color.red));
                                    atClosePercent.setText(closePercent);
                                    atCloseChange.setText(closeDollarString);
                                }
                                String closePriceString = "$" + unitFormat.format(closeStockPrice);
                                atClosePrice.setText(closePriceString);
                                if(symbol.startsWith("^")) { //for index and ETFs
                                    atCloseIndicator.setVisibility(View.INVISIBLE);
                                    atClosePercent.setVisibility(View.INVISIBLE);
                                    atCloseChange.setVisibility(View.INVISIBLE);
                                    atClosePrice.setVisibility(View.INVISIBLE);
                                } else {
                                    atCloseIndicator.setVisibility(View.VISIBLE);
                                    atClosePercent.setVisibility(View.VISIBLE);
                                    atCloseChange.setVisibility(View.VISIBLE);
                                    atClosePrice.setVisibility(View.VISIBLE);
                                }
                            }

                            if(marketState.equals("Market Open")) {
                                atCloseIndicator.setVisibility(View.INVISIBLE);
                                atClosePercent.setVisibility(View.INVISIBLE);
                                atCloseChange.setVisibility(View.INVISIBLE);
                                atClosePrice.setVisibility(View.INVISIBLE);
                            }

                            priceString = "$" + unitFormat.format(stockPrice);
                            if(openString.length() == 0) openString = "$" + unitFormat.format(stockOpen);
                            if(highString.length() == 0) highString = "$" + unitFormat.format(stockHigh);
                            if(lowString.length() == 0) lowString = "$" + unitFormat.format(stockLow);

                            unitFormat.setMinimumFractionDigits(0);
                            unitFormat.setDecimalSeparatorAlwaysShown(false);

                            if(volumeString.length() == 0) volumeString = simpleVolume(unitFormat.format(stockVolume));
                            if(avgVolString.length() == 0) avgVolString = simpleVolume(unitFormat.format(stockAvgVolume));
                            if(marketCapString.length() == 0) marketCapString = "$"+simpleMarketCap(unitFormat.format(stockMarketCap));

                            if (stockPercentChange < 0) { //Set color for change
                                dollar.setTextColor(getResources().getColor(R.color.red));
                                percent.setTextColor(getResources().getColor(R.color.red));
                            } else {
                                dollar.setTextColor(getResources().getColor(R.color.green));
                                percent.setTextColor(getResources().getColor(R.color.green));
                            }

                            //Blink change
                            if(isRefresh && !price.getText().toString().equals(priceString)) {
                                String cp = price.getText().toString().substring(1).replace(",", "");
                                String np = priceString.substring(1).replace(",", "");
                                double current = Double.parseDouble(cp);
                                double newPrice = Double.parseDouble(np);
                                blickChange(newPrice - current);
                            }

                            //Display and update stock information
                            ticker.setText(stockSymbol);
                            company.setText(stockName);
                            price.setText(priceString);
                            dollar.setText(dollarString);
                            percent.setText(percentString);
                            open_price.setText(openString);
                            high_price.setText(highString);
                            low_price.setText(lowString);
                            vol_unit.setText(volumeString);
                            avg_vol_unit.setText(avgVolString);
                            market_cap_unit.setText(marketCapString);

                            refreshLayout.setRefreshing(false);
                            updateRecents(stockSymbol, stockName);

                            dayStatus.setVisibility(View.VISIBLE);
                            if(marketState.equals("Market Closed")) {
                                dayStatus.setImageResource(R.drawable.ic_sleep);
                            } else if(marketState.equals("Pre-Market")) {
                                dayStatus.setImageResource(R.drawable.ic_rise);
                            } else if(marketState.equals("After Hours")) {
                                dayStatus.setImageResource(R.drawable.ic_night);
                            } else if (marketState.equals("Market Open")) {
                                dayStatus.setImageResource(R.drawable.ic_day);
                            } else {
                                dayStatus.setVisibility(View.INVISIBLE);
                            }

                            showInfo();

                            stopRepeating();

                            startRepearting();

                            marketStatus.setText(marketState);
                            marketStatus.setVisibility(View.VISIBLE);

                            state = marketState;

                            direction_text.setText(R.string.search_for_symbols_or_companies);

                        } catch (JSONException e) {
                            stopRepeating();
                            clearInfo();
                            direction_text.setText("Error requesting '" + symbol + "' ticker symbol. Please try again");
                            main_img.setImageResource(R.drawable.warning_img_foreground);
                        }
                    } else {
                        stopRepeating();
                        clearInfo();
                        direction_text.setText("Error requesting '" + symbol + "' ticker symbol. Please try again");
                        main_img.setImageResource(R.drawable.warning_img_foreground);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    stopRepeating();
                    clearInfo();
                    main_img.setImageResource(R.drawable.warning_img_foreground);
                    direction_text.setText("Network Error Occurred. Try Again Later");
                    if (search.getText().toString().length() > 0){
                        error_refresh.setVisibility(View.VISIBLE);
                        refreshLayout.setEnabled(true);
                    }
                    if (!netRequest.isConnected()) {
                        Snackbar.make(mainLayout, "No Connection", BaseTransientBottomBar.LENGTH_LONG).show();
                    }
                }
            });
            //Add JsonArray request to request queue
            stockRequestQueue.add(stockRequest);

            //No connection
        } else {
            clearInfo();
            main_img.setImageResource(R.drawable.warning_img_foreground);
            direction_text.setText("Network Error Occurred");
            if (search.getText().toString().length() > 0){
                error_refresh.setVisibility(View.VISIBLE);
                refreshLayout.setEnabled(true);
            }
            if (!netRequest.isConnected()) {
                Snackbar.make(mainLayout, "No Connection", BaseTransientBottomBar.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Blink price on change
     * @param change
     */
    public void blickChange(double change) {
        ObjectAnimator animator;
        if(change > 0) {
            animator = ObjectAnimator.ofInt(price, "backgroundColor", getResources().getColor(R.color.transparent), getResources().getColor(R.color.green), getResources().getColor(R.color.transparent));
        } else if (change < 0) {
            animator = ObjectAnimator.ofInt(price, "backgroundColor", getResources().getColor(R.color.transparent), getResources().getColor(R.color.red), getResources().getColor(R.color.transparent));
        } else {
            animator = ObjectAnimator.ofInt(price, "backgroundColor", getResources().getColor(R.color.transparent), getResources().getColor(R.color.transparent), getResources().getColor(R.color.transparent));
        }
        animator.setDuration(800);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setRepeatCount(0);
        animator.start();
    }


    /**
     * Start auto refresh
     */
    public void startRepearting() {
        if(state.equals("Market Closed")) {
            stopRepeating();
        } else {
            if(state.equals("Market Open")) {
                refreshHandler.postDelayed(mRefreshRunnable, MARKET_HOURS_REFRESH_RATE);
            } else if(state.equals("After Hours") || state.equals("Pre-Market")) {
                refreshHandler.postDelayed(mRefreshRunnable, PREP_HOURS_REFRESH_RATE);
            } else {
                refreshHandler.postDelayed(mRefreshRunnable, OUTSIDE_HOURS_REFRESH_RATE);
            }
        }
    }

    //Stop auto refresh thread
    public void stopRepeating() {
        refreshHandler.removeCallbacks(mRefreshRunnable);
    }

    //Refresh after a period
    private Runnable mRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            getStock(ticker.getText().toString(), true);
        }
    };



    /**
     * Makes Network request for list of stocks
     */
    public void getStockList(String symbol) {
        //Initialize new RequestQueue instance
        RequestQueue stocklistRequestQueue = Volley.newRequestQueue(this);
        //JsonArray Request instance
        JsonObjectRequest stocklistRequest = new JsonObjectRequest(Request.Method.GET, netRequest.getSymbolSearchLink(symbol), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    stockList.clear();
                    //Loop through json array and extract stock identifiers
                    JSONArray stockArray = response.getJSONObject("ResultSet").getJSONArray("Result");
                    for (int i = 0; i < stockArray.length(); i++) {
                        JSONObject stock = stockArray.getJSONObject(i);
                        String ticker = stock.getString("symbol");
                        String name = stock.getString("name");
                        String fullname = name + " (" + ticker + ")";
                        if(stockList.size() < 6 && !ticker.matches(".*\\d.*")) stockList.add(fullname);
                    }
                    searchAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        //Add JsonArray request to request queue
        stocklistRequestQueue.add(stocklistRequest);
    }

    /**
     * Updates recent list in preferences by inserting and shifting values after successful search
     *
     * @param ticker
     * @param companyName
     */
    public void updateRecents(String ticker, String companyName) {
        recentPreferences = MainActivity.this.getSharedPreferences("com.stockpin.Recents", Context.MODE_PRIVATE);
        String recentString = recentPreferences.getString("recentList", "(GOOGL) Alphabet Inc//(AAPL) Apple Inc//(MSFT) Microsoft Corp");
        String[] recents = recentPreferences.getString("recentList", "(GOOGL) Alphabet Inc//(AAPL) Apple Inc//(MSFT) Microsoft Corp").split("//");

        //Check if value is already in quick access
        if (!recentString.contains(ticker)) {
            recents[2] = recents[1];
            recents[1] = recents[0];
            recents[0] = "(" + ticker + ") " + companyName;

            //After inserting, update elements
            String tick = extractTicker(recents[0]);
            String name = recents[0].substring(recents[0].indexOf(")") + 2);
            recentTicker1.setText("(" + tick + ")");
            recentStock1.setText(name);

            tick = extractTicker(recents[1]);
            name = recents[1].substring(recents[1].indexOf(")") + 2);
            recentTicker2.setText("(" + tick + ")");
            recentStock2.setText(name);

            tick = extractTicker(recents[2]);
            name = recents[2].substring(recents[2].indexOf(")") + 2);
            recentTicker3.setText("(" + tick + ")");
            recentStock3.setText(name);

            //Save new recent list into preferences
            String newRecents = recents[0] + "//" + recents[1] + "//" + recents[2];

            SharedPreferences.Editor editor = recentPreferences.edit();
            editor.putString("recentList", newRecents);
            editor.commit();
        }

    }

    /**
     * Loads recent searches into card views
     */
    public void loadRecents() {
        recentPreferences = this.getSharedPreferences("com.stockpin.Recents", Context.MODE_PRIVATE);
        String[] recents = recentPreferences.getString("recentList", "(GOOGL) Alphabet Inc//(AAPL) Apple Inc//(MSFT) Microsoft Corp").split("//");

        String tick = extractTicker(recents[0]);
        String name = recents[0].substring(recents[0].indexOf(")") + 2);
        recentTicker1.setText("(" + tick + ")");
        recentStock1.setText(name);

        tick = extractTicker(recents[1]);
        name = recents[1].substring(recents[1].indexOf(")") + 2);
        recentTicker2.setText("(" + tick + ")");
        recentStock2.setText(name);

        tick = extractTicker(recents[2]);
        name = recents[2].substring(recents[2].indexOf(")") + 2);
        recentTicker3.setText("(" + tick + ")");
        recentStock3.setText(name);

    }

    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest = new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        banner.setAdSize(adSize);


        // Step 5 - Start loading the ad in the background.
        banner.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    /**
     * Clears screen of stock information
     */
    public void clearInfo() {
        for (TextView view : textViews) {
            if (view!= marketStatus && view != direction_text) {
                view.setVisibility(View.INVISIBLE);
            }
        }
        main_img.setVisibility(View.VISIBLE);
        direction_text.setVisibility(View.VISIBLE);
        pinButton.setVisibility(View.INVISIBLE);
        error_refresh.setVisibility(View.INVISIBLE);

        atCloseIndicator.setVisibility(View.INVISIBLE);
        atClosePercent.setVisibility(View.INVISIBLE);
        atCloseChange.setVisibility(View.INVISIBLE);
        atClosePrice.setVisibility(View.INVISIBLE);
        dayStatus.setVisibility(View.INVISIBLE);

        refreshLayout.setEnabled(false);
    }

    /**
     * Makes stock information visible
     */
    public void showInfo() {
        refreshLayout.setEnabled(true);
        main_img.setVisibility(View.INVISIBLE);
        direction_text.setVisibility(View.INVISIBLE);
        error_refresh.setVisibility(View.INVISIBLE);
        pinButton.setVisibility(View.VISIBLE);
        for (TextView view : textViews) {
            if (view != marketStatus && view != direction_text) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Initialize all views in activity
     */
    public void connectViews() {

        //Initialize Text views
        for (int i = 0; i < textViews.length; i++)
            textViews[i] = findViewById(ids[i]);

        refreshLayout = findViewById(R.id.refreshMainLayout);
        marketStatus = findViewById(R.id.markets_status);
        ticker = findViewById(R.id.ticker);
        company = findViewById(R.id.company_name);
        price = findViewById(R.id.price);
        dollar = findViewById(R.id.dollar_change);
        percent = findViewById(R.id.percent_change);
        open = findViewById(R.id.open_title);
        open_price = findViewById(R.id.open_price);
        high = findViewById(R.id.high_title);
        high_price = findViewById(R.id.high_price);
        low = findViewById(R.id.low_title);
        low_price = findViewById(R.id.low_price);
        vol = findViewById(R.id.volume_title);
        vol_unit = findViewById(R.id.volume_unit);
        avg_vol = findViewById(R.id.avg_volume_title);
        avg_vol_unit = findViewById(R.id.average_volume_unit);
        market_cap = findViewById(R.id.marketCapTitle);
        market_cap_unit = findViewById(R.id.marketCap_listing);

        atClosePrice = findViewById(R.id.atClosePrice);
        atCloseChange = findViewById(R.id.atCloseDollar_change);
        atClosePercent = findViewById(R.id.atClosePercent_change);
        atCloseIndicator = findViewById(R.id.atCloseIndicator);
        dayStatus = findViewById(R.id.dayImage);

        search = findViewById(R.id.company_search);
        main_img = findViewById(R.id.main_img);
        direction_text = findViewById(R.id.direction_text);
        pinButton = findViewById(R.id.pin_btn);
        error_refresh = findViewById(R.id.error_refresh);

        card1 = findViewById(R.id.recent1);
        card2 = findViewById(R.id.recent2);
        card3 = findViewById(R.id.recent3);
        recentTicker1 = findViewById(R.id.recentTicker1);
        recentTicker2 = findViewById(R.id.recentTicker2);
        recentTicker3 = findViewById(R.id.recentTicker3);
        recentStock1 = findViewById(R.id.recentStock1);
        recentStock2 = findViewById(R.id.recentStock2);
        recentStock3 = findViewById(R.id.recentStock3);
        mainLayout = findViewById(R.id.main_layout);
        sideBar = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menuButton = findViewById(R.id.menuButton);


    }

    /**
     * Extracts ticker symbol after autocomplete
     *
     * @param ticker
     * @return String
     */
    public String extractTicker(String ticker) {
        try {
            int start = ticker.lastIndexOf("(") + 1;
            int end = ticker.lastIndexOf(")");
            return ticker.substring(start, end);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }

    }

    /**
     * return a simplified volume string to reduce clutter
     * @param vol
     * @return
     */
    public String simpleVolume(String vol) {
        String[] sections = vol.split(",");
        switch (sections.length) {
            case 3:
                if(Integer.parseInt(sections[1].substring(0,1)) >= 5 && Integer.parseInt(sections[1]) < 999) {
                    return (Integer.parseInt(sections[0]) + 1) + "M"; //round up
                } else {
                    return sections[0]+"M"; //millions
                }
            case 4:
                if(Integer.parseInt(sections[1].substring(0,1)) >= 5 && Integer.parseInt(sections[1]) < 999) {
                    return (Integer.parseInt(sections[0]) + 1) + "B"; //round up
                } else {
                    return sections[0]+"B"; //billions
                }
            case 5:
                if(Integer.parseInt(sections[1].substring(0,1)) >= 5 && Integer.parseInt(sections[1]) < 999) {
                    return (Integer.parseInt(sections[0]) + 1) + "T"; //round up
                } else {
                    return sections[0]+"T"; //trillions
                }
            default: return vol;
        }
    }

    /**
     * return a simplified market cap string to reduce clutter
     * @param cap
     * @return
     */
    static public String simpleMarketCap(String cap) {
        String[] sections = cap.split(",");

        DecimalFormat unitFormat = new DecimalFormat("###.##");
        unitFormat.setDecimalSeparatorAlwaysShown(false);
        unitFormat.setMinimumFractionDigits(0);
        String section = "";
        switch (sections.length) {
            case 3:
                section = sections[0] + "." + sections[1];
                return unitFormat.format(Float.parseFloat(section)) + "M";
            case 4:
                section = sections[0] + "." + sections[1];
                return unitFormat.format(Float.parseFloat(section)) + "B";
            case 5:
                section = sections[0] + "." + sections[1];
                return unitFormat.format(Float.parseFloat(section)) + "T";
            case 6:
                section = sections[0] + "." + sections[1];
                return unitFormat.format(Float.parseFloat(section)) + "q";
            default: return cap;
        }
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
                return channel.getImportance() != NotificationManager.IMPORTANCE_NONE && NotificationManagerCompat.from(context).areNotificationsEnabled();
            }
            return false;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }


    /**
     * Check if ticker is in pinned stock list
     * @param stocks
     * @param tick
     * @return
     */
    public boolean isStockStoreReady(ArrayList<StockStorage> stocks, String tick) {
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getTicker().equals(tick)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Save pinned stocks
     * @param stockStore
     */
    public void saveStockStore(ArrayList<StockStorage> stockStore) {
        SharedPreferences stockPreferences = getApplicationContext().getSharedPreferences("com.stockpin.stocks", MODE_PRIVATE);
        SharedPreferences.Editor editor = stockPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(stockStore);
        editor.putString("stockData", json);
        editor.commit();
    }

    public ArrayList<StockStorage> retrieveStockStore() {
        ArrayList<StockStorage> stockStore;
        SharedPreferences stockPreferences = getApplicationContext().getSharedPreferences("com.stockpin.stocks", getApplicationContext().MODE_PRIVATE);
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


    public void showPermissionDialog() {
        settingsPreferences = this.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        permissionDialog = new Dialog(this);
        permissionDialog.setContentView(R.layout.confirmation_dialog);
        enable_btn = permissionDialog.findViewById(R.id.enable_permission);
        disable_btn = permissionDialog.findViewById(R.id.disable_permission);
        permissionDialog.setCancelable(false);
        permissionDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        disable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settingsPreferences.edit();
                editor.putInt("permission", 0);
                editor.commit();
                permissionDialog.dismiss();
            }
        });
        enable_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                } else {
                    SharedPreferences.Editor editor = settingsPreferences.edit();
                    editor.putInt("permission", 1);
                    editor.commit();
                }
                permissionDialog.dismiss();
            }
        });
        permissionDialog.show();

    }


    private void removeBubble() {
        if (isMyServiceRunning(BubbleService.class) && BubbleService.bubbleViewManager != null) {
            stopService(new Intent(getApplicationContext(), BubbleService.class));
        }
    }

    private void addNewBubble() {
        if (!isMyServiceRunning(BubbleService.class)) {
            // launch service
            final Intent blowBubble = new Intent(getApplicationContext(), BubbleService.class);
            try {
                ContextCompat.startForegroundService(this, blowBubble);
                vibrator.vibrate(30);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Unable to create bubble", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void setAndShowBubble() {
        //Show bubble
        //Clear all notifications if any
        notificationManager.cancelAll();

        if (isStockStoreReady(stockStore, ticker.getText().toString())) {
            StockStorage newStock = new StockStorage(ticker.getText().toString(), false);
            stockStore.add(0, newStock);
            for (int i = 0; i < stockStore.size(); i++)
                stockStore.get(i).setFocus(false);
            stockStore.get(0).setFocus(true);
            saveStockStore(stockStore);
            Toast.makeText(getApplicationContext(), ticker.getText().toString() + " added", Toast.LENGTH_SHORT).show();
            vibrator.vibrate(50);

        } else {
            int refocusPos = 0;
            StockStorage reStock;
            for (int i = 0; i < stockStore.size(); i++) {
                if(stockStore.get(i).getTicker().equals(ticker.getText().toString())) refocusPos = i;
                stockStore.get(i).setFocus(false);
            }
            reStock = stockStore.get(refocusPos);
            stockStore.remove(refocusPos);
            stockStore.add(0, reStock);
            stockStore.get(0).setFocus(true);
            saveStockStore(stockStore);
            Toast.makeText(getApplicationContext(), ticker.getText().toString() + " already added", Toast.LENGTH_SHORT).show();
            vibrator.vibrate(30);
        }

        //Show ad before displaying content periodically
        if (fullscreenAd.isLoaded()) {
            fullscreenAd.show();
        } else {
            fullscreenAd.loadAd(new AdRequest.Builder().build());
        }

        addNewBubble();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                // You don't have permission
                settingsPreferences = this.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settingsPreferences.edit();
                editor.putInt("permission", 0);
                editor.commit();
                //Toast.makeText(getApplicationContext(), "YOU DON'T GOT THE PERMISSION", Toast.LENGTH_SHORT).show();
            } else {
                // gained the permission
                settingsPreferences = this.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settingsPreferences.edit();
                editor.putInt("permission", 1);
                editor.commit();

            }
        }
    }


    public class SettingsListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");

            //for Android 5-7
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);

            // for Android 8 and above
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());

            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        if (ticker.getVisibility() == View.VISIBLE) {
            showInfo();
        } else {
            clearInfo();
        }

        settingsPreferences = getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        int permissionInt = settingsPreferences.getInt("permission", -1);
        SharedPreferences.Editor editor = settingsPreferences.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionInt == 1 && !Settings.canDrawOverlays(MainActivity.this)) {
            editor.putInt("permission", -1);
            editor.commit();
        }

        stockStore = retrieveStockStore();
        if(ticker.getVisibility() == View.VISIBLE) {
            getStock(ticker.getText().toString(), true);
        }

        super.onResume();
    }



    @Override
    protected void onPause() {
        stopRepeating();
        super.onPause();
    }

    @Override
    protected void onStop() {
        stopRepeating();
        super.onStop();
    }
}
