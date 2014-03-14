
/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * A generic base adapter that binds items to views using the ViewBinder interface.
 * @param <T> The item type in the adapter.
 */
public class ViewBinderArrayAdapter<T> extends BaseAdapter {

    /**
     * View binder interface
     */
    public static interface ViewBinder<T> {
        void bindView(View view, T item, int position);
    }

    private final Object listLock = new Object();

    private final ViewBinder<T> viewBinder;
    private final List<T> items;
    private final Context context;
    private final int layout;

    /**
     * Creates a ViewBinder
     * @param context The application context
     * @param layout The layout for each line item
     * @param viewBinder The view binder interface that is used to bind items a view
     */
    public ViewBinderArrayAdapter(Context context, int layout, ViewBinder<T> viewBinder) {
        this.context = context;
        this.layout = layout;
        this.viewBinder = viewBinder;
        this.items = new ArrayList<T>();
    }


    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(layout, parent, false);
        }

        viewBinder.bindView(view, items.get(position), position);

        return view;
    }

    /**
     * Adds an item to the adapter at a specific index.
     * @param index The index to add the item
     * @param item The item to add
     */
    public void add(int index, T item) {
        synchronized (listLock) {
            items.add(index, item);
        }

        notifyDataSetChanged();
    }

    /**
     * Adds an item to the adapter.
     * @param item The item to add
     */
    public void add(T item) {
        synchronized (listLock) {
            items.add(item);
        }

        notifyDataSetChanged();
    }

    /**
     * Removes a single item in the adapter.
     * @param item The item to remove
     */
    public void remove(T item) {
        synchronized (listLock) {
            items.remove(item);
        }

        notifyDataSetChanged();
    }

    /**
     * Clears all the items in the adapter
     */
    public void clear() {
        synchronized (listLock) {
            items.clear();
        }

        notifyDataSetChanged();
    }

    /**
     * Sets the current items in the adapter to the collection.
     * @param collection Collection of items
     */
    public void set(Collection<? extends T> collection) {
        synchronized (listLock) {
            items.clear();
            items.addAll(collection);
        }
        notifyDataSetChanged();
    }

    /**
     * Adds all items in a collection to the adapter.
     * @param collection Collection of items to add to the adapter
     */
    public void addAll(Collection<? extends T> collection) {
        synchronized (listLock) {
            items.addAll(collection);
        }
        notifyDataSetChanged();
    }

}
