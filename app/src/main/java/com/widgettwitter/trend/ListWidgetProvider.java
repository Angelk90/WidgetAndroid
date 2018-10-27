package com.widgettwitter.trend;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.widgettwitter.AppIntent;
import com.widgettwitter.R;

public class ListWidgetProvider extends AppWidgetProvider {
    private static final String TAG = ListWidgetProvider.class.getName();

    public static final int OPEN_CLICK_TYPE = 1;
    //private static Context contextApp = null;
    //private static int appWidgetIdApp;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v("Class:" + TAG, "onUpdate:" + appWidgetIds.length);
        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        //contextApp = context;
        //appWidgetIdApp = appWidgetId;

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget2);

        Intent intent = new Intent(context, ListWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));//problem
        remoteView.setRemoteAdapter(R.id.widgetListViewTrends, intent);
        remoteView.setEmptyView(R.id.widgetListViewTrends, R.id.empty_view_trends);

        final Intent onClickIntent = new Intent(context, ListWidgetProvider.class);
        onClickIntent.setAction(AppIntent.ACTION_CLICK_LIST_WIDGET);
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setPendingIntentTemplate(R.id.widgetListViewTrends, onClickPendingIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetListViewTrends);
        appWidgetManager.updateAppWidget(appWidgetId, remoteView);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (AppIntent.ACTION_CLICK_LIST_WIDGET.equals(intent.getAction())) {
            switch (intent.getIntExtra(AppIntent.EXTRA_CLICK_TYPE, -1)) {
                case OPEN_CLICK_TYPE:
                    int hash = intent.getIntExtra(AppIntent.EXTRA_ID, 1);
                    String txt = intent.getStringExtra(AppIntent.EXTRA_USERNAME);
                    txt = txt.replaceAll("#", "");
                    if (hash == 1) txt = "%23" + txt;
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mobile.twitter.com/search?q=" + txt));
                    context.startActivity(webIntent);
                    break;
            }
        }
    }

}
