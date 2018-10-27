package com.widgettwitter.tweet;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.widgettwitter.AppIntent;
import com.widgettwitter.R;

import static com.widgettwitter.tweet.ListWidgetViewsFactory.widgetText;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WidgetConfigureActivity WidgetConfigureActivity}
 */
public class ListWidgetProvider extends AppWidgetProvider {
    public static final int OPEN_CLICK_TYPE = 1;
    private static final String TAG = ListWidgetProvider.class.getName();
    private static final String WIDGET_APP_CONFIG = "com.widgettwitter.WIDGET_CONFIG";

    public static String WIDGET_TWITTER_NEW = "com.widgettwitter.WIDGET_NEW";
    public static String WIDGET_LIST_REFRESH = "com.widgettwitter.WIDGET_REFRESH";
    private static String WIDGET_LIST_CLEAR = "com.widgettwitter.WIDGET_CLEAR";
    static Context contextApp;
    static int appWidgetIdApp;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v("Class:" + TAG, "onUpdate:" + appWidgetIds.length);
        for (int appWidgetId : appWidgetIds)
            updateAppWidget(context, appWidgetManager, appWidgetId);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        contextApp = context;
        appWidgetIdApp = appWidgetId;

        widgetText = WidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        Log.v("Class:" + TAG, "updateAppWidget:" + String.valueOf(widgetText));

        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteView.setTextViewText(R.id.hashtag, widgetText);

        Intent intent = new Intent(context, ListWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));//problem
        remoteView.setRemoteAdapter(R.id.widgetListViewTweets, intent);
        remoteView.setEmptyView(R.id.widgetListViewTweets, R.id.empty_view_tweets);

        final Intent newIntent = new Intent(context, ListWidgetProvider.class);
        newIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        newIntent.setAction(ListWidgetProvider.WIDGET_TWITTER_NEW);
        PendingIntent newPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, newIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.add, newPendingIntent);

        final Intent configIntent = new Intent(context, ListWidgetProvider.class);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        configIntent.setAction(ListWidgetProvider.WIDGET_APP_CONFIG);
        PendingIntent configPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.hashtag, configPendingIntent);

        final Intent refreshIntent = new Intent(context, ListWidgetProvider.class);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        refreshIntent.setAction(ListWidgetProvider.WIDGET_LIST_REFRESH);
        refreshIntent.setData(Uri.withAppendedPath(Uri.parse("widget://widget/id/#"+appWidgetId), String.valueOf(appWidgetId)));
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);

        final Intent clearIntent = new Intent(context, ListWidgetProvider.class);
        clearIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        clearIntent.setAction(ListWidgetProvider.WIDGET_LIST_CLEAR);
        PendingIntent clearPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, clearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.clear, clearPendingIntent);

        final Intent onClickIntent = new Intent(context, ListWidgetProvider.class);
        onClickIntent.setAction(AppIntent.ACTION_CLICK_LIST_WIDGET);
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setPendingIntentTemplate(R.id.widgetListViewTweets, onClickPendingIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetListViewTweets);
        appWidgetManager.updateAppWidget(appWidgetId, remoteView);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds)
            WidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Log.v("Class:" + TAG, "onReceive:" + intent.getAction() + " widgetText:" + widgetText);

        if (WIDGET_LIST_REFRESH.equals(intent.getAction())) {
            Toast.makeText(contextApp, "Refresh:" + widgetText, Toast.LENGTH_SHORT).show();
            ListWidgetViewsFactory.onRefresh();
        } else if (WIDGET_APP_CONFIG.equals(intent.getAction())) {
            Intent appIntent = new Intent(context, WidgetConfigureActivity.class);
            appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
        } else if (WIDGET_LIST_CLEAR.equals(intent.getAction())) {
            ListWidgetViewsFactory.onClear();
        } else if (WIDGET_TWITTER_NEW.equals(intent.getAction())) {
            String text = widgetText.replaceAll("#", "");
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=%23" + text + " "));
            context.startActivity(appIntent);
        } else if (AppIntent.ACTION_CLICK_LIST_WIDGET.equals(intent.getAction())) {
            switch (intent.getIntExtra(AppIntent.EXTRA_CLICK_TYPE, -1)) {
                case OPEN_CLICK_TYPE:
                    String id = intent.getStringExtra(AppIntent.EXTRA_ID);
                    String user = intent.getStringExtra(AppIntent.EXTRA_USERNAME);
                    user = user.replaceAll("@", "");

                    Log.v("Class:" + TAG, "ID:" + id + ",User:" + user);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + user + "/status/" + id));
                    context.startActivity(webIntent);
                    break;
            }
        }
    }

}

