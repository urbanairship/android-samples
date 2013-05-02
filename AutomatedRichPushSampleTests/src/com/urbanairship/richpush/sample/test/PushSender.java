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
    private static final String TAG = "RichPushSampleUiTests";
    private static final String RICH_PUSH_BROADCAST_URL = "https://go.urbanairship.com/api/airmail/send/broadcast/";
    private static final String RICH_PUSH_URL = "https://go.urbanairship.com/api/airmail/send/";

    private final String masterSecret;
    private final String appKey;

    /**
     * Constructor for PushSender
     * @param masterSecret The specified master secret for the app
     * @param appKey The specified app key for the app
     */
    public PushSender(String masterSecret, String appKey) {
        this.masterSecret = masterSecret;
        this.appKey = appKey;
    }

    /**
     * Sends a rich push message
     * @throws Exception
     */
    public void sendRichPushMessage() throws Exception {
        sendRichPushMessage("");
    }

    /**
     * Sends a rich push message to an activity
     * @param activity The specified activity to send the rich push message to
     * @throws Exception
     */
    public void sendRichPushMessage(String activity) throws Exception {
        sendMessage(RICH_PUSH_BROADCAST_URL, null, activity);
    }

    /**
     * Sends a rich push message to a tag
     * @param tag The specified tag to send the rich push message to
     * @throws Exception
     */
    public void sendRichPushToTag(String tag) throws Exception {
        sendMessage(RICH_PUSH_URL, "\"tags\": [\"" + tag + "\"],", "");
    }

    /**
     * Sends a rich push message to an alias
     * @param alias The specified alias to send the rich push message to
     * @throws Exception
     */
    public void sendRichPushToAlias(String alias) throws Exception {
        sendMessage(RICH_PUSH_URL, "\"aliases\": [\"" + alias + "\", \"anotherAlias\"],", "");
    }

    /**
     * Sends a rich push message to a user
     * @param user The specified user id to send the rich push message to
     * @throws Exception
     */
    public void sendRichPushToUser(String user) throws Exception {
        sendMessage(RICH_PUSH_URL, "\"users\": [\"" + user + "\"],", "");
    }

    /**
     * Actually sends the rich push message
     * @param url The specified url the message is sent to
     * @param message The json formatted message to be sent
     * @throws IOException
     * @throws
     */
    private void sendMessage(String urlString, String pushString, String activity) throws Exception {
        String json = createMessage(pushString, activity);

        try {
            sendMessageHelper(urlString, json);
        } catch (Exception ex) {
            Thread.sleep(3000);
            Log.e(TAG, "Failed to send message, retrying", ex);
            sendMessageHelper(urlString, json);
        }
    }

    /**
     * Actually sends the rich push message
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
                Log.e(TAG, "Sending rich push failed with: " + conn.getResponseCode() + " " + conn.getResponseMessage() + " Message: " + message);
                throw new IOException(conn.getResponseMessage());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Builds the message to be sent
     * @param pushString The string to append based on the type of push (user, alias, tag)
     * @param activity The activity to send the push to
     * @return The message to be sent
     */
    private String createMessage(String pushString, String activity) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"push\": {\"android\": { \"alert\": \"Rich Push Alert\", \"extra\": { \"activity\": \"" + activity + "\" } } },");
        if (pushString != null) {
            builder.append(pushString);
        }

        builder.append("\"title\": \"Rich Push Title\",");
        builder.append("\"message\": \"Rich Push Message\",");
        builder.append("\"content-type\": \"text/html\"}");

        return builder.toString();
    }
}
