# stock-watch
Android app that allows users to search for stocks and add stocks to their watch list. Uses
the web-based IEX API (https://iextrading.com/developers/docs/) to download stock symbols and company data.
Uses the IEX Cloud API (https://iexcloud.io/) to query stock prices.
- *AsyncTask* is used to download stock information upon app startup
- *RecyclerView* fills itself with stock objects which are managed by an adapter class, RecyclerView.Adapter
- *SwipeRefreshLayout* updates stock prices when user pulls down to refresh
- user's stock list is saved on internal JSON file and are loaded/saved in onCreate()/onPause() methods
