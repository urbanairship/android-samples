/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.richpush.sample;

import com.urbanairship.richpush.RichPushMessage;

import java.util.List;

/**
 *  Rich push message utilities
 */
public class RichPushMessageUtils {

    /**
     * Gets a message's position in a list of messages
     * @param messageId The message's id of the requested message position
     * @param messages List of messages to look through
     * @return message's position if found, 0 if not
     */
    public static int getMessagePosition(String messageId, List<RichPushMessage> messages) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getMessageId().equals(messageId)) {
                return i;
            }
        }
        return 0;
    }
}
