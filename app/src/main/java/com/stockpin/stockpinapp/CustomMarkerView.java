package com.stockpin.stockpinapp;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CustomMarkerView extends MarkerView {

    private final TextView tvContent;
    public static boolean canScroll;
    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.markContent);
        canScroll = true;
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        canScroll = true;


        tvContent.setSingleLine(false);

        String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd yyyy hh mm aa");
        String[] dateInfo = dateFormat.format(new Date((long) e.getX() * 1000)).split(" ");

        int monthIndex = Integer.parseInt(dateInfo[0]) - 1;
        String dayString = dateInfo[1];
        if(dayString.startsWith("0")) dayString = dayString.substring(1);

        String yearString = dateInfo[2];

        DecimalFormat ndf = new DecimalFormat("###,###,##0.00");
        String priceString = ndf.format(e.getY());

        String hourString = dateInfo[3];
        if(hourString.startsWith("0")) hourString = hourString.substring(1);


        tvContent.setText("$" + priceString + " \n" + months[monthIndex] + " " + dayString + ", " + yearString + " " + hourString + ":" + dateInfo[4] + " " + dateInfo[5]);
//            tvContent.setText(Utils.formatNumber(e.getY(), 0, false));

        canScroll = true;

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
