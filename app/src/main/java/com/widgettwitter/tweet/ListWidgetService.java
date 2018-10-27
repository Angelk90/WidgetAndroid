package com.widgettwitter.tweet;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class ListWidgetService extends RemoteViewsService {
    private static final String TAG = ListWidgetService.class.getName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        String widgetText = WidgetConfigureActivity.loadTitlePref(getApplicationContext(), appWidgetId);
        Log.v("Class:" + TAG, "onGetViewFactory:" + widgetText);

        /*Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://twitter?tweet_id=1054634400686751744"));
        appIntent.setPackage("com.twitter.android");
        Intent webIntent = new Intent(Intent.ACTION_VIEW,Uri.parse("https://twitter.com/SillyLarkS/status/1054634400686751744"));
        try {
            startActivity(webIntent);
        } catch (ActivityNotFoundException ex) {
            //context.startActivity(webIntent);
        }*/

        return new ListWidgetViewsFactory(getApplicationContext(), intent);
    }
}