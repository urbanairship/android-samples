/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.RemoteViews;

import com.urbanairship.richpush.sample.R;

/**
 * The widget provider for the rich push inbox
 */
public class RichPushWidgetProvider extends AppWidgetProvider {
    public static String OPEN_MESSAGE_ACTION = "com.urbanairship.richpush.sample.widget.OPEN_MESSAGE";
    public static String REFRESH_ACTION = "com.urbanairship.richpush.sample.widget.REFRESH";

    private static HandlerThread workerThread;
    private static Handler workerQueue;

    public RichPushWidgetProvider() {
        // Start the worker thread
        workerThread = new HandlerThread("RichPushSampleInbox-Provider");
        workerThread.start();
        workerQueue = new Handler(workerThread.getLooper());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(REFRESH_ACTION)) {
            scheduleUpdate(context);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        for (int id : appWidgetIds) {
            RemoteViews layout = null;

            // API 16 and above supports reconfigurable layouts
            if (Build.VERSION.SDK_INT >= 16) {
                Bundle options = appWidgetManager.getAppWidgetOptions(id);
                layout = RemoteViewsFactory.createLayout(context, id, options);
            } else {
                layout = RemoteViewsFactory.createLayout(context, id);
            }

            appWidgetManager.updateAppWidget(id, layout);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
            AppWidgetManager appWidgetManager,
            int appWidgetId,
            Bundle newOptions) {

        RemoteViews layout = RemoteViewsFactory.createLayout(context, appWidgetId, newOptions);
        appWidgetManager.updateAppWidget(appWidgetId, layout);
    }

    /**
     * Adds a runnable to update the widgets in the worker queue
     * @param context used for creating layouts
     */
    private void scheduleUpdate(final Context context) {
        workerQueue.removeMessages(0);
        workerQueue.post(new Runnable() {
            @Override
            public void run() {
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName cn = new ComponentName(context, RichPushWidgetProvider.class);
                onUpdate(context, mgr, mgr.getAppWidgetIds(cn));

                if (Build.VERSION.SDK_INT >= 11) {
                    mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.message_list);
                }
            }
        });
    }
}