package com.urbanairship.richpush.sample;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import com.urbanairship.Logger;
import com.urbanairship.richpush.RichPushManager;

public class RichPushCursorLoader extends CursorLoader {

    public RichPushCursorLoader(Context context) {
        super(context);
    }

    @Override
    protected Cursor onLoadInBackground() {
        Cursor cursor = RichPushManager.shared().getRichPushUser().getInbox().refresh();
        Logger.verbose("Loaded " + cursor.getCount() + " messages in the background");
        return cursor;
    }
}
