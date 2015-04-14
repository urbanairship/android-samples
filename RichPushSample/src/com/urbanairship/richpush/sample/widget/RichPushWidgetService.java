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

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.urbanairship.UAirship;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.MainActivity;
import com.urbanairship.richpush.sample.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * This is the service that provides the factory to be bound to the collection service.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class RichPushWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MessageRemoteViewFactory();
    }

    /**
     * The Rich Push Message RemoteView factory
     */
    private class MessageRemoteViewFactory implements RemoteViewsFactory {
        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return UAirship.shared().getRichPushManager().getRichPushInbox().getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {

            List<RichPushMessage> messages = UAirship.shared().getRichPushManager().getRichPushInbox().getMessages();

            if (position > messages.size()) {
                return null;
            }

            // Get the data for this position from the content provider
            RichPushMessage message = messages.get(position);

            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_item);
            remoteViews.setTextViewText(R.id.title, message.getTitle());

            int iconDrawable = message.isRead() ? R.drawable.ic_mark_read : R.drawable.ic_mark_unread;
            remoteViews.setImageViewResource(R.id.icon, iconDrawable);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            remoteViews.setTextViewText(R.id.date_sent, dateFormat.format(message.getSentDate()));

            // Fill the intent to launch to the message id
            Intent fillInIntent = new Intent().putExtra(MainActivity.EXTRA_MESSAGE_ID, message.getMessageId());

            remoteViews.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
