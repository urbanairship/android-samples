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

package com.urbanairship.sample.inbox;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.PopupMenu;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.urbanairship.Logger;
import com.urbanairship.richpush.RichPushMessage;
import com.urbanairship.sample.R;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Implementation of the AbstractInboxFragment that supports an action mode
 */
public class InboxFragment extends AbstractInboxFragment implements ActionMode.Callback {

    private final static long ONE_DAY_MS = 24 * 60 * 60 * 1000;

    private ActionMode actionMode;
    private Button actionSelectionButton;
    private SwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        startActionModeIfNecessary();
    }

    @Override
    public int getRowLayoutId() {
        return R.layout.inbox_list_item;
    }

    @Override
    public int getEmptyListStringId() {
        return R.string.no_messages;
    }

    private String getMessageDate(Date date) {
        long age = System.currentTimeMillis() - date.getTime();

        // Only show the date if older then 1 day
        if (age > ONE_DAY_MS) {
            return DateFormat.getDateFormat(getActivity()).format(date);
        }

        return DateFormat.getTimeFormat(getActivity()).format(date);
    }


    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inbox, null);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshMessages();
            }
        });
        return view;
    }

    public void bindView(final View view, final RichPushMessage message, final int position) {
        TextView title = (TextView) view.findViewById(R.id.title);
        TextView timeStamp = (TextView) view.findViewById(R.id.date_sent);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);

        if (message.isRead()) {
            view.setBackgroundResource(R.drawable.message_read_background);
            view.setContentDescription("Read message");
            title.setTypeface(Typeface.DEFAULT);
        } else {
            view.setBackgroundResource(R.drawable.message_unread_background);
            view.setContentDescription("Unread message");
            title.setTypeface(Typeface.DEFAULT_BOLD);
        }

        title.setText(message.getTitle());
        timeStamp.setText(getMessageDate(message.getSentDate()));

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

    @Override
    public void clearSelection() {
        super.clearSelection();
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    /**
     * Starts the action mode if there are any selected
     * messages in the inbox fragment
     */
    private void startActionModeIfNecessary() {
        List<String> checkedIds = getSelectedMessages();
        if (actionMode != null && checkedIds.isEmpty()) {
            actionMode.finish();
        } else if (actionMode == null && !checkedIds.isEmpty()) {
            actionMode = ((AppCompatActivity) this.getActivity()).startSupportActionMode(this);
        }
    }

    @Override
    protected void onManualRefreshFinished(boolean success) {
        super.onManualRefreshFinished(success);
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void refreshMessages() {
        super.refreshMessages();
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void selectAll() {
        super.selectAll();

        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    protected void onMessageSelected(String messageId, boolean isChecked) {
        super.onMessageSelected(messageId, isChecked);
        startActionModeIfNecessary();

        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);
        // If we are in actionMode, update the menu items
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.inbox_actions_menu, menu);

        View customView = LayoutInflater.from(this.getActivity()).inflate(R.layout.cab_selection_dropdown, null);
        actionSelectionButton = (Button) customView.findViewById(R.id.selection_button);

        final PopupMenu popupMenu = new PopupMenu(this.getActivity(), customView);
        popupMenu.getMenuInflater().inflate(R.menu.selection, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_deselect_all) {
                    clearSelection();
                } else {
                    selectAll();
                }
                return true;
            }
        });

        actionSelectionButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Menu menu = popupMenu.getMenu();
                menu.findItem(R.id.menu_deselect_all).setVisible(true);
                menu.findItem(R.id.menu_select_all).setVisible(getSelectedMessages().size() != getMessages().size());
                popupMenu.show();
            }

        });


        mode.setCustomView(customView);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        Logger.debug("onPrepareActionMode");

        boolean selectionContainsRead = false;
        boolean selectionContainsUnread = false;

        for (String id : getSelectedMessages()) {
            RichPushMessage message = getRichPushInbox().getMessage(id);
            if (message == null) {
                continue;
            }

            if (message.isRead()) {
                selectionContainsRead = true;
            } else {
                selectionContainsUnread = true;
            }

            if (selectionContainsRead && selectionContainsUnread) {
                break;
            }
        }

        // Show the mark read action if any of the selected messages are unread
        menu.findItem(R.id.mark_read).setVisible(selectionContainsUnread);
        
        // Show the mark unread action if any of the selected messages are read
        menu.findItem(R.id.mark_unread).setVisible(selectionContainsRead);

        // If we have an action selection_popup button update the text
        if (actionSelectionButton != null) {
            String selectionText = this.getString(R.string.cab_selection, getSelectedMessages().size());
            actionSelectionButton.setText(selectionText);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Logger.debug("onActionItemClicked");
        switch (item.getItemId()) {
            case R.id.mark_read:
                getRichPushInbox().markMessagesRead(new HashSet<>(getSelectedMessages()));
                break;
            case R.id.mark_unread:
                getRichPushInbox().markMessagesUnread(new HashSet<>(getSelectedMessages()));
                break;
            case R.id.delete:
                getRichPushInbox().deleteMessages(new HashSet<>(getSelectedMessages()));
                break;
            default:
                return false;
        }

        actionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        Logger.debug("onDestroyActionMode");
        if (actionMode != null) {
            actionMode = null;
            clearSelection();
        }
    }

    @Override
    public void onInboxUpdated() {
        super.onInboxUpdated();
        if (actionMode != null) {
            actionMode.invalidate();
        }
    }
}
