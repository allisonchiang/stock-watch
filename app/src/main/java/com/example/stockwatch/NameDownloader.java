package com.example.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NameDownloader extends AsyncTask<String, Integer, String> {

    private static final String TAG = "NameDownloader";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    private static final String DATA_URL =
            "https://api.iextrading.com/1.0/ref-data/symbols";

    NameDownloader(MainActivity ma) {
        this.mainActivity = ma;
    }

    private static HashMap<String, String> stockMap = new HashMap<>();

    // returns "FB - Facebook, Inc." and other companies if "FB" is searched
    static ArrayList<String[]> returnNameSearchMatches (String keyword) {
        ArrayList<String[]> matchedList = new ArrayList<>();

        // Using for-each loop
        for (Map.Entry mapElement : stockMap.entrySet()) {
            String symbol = (String)mapElement.getKey();
            String companyName = (String)mapElement.getValue();

            if (symbol.contains(keyword) || companyName.contains(keyword)) {
                matchedList.add(new String[] {symbol, companyName});
            }
        }
        return matchedList;
    }

    @Override
    protected void onPostExecute(String s) {
        parseJSON(s);

//        if (stockMap != null)
//            Toast.makeText(mainActivity, "NameDownloader loaded " + stockMap.size() + " stocks.", Toast.LENGTH_SHORT).show();
    }


    @Override
    protected String doInBackground(String... params) {
        Uri dataUri = Uri.parse(DATA_URL);
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


    private void parseJSON(String s) {
        try {
            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {
                JSONObject jStock = (JSONObject) jObjMain.get(i);
                String symbol = jStock.getString("symbol");
                String name = jStock.getString("name");

                stockMap.put(symbol, name);
            }
        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}