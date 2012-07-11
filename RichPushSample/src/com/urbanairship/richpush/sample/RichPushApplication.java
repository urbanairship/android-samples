package com.urbanairship.richpush.sample;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import java.util.List;

public class RichPushApplication extends Application {

    @Override
    public void onCreate() {
        UAirship.takeOff(this);
        if (this.isProcess(this.getPackageName())) {
            PushManager.enablePush();
        }
    }

    private boolean isProcess(String processName) {
        Context context = getApplicationContext();
        ActivityManager actMgr = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = actMgr.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : appList) {
            if (info.pid == android.os.Process.myPid() && processName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

}
