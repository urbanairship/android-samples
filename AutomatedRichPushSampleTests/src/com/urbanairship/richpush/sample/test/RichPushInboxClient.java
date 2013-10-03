package com.urbanairship.richpush.sample.test;

import org.apache.http.Header;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.BasicScheme;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RichPushInboxClient {

    private final Header credentialHeader;
    private String userId;
    private static final String MESSAGES_URL = "https://device-api.urbanairship.com/api/user/%s/messages";
    private static final String MESSAGE_URL = "https://device-api.urbanairship.com/api/user/%s/messages/message/%s/";

    public RichPushInboxClient(String user, String password, String userId) {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, password);
        credentialHeader = BasicScheme.authenticate(credentials, "UTF-8", false);

        this.userId = userId;
    }

    public List<String> getMessageIds() throws Exception {
        List<String> messageIds = new ArrayList<String>();

        JSONObject request = getMessageListRequest();

        JSONArray messagesJsonArray = request.getJSONArray("messages");
        int count = messagesJsonArray.length();

        for (int i = 0; i < count; i++) {
            JSONObject messageJson = messagesJsonArray.getJSONObject(i);
            messageIds.add(messageJson.getString("message_id"));
        }

        return messageIds;
    }

    public void deleteMessag(String messageId) throws Exception {
        URL url = new URL(String.format(MESSAGE_URL, userId, messageId));

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty(credentialHeader.getName(), credentialHeader.getValue());

            if (conn.getResponseCode() != 200) {
                throw new Exception("Failed to delete message: " + conn.getResponseMessage());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private JSONObject getMessageListRequest() throws IOException, JSONException {
        URL url = new URL(String.format(MESSAGES_URL, userId));

        HttpURLConnection conn = null;

        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty(credentialHeader.getName(), credentialHeader.getValue());

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            return new JSONObject(builder.toString());

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
