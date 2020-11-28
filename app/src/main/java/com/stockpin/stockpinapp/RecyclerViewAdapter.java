package com.stockpin.stockpinapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<StockStorage> tickers = new ArrayList<>();
    Context mContext;
    SharedPreferences settings;

    private OnItemClicked onItemClicked;


    public RecyclerViewAdapter(ArrayList<StockStorage> tickers, Context mContext, OnItemClicked onItemClicked) {
        this.tickers = tickers;
        this.mContext = mContext;
        this.onItemClicked = onItemClicked;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view, onItemClicked);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.listTicker.setText(tickers.get(position).getTicker());

        if (tickers.get(position).isFocus()) {
            holder.circle.setImageResource(R.drawable.circle_borders);
        } else {
            holder.circle.setImageResource(R.color.transparent);
        }


    }

    @Override
    public int getItemCount() {
        return tickers.size();
    }


    //Holds UI elements
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView circle;
        TextView listTicker;

        OnItemClicked onItemClicked;

        public ViewHolder(View itemView, OnItemClicked onItemClicked) {
            super(itemView);
            circle = itemView.findViewById(R.id.item_background_circle);
            listTicker = itemView.findViewById(R.id.item_text);

            settings = mContext.getSharedPreferences("com.stockpin.Settings", Context.MODE_PRIVATE);
            int bubbleColor = settings.getInt("bubbleColor", 1);
            switch (bubbleColor) {
                case 1:
                    circle.setBackgroundResource(R.drawable.blue_circle_back);
                    break;
                case 2:
                    circle.setBackgroundResource(R.drawable.red_circle_back);
                    break;
                case 3:
                    circle.setBackgroundResource(R.drawable.gold_circle_back);
                    break;
                case 4:
                    circle.setBackgroundResource(R.drawable.green_circle_back);
                    break;
                case 5:
                    circle.setBackgroundResource(R.drawable.purple_circle_back);
                    break;
                case 6:
                    circle.setBackgroundResource(R.drawable.black_circle_back);
                    break;
            }

            this.onItemClicked = onItemClicked;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            onItemClicked.onItemClick(getAdapterPosition());
        }
    }
    //Declare interface
    public interface OnItemClicked {
        void onItemClick(int position);
    }


}
