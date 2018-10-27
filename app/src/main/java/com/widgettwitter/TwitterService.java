package com.widgettwitter;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/*Intent listRefreshIntent = new Intent(context, TwitterService.class);
            listRefreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIdApp);
            listRefreshIntent.setAction(TwitterService.ACTION_FETCH);
            context.startService(listRefreshIntent);*/

public class TwitterService extends Service {
    public static final String ACTION_FETCH = "timeLineFetch";
    private int appWidgetId;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.v("Ok:TwitterService", "@onStartCommand");
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            if (intent.getAction().equals(ACTION_FETCH)) {
                Log.v("Ok:TwitterService", "@TwitterService");
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
