package com.example.stockwatch;

import android.view.View;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

class MyViewHolder extends RecyclerView.ViewHolder {

    TextView symbol;
    TextView companyName;
    TextView price;
    TextView priceChange;
    TextView changePercentage;

    MyViewHolder(View view) {
        super(view);
        symbol = view.findViewById(R.id.symbol);
        companyName = view.findViewById(R.id.companyName);
        price = view.findViewById(R.id.price);
        priceChange = view.findViewById(R.id.priceChange);
        changePercentage = view.findViewById(R.id.changePercentage);
    }
}
