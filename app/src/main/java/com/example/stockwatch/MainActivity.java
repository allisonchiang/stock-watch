package com.example.stockwatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private SwipeRefreshLayout swiper;

    private final List<Stock> stockList = new ArrayList<>();
    private List<Stock> tempStockList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Stock Watch");

        recyclerView = findViewById(R.id.recycler);

        stockAdapter = new StockAdapter(stockList, this);

        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swiper = findViewById(R.id.swiper);
        swiper.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        doRead();

//        // dummy stock data
//        stockList.add(new Stock("AAPL", "Apple", 0, 0, 0));
//        stockList.add(new Stock("FB", "Facebook", 0, 0, 0));
//        stockList.add(new Stock("TSLA", "Tesla", 0, 0, 0));

        // check network connection
        if (doNetCheck()) {
            // execute NameDownloader AsyncTask
            new NameDownloader(this).execute();
            loadStockData();
        } else {
            showNoNetworkDialog("Stock info cannot be retrieved while offline.");
            Collections.sort(stockList);
            stockAdapter.notifyDataSetChanged();
        }
    }

    public void showNoNetworkDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showNoStockMatchDialog(String stockSymbol) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found: " + stockSymbol);
        builder.setMessage("Data for stock symbol");
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // JSON file saving should happen in the onPause method
    @Override
    protected void onPause() {
        doWrite();
        super.onPause();
    }

    private boolean doNetCheck() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
//            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
//            Toast.makeText(this, "Connected to network", Toast.LENGTH_SHORT).show();
            return true;
        } else {
//            Toast.makeText(this, "Not connected to network", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    // loads all stock data
    private void loadStockData() {
        tempStockList.addAll(stockList);
        stockList.clear();
        for (Stock stock : tempStockList) {
            new StockDownloader(this).execute(stock.getSymbol());
        }
//        Collections.sort(stockList);
//        stockAdapter.notifyDataSetChanged();
        tempStockList.clear();
    }

    // StockDownloader's onPostExecute calls this
    public void updateData(Stock stock) {
        stockList.add(stock);
        Collections.sort(stockList);
        stockAdapter.notifyDataSetChanged();
        doWrite();
    }

    // checks for duplicate before adding stock
    public void addStock(String symbol) {
        boolean duplicateFound = false;

        for (Stock stock : stockList) {
            if (stock.getSymbol().equals(symbol)) {
                duplicateFound = true;
                break;
            }
        }

        if (duplicateFound) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Duplicate Stock");
            builder.setMessage("Stock symbol " + symbol + " is already displayed.");
            AlertDialog dialog3 = builder.create();
            dialog3.show();
        } else {
            new StockDownloader(MainActivity.this).execute(symbol);
        }
    }

    private void doRefresh() {
        doRead(); // clears stockList
        if (doNetCheck()) {
            loadStockData();
            Collections.sort(stockList);
            stockAdapter.notifyDataSetChanged();
            swiper.setRefreshing(false);
        } else {
            showNoNetworkDialog("Stocks cannot be updated without a network connection.");
            swiper.setRefreshing(false);
        }
    }

    // opens web browser
    @Override
    public void onClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        Stock selectedStock = stockList.get(pos);
//        Toast.makeText(this, "Stock clicked", Toast.LENGTH_LONG).show();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("https://www.marketwatch.com/investing/stock/" + selectedStock.getSymbol()));
        startActivity(i);
    }

    // delete stock
    @Override
    public boolean onLongClick(View v) {
        int pos = recyclerView.getChildLayoutPosition(v);
        final Stock selectedStock = stockList.get(pos);

        // display delete stock confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock");
        builder.setIcon(R.drawable.delete);
        builder.setMessage("Delete Stock Symbol " + selectedStock.getSymbol() + "?");
        builder.setPositiveButton("DELETE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Code goes here
                        stockList.remove(selectedStock);
                        doWrite();
                        stockAdapter.notifyDataSetChanged();
                    }
                });
        builder.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();

        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAdd:
                if (doNetCheck()) {
                    // display add stock symbol entry dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Stock Selection");
                    builder.setMessage("Please enter a Stock Symbol:");

                    final EditText input = new EditText(this);
                    input.setGravity(Gravity.CENTER_HORIZONTAL);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

                    builder.setView(input);

                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String dialogText;
                            String symbol;

                            // Code goes here
                            dialogText = input.getText().toString();
                            // get matching symbols/names from NameDownloader
                            final ArrayList<String[]> stockMatchList = NameDownloader.returnNameSearchMatches(dialogText);
                            if (stockMatchList.size() == 0) {
                                showNoStockMatchDialog(dialogText);
                            } else if (stockMatchList.size() == 1) {
                                symbol = stockMatchList.get(0)[0];
                                addStock(symbol);
                            } else {
                                final String[] results = new String[stockMatchList.size()];
                                for (int i = 0; i < stockMatchList.size(); i++) {
                                    results[i] = stockMatchList.get(i)[0] + " - " + stockMatchList.get(i)[1];
                                }
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Make a Selection");
                                builder.setItems(results, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String symbol = stockMatchList.get(which)[0];
                                        addStock(symbol);
                                    }
                                });

                                builder.setNegativeButton("NEVERMIND",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        });

                                AlertDialog dialog2 = builder.create();
                                dialog2.show();
                            }
                        }
                    });
                    builder.setNegativeButton("CANCEL",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    showNoNetworkDialog("Stocks cannot be added without a network connection.");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void doWrite() {

        JSONArray jsonArray = new JSONArray();

        for (Stock s : stockList) {
            try {
                JSONObject noteJSON = new JSONObject();
                noteJSON.put("symbol", s.getSymbol());
                noteJSON.put("companyName", s.getCompanyName());

                jsonArray.put(noteJSON);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String jsonText = jsonArray.toString();

        Log.d(TAG, "doWrite: " + jsonText);

        try {
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(
                            openFileOutput("data.json", Context.MODE_PRIVATE)
                    );

            outputStreamWriter.write(jsonText);
            outputStreamWriter.close();
//            Toast.makeText(this, "File write success!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.d(TAG, "doWrite: File write failed: " + e.toString());
//            Toast.makeText(this, "File write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void doRead() {
        stockList.clear();
        try {
            InputStream inputStream = openFileInput("data.json");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                String jsonText = stringBuilder.toString();

                try {
                    JSONArray jsonArray = new JSONArray(jsonText);
                    Log.d(TAG, "doRead: " + jsonArray.length());

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String symbol = jsonObject.getString("symbol");
                        String companyName = jsonObject.getString("companyName");
                        double price = 0.0;
                        double priceChange = 0.0;
                        double changePercentage = 0.0;

                        Stock s = new Stock(symbol, companyName, price, priceChange, changePercentage);
                        stockList.add(s);
                    }

                    Log.d(TAG, "doRead: " + stockList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, "doRead: File not found: \" + e.toString()");
        } catch (IOException e) {
            Log.d(TAG, "doRead: Can not read file: " + e.toString());
        }
    }
}