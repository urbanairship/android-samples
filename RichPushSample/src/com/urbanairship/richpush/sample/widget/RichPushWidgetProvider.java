/*
Copyright 2009-2015 Urban Airship Inc. All rights reserved.


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
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.urbanairship.UAirship;
import com.urbanairship.richpush.sample.MainActivity;
import com.urbanairship.richpush.sample.R;

/**
 * The widget provider for the rich push inbox
 */
public class RichPushWidgetProvider extends AppWidgetProvider {

    private static int LARGE_LAYOUT_MIN_HEIGHT = 100;

    @SuppressLint("NewApi")
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Update each of the widgets with the remote adapter
        for (int id : appWidgetIds) {
            RemoteViews remoteViews = createLayout(context, appWidgetManager, id);
            appWidgetManager.updateAppWidget(id, remoteViews);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        RemoteViews remoteViews;
        if (newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) >= LARGE_LAYOUT_MIN_HEIGHT) {
            remoteViews = createLargeLayout(context, appWidgetId);
        } else {
            remoteViews = createSmallLayout(context);
        }

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    /**
     * Helper method to trigger a widget refresh.
     *
     * @param context The application context.
     */
    @SuppressLint("NewApi")
    public static void refreshAppWidgets(Context context) {

        // Get all the widget ids for this provider
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, RichPushWidgetProvider.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);

        if (Build.VERSION.SDK_INT >= 11) {
            // Calling notifyAppWidgetViewDataChanged will also refresh the remote widget service
            widgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.message_list);
        } else {
            // Send an update broadcast
            Intent update = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);

            context.sendBroadcast(update);
        }
    }


    /**
     * Creates the widget layout.
     * <p/>
     * The small layout that only shows the unread count will be created on Gingerbread (10) or
     * older devices, or devices running Jelly Bean (16) who configured the widget with a height
     * smaller than {@link #LARGE_LAYOUT_MIN_HEIGHT}. For all other devices, the large layout with
     * a list view of message center messages will be created.
     *
     * @param context Application context
     * @param appWidgetManager The app
     * @param appWidgetId Id of the widget
     * @return RemoteViews for the layout
     */
    @SuppressLint("NewApi")
    private RemoteViews createLayout(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // API 16 and above supports reconfigurable layouts
        if (Build.VERSION.SDK_INT >= 16) {
            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

            if (options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) >= LARGE_LAYOUT_MIN_HEIGHT) {
                return createLargeLayout(context, appWidgetId);
            }

            return createSmallLayout(context);
        }


        // Show the large layout on honeycomb
        if (Build.VERSION.SDK_INT >= 11) {
            return createLargeLayout(context, appWidgetId);
        }

        // Fallback to small layout on older devices
        return createSmallLayout(context);
    }

    /**
     * Creates a large layout for the app widget
     * <p/>
     * This layout is only supported in SDK >= 11 (Honeycomb)
     *
     * @param context Application context
     * @param appWidgetId id of the widget
     * @return RemoteViews for the large layout
     */
    @SuppressLint("NewApi")
    private RemoteViews createLargeLayout(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Specify the service to provide data for the collection widget.  Note that we need to
        // embed the appWidgetId via the data otherwise it will be ignored.
        Intent intent = new Intent(context, RichPushWidgetService.class)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        // Using deprecated setRemoteAdapter to support API 11+
        remoteViews.setRemoteAdapter(appWidgetId, R.id.message_list, intent);

        // Set the empty view to be displayed if the collection is empty.  It must be a sibling
        // view of the collection view.
        remoteViews.setEmptyView(R.id.message_list, R.id.empty_view);

        // Bind a click listener template for the contents of the message list
        remoteViews.setPendingIntentTemplate(R.id.message_list, createMessageTemplateIntent(context, appWidgetId));

        // Add a click pending intent to launch the inbox
        remoteViews.setOnClickPendingIntent(R.id.widget_header, createMainActivityPendingIntent(context));

        return remoteViews;
    }

    /**
     * Creates a small layout for the app widget
     *
     * @param context Application context
     * @return RemoteViews for the small layout
     */
    private RemoteViews createSmallLayout(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);

        // Update the header for the current unread message count
        int count = UAirship.shared().getRichPushManager().getRichPushInbox().getUnreadCount();
        String header = context.getString(R.string.header_format_string, count);

        remoteViews.setTextViewText(R.id.widget_header_text, header);

        // Add a click pending intent to launch the inbox
        remoteViews.setOnClickPendingIntent(R.id.widget_header, createMainActivityPendingIntent(context));

        return remoteViews;
    }

    /**
     * Creates a pending activity intent to launch the main activity to the inbox.
     *
     * @param context Application context
     * @return Pending activity intent
     */
    private PendingIntent createMainActivityPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(MainActivity.EXTRA_NAVIGATE_ITEM, MainActivity.INBOX_ITEM);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Creates a pending broadcast intent as a template
     * for each message in the app widget
     *
     * @param context Application context
     * @param appWidgetId Id of the widget
     * @return Pending broadcast intent
     */
    private PendingIntent createMessageTemplateIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                .putExtra(MainActivity.EXTRA_NAVIGATE_ITEM, MainActivity.INBOX_ITEM);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}