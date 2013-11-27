package com.urbanairship.automatorutils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class RichPushSender extends PushSender {

    private static final String RICH_PUSH_BROADCAST_URL = "https://go.urbanairship.com/api/airmail/send/broadcast/";
    private static final String RICH_PUSH_URL = "https://go.urbanairship.com/api/airmail/send/";

    /**
     * Constructor for RichPushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     * @param appName Name of the application
     */
    public RichPushSender(String masterSecret, String appKey) {
        super(masterSecret, appKey, RICH_PUSH_BROADCAST_URL, RICH_PUSH_URL);
    }

    @Override
    protected String createMessage(String recipientString, String recipientValueString, Map<String, String> extras, String uniqueAlertId) throws JSONException {
        JSONObject jsonPayload = new JSONObject();
        JSONObject jsonPush = new JSONObject();
        if (recipientString != null) {
            JSONArray jsonPushArray = new JSONArray();
            jsonPushArray.put(recipientValueString);
            jsonPayload.put(recipientString, jsonPushArray);
        }
        JSONObject jsonAlert = new JSONObject();
        jsonAlert.put("alert", uniqueAlertId);
        if (extras != null) {
            JSONObject jsonExtras = new JSONObject(extras);
            jsonAlert.put("extra", jsonExtras);
        }
        jsonPush.put("android", jsonAlert);
        jsonPayload.put("push", jsonPush);
        jsonPayload.put("title", "Rich Push " + uniqueAlertId);
        jsonPayload.put("message", "Rich Push Message " + uniqueAlertId);
        jsonPayload.put("content-type", "text/html");

        return jsonPayload.toString();
    }

    /**
     * Sends a rich push message to a user
     * @param user The specified user id to send the rich push message to
     * @return A unique alert Id
     * @throws Exception
     */
    public String sendRichPushToUser(String user) throws Exception {
        Log.i(TAG, "Send message to user: " + user);
        return sendMessage(RICH_PUSH_URL, "users", user, null);
    }

    /**
     * Sends a push message to an APID
     * @param apid The specified apid to send the push message to
     * @return A unique alert Id
     * @throws Exception
     */
    @Override
    public String sendPushToApid(String apid) throws Exception {
        Log.i(TAG, "Send message to apid: " + apid);
        JSONObject jsonAudience = new JSONObject();
        jsonAudience.put("apid", apid);
        return sendMessage(RICH_PUSH_URL, "audience", jsonAudience.toString(), null);
    }
}
