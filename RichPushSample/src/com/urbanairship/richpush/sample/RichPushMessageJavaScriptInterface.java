package com.urbanairship.richpush.sample;

public interface RichPushMessageJavaScriptInterface {

    public int getViewHeight();
    public int getViewWidth();
    public String getUserId();
    public String getMessageId();
    public String getDeviceModel();
    public String getDeviceOrientation();

    public void navigateTo(String activityName);

}
