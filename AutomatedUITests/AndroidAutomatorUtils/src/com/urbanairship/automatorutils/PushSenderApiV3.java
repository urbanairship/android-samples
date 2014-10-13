/*
 * Copyright 2013 Urban Airship
 */

package com.urbanairship.automatorutils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to send push notifications
 *
 */
public class PushSenderApiV3 extends PushSender {
    private static final String PUSH_URL = "https://go.urbanairship.com/api/push/";
    private HashMap<String, String> requestProperties = new HashMap<String, String>();

    /**
     * Constructor for PushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     */
    public PushSenderApiV3(String masterSecret, String appKey) {
        super(masterSecret, appKey, PUSH_URL);
        requestProperties.put("Accept", "application/vnd.urbanairship+json; version=3;");
    }

    @Override
    protected String createMessage(String recipientString, String recipientValueString, Map<String, String> extras, String uniqueAlertId) throws JSONException {
        JSONObject jsonPayload = new JSONObject();
        if (recipientValueString.equalsIgnoreCase("all")) {
            jsonPayload.put(recipientString, recipientValueString);
        } else {
            JSONObject jsonAudience = new JSONObject();

            JSONObject jsonAudienceType = new JSONObject(recipientValueString);
            JSONArray namesArray = jsonAudienceType.names();
            String name = namesArray.getString(0);
            jsonAudience.put(name, jsonAudienceType.get(name));

            jsonPayload.put("audience", jsonAudience);
        }

        JSONArray jsonDeviceType = new JSONArray();
        jsonDeviceType.put("android");
        jsonPayload.put("device_types", jsonDeviceType);

        JSONObject jsonNotification = new JSONObject();
        jsonNotification.put("alert", uniqueAlertId);
        jsonPayload.put("notification", jsonNotification);

        return jsonPayload.toString();
    }
    /**
     * Broadcast a push message
     * @return A unique alert Id
     * @throws Exception
     */
    @Override
    public String sendPushMessage() throws Exception {
        Log.i(TAG, "Broadcast message");
        return sendMessage(PUSH_URL, "audience", "all", null, requestProperties);
    }

    /**
     * Sends a push message to an activity
     * @param extras Any notification extras
     * @return A unique alert Id
     * @throws Exception
     */
    @Override
    public String sendPushMessage(Map<String, String> extras) throws Exception {
        Log.i(TAG, "Broadcast message: to activity");
        return sendMessage(PUSH_URL, "audience", "all", extras, requestProperties);
    }

    /**
     * Sends a push message to a tag
     * @param tag The specified tag to send the push message to
     * @return A unique alert Id
     * @throws Exception
     */
    @Override
    public String sendPushToTag(String tag) throws Exception {
        Log.i(TAG, "Send message to tag: " + tag);
        JSONObject jsonAudience = new JSONObject();
        jsonAudience.put("tag", tag);
        return sendMessage(PUSH_URL, "audience", jsonAudience.toString(), null, requestProperties);
    }

    /**
     * Sends a push message to an alias
     * @param alias The specified alias to send the push message to
     * @return A unique alert Id
     * @throws Exception
     */
    @Override
    public String sendPushToAlias(String alias) throws Exception {
        Log.i(TAG, "Send message to alias: " + alias);
        JSONObject jsonAudience = new JSONObject();
        jsonAudience.put("alias", alias);
        return sendMessage(PUSH_URL, "audience", jsonAudience.toString(), null, requestProperties);
    }

    /**
     * Sends a push message to a Channel ID
     * @param channelId The specified channel ID to send the push message to
     * @return A unique alert ID
     * @throws Exception
     */
    public String sendPushToChannelId(String channelId) throws Exception {
        Log.i(TAG, "Send message to channel ID: " + channelId);
        JSONObject jsonAudience = new JSONObject();
        jsonAudience.put("android_channel", channelId);
        return sendMessage(PUSH_URL, "audience", jsonAudience.toString(), null, requestProperties);
    }
}
