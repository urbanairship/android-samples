package com.urbanairship.richpush.sample;

import android.app.Application;
import android.util.Log;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;

public class RichPushApplication extends Application {

    @Override
    public void onCreate() {
        UAirship.takeOff(this);
        Logger.logLevel = Log.VERBOSE;
    }

}
