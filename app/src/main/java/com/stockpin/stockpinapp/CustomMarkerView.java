//package com.stockpin.stockpinapp;
//
//import android.content.Context;
//import android.widget.TextView;
//
//import com.github.mikephil.charting.components.MarkerView;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.highlight.Highlight;
//import com.github.mikephil.charting.utils.MPPointF;
//
//public class CustomMarkerView extends MarkerView {
//
//    private final TextView tvContent;
//
//    public CustomMarkerView(Context context, int layoutResource) {
//        super(context, layoutResource);
//
//        tvContent = findViewById(R.id.markContent);
//    }
//
//    // runs every time the MarkerView is redrawn, can be used to update the
//    // content (user-interface)
//    @Override
//    public void refreshContent(Entry e, Highlight highlight) {
//
//        tvContent.setSingleLine(false);
//        tvContent.setText("$"+e.getY() + " \n" + e.getX());
////            tvContent.setText(Utils.formatNumber(e.getY(), 0, false));
//
//
//        super.refreshContent(e, highlight);
//    }
//
//    @Override
//    public MPPointF getOffset() {
//        return new MPPointF(-(getWidth() / 2), -getHeight());
//    }
//}
