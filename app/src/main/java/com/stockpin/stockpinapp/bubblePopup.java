package com.stockpin.stockpinapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.stockpin.bubble.BubbleService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;


public class bubblePopup extends AppCompatActivity implements RecyclerViewAdapter.OnItemClicked {

    TextView ticker, company, price, dollar, percent, direction, marketStatus;
    TextView open, open_price, high, high_price, low, low_price;
    TextView vol, vol_unit, avg_vol, avg_vol_unit, market_cap, market_cap_unit;
    TextView atClosePrice, atClosePercent, atCloseChange, atCloseIndicator;
    ImageButton refresh_btn, error_refresh, app_open;
    Button remove;
    ImageView main_img, dayStatus;
    NetworkRequest networkRequest;
    RelativeLayout bubbleLayout;
    ScrollView bubbleScroll;
    RecyclerView list;
    CardView bubbleCard;
    SharedPreferences settings;

    private Vibrator vibrator;

    ArrayList<StockStorage> stocks;
    RecyclerViewAdapter adapter;

    //Auto refresh components
    final int MARKET_HOURS_REFRESH_RATE = 5000;
    final int PREP_HOURS_REFRESH_RATE = 10000;
    final int OUTSIDE_HOURS_REFRESH_RATE = 15000;
    String state = "Market Closed";
    final Handler refreshHandler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_popup);

        ticker = findViewById(R.id.bpticker);
        company = findViewById(R.id.bpcompany_name);
        price = findViewById(R.id.bpprice);
        dollar = findViewById(R.id.bpdollar_change);
        percent = findViewById(R.id.bppercent_change);
        open = findViewById(R.id.bpopen_title);
        open_price = findViewById(R.id.bpopen_price);
        high = findViewById(R.id.bphigh_title);
        high_price = findViewById(R.id.bphigh_price);
        low = findViewById(R.id.bplow_title);
        low_price = findViewById(R.id.bplow_price);
        vol = findViewById(R.id.bpvolume_title);
        vol_unit = findViewById(R.id.bpvolume_unit);
        avg_vol = findViewById(R.id.bpavg_volume_title);
        avg_vol_unit = findViewById(R.id.bpaverage_volume_unit);
        market_cap = findViewById(R.id.bpmarketCap_title);
        market_cap_unit = findViewById(R.id.bpmarketCap_listing);
        error_refresh = findViewById(R.id.bperror_refresh);
        refresh_btn = findViewById(R.id.bprefresh_btn);
        direction = findViewById(R.id.bpdirection);
        main_img = findViewById(R.id.bpmain_img);
        marketStatus = findViewById(R.id.bpmarketStatus);
        bubbleLayout = findViewById(R.id.bubblepopUpLayout);
        app_open = findViewById(R.id.bpopen_app);
        remove = findViewById(R.id.remove_btn);
        bubbleScroll = findViewById(R.id.bubbleScroll);
        atClosePrice = findViewById(R.id.closeBpprice);
        atCloseChange = findViewById(R.id.closeBpdollar_change);
        atClosePercent = findViewById(R.id.closeBppercent_change);
        atCloseIndicator = findViewById(R.id.closeBpIndicator);
        dayStatus = findViewById(R.id.bubbleDayStatusIcon);
        bubbleCard = findViewById(R.id.bppopupCard);


        bubbleScroll.fullScroll(ScrollView.FOCUS_UP);

        loadBubblePreferences();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        list = findViewById(R.id.recycleview);
        list.setLayoutManager(linearLayoutManager);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(list);

        networkRequest = new NetworkRequest(getApplicationContext());

        int stockFocus = 0;
        stocks = retrieveStockStore();
        if (stocks == null || stocks.size() == 0) {
            finish(); //error happened
        } else {
            for (int i = 0; i < stocks.size(); i++) {
                if (stocks.get(i).isFocus()) {
                    stockFocus = i;
                    break;
                }
            }
            stopRepeating();
            getStock(stocks.get(stockFocus).getTicker(), false);
            saveStockStore(stocks);
        }

        adapter = new RecyclerViewAdapter(stocks, getApplicationContext(), this);
        list.setAdapter(adapter);

        list.scrollToPosition(stockFocus);

        refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ticker.getVisibility() == View.VISIBLE) {
                    stopRepeating();
                    getStock(ticker.getText().toString(), true);
                    vibrator.vibrate(30);
                }
            }
        });
        error_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (networkRequest.isConnected()) {
                    int refreshPos = 0;
                    for (int i = 0; i < stocks.size(); i++)
                        if (stocks.get(i).isFocus)
                            refreshPos = i;
                    getStock(stocks.get(refreshPos).getTicker(), false); //repeat network request of current stock
                    vibrator.vibrate(30);
                } else {
                    Toast.makeText(getApplicationContext(), "No Connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        app_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent open = new Intent(getApplicationContext(), MainActivity.class);
                open.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivityIfNeeded(open, 0);
            }
        });

        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < stocks.size(); i++) {
                    if (stocks.get(i).isFocus) {

                        if (stocks.size() != 1) {

                            if (i >= 1) {
                                for (int a = 0; a < stocks.size(); a++)
                                    stocks.get(a).setFocus(false);
                                stocks.get(i - 1).setFocus(true); //set previous to focus
                                String selectedTicker = stocks.get(i - 1).getTicker();
                                stopRepeating();
                                getStock(selectedTicker, false);
                            } else {
                                for (int a = 0; a < stocks.size(); a++)
                                    stocks.get(a).setFocus(false);
                                stocks.get(i + 1).setFocus(true); //set previous to focus
                                String selectedTicker = stocks.get(i + 1).getTicker();
                                stopRepeating();
                                getStock(selectedTicker, false);

                            }
                            stocks.remove(i);
                            saveStockStore(stocks);
                            adapter.notifyDataSetChanged();
                        } else {
                            stocks.remove(i);
                            saveStockStore(new ArrayList<StockStorage>(0));
                            adapter.notifyDataSetChanged();
                        }

                        if (stocks.size() == 0) {
                            removeBubble();
                            finish(); //all stocks are gone
                        }
                    }
                }
            }
        });

        //Clear list
        remove.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                stocks.clear();
                saveStockStore(new ArrayList<StockStorage>(0));
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "List Cleared", Toast.LENGTH_SHORT).show();
                if (stocks.size() == 0) {
                    removeBubble();
                    finish(); //all stocks are gone
                }
                return false;
            }
        });

        bubbleLayout.setOnTouchListener(new OnSwipeListener(bubblePopup.this) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                int pos = 0;
                for (int i = 0; i < stocks.size(); i++) {
                    if(stocks.get(i).isFocus()){
                        pos = i;
                        break;
                    }
                }
                if(stocks.get(pos+1) != null) {
                    for (int a = 0; a < stocks.size(); a++)
                        stocks.get(a).setFocus(false);
                    stocks.get(pos+1).setFocus(true);
                    stopRepeating();
                    getStock(stocks.get(pos+1).getTicker(), false);
                    adapter.notifyDataSetChanged();
                    list.scrollToPosition(pos+1);
                }
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                int pos = 0;
                for (int i = 0; i < stocks.size(); i++) {
                    if(stocks.get(i).isFocus()){
                        pos = i;
                        break;
                    }
                }
                if(stocks.get(pos-1) != null) {
                    for (int a = 0; a < stocks.size(); a++)
                        stocks.get(a).setFocus(false);
                    stocks.get(pos-1).setFocus(true);
                    stopRepeating();
                    getStock(stocks.get(pos-1).getTicker(), false);
                    adapter.notifyDataSetChanged();
                    list.scrollToPosition(pos-1);
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
        if (networkRequest.isConnected()) {

            main_img.setImageResource(R.drawable.icon_trans);
            direction.setText(R.string.loading);
            //Initialize new RequestQueue instance
            RequestQueue stockRequestQueue = Volley.newRequestQueue(this);
            //JsonArray Request instance
            JsonObjectRequest stockRequest = new JsonObjectRequest(Request.Method.GET, networkRequest.getQuote(symbol), null, new Response.Listener<JSONObject>() {
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
                                refresh_btn.setVisibility(View.INVISIBLE);
                            } else if(marketState.contains("REGULAR")) {
                                marketState = "Market Open";
                                refresh_btn.setVisibility(View.VISIBLE);
                            } else if(marketState.equals("PRE")) {
                                marketState = "Pre-Market";
                                refresh_btn.setVisibility(View.VISIBLE);
                            } else if(marketState.equals("POST")) {
                                marketState = "After Hours";
                                refresh_btn.setVisibility(View.VISIBLE);
                            } else {
                                marketState = "";
                                refresh_btn.setVisibility(View.VISIBLE);
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

                            direction.setText(R.string.search_for_symbols_or_companies);

                        } catch (JSONException e) {
                            stopRepeating();
                            clearInfo();
                            direction.setText("Error requesting '" + symbol + "' ticker symbol. Please try again");
                            main_img.setImageResource(R.drawable.warning_img_foreground);
                        }
                    } else {
                        stopRepeating();
                        clearInfo();
                        main_img.setImageResource(R.drawable.warning_img_foreground);
                        direction.setText("Error requesting '" + symbol + "' ticker symbol. Please try again");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    stopRepeating();
                    clearInfo();
                    main_img.setImageResource(R.drawable.warning_img_foreground);
                    direction.setText("Network Error Occurred. Try Again Later");
                }
            });
            //Add JsonArray request to request queue
            stockRequestQueue.add(stockRequest);

            //No connection
        } else {
            clearInfo();
            main_img.setImageResource(R.drawable.warning_img_foreground);
            direction.setText("Network Error Occurred");
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
            int s = 0;
            for (int i = 0; i < stocks.size(); i++) {
                if (stocks.get(i).isFocus()) {
                    s = i;
                    break;
                }
            }
            getStock(stocks.get(s).getTicker(), true);
        }
    };

    private void showInfo() {
        remove.setVisibility(View.VISIBLE);
        app_open.setVisibility(View.VISIBLE);
        direction.setVisibility(View.INVISIBLE);
        main_img.setVisibility(View.INVISIBLE);
        error_refresh.setVisibility(View.INVISIBLE);
        ticker.setVisibility(View.VISIBLE);
        price.setVisibility(View.VISIBLE);
        company.setVisibility(View.VISIBLE);
        dollar.setVisibility(View.VISIBLE);
        percent.setVisibility(View.VISIBLE);
        open.setVisibility(View.VISIBLE);
        open_price.setVisibility(View.VISIBLE);
        high.setVisibility(View.VISIBLE);
        high_price.setVisibility(View.VISIBLE);
        low.setVisibility(View.VISIBLE);
        low_price.setVisibility(View.VISIBLE);
        vol.setVisibility(View.VISIBLE);
        vol_unit.setVisibility(View.VISIBLE);
        avg_vol.setVisibility(View.VISIBLE);
        avg_vol_unit.setVisibility(View.VISIBLE);
        market_cap.setVisibility(View.VISIBLE);
        market_cap_unit.setVisibility(View.VISIBLE);
        list.setVisibility(View.VISIBLE);
    }

    private void clearInfo() {
        remove.setVisibility(View.INVISIBLE);
        direction.setVisibility(View.VISIBLE);
        error_refresh.setVisibility(View.INVISIBLE);
        main_img.setVisibility(View.VISIBLE);
        ticker.setVisibility(View.INVISIBLE);
        price.setVisibility(View.INVISIBLE);
        company.setVisibility(View.INVISIBLE);
        dollar.setVisibility(View.INVISIBLE);
        percent.setVisibility(View.INVISIBLE);
        open.setVisibility(View.INVISIBLE);
        open_price.setVisibility(View.INVISIBLE);
        high.setVisibility(View.INVISIBLE);
        high_price.setVisibility(View.INVISIBLE);
        low.setVisibility(View.INVISIBLE);
        low_price.setVisibility(View.INVISIBLE);
        vol.setVisibility(View.INVISIBLE);
        vol_unit.setVisibility(View.INVISIBLE);
        avg_vol.setVisibility(View.INVISIBLE);
        avg_vol_unit.setVisibility(View.INVISIBLE);
        market_cap.setVisibility(View.INVISIBLE);
        market_cap_unit.setVisibility(View.INVISIBLE);
        refresh_btn.setVisibility(View.INVISIBLE);
        list.setVisibility(View.INVISIBLE);
        app_open.setVisibility(View.VISIBLE);

        atCloseIndicator.setVisibility(View.INVISIBLE);
        atClosePercent.setVisibility(View.INVISIBLE);
        atCloseChange.setVisibility(View.INVISIBLE);
        atClosePrice.setVisibility(View.INVISIBLE);
        dayStatus.setVisibility(View.INVISIBLE);

    }


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
        SharedPreferences stockPreferences = getSharedPreferences("com.stockpin.stocks", MODE_PRIVATE);
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



    @Override
    public void onItemClick(int position) {
        for (int i = 0; i < stocks.size(); i++)
            stocks.get(i).setFocus(false);
        stocks.get(position).setFocus(true);

        stopRepeating();

        String selectedTicker = stocks.get(position).getTicker();

        if(ticker.getVisibility() == View.VISIBLE && ticker.getText().toString().equals(selectedTicker)) {
            stopRepeating();
            getStock(selectedTicker, true);
        } else {
            stopRepeating();
            getStock(selectedTicker, false);
        }

        adapter.notifyDataSetChanged();
        list.scrollToPosition(position);
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(stocks, fromPosition, toPosition);

            adapter.notifyItemMoved(fromPosition, toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };

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

    public void loadBubblePreferences() {
        settings = getApplicationContext().getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
        int bubbleColor = settings.getInt("bubbleColor", 1);
        switch (bubbleColor) {
            case 1:
                bubbleCard.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.lightBlue));
                break;
            case 2:
                bubbleCard.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.redCard));
                break;
            case 3:
                bubbleCard.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.goldCard));
                break;
            case 4:
                bubbleCard.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.greenCard));
                break;
            case 5:
                bubbleCard.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.purpleCard));
                break;
            case 6:
                bubbleCard.setCardBackgroundColor(getApplicationContext().getResources().getColor(R.color.black));
                break;
        }
    }


    private void removeBubble() {
        if (isMyServiceRunning(BubbleService.class) && BubbleService.bubbleViewManager != null) {
            stopService(new Intent(getApplicationContext(), BubbleService.class));
        }
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
    protected void onResume() {
        int stockFocus = 0;
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).isFocus()) {
                stockFocus = i;
                break;
            }
        }
        list.scrollToPosition(stockFocus);
        if(ticker.getVisibility() == View.VISIBLE) {
            getStock(ticker.getText().toString(), true);
        } else {
            getStock(stocks.get(stockFocus).getTicker(), false);
        }
        if(state.equals("Market Closed")) {
            refresh_btn.setVisibility(View.INVISIBLE);
        } else {
            refresh_btn.setVisibility(View.VISIBLE);
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
        saveStockStore(stocks);
        stopRepeating();
        super.onStop();
    }
}