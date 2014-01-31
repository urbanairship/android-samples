/*
 * Copyright 2014 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.widget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.richpush.sample.inbox.InboxActivity;

/**
 * Factory class to create remote views for the widget layouts
 *
 */
@SuppressLint("NewApi")
class RemoteViewsFactory {


    /**
     * Creates a layout depending on the app widgets options
     * 
     * @param context Application context
     * @param appWidgetId Id of the widget
     * @param options Widgets options
     * @return RemoteViews for the layout
     */
    static RemoteViews createLayout(Context context, int appWidgetId, Bundle options) {
        boolean isLargeLayout =  options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) >= 100;

        return isLargeLayout ?  RemoteViewsFactory.createLargeLayout(context, appWidgetId) :
            RemoteViewsFactory.createSmallLayout(context, appWidgetId);
    }

    /**
     * Creates a layout depending on the current sdk version
     * 
     * 
     * @param context Application context
     * @param appWidgetId Id of the widget
     * @return RemoteViews for the layout
     */
    static RemoteViews createLayout(Context context, int appWidgetId) {
        // Only in api >= 11 (Honeycomb) can we support the large layout because we depend on
        // the remote view service.
        return (Build.VERSION.SDK_INT >= 11) ?  RemoteViewsFactory.createLargeLayout(context, appWidgetId) :
            RemoteViewsFactory.createSmallLayout(context, appWidgetId);
    }

    /**
     * Creates a large layout for the app widget
     * 
     * This layout is only supported in SDK >= 11 (Honeycomb)
     * 
     * @param context Application context
     * @param appWidgetId id of the widget
     * @return RemoteViews for the large layout
     */
    private static RemoteViews createLargeLayout(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        // Specify the service to provide data for the collection widget.  Note that we need to
        // embed the appWidgetId via the data otherwise it will be ignored.
        Intent intent = new Intent(context, RichPushWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        remoteViews.setRemoteAdapter(appWidgetId, R.id.message_list, intent);

        // Set the empty view to be displayed if the collection is empty.  It must be a sibling
        // view of the collection view.
        remoteViews.setEmptyView(R.id.message_list, R.id.empty_view);

        // Bind a click listener template for the contents of the message list
        remoteViews.setPendingIntentTemplate(R.id.message_list, createMessageTemplateIntent(context, appWidgetId));

        // Add a click pending intent to launch the inbox
        remoteViews.setOnClickPendingIntent(R.id.widget_header, createInboxActivityPendingIntent(context));

        return remoteViews;
    }

    /**
     * Creates a small layout for the app widget
     * 
     * 
     * @param context Application context
     * @param appWidgetId id of the widget
     * @return RemoteViews for the small layout
     */
    private static RemoteViews createSmallLayout(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);

        // Update the header for the current unread message count
        int count = RichPushManager.shared().getRichPushUser().getInbox().getUnreadCount();
        String header = context.getString(R.string.header_format_string, count);

        remoteViews.setTextViewText(R.id.widget_header_text, header);

        // Add a click pending intent to launch the inbox
        remoteViews.setOnClickPendingIntent(R.id.widget_header, createInboxActivityPendingIntent(context));

        return remoteViews;
    }

    /**
     * Creates an pending activity intent to launch the inbox
     * @param context Application context
     * @return Pending inbox activity intent
     */
    private static PendingIntent createInboxActivityPendingIntent(Context context) {
        Intent intent = new Intent(context, InboxActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
    private static PendingIntent createMessageTemplateIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, InboxActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
