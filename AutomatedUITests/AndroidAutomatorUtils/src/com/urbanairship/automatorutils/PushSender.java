/*
 * Copyright 2013 Urban Airship
 */

package com.urbanairship.automatorutils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.Deprecated;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Helper class to send push notifications
 *
 */
public class PushSender {
    private final String masterSecret;
    private final String appKey;
    private final String broadcastUrl;
    private final String pushUrl;

    protected static String TAG = "PushSender";

    private static int MAX_SEND_MESG_RETRIES = 3;
    private static int SEND_MESG_RETRY_DELAY = 3000;  // 3 seconds

    private static final String PUSH_BROADCAST_URL = "https://go.urbanairship.com/api/push/broadcast/";
    private static final String PUSH_URL = "https://go.urbanairship.com/api/push/";


    /**
     * Constructor for PushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     */
    public PushSender(String masterSecret, String appKey) {
        this(masterSecret, appKey, PUSH_BROADCAST_URL, PUSH_URL);
    }

    /**
     * Constructor for PushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     * @param pushUrl The URL for push messages (APIv3)
     */
    protected PushSender(String masterSecret, String appKey, String pushUrl) {
        this.masterSecret = masterSecret;
        this.appKey = appKey;
        this.broadcastUrl = pushUrl;
        this.pushUrl = pushUrl;
    }

    /**
     * Constructor for PushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     * @param broadcastUrl The URL for broadcasting push messages
     * @param pushUrl The URL for push messages
     */
    protected PushSender(String masterSecret, String appKey, String broadcastUrl, String pushUrl) {
        this.masterSecret = masterSecret;
        this.appKey = appKey;
        this.broadcastUrl = broadcastUrl;
        this.pushUrl = pushUrl;
    }

    /**
     * Builds the message to be sent
     * @param recipientString The string to append based on the type of push (user, alias, tag)
     * @param recipientValueString The value of the recipientString based on the type
     * @param activity The specified activity to send the push message to
     * @param uniqueAlertId The string used to identify push messages
     * @return The message to be sent
     * @throws JSONException
     */
    protected String createMessage(String recipientString, String recipientValueString, Map<String, String> extras, String uniqueAlertId) throws JSONException {
        JSONObject jsonPayload = new JSONObject();
        if (recipientString != null) {
            JSONArray jsonPushArray = new JSONArray();
            jsonPushArray.put(recipientValueString);
            jsonPayload.put(recipientString, jsonPushArray);
        }
        JSONObject jsonAlert = new JSONObject();
        jsonAlert.put("alert", uniqueAlertId);
        jsonPayload.put("android", jsonAlert);
        if (extras != null) {
            JSONObject jsonExtras = new JSONObject(extras);
            jsonPayload.put("extra", jsonExtras);
        }
        Log.w(TAG, jsonPayload.toString());
        return jsonPayload.toString();
    }


    /**
     * Broadcast a push message
     * @return A unique alert Id
     * @throws Exception
     */
    public String sendPushMessage() throws Exception {
        Log.i(TAG, "Broadcast message");
        return sendMessage(broadcastUrl, null, null, null);
    }

    /**
     * Sends a push message to an activity
     * @param extras Any notification extras
     * @return A unique alert Id
     * @throws Exception
     */
    public String sendPushMessage(Map<String, String> extras) throws Exception {
        Log.i(TAG, "Broadcast message: to activity");
        return sendMessage(broadcastUrl, null, null, extras);
    }

    /**
     * Sends a push message to a tag
     * @param tag The specified tag to send the push message to
     * @return A unique alert Id
     * @throws Exception
     */
    public String sendPushToTag(String tag) throws Exception {
        Log.i(TAG, "Send message to tag: " + tag);
        return sendMessage(pushUrl, "tags", tag, null);
    }

    /**
     * Sends a push message to an alias
     * @param alias The specified alias to send the push message to
     * @return A unique alert Id
     * @throws Exception
     */
    public String sendPushToAlias(String alias) throws Exception {
        Log.i(TAG, "Send message to alias: " + alias);
        return sendMessage(pushUrl, "aliases", alias, null);
    }

    /**
     * Sends a push message to an APID
     * @param apid The specified apid to send the push message to
     * @return A unique alert Id
     * @deprecated As of 5.0.0. Use sendPushToChannelId instead.
     * @throws Exception
     */
    @Deprecated
    public String sendPushToApid(String apid) throws Exception {
        Log.i(TAG, "Send message to apid: " + apid);
        return sendMessage(pushUrl, "apids", apid, null);
    }

    /**
     * Actually sends the push message
     * @param urlString The specified url the message is sent to
     * @param recipientString The specified type of push
     * @param recipientValueString The value of the recipientString based on the type
     * @param extras Any notification extras
     * @throws Exception
     */
    protected String sendMessage(String urlString, String recipientString, String recipientValueString, Map<String, String> extras) throws Exception {
        return sendMessage(urlString, recipientString, recipientValueString, extras, null);
    }

    /**
     * Actually sends the push message
     * @param urlString The specified url the message is sent to
     * @param recipientString The specified type of push
     * @param recipientValueString The value of the recipientString based on the type
     * @param extras Any notification extras
     * @param requestProperties The specified connection request property
     * @return A unique alert Id
     * @throws Exception
     */
    protected String sendMessage(String urlString, String recipientString, String recipientValueString, Map<String, String> extras, Map<String, String> requestProperties) throws Exception {
        int sendMesgRetryCount = 0;
        String uniqueAlertId = "uniqueAlertId";
        while ( sendMesgRetryCount < MAX_SEND_MESG_RETRIES ) {
            uniqueAlertId = AutomatorUtils.generateUniqueAlertId();
            String json = createMessage(recipientString, recipientValueString, extras, uniqueAlertId);
            Log.i(TAG,  "Created message to send" + json);

            try {
                sendMessageHelper(urlString, json, requestProperties);
                return uniqueAlertId;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to send message: " + json, ex);
                Thread.sleep(SEND_MESG_RETRY_DELAY);
                sendMesgRetryCount++;
            }
        }
        return uniqueAlertId;
    }

    /**
     * Actually sends the push message
     * @param urlString The specified url the message is sent to
     * @param message The json formatted message to be sent
     * @throws IOException
     */
    protected void sendMessageHelper(String urlString, String message, Map<String, String> requestProperties) throws IOException  {
        URL url = new URL(urlString);
        HttpURLConnection conn = null;

        String basicAuthString =  "Basic "+Base64.encodeToString(String.format("%s:%s", appKey, masterSecret).getBytes(), Base64.NO_WRAP);

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", basicAuthString);

            if (requestProperties != null) {
                for (String key: requestProperties.keySet()) {
                    conn.setRequestProperty(key, requestProperties.get(key));
                }
            }

            // Create the form content
            OutputStream out = conn.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(message);
            writer.close();
            out.close();

            if (conn.getResponseCode() == 200 || conn.getResponseCode() == 202) {
                Log.i(TAG, "Push sent: " + message);
            } else {
                Log.e(TAG, "Sending push failed with: " + conn.getResponseCode() + " " + conn.getResponseMessage() + " Message: " + message);
                throw new IOException(conn.getResponseMessage());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
