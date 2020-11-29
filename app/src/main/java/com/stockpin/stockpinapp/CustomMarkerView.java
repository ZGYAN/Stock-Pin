package com.stockpin.stockpinapp;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.SimpleDateFormat;
import java.util.Date;


public class CustomMarkerView extends MarkerView {

    private final TextView tvContent;
    public CustomMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.markContent);
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        tvContent.setSingleLine(false);

        String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"};

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd yyyy hh mm aa");
        String[] dateInfo = dateFormat.format(new Date((long) e.getX() * 1000)).split(" ");

        int monthIndex = Integer.parseInt(dateInfo[0]) - 1;
        String dayString = dateInfo[1];
        if(dayString.startsWith("0")) dayString = dayString.substring(1);

        String yearString = dateInfo[2];


        tvContent.setText("$" + e.getY() + " \n" + months[monthIndex] + " " + dayString + " " + yearString + " " + dateInfo[3] + " " + dateInfo[4] + " " + dateInfo[5]);
//            tvContent.setText(Utils.formatNumber(e.getY(), 0, false));


        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}
