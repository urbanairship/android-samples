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
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RichPushWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

/**
 * This is the factory that will provide data to the collection widget.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
        return RichPushManager.shared().getRichPushInbox().getCount();
    }

    @Override
    @SuppressLint("NewApi")
    public RemoteViews getViewAt(int position) {

        List<RichPushMessage> messages = RichPushManager.shared().getRichPushInbox().getMessages();

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

        int iconDrawable = message.isRead() ? R.drawable.ic_mark_read : R.drawable.ic_mark_unread;
        rv.setImageViewResource(R.id.widget_item_icon, iconDrawable);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        rv.setTextViewText(R.id.date_sent, dateFormat.format(message.getSentDate()));

        // Add the message id to the intent
        Intent fillInIntent = new Intent();
        Bundle extras = new Bundle();
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
