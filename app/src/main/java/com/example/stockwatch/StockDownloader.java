package com.example.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class StockDownloader extends AsyncTask<String, Integer, String> {

    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    private static final String DATA_URL1 =
            "https://cloud.iexapis.com/stable/stock/";

    private static final String DATA_URL2 =
            "/quote?token=";

    private static final String TOKEN =
            "sk_cc689fa369fb42648982b13d4dac197e";

    private static final String TAG = "StockDownloader";

    StockDownloader(MainActivity ma) {
        this.mainActivity = ma;
    }



    @Override
    protected void onPostExecute(String s) {
        Stock stock = parseJSON(s);
        if (stock != null)
//            Toast.makeText(mainActivity, "Loaded stock financial data for " + stock.getCompanyName(), Toast.LENGTH_SHORT).show();

        mainActivity.updateData(stock);
    }


    @Override
    protected String doInBackground(String... params) {

        String symbol = params[0];

        Uri dataUri = Uri.parse(DATA_URL1 + symbol + DATA_URL2 + TOKEN);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            Log.d(TAG, "doInBackground: ResponseCode: " + conn.getResponseCode());

            conn.setRequestMethod("GET");

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "doInBackground: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "doInBackground: ", e);
            return null;
        }

        return sb.toString();
    }


    private Stock parseJSON(String s) {
        Stock stock = null;

        try {
            JSONObject jsonObject = new JSONObject(s);
            String symbol = jsonObject.getString("symbol");
            String companyName = jsonObject.getString("companyName");
            double latestPrice, change, changePercent;

            String latestPriceString = jsonObject.getString("latestPrice");
            String changeString = jsonObject.getString("change");
            String changePercentString = jsonObject.getString("changePercent");

            // if double fields are null set to 0
            if (latestPriceString.equals("null")) {
                latestPrice = 0;
            } else {
                latestPrice = Double.parseDouble(latestPriceString);
            }

            if (changeString.equals("null")) {
                change = 0;
            } else {
                change = Double.parseDouble(changeString);
            }

            if (changePercentString.equals("null")) {
                changePercent = 0;
            } else {
                changePercent = Double.parseDouble(changePercentString);
            }

//            double latestPrice = Double.parseDouble(jsonObject.getString("latestPrice"));
//            double change = Double.parseDouble(jsonObject.getString("change"));
//            double changePercent = Double.parseDouble(jsonObject.getString("changePercent"));

            stock = new Stock(symbol, companyName, latestPrice, change, changePercent);
        } catch (JSONException err) {
            Log.d("Error", err.toString());
        }
        return stock;
    }
}