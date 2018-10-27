package com.widgettwitter.trend;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class ListWidgetService extends RemoteViewsService {
    private static final String TAG = ListWidgetService.class.getName();
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListWidgetViewsFactory(getApplicationContext(), intent);
    }
}