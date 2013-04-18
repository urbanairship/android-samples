/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.urbanairship.Logger;
import com.urbanairship.richpush.RichPushMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * ArrayAdapter for rich push messages
 *
 */
public class RichPushMessageAdapter extends ArrayAdapter<RichPushMessage> {

    int layout;
    ViewBinder binder;
    SparseArray<String> mapping;
    private List<RichPushMessage> messages;

    /**
     * Creates a new RichPushMessageAdapter
     * @param context Application context
     * @param layout The layout for the created views
     * @param mapping The mapping for message value to view
     */
    public RichPushMessageAdapter(Context context, int layout, SparseArray<String> mapping) {
        this(context, layout, new ArrayList<RichPushMessage>(), mapping);
    }

    RichPushMessageAdapter(Context context, int layout, List<RichPushMessage> messages, SparseArray<String> mapping) {
        super(context, layout, messages);
        this.layout = layout;
        this.mapping = mapping;
        this.messages = messages;
    }

    private View createView(ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(layout, parent, false);
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Use either the convertView or create a new view
        View view = convertView == null ? createView(parent) : convertView;
        RichPushMessage message = this.getItem(position);

        if (message == null) {
            Logger.error("Message at " + position + " is null!");
            return view;
        }

        // Populate the views data with the rich push messsage
        int count = this.mapping.size();
        for (int i = 0; i < count; i++) {
            int key = this.mapping.keyAt(i);
            View toView = view.findViewById(key);
            if (toView != null) {
                this.binder.setViewValue(toView, message, this.mapping.get(key));
            }
        }

        return view;
    }

    /**
     * Sets the view binder
     * @param binder The specified view binder
     */
    public void setViewBinder(ViewBinder binder) {
        this.binder = binder;
    }

    /**
     * Sets the current list of rich push messages and notifies data set changed
     *
     * Must be called on the ui thread
     *
     * @param messages Current list of rich push messages
     */
    public void setMessages(List<RichPushMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        this.notifyDataSetChanged();
    }

    /**
     * View binder interface
     *
     */
    public static interface ViewBinder {
        void setViewValue(View view, RichPushMessage message, String columnName);
    }
}
