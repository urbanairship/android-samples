package com.urbanairship.automatorutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class RichPushSenderApiV3 extends PushSenderApiV3 {

    /**
     * Constructor for RichPushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     * @param appName Name of the application
     */
    public RichPushSenderApiV3(String masterSecret, String appKey) {
        super(masterSecret, appKey);
    }

    @Override
    protected String createMessage(String recipientString, String recipientValueString, Map<String, String> extras, String uniqueAlertId) throws JSONException {
        String message = super.createMessage(recipientString, recipientValueString, extras, uniqueAlertId);
        JSONObject jsonPayload = new JSONObject(message);

        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("title", "Rich Push " + uniqueAlertId);
        jsonMessage.put("body", "Rich Push Message " + uniqueAlertId);
        jsonMessage.put("content_type", "text/html");
        jsonPayload.put("message", jsonMessage);

        return jsonPayload.toString();
    }
}
