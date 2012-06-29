package com.urbanairship.richpush.sample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

public class InboxActivity extends FragmentActivity implements InboxFragment.OnMessageListener {

    private static final String STUB_MESSAGE_JSON_STRING =
            "{\"unread\": true," +
                    "\"message_sent\": \"2010-09-05 12:13 -0000\"," +
                    "\"title\": \"Message title\"," +
                    "\"message\": \"Your full message here.\"," +
                    "\"message_body_url\": \"https://go.urbanairship.com/api/user/some_user_id/messages/message_id/body/\"," +
                    "\"message_read_url\": \"https://go.urbanairship.com/api/user/some_user_id/messages/message_id/read/\"," +
                    "\"extra\": {\"some_key\": \"some_value\"}," +
                    "\"content_type\": \"text/html\"," +
                    "\"content_size\": \"128\"}";

    private static final String[] TITLES = new String[] { "Free Stuff", "Your Tickets", "Urgent!", "Welcome",
    "This is a really, really long title that should marquee without fucking stuff up." };
    private static final String[] MESSAGES = new String[] { "We know you like free stuff, so here you go.",
        "Here are your tickets to tonight's show.", "We can't charge your account. Please act now or we will" +
                " have to interrupt your service.", "Welcome to the Rich Push Sample App!", "Short message." };
    private static final SimpleDateFormat UA_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");

    Button markMessagesReadButton;
    Button markMessagesUnreadButton;
    Button deleteMessagesButton;
    Button addMessageButton;
    InboxFragment inbox;
    Random generator = new Random();
    RichPushAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);

        markMessagesReadButton = (Button) this.findViewById(R.id.mark_messages_read);
        markMessagesUnreadButton = (Button) this.findViewById(R.id.mark_messages_unread);
        deleteMessagesButton = (Button) this.findViewById(R.id.delete_messages);
        addMessageButton = (Button) this.findViewById(R.id.add_message);

        inbox = (InboxFragment) this.getSupportFragmentManager().findFragmentById(R.id.inbox);
        inbox.setOnMessageListener(this);

        adapter = (RichPushAdapter) inbox.getListAdapter();

        markMessagesReadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int numMessages = inbox.getListView().getChildCount();
                for (int i = 0; i < numMessages; i++) {
                    View messageView = inbox.getListView().getChildAt(i);
                    if (((CheckBox) messageView.findViewById(R.id.message_checkbox)).isChecked()) {
                        getListAdapter().markRead(i);
                    }
                }
            }

        });

        markMessagesUnreadButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int numMessages = inbox.getListView().getChildCount();
                for (int i = 0; i < numMessages; i++) {
                    View messageView = inbox.getListView().getChildAt(i);
                    if (((CheckBox) messageView.findViewById(R.id.message_checkbox)).isChecked()) {
                        getListAdapter().markUnread(i);
                    }
                }
            }

        });

        deleteMessagesButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int numMessages = inbox.getListView().getChildCount();
                for (int i = 0; i < numMessages; i++) {
                    View messageView = inbox.getListView().getChildAt(i);
                    if (((CheckBox) messageView.findViewById(R.id.message_checkbox)).isChecked()) {
                        getListAdapter().delete(i);
                    }
                }
            }

        });

        addMessageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                loadMessage();
            }

        });
    }

    @Override
    public void onMessageSelected(RichPushMessage message) {
        message.markRead();

        // TODO We should have a check here that sees if we have a big screen and
        // want to display multiple fragments. For now, start a new activity.
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(MessageActivity.EXTRA_URL_KEY, "http://www.google.com");
        this.startActivity(intent);
    }

    // helpers

    private RichPushAdapter getListAdapter() {
        if (this.adapter == null) this.adapter = (RichPushAdapter) this.inbox.getListAdapter();
        return this.adapter;
    }

    private void loadMessage() {
        JSONObject messageJson = null;
        try {
            messageJson = new JSONObject(STUB_MESSAGE_JSON_STRING);
            int random = generator.nextInt(TITLES.length);
            messageJson.put("title", TITLES[random]);
            messageJson.put("message", MESSAGES[random]);
            messageJson.put("message_sent", UA_DATE_FORMATTER.format(new Date()));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        RichPushManager.deliverPush(System.currentTimeMillis() + "_message_id", messageJson);
    }

}