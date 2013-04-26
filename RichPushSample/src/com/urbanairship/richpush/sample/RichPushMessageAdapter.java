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

public class RichPushMessageAdapter extends ArrayAdapter<RichPushMessage> {

    int layout;
    ViewBinder binder;
    SparseArray<String> mapping;
    private List<RichPushMessage> messages;

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
        View view = convertView == null ? createView(parent) : convertView;
        RichPushMessage message = this.getItem(position);

        if (message == null) {
            Logger.error("Message at " + position + " is null!");
            return view;
        }

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

    public void setViewBinder(ViewBinder binder) {
        this.binder = binder;
    }

    public void setMessages(List<RichPushMessage> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        this.notifyDataSetChanged();
    }


    // interfaces
    public static interface ViewBinder {
        void setViewValue(View view, RichPushMessage message, String columnName);
    }

}
