/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import com.urbanairship.richpush.RichPushMessage;

import java.util.List;

public class RichPushMessageUtils {

    public static int getMessagePosition(String messageId, List<RichPushMessage> messages) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageId().equals(messageId)) {
                return i;
            }
        }
        return 0;
    }
}
