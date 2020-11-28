package com.stockpin.stockpinapp;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends ArrayAdapter<String> {

    private List<String> stocksFull; //contains all stocks
    String filterPattern = "";
    Context context;

    public SuggestionAdapter(@NonNull Context context, int resource, @NonNull List<String> stocks) {
        super(context, resource, stocks);
        stocksFull = new ArrayList<>(stocks);
        this.context = context;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return stockFilter;
    }

    private Filter stockFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            final List<String> suggestions = new ArrayList<>(); //suggestions are stored here and added to filter results

            //constraint is what was typed into edit text
            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(stocksFull); //add everything to suggestions
            } else {
                //Filter results if something is typed in
                filterPattern = constraint.toString().toLowerCase().trim();
                ArrayList<String> tickers = new ArrayList<>();
                tickers.clear();

                for (String item : stocksFull) {
                    String tick = item.substring(item.lastIndexOf("(") + 1, item.lastIndexOf(")"));
                    if (filterPattern.toUpperCase().equals(tick)) {
                        suggestions.clear();
                        suggestions.add(item);
                        break;
                    } else if (tick.toUpperCase().startsWith(filterPattern.toUpperCase())) {
                        if(suggestions.size() < 5 && !suggestions.contains(item) && !tickers.contains(tick)) {
                            suggestions.add(item);
                            tickers.add(tick);
                        }
                    } else if(item.toLowerCase().startsWith(filterPattern)) {
                        if(suggestions.size() < 5 && !suggestions.contains(item) && !tickers.contains(tick)) {
                            suggestions.add(item);
                            tickers.add(tick);
                        }
                    }
                }
            }

            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((String) resultValue);
        }
    };


}