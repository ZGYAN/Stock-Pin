package com.stockpin.stockpinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;



public class popUpActivity extends AppCompatActivity {

    TextView ticker, company, price, dollar, percent, direction, marketStatus;
    TextView open, open_price, high, high_price, low, low_price;
    TextView vol, vol_unit, avg_vol, avg_vol_unit, market_cap, market_cap_unit;
    TextView atClosePrice, atClosePercent, atCloseChange, atCloseIndicator;
    ImageButton refresh_btn, error_refresh, app_open;
    Button dismiss_btn;
    ImageView main_img, dayStatus;
    NetworkRequest networkRequest;
    RelativeLayout bubbleLayout;
    ScrollView noteScroll;

    private Vibrator vibrator;
    static String tickerSymbol = "";

    //Auto refresh components
    final int MARKET_HOURS_REFRESH_RATE = 5000;
    final int PREP_HOURS_REFRESH_RATE = 10000;
    final int OUTSIDE_HOURS_REFRESH_RATE = 15000;
    String state = "Market Closed";
    final Handler refreshHandler = new Handler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_popup);

        ticker = findViewById(R.id.bticker);
        company = findViewById(R.id.bcompany_name);
        price = findViewById(R.id.bprice);
        dollar = findViewById(R.id.bdollar_change);
        percent = findViewById(R.id.bpercent_change);
        open = findViewById(R.id.bopen_title);
        open_price = findViewById(R.id.bopen_price);
        high = findViewById(R.id.bhigh_title);
        high_price = findViewById(R.id.bhigh_price);
        low = findViewById(R.id.blow_title);
        low_price = findViewById(R.id.blow_price);
        vol = findViewById(R.id.bvolume_title);
        vol_unit = findViewById(R.id.bvolume_unit);
        avg_vol = findViewById(R.id.bavg_volume_title);
        avg_vol_unit = findViewById(R.id.baverage_volume_unit);
        market_cap = findViewById(R.id.bmarketCap_title);
        market_cap_unit = findViewById(R.id.bmarketCap_listing);
        refresh_btn = findViewById(R.id.brefresh_btn);
        error_refresh = findViewById(R.id.berror_refresh);
        direction = findViewById(R.id.bdirection);
        main_img = findViewById(R.id.bmain_img);
        marketStatus = findViewById(R.id.bmarketStatus);
        bubbleLayout = findViewById(R.id.bubbleLayout);
        dismiss_btn = findViewById(R.id.dismiss_btn);
        app_open = findViewById(R.id.open_app);
        noteScroll = findViewById(R.id.noteScroll);
        atClosePrice = findViewById(R.id.closeBprice);
        atCloseChange = findViewById(R.id.closeBdollar_change);
        atClosePercent = findViewById(R.id.closeBpercent_change);
        atCloseIndicator = findViewById(R.id.closeBIndicator);
        dayStatus = findViewById(R.id.popUpDayStatusIcon);

        noteScroll.fullScroll(ScrollView.FOCUS_UP);

        marketStatus.setVisibility(View.INVISIBLE);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        networkRequest = new NetworkRequest(popUpActivity.this);

        popUpActivity p = new popUpActivity();
        p.onNewIntent(this.getIntent());

        getStock(tickerSymbol, false);


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
                if(networkRequest.isConnected()) {
                    stopRepeating();
                    getStock(tickerSymbol, false); //repeat network request of current stock
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
        dismiss_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

                            if (stockPrice < 0.5) {
                                unitFormat = new DecimalFormat("###,###,###,###,###,###.####");
                                unitFormat.setDecimalSeparatorAlwaysShown(true);
                                unitFormat.setMinimumFractionDigits(4);
                            } else if (stockPrice < 1) {
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
                        direction.setText("Error requesting '" + symbol + "' ticker symbol. Please try again");
                        main_img.setImageResource(R.drawable.warning_img_foreground);
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
            getStock(ticker.getText().toString(), true);
        }
    };

    private void showInfo() {
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
    }

    private void clearInfo() {
        app_open.setVisibility(View.VISIBLE);
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

        atCloseIndicator.setVisibility(View.INVISIBLE);
        atClosePercent.setVisibility(View.INVISIBLE);
        atCloseChange.setVisibility(View.INVISIBLE);
        atClosePrice.setVisibility(View.INVISIBLE);
        dayStatus.setVisibility(View.INVISIBLE);
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


    @Override
    protected void onNewIntent(Intent intent) {
        tickerSymbol = intent.getExtras().getString("ticker");
        super.onNewIntent(intent);
    }


    @Override
    protected void onResume() {
        if(ticker.getVisibility() == View.VISIBLE) {
            getStock(tickerSymbol, true);
        } else {
            getStock(tickerSymbol, false);
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
    protected void onStart() {
        stopRepeating();
        super.onStart();
    }
}
