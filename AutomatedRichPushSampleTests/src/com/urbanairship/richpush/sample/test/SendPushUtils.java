package com.urbanairship.richpush.sample.test;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendPushUtils {
    static final String TAG = "RichPushSampleUiTests";

    private static final String RICH_PUSH_URL = "https://go.urbanairship.com/api/airmail/send/";
    private static final String MASTER_SECRET = "";
    private static final String APP_KEY = "";
    private static final String RICH_PUSH_USER_ID = "";

    public static void sendRichPushMessage() throws Exception {
        sendRichPushMessage("");
    }

    public static void sendRichPushMessage(String activity) throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append("{ \"push\": {\"android\": { \"alert\": \"Rich Push Alert\", \"extra\": { \"activity\": \"" + activity + "\" } } },");
        builder.append("\"users\": [\"" + RICH_PUSH_USER_ID + "\"],");
        builder.append("\"title\": \"Rich Push Title\",");
        builder.append("\"message\": \"Rich Push Message\",");
        builder.append("\"content-type\": \"text/html\"}");

        String json = builder.toString();
        URL url = new URL(RICH_PUSH_URL);
        String basicAuthString =  "Basic "+Base64.encodeToString(String.format("%s:%s", APP_KEY, MASTER_SECRET).getBytes(), Base64.NO_WRAP);

        try {
            sendMessage(url, json, basicAuthString);
        } catch (Exception ex) {
            // Try again if we fail for whatever reason
            Thread.sleep(3000);
            sendMessage(url, json, basicAuthString);
        }
    }

    private static void sendMessage(URL url, String message, String basicAuthString) throws IOException {
        HttpURLConnection conn = null;

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
}
