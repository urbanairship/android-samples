package com.urbanairship.richpush.sample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.urbanairship.richpush.RichPushInbox;
import com.urbanairship.richpush.RichPushInbox.MessageType;
import com.urbanairship.richpush.RichPushManager;
import com.urbanairship.richpush.RichPushMessage;

public class RichPushAdapter extends BaseAdapter implements Observer {

    public static final int EMPTY_RESOURCE_ID = -1;

    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final MessageType type;
    private final int textViewResourceId;
    private final int resourceId;
    private final LayoutInflater inflater;

    private ArrayList<RichPushMessage> messages;

    public RichPushAdapter(Context context, int textViewResourceId) {
        this(context, EMPTY_RESOURCE_ID, textViewResourceId, MessageType.ALL);
    }

    public RichPushAdapter(Context context, int resource, int textViewResourceId) {
        this(context, resource, textViewResourceId, MessageType.ALL);
    }

    @SuppressWarnings("unchecked")
    public RichPushAdapter(Context context, int resource, int textViewResourceId, MessageType type) {
        super();
        this.resourceId = resource;
        this.textViewResourceId = textViewResourceId;
        this.type = type;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.messages = (ArrayList<RichPushMessage>) this.getMessages().clone();
        this.addInboxObserver();
        DATE_FORMATTER.setTimeZone(TimeZone.getDefault());
    }

    @Override
    public int getCount() {
        return this.messages == null ? 0 : this.messages.size();
    }

    @Override
    public Object getItem(int position) {
        return this.messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View newView = this.getOrReuseView(convertView);
        this.createViewHolderIfAbsent(newView);
        return this.populateView(newView, this.messages.get(position));
    }

    // setters

    public void setDateFormat(SimpleDateFormat dateFormat) {
        DATE_FORMATTER = dateFormat;
    }

    // actions

    public void markRead(int position) {
        RichPushMessage message = ((RichPushMessage)this.getItem(position));
        this.getInbox().getMessage(message.getMessageId()).markRead();
        message.markRead();
        this.notifyDataSetChanged();
    }

    public void markUnread(int position) {
        RichPushMessage message = ((RichPushMessage)this.getItem(position));
        this.getInbox().getMessage(message.getMessageId()).markUnread();
        message.markUnread();
        this.notifyDataSetChanged();
    }

    public void delete(int position) {
        RichPushMessage message = ((RichPushMessage)this.getItem(position));
        this.getInbox().getMessage(message.getMessageId()).delete();
        this.messages.remove(message);
        this.notifyDataSetChanged();
    }

    public void save() {
        this.getInbox().save();
    }

    public void cleanup() {
        this.save();
        this.getInbox().deleteObserver(this);
    }

    // helpers

    private ArrayList<RichPushMessage> getMessages() {
        switch (this.type) {
            case READ:
                return (ArrayList<RichPushMessage>) this.getInbox().getUnreadMessages();
            case UNREAD:
                return (ArrayList<RichPushMessage>) this.getInbox().getReadMessages();
            case ALL:
            default:
                return (ArrayList<RichPushMessage>) this.getInbox().getMessages();
        }
    }

    private View populateView(View view, RichPushMessage message) {
        if (message != null) {
            ViewHolder holder = (ViewHolder) view.getTag();
            if (!message.isRead() && holder.unreadIndicator != null) {
                holder.unreadIndicator.setBackgroundColor(Color.YELLOW);
            } else if (holder.unreadIndicator != null) {
                holder.unreadIndicator.setBackgroundColor(Color.BLACK);
            }
            if (holder.checkbox != null) holder.checkbox.setChecked(false);
            if (holder.title != null) holder.title.setText(message.getTitle());
            if (holder.message != null) holder.message.setText(message.getMessage());
            if (holder.date != null) holder.date.setText(DATE_FORMATTER.format(message.getSentDate()));
        }
        return view;
    }

    private View getOrReuseView(View convertView) {
        if (convertView != null) {
            return convertView;
        } else if (this.resourceId != EMPTY_RESOURCE_ID) {
            View rootView = this.inflater.inflate(this.resourceId, null);
            return rootView.findViewById(this.textViewResourceId);
        } else {
            return this.inflater.inflate(this.textViewResourceId, null);
        }
    }

    private void addInboxObserver() {
        RichPushManager.shared().getInbox().addObserver(this);
    }

    private RichPushInbox getInbox() {
        return RichPushManager.shared().getInbox();
    }

    private void createViewHolderIfAbsent(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            View tempView = this.findAndSetUpView(view, R.id.message_checkbox);
            if (tempView != null)
                holder.checkbox = (CheckBox) tempView;
            tempView = this.findAndSetUpView(view, R.id.unread_indicator);
            if (tempView != null)
                holder.unreadIndicator = (LinearLayout) tempView;
            tempView = this.findAndSetUpView(view, R.id.title);
            if (tempView != null)
                holder.title = (TextView) tempView;
            tempView = this.findAndSetUpView(view, R.id.message);
            if (tempView != null)
                holder.message = (TextView) tempView;
            tempView = this.findAndSetUpView(view, R.id.date_sent);
            if (tempView != null)
                holder.date = (TextView) tempView;
            view.setTag(holder);
        }
    }

    private View findAndSetUpView(View view, int id) {
        View childView = view.findViewById(id);
        if (childView != null) {
            childView.setFocusable(false);
            childView.setFocusableInTouchMode(false);
        }
        return childView;
    }

    static class ViewHolder {
        CheckBox checkbox;
        LinearLayout unreadIndicator;
        TextView title;
        TextView message;
        TextView date;
    }

    @Override
    public void update(Observable observable, Object data) {
        this.messages = this.getMessages();
        this.notifyDataSetChanged();
    }

}
