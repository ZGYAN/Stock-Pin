package com.stockpin.stockpinapp;

public class StockStorage {
    String ticker;
    boolean isFocus;

    public StockStorage(String ticker, boolean isFocus) {
        this.ticker = ticker;
        this.isFocus = isFocus;
    }


    public void setFocus(boolean focus) {
        isFocus = focus;
    }

    public String getTicker() {
        return ticker;
    }

    public boolean isFocus() {
        return isFocus;
    }




}
