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
package com.urbanairship.sample;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.ActionMode;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.urbanairship.richpush.InboxMultiChoiceModeListener;
import com.urbanairship.richpush.InboxFragment;
import com.urbanairship.richpush.InboxViewAdapter;
import com.urbanairship.richpush.RichPushMessage;

import java.util.Date;

public class CustomInboxFragment extends InboxFragment {
    private final static long ONE_DAY_MS = 24 * 60 * 60 * 1000;

    protected InboxViewAdapter createMessageViewAdapter() {
        return new CustomViewAdapter(getContext());
    }

    @Override
    protected void onAbsListViewCreated(AbsListView absListView) {
        // Enable selection
        absListView.setMultiChoiceModeListener(new InboxMultiChoiceModeListener(this) {

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                super.onItemCheckedStateChanged(mode, position, id, checked);

                // Notify the data set changed to update the checkbox
                getAdapter().notifyDataSetChanged();
            }
        });

        absListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
    }

    private class CustomViewAdapter extends InboxViewAdapter {
        public CustomViewAdapter(Context context) {
            super(context, R.layout.item_mc);
        }

        protected void bindView(View view, RichPushMessage message, final int position) {
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView timeStamp = (TextView) view.findViewById(R.id.date_sent);
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.message_checkbox);

            if (message.isRead()) {
                view.setContentDescription("Read message");
                title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            } else {
                view.setContentDescription("Unread message");
                title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }

            title.setText(message.getTitle());

            Date date = message.getSentDate();
            long age = System.currentTimeMillis() - date.getTime();

            // Only show the date if older then 1 day
            if (age > ONE_DAY_MS) {
                timeStamp.setText(DateFormat.getDateFormat(getActivity()).format(date));
            } else {
                timeStamp.setText(DateFormat.getTimeFormat(getActivity()).format(date));
            }

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getAbsListView().setItemChecked(position, checkBox.isChecked());
                }
            });

            checkBox.setChecked(getAbsListView().isItemChecked(position));
        }
    }

}
