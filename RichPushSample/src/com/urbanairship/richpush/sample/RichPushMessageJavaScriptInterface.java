package com.urbanairship.richpush.sample;

public interface RichPushMessageJavaScriptInterface {

    public int getViewHeight();
    public int getViewWidth();
    public String getMessageId();
    public String getDeviceOrientation();

    public void close();
    public void navigateTo(String activityName);
    public void nextMessage();
    public void previousMessage();
    public void goToMessage(String messageId);

}
