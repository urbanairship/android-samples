/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.richpush.sample.RichPushMessageAdapter.ViewBinder;

import java.text.SimpleDateFormat;

/**
 * Sample implementation of the InboxFragment
 *
 */
public class RichPushSampleInboxFragment extends InboxFragment {

    private static final SimpleDateFormat UA_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public int getRowLayoutId() {
        return R.layout.inbox_message;
    }

    @Override
    public int getEmptyListStringId() {
        return R.string.no_messages;
    }

    @Override
    protected ViewBinder getMessageBinder() {
        return new RichPushMessageAdapter.ViewBinder() {

            @Override
            public void bindView(View view, final RichPushMessage message) {
                View unreadIndicator = view.findViewById(R.id.unread_indicator);
                TextView title = (TextView) view.findViewById(R.id.title);
                TextView timeStamp = (TextView) view.findViewById(R.id.date_sent);
                final CheckBox checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);

                if (message.isRead()) {
                    unreadIndicator.setBackgroundColor(Color.BLACK);
                    unreadIndicator.setContentDescription("Message is read");
                } else {
                    unreadIndicator.setBackgroundColor(Color.YELLOW);
                    unreadIndicator.setContentDescription("Message is unread");
                }

                title.setText(message.getTitle());
                timeStamp.setText(UA_DATE_FORMATTER.format(message.getSentDate()));

                checkBox.setChecked(isMessageSelected(message.getMessageId()));

                checkBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onMessageSelected(message.getMessageId(), checkBox.isChecked());
                    }
                });
                view.setFocusable(false);
                view.setFocusableInTouchMode(false);
            }
        };
    }
}
