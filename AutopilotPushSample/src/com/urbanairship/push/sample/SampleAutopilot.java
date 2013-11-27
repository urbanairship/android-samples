package com.urbanairship.push.sample;

import android.app.Application;
import android.util.Log;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Autopilot;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.CustomPushNotificationBuilder;
import com.urbanairship.push.PushManager;

public class SampleAutopilot extends Autopilot {

    @Override
    public void execute(Application application) {
        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(application);

        // Optionally, customize your config at runtime:
        //
        // options.inProduction = false;
        // options.developmentAppKey = "Your Development App Key";
        // options.developmentAppSecret "Your Development App Secret";

        UAirship.takeOff(application, options);
        Logger.logLevel = Log.VERBOSE;

        //use CustomPushNotificationBuilder to specify a custom layout
        CustomPushNotificationBuilder nb = new CustomPushNotificationBuilder();

        nb.statusBarIconDrawableId = R.drawable.icon_small;//custom status bar icon

        nb.layout = R.layout.notification_multi_line;
        nb.layoutIconDrawableId = R.drawable.icon;//custom layout icon
        nb.layoutIconId = R.id.icon;
        nb.layoutSubjectId = R.id.subject;
        nb.layoutMessageId = R.id.message;

        // customize the sound played when a push is received
        //nb.soundUri = Uri.parse("android.resource://"+this.getPackageName()+"/" +R.raw.cat);

        PushManager.shared().setNotificationBuilder(nb);
        PushManager.shared().setIntentReceiver(IntentReceiver.class);

    }

}
