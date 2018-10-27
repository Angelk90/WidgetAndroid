package com.widgettwitter.trend;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.widgettwitter.AppIntent;
import com.widgettwitter.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class ListWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = ListWidgetViewsFactory.class.getName();

    private Context contextApp = null;
    private int appWidgetIdApp;
    private AppWidgetManager appWidgetManager = null;
    private RemoteViews remoteViewWidget = null;

    private ArrayList<String> listTrends = new ArrayList<String>();

    public ListWidgetViewsFactory(Context context, Intent intent) {
        Log.v("Class:" + TAG, "ListWidgetViewsFactory");
        contextApp = context;
        appWidgetIdApp = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        appWidgetManager = AppWidgetManager.getInstance(context);
        remoteViewWidget = new RemoteViews(context.getPackageName(), R.layout.widget2);
    }

    @Override
    public void onCreate() {
        Log.v("Class:" + TAG, "onCreate:");
        new Html().execute();
    }

    @Override
    public void onDataSetChanged() {
        Log.v("Class:" + TAG, "onDataSetChanged:");
    }

    @Override
    public void onDestroy() {
        if (listTrends != null) listTrends.clear();
    }

    @Override
    public int getCount() {
        return listTrends.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.v("Class:" + TAG, "getViewAt:" + position);
        String listItem = listTrends.get(position);
        int hash = listItem.indexOf("#") != -1 ? 1 : 0;
        final RemoteViews remoteView = new RemoteViews(contextApp.getPackageName(), android.R.layout.simple_list_item_1);
        remoteView.setTextViewText(android.R.id.text1, listItem);

        final Intent openIntent = new Intent();
        openIntent.putExtra(AppIntent.EXTRA_CLICK_TYPE, ListWidgetProvider.OPEN_CLICK_TYPE);
        openIntent.putExtra(AppIntent.EXTRA_ID, hash);
        openIntent.putExtra(AppIntent.EXTRA_USERNAME, listItem);
        remoteView.setOnClickFillInIntent(android.R.id.text1, openIntent);

        return remoteView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Html extends AsyncTask<Void, Void, Void> {
        private String url = "http://www.breaking-news.it";
        private JSONArray array = new JSONArray();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.v("Class:" + TAG, "doInBackground:" + url);

                Document doc = Jsoup.connect(url).get();
                Elements mElementDataSize = doc.select("section > p > a");
                int mElementSize = mElementDataSize.size();
                Log.v("Class:" + TAG, "Size:" + String.valueOf(mElementSize));

                for (int i = 3; i < mElementSize; i++) {
                    Elements mElementName = doc.select("a").eq(i);
                    String mName = mElementName.text();
                    String[] str = mName.split("[ ]");
                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("name", str[1]);
                        array.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            populateListItem(array);
        }

        private void populateListItem(JSONArray array) {
            for (int id = 0; id < array.length(); id++) {
                try {
                    Log.v("Class:" + TAG, "populateListItem:" + array.getJSONObject(id).getString("name"));
                    listTrends.add(array.getJSONObject(id).getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            remoteViewWidget.setTextViewText(R.id.trend, "#Trends (" + listTrends.size() + ")");
            appWidgetManager.updateAppWidget(appWidgetIdApp, remoteViewWidget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIdApp, R.id.widgetListViewTrends);
        }
    }
}
