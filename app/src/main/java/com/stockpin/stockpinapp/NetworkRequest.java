package com.stockpin.stockpinapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.util.Log;

public class NetworkRequest {

    Context context;
    final private String stockLink = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=";
    final private String symbolSearchLink = "https://s.yimg.com/aq/autoc?query=";
    final private String symbolSearchEnd = "&region=US&lang=en-US";

    final private String chartLink = "https://query1.finance.yahoo.com/v8/finance/chart/";

    public NetworkRequest(Context context) {
        this.context = context;
    }

    public String getSymbolSearchLink(String symbol) {
        return symbolSearchLink+symbol+symbolSearchEnd;
    }
    /**
     * @param symbol
     * @return API call link
     */
    public String getQuote(String symbol) {
        return stockLink + symbol;
    }

    public String getChart(String symbol, String startEpoch, String dataGranularity, boolean includePrePost) {
        if(includePrePost) {
            return chartLink + symbol +"?symbol=" + symbol + "&period1=" + startEpoch + "&period2=9999999999&interval="+ dataGranularity +"&includePrePost=true";
        } else {
            return chartLink + symbol +"?symbol=" + symbol + "&period1=" + startEpoch + "&period2=9999999999&interval="+ dataGranularity +"&includePrePost=false";
        }
    }

    /**
     * Checks network state
     * True = connected, False = not connected
     * @return Boolean
     */
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activeNetwork = cm.getActiveNetwork();
        } else {
            return true;
        }
        return activeNetwork != null;
    }

}
