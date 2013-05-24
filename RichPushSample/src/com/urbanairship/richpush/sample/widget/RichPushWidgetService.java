/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.R;
import com.urbanairship.richpush.sample.RichPushApplication;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
public class RichPushWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * This is the factory that will provide data to the collection widget.
 */
class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return RichPushManager.shared().getRichPushUser().getInbox().getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        List<RichPushMessage> messages = RichPushManager.shared().getRichPushUser().getInbox().getMessages();

        if (position > messages.size()) {
            return null;
        }

        // Get the data for this position from the content provider
        RichPushMessage message = messages.get(position);

        // Return a proper item
        final String formatStr = context.getResources().getString(R.string.item_format_string);
        final int itemId = R.layout.widget_item;
        RemoteViews rv = new RemoteViews(context.getPackageName(), itemId);
        rv.setTextViewText(R.id.widget_item_text, String.format(formatStr, message.getTitle()));

        int iconDrawable = message.isRead() ? R.drawable.mark_read : R.drawable.mark_unread;
        rv.setImageViewResource(R.id.widget_item_icon, iconDrawable);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        rv.setTextViewText(R.id.date_sent, dateFormat.format(message.getSentDate()));

        // Set the click intent so that we can handle it and show a toast message
        final Intent fillInIntent = new Intent();
        final Bundle extras = new Bundle();
        extras.putString(RichPushApplication.MESSAGE_ID_RECEIVED_KEY, message.getMessageId());
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        return rv;
    }
    @Override
    public RemoteViews getLoadingView() {
        // We aren't going to return a default loading view in this sample
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // Technically, we have two types of views (the dark and light background views)
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void onDataSetChanged() {

    }
}
