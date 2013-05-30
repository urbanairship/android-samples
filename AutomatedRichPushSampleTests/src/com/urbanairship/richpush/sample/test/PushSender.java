package com.urbanairship.richpush.sample.test;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class to send push notifications
 *
 */
public class PushSender {

    private final String masterSecret;
    private final String appKey;
    private final String appName;
    private final String broadcastUrl;
    private final String pushUrl;

    private static int MAX_SEND_MESG_RETRIES = 3;
    private static int SEND_MESG_RETRY_DELAY = 3000;  // 3 seconds

    /**
     * Constructor for PushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     * @param appName Name of the application
     * @param broadcastUrl The url for broadcasting push messages
     * @param pushUrl The url for push messages
     */
    public PushSender(String masterSecret, String appKey, String appName, String broadcastUrl, String pushUrl) {
        this.masterSecret = masterSecret;
        this.appKey = appKey;
        this.appName = appName;
        this.broadcastUrl = broadcastUrl;
        this.pushUrl = pushUrl;
    }

    /**
     * Builds the message to be sent
     * @param pushString The string to append based on the type of push (user, alias, tag)
     * @param activity The specified activity to send the push message to
     * @param uniqueAlertId The string used to identify push messages
     * @param sendAttempt The string identifying the attempt to send a message
     * @return The message to be sent
     */
    private String createMessage(String pushString, String activity, String uniqueAlertId, String sendAttempt) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        if (pushString != null) {
            builder.append(pushString);
        }
        if (appName.equalsIgnoreCase("Push Sample")) {
            builder.append("\"android\": { \"alert\": \"" + uniqueAlertId + sendAttempt + "\", \"extra\": {\"a_key\":\"a_value\"} } }");
        } else if (appName.equalsIgnoreCase("Rich Push Sample")) {
            builder.append("\"push\": {\"android\": { \"alert\": \"" + uniqueAlertId + sendAttempt + "\", \"extra\": { \"activity\": \"" + activity + "\" } } },");
            builder.append("\"title\": \"Rich Push " + uniqueAlertId + sendAttempt +  "\",");
            builder.append("\"message\": \"Rich Push Message " + uniqueAlertId + sendAttempt + "\",");
            builder.append("\"content-type\": \"text/html\"}");
        }

        return builder.toString();
    }


    /**
     * Broadcast a push message
     * @param uniqueAlertId The string used to identify push messages
     * @throws Exception
     */
    public void sendPushMessage(String uniqueAlertId) throws Exception {
        Log.i(appName + " PushSender", "Broadcast message AlertId: " + uniqueAlertId);
        sendMessage(broadcastUrl, null, "", uniqueAlertId);
    }

    /**
     * Sends a push message to an activity
     * @param activity The specified activity to send the push message to
     * @param uniqueAlertId The string used to identify push messages
     * @throws Exception
     */
    public void sendPushMessage(String activity, String uniqueAlertId) throws Exception {
        Log.i(appName + " PushSender", "Broadcast message to activity: " + activity + " AlertId" + uniqueAlertId);
        sendMessage(broadcastUrl, null, activity, uniqueAlertId);
    }

    /**
     * Sends a push message to a tag
     * @param tag The specified tag to send the push message to
     * @param uniqueAlertId The string used to identify push messages
     * @throws Exception
     */
    public void sendPushToTag(String tag, String uniqueAlertId) throws Exception {
        Log.i(appName + " PushSender", "Send message to tag: " + tag + " AlertId: " + uniqueAlertId);
        sendMessage(pushUrl, "\"tags\": [\"" + tag + "\"],", "", uniqueAlertId);
    }

    /**
     * Sends a push message to an alias
     * @param alias The specified alias to send the push message to
     * @param uniqueAlertId The string used to identify push messages
     * @throws Exception
     */
    public void sendPushToAlias(String alias, String uniqueAlertId) throws Exception {
        Log.i(appName + " PushSender", "Send message to tag: " + alias + " AlertId: " + uniqueAlertId);
        sendMessage(pushUrl, "\"aliases\": [\"" + alias + "\", \"anotherAlias\"],", "", uniqueAlertId);
    }

    /**
     * Sends a push message to an APID
     * @param apid The specified apid to send the push message to
     * @param uniqueAlertId The string used to identify push messages
     * @throws Exception
     */
    public void sendPushToApid(String apid, String uniqueAlertId) throws Exception {
        Log.i(appName + " PushSender", "Send message to apid: " + apid + " AlertId: " + uniqueAlertId);
        sendMessage(pushUrl, "\"apids\": [\"" + apid + "\"],", "", uniqueAlertId);
    }

    /**
     * Sends a rich push message to a user
     * @param user The specified user id to send the rich push message to
     * @param uniqueAlertId The string used to identify push messages
     * @throws Exception
     */
    public void sendRichPushToUser(String user, String uniqueAlertId) throws Exception {
        Log.i(appName + " PushSender", "Send message to user: " + user + " AlertId: " + uniqueAlertId);
        sendMessage(pushUrl, "\"users\": [\"" + user + "\"],", "", uniqueAlertId);
    }

    /**
     * Actually sends the push message
     * @param urlString The specified url the message is sent to
     * @param pushString The specified type of push
     * @param activity The specified activity to send the push message to
     * @param uniqueAlertId The string used to identify push messages
     * @throws IOException
     * @throws
     */
    private void sendMessage(String urlString, String pushString, String activity, String uniqueAlertId) throws Exception {
        int sendMesgRetryCount = 0;
        while ( sendMesgRetryCount < MAX_SEND_MESG_RETRIES ) {
            String json = createMessage(pushString, activity, uniqueAlertId, " " + String.valueOf(sendMesgRetryCount));
            Log.i(appName + " PushSender",  "Created message to send" + json);

            try {
                sendMessageHelper(urlString, json);
                return;
            } catch (Exception ex) {
                Log.e(appName + " PushSender", "Failed to send message: " + json, ex);
                Thread.sleep(SEND_MESG_RETRY_DELAY);
                sendMesgRetryCount++;
            }
        }
    }

    /**
     * Actually sends the push message
     * @param urlString The specified url the message is sent to
     * @param message The json formatted message to be sent
     * @throws IOException
     */
    private void sendMessageHelper(String urlString, String message) throws IOException  {
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
            conn.setRequestProperty("Content-Type",
                    "application/json");

            conn.setRequestProperty("Authorization", basicAuthString);

            // Create the form content
            OutputStream out = conn.getOutputStream();
            Writer writer = new OutputStreamWriter(out, "UTF-8");
            writer.write(message);
            writer.close();
            out.close();

            if (conn.getResponseCode() != 200) {
                Log.e(appName + " PushSender", "Sending push failed with: " + conn.getResponseCode() + " " + conn.getResponseMessage() + " Message: " + message);
                throw new IOException(conn.getResponseMessage());
            } else {
                Log.i(appName + " PushSender", "Push sent: " + message);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
