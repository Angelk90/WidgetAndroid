package com.widgettwitter.tweet;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.widgettwitter.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The configuration screen for the {@link ListWidgetProvider Widget} AppWidget.
 */
public class WidgetConfigureActivity extends Activity {
    private static final String TAG = WidgetConfigureActivity.class.getName();
    private static final String PREFS_NAME = "com.widgettwitter.Widget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> listTrends = new ArrayList<String>();
    private EditText mAppWidgetText;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        Log.v("Class:" + TAG, "onCreate");
        setContentView(R.layout.widget_configure);
        final EditText appwidget_text = (EditText) findViewById(R.id.appwidget_text);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listTrends);
        final ListView listView = (ListView) findViewById(R.id.ListViewTrend);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int pos, long mylng) {
                String selectedFromList = (listView.getItemAtPosition(pos).toString());
                appwidget_text.setText(selectedFromList);
            }
        });
        new Html().execute();
        listView.setAdapter(adapter);

        mAppWidgetText = (EditText) findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(loadTitlePref(WidgetConfigureActivity.this, mAppWidgetId));
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = WidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetText = mAppWidgetText.getText().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ListWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public WidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
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
            adapter.notifyDataSetChanged();
        }
    }
}

