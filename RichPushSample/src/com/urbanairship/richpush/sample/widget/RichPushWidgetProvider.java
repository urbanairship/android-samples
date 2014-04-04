/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.urbanairship.richpush.sample.widget;

import android.annotation.SuppressLint;
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

    @SuppressLint("NewApi")
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        for (int id : appWidgetIds) {
            RemoteViews layout;

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
    @SuppressLint("NewApi")
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