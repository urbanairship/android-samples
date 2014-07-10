package com.urbanairship.combined.sample;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.CustomPushNotificationBuilder;
import com.urbanairship.push.PushManager;


public class UrbanAirshipShim {

    /**
     * Perform takeoff. Called from the main application.
     * @param application The application.
     */
    public static void takeOff(Application application) {
        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(application);

        UAirship.takeOff(application, options);
        Logger.logLevel = Log.VERBOSE;

        //use CustomPushNotificationBuilder to specify a custom layout
        CustomPushNotificationBuilder nb = new CustomPushNotificationBuilder();

        nb.statusBarIconDrawableId = R.drawable.icon_small;//custom status bar icon

        nb.layout = R.layout.notification;
        nb.layoutIconDrawableId = R.drawable.icon;//custom layout icon
        nb.layoutIconId = R.id.icon;
        nb.layoutSubjectId = R.id.subject;
        nb.layoutMessageId = R.id.message;

        PushManager.shared().setNotificationBuilder(nb);
        PushManager.shared().setIntentReceiver(IntentReceiver.class);
    }

    /**
     * Handle any PushManager intents that are specific for the Urban Airship Amazon SDK.
     * @param context The applications context.
     * @param intent The intent received in IntentReceiver.
     */
    public static void handlePushIntent(Context context, Intent intent) {
        if (PushManager.ACTION_REGISTRATION_SUCCEEDED.equals(intent.getAction())) {
            String channelID = intent.getStringExtra(PushManager.EXTRA_CHANNEL);
            Logger.info("Registration complete. Channel Id:" + channelID + ".");

            // Notify the MainActivity of the finished registration
            Intent registrationIntent = new Intent(MainActivity.ACTION_REGISTRATION_FINISHED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(registrationIntent);
        }
    }

    // PushManager

    /**
     * Gets the push token for the Amazon SDK.
     * @return The push token.
     */
    public static String getPushToken() {
        return PushManager.shared().getChannelId();
    }

    // LocationManager

    /**
     * Enables location updates.
     * @param enabled If location updates are enabled or not.
     */
    public static void setLocationUpdatesEnabled(boolean enabled) {
        UALocationManager.shared().setLocationUpdatesEnabled(enabled);
    }

    /**
     * Allows location updates to continue in the background.
     * @param enabled If background updates are allowed in the background or not.
     */
    public static void setBackgroundLocationAllowed(boolean enabled) {
        UALocationManager.shared().setBackgroundLocationAllowed(enabled);
    }

    /**
     * Checks if continuous location updates is enabled or not.
     *
     * @return <code>true</code> if location updates are enabled, otherwise
     * <code>false</code>.
     */
    public static boolean isLocationUpdatesEnabled() {
        return UALocationManager.shared().isLocationUpdatesEnabled();
    }

    /**
     * Checks if continuous location updates are allowed to continue
     * when the application is in the background.
     *
     * @return <code>true</code> if continuous location update are allowed,
     * otherwise <code>false</code>.
     */
    public static boolean isBackgroundLocationAllowed() {
        return UALocationManager.shared().isBackgroundLocationAllowed();
    }
}
