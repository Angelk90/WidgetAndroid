package com.widgettwitter.tweet;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

class ListItem {
    public String num, id, ch, fullname, username, timestamp, text, photo;
}

public class ListWidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = ListWidgetViewsFactory.class.getName();

    private static Context contextApp = null;
    private static int appWidgetIdApp;
    private static AppWidgetManager appWidgetManager = null;
    private static RemoteViews remoteViewWidget = null;

    public static ArrayList<ListItem> listItemList = new ArrayList<ListItem>();
    public static String ultimeTwitter = "";

    private static String url;
    static String widgetText;
    static Boolean tag = false;

    public ListWidgetViewsFactory(Context context, Intent intent) {
        contextApp = context;
        appWidgetIdApp = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        widgetText = WidgetConfigureActivity.loadTitlePref(context, appWidgetIdApp);
        Log.v("Class:" + TAG, widgetText);

        String query = String.valueOf(widgetText);
        if (query.contains("#")) {
            widgetText = query.replaceAll("#", "");
            url = "https://twitter.com/search?f=tweets&q=%23" + widgetText + "&lang=it";
            tag = true;
        } else {
            url = "https://twitter.com/search?f=tweets&q=" + widgetText + "&lang=it";
            tag = false;
        }
        appWidgetManager = AppWidgetManager.getInstance(context);
        remoteViewWidget = new RemoteViews(context.getPackageName(), R.layout.widget);
    }

    @Override
    public void onCreate() {
        Log.v("Class:" + TAG, "onCreate:" + widgetText);
        remoteViewWidget.setTextViewText(R.id.hashtag, "Init");
        remoteViewWidget.setViewVisibility(R.id.progressBar, View.VISIBLE);
        appWidgetManager.updateAppWidget(appWidgetIdApp, remoteViewWidget);

        new Html().execute();
    }

    @Override
    public void onDataSetChanged() {
        Log.v("Class:" + TAG, "onDataSetChanged:" + appWidgetIdApp);
    }

    public static void onClear() {
        listItemList.clear();
        ultimeTwitter = "";
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIdApp, R.id.widgetListViewTweets);
    }


    public static void onRefresh() {
        remoteViewWidget.setTextViewText(R.id.hashtag, "#Refresh");
        remoteViewWidget.setViewVisibility(R.id.progressBar, View.VISIBLE);
        appWidgetManager.updateAppWidget(appWidgetIdApp, remoteViewWidget);

        new Html().execute();
    }

    @Override
    public void onDestroy() {
        if (listItemList != null) listItemList.clear();
    }

    @Override
    public int getCount() {
        return listItemList.size();
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

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.v("Class:" + TAG, "getViewAt:" + position);
        final RemoteViews remoteView = new RemoteViews(contextApp.getPackageName(), R.layout.list_row);
        ListItem listItem = listItemList.get(position);
        URL url = null;
        try {
            url = new URL(listItem.ch);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        bmp = getCroppedBitmap(bmp);
        remoteView.setImageViewBitmap(R.id.ch, bmp);
        remoteView.setTextViewText(R.id.name, listItem.fullname);
        remoteView.setTextViewText(R.id.username, listItem.username);
        remoteView.setTextViewText(R.id.timestamp, listItem.timestamp);
        remoteView.setTextViewText(R.id.text, listItem.text);

        /*if (listItem.photo != "") {
            try {
                url = new URL(listItem.photo);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            bmp = null;
            try {
                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            remoteView.setViewVisibility(R.id.photo, View.VISIBLE);
            remoteView.setImageViewBitmap(R.id.photo, bmp);
        }*/

        final Intent openIntent = new Intent();
        openIntent.putExtra(AppIntent.EXTRA_CLICK_TYPE, ListWidgetProvider.OPEN_CLICK_TYPE);
        openIntent.putExtra(AppIntent.EXTRA_ID, listItem.id);
        openIntent.putExtra(AppIntent.EXTRA_USERNAME, listItem.username);
        remoteView.setOnClickFillInIntent(R.id.widgetItemContainer, openIntent);

        return remoteView;
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private static class Html extends AsyncTask<Void, Void, Void> {
        private JSONArray array = new JSONArray();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                // Connect to the web site
                Document doc = Jsoup.connect(url).get();
                // Using Elements to get the Meta data
                Elements mElementDataSize = doc.select("div[class=content]");
                // Locate the content attribute
                int mElementSize = mElementDataSize.size();

                for (int i = 0; i < mElementSize; i++) {
                    JSONObject obj = new JSONObject();

                    Elements mElementId = doc.select("li[class=js-stream-item stream-item stream-item]").eq(i);
                    String mId = mElementId.attr("data-item-id");

                    Elements mElementAvatar = doc.select("img[class=avatar js-action-profile-avatar]").eq(i);
                    String mAvatar = mElementAvatar.attr("src");

                    Elements mElementFullName = doc.select("strong[class=fullname show-popup-with-id u-textTruncate]").eq(i);
                    String mFullName = mElementFullName.text();

                    Elements mElementUsername = doc.select("span[class=username u-dir u-textTruncate]").eq(i);
                    String mUsername = mElementUsername.text();

                    Elements mElementTimestamp = doc.select("span[class=_timestamp js-short-timestamp js-relative-timestamp]").eq(i);
                    String mTimestamp = mElementTimestamp.text();

                    Elements mElementText = doc.select("p[class=TweetTextSize  js-tweet-text tweet-text]").eq(i);
                    String mText = mElementText.text();

                    Elements mElementPhoto = doc.select("div[class=AdaptiveMedia-photoContainer js-adaptive-photo]").eq(i);
                    String mPhoto = mElementPhoto.attr("data-image-url");

                    try {
                        obj.put("id", mId);
                        obj.put("avatar", mAvatar);
                        obj.put("fullname", mFullName);
                        obj.put("username", mUsername);
                        obj.put("timestamp", mTimestamp);
                        obj.put("text", mText);
                        obj.put("photo", mPhoto);
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
            int id = array.length() - 1;
            if (!ultimeTwitter.equals("")) {
                for (int i = array.length() - 1; i >= 0; i--) {
                    try {
                        JSONObject o = array.getJSONObject(i);
                        if (o.getString("id").equals(ultimeTwitter)) {
                            id = i - 1;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (; id >= 0; id--) {
                ListItem listItem = new ListItem();
                try {
                    JSONObject o = array.getJSONObject(id);
                    //Log.v("Class:" + TAG, "populateListItem" + String.valueOf(array.getJSONObject(id)));
                    ultimeTwitter = o.getString("id");
                    listItem.id = o.getString("id");
                    listItem.fullname = listItemList.size() + ") " + o.getString("fullname");
                    listItem.username = o.getString("username");
                    listItem.timestamp = "· " + o.getString("timestamp");
                    listItem.text = o.getString("text");
                    listItem.ch = o.getString("avatar");
                    listItem.photo = o.getString("photo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                listItemList.add(listItem);
            }

            Date date = new Date();
            //long timeInMilliSeconds = date.getTime();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            String formattedDate = dateFormat.format(date);

            String str = tag ? "#" : "";
            remoteViewWidget.setTextViewText(R.id.hashtag, str + String.valueOf(widgetText) + " (" + listItemList.size() + ")");
            remoteViewWidget.setViewVisibility(R.id.progressBar, View.INVISIBLE);
            remoteViewWidget.setTextViewText(R.id.time, formattedDate);

            appWidgetManager.updateAppWidget(appWidgetIdApp, remoteViewWidget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIdApp, R.id.widgetListViewTweets);
            Log.v("Class:" + TAG, "TweetProvider:onPostExecute");
        }
    }

}