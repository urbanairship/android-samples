package com.urbanairship.richpush.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import com.urbanairship.UrbanAirshipProvider;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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
		"This is a really, really long title that should marquee without messing stuff up." };
    private static final String[] MESSAGES = new String[] { "We know you like free stuff, so here you go.",
        "Here are your tickets to tonight's show.", "We can't charge your account. Please act now or we will" +
		" have to interrupt your service.", "Welcome to the Rich Push Sample App!", "Short message." };
    private static final SimpleDateFormat UA_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");

    Random generator = new Random();
	final Set<String> checkedIds = new HashSet<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.main);

        ((RichPushSampleInboxFragment) this.getSupportFragmentManager().findFragmentById(
				R.id.inbox)).setViewBinder(new MessageBinder());

        this.findViewById(R.id.mark_messages_read).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				RichPushManager.shared().getRichPushUser().getInbox()
                        .markMessagesRead(InboxActivity.this.checkedIds);
				InboxActivity.this.checkedIds.clear();
            }
        });

        this.findViewById(R.id.mark_messages_unread).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				RichPushManager.shared().getRichPushUser().getInbox()
                        .markMessagesUnread(InboxActivity.this.checkedIds);
				InboxActivity.this.checkedIds.clear();
            }
        });

        this.findViewById(R.id.delete_messages).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
				RichPushManager.shared().getRichPushUser().getInbox()
                        .deleteMessages(InboxActivity.this.checkedIds);
				InboxActivity.this.checkedIds.clear();
            }
        });

        this.findViewById(R.id.add_message).setOnClickListener(new OnClickListener() {
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

    private void loadMessage() {
        JSONObject messageJson;
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

	// inner-classes

	class MessageBinder implements RichPushCursorAdapter.ViewBinder {

		@Override
		public void setViewValue(View view, RichPushMessage message, String columnName) {
			if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_UNREAD)) {
				view.setBackgroundColor(message.isRead() ? Color.BLACK : Color.YELLOW);
			} else if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_MESSAGE)) {
				((TextView) view).setText(message.getMessage());
			} else if (columnName.equals(UrbanAirshipProvider.RichPush.COLUMN_NAME_TITLE)) {
				((TextView) view).setText(message.getTitle());
			} else if (columnName.equals(UrbanAirshipProvider.COLUMN_NAME_TIMESTAMP)) {
				((TextView) view).setText(UA_DATE_FORMATTER.format(message.getSentDate()));
			} else {
				view.setOnClickListener(InboxActivity.this.checkBoxListener);
				view.setTag(message.getMessageId());
				if (InboxActivity.this.checkedIds.contains(message.getMessageId())) {
					((CheckBox)view).setChecked(true);
				} else {
					((CheckBox)view).setChecked(false);
				}
			}
			view.setFocusable(false);
			view.setFocusableInTouchMode(false);
		}

	}

	OnClickListener checkBoxListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (((CheckBox)view).isChecked()) {
				InboxActivity.this.checkedIds.add((String) view.getTag());
			} else {
				InboxActivity.this.checkedIds.remove((String) view.getTag());
			}
		}

	};

}