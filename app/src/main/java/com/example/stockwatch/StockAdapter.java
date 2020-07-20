package com.example.stockwatch;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private static final String TAG = "StockAdapter";
    private List<Stock> stockList;
    private MainActivity mainAct;

    StockAdapter(List<Stock> noteList, MainActivity mainAct) {
        this.stockList = noteList;
        this.mainAct = mainAct;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: Making new MyViewHolder");

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_row, parent, false);

        itemView.setOnClickListener(mainAct);
        itemView.setOnLongClickListener(mainAct);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: Filling viewHolder employee " + position);

        Stock stock = stockList.get(position);
        Double priceChange = stock.getPriceChange();

        int textColor;
        String triangle;
        if (priceChange >= 0) {
            textColor = Color.GREEN;
            triangle = "▲";
        } else {
            textColor = Color.RED;
            triangle = "▼";
        }



        holder.symbol.setText(stock.getSymbol());
        holder.companyName.setText(stock.getCompanyName());
        holder.price.setText(String.format(Locale.getDefault(),
                "%.2f", stock.getPrice()));
        holder.priceChange.setText(triangle + String.format(Locale.getDefault(),
                "%.2f", stock.getPriceChange()));
        holder.changePercentage.setText("(" + String.format(Locale.getDefault(),
                "%.2f", stock.getChangePercentage()*100) + "%)");

        holder.symbol.setTextColor(textColor);
        holder.companyName.setTextColor(textColor);
        holder.price.setTextColor(textColor);
        holder.priceChange.setTextColor(textColor);
        holder.changePercentage.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
