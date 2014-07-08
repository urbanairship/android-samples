package com.urbanairship.combined.sample;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.LocationPreferences;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.push.CustomPushNotificationBuilder;
import com.urbanairship.push.GCMMessageHandler;
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
     * Handle any PushManager intents that are specific for the Urban Airship Google SDK.
     * @param context The applications context.
     * @param intent The intent received in IntentReceiver.
     */
    public static void handlePushIntent(Context context, Intent intent) {
        if (PushManager.ACTION_REGISTRATION_FINISHED.equals(intent.getAction())) {
            Logger.info("Registration complete. APID:" + intent.getStringExtra(PushManager.EXTRA_APID)
                    + ". Valid: " + intent.getBooleanExtra(PushManager.EXTRA_REGISTRATION_VALID, false));

            // Notify the MainActivity of the finished registration
            Intent registrationIntent = new Intent(MainActivity.ACTION_REGISTRATION_FINISHED);
            LocalBroadcastManager.getInstance(context).sendBroadcast(registrationIntent);

        } else if (GCMMessageHandler.ACTION_GCM_DELETED_MESSAGES.equals(intent.getAction())) {
            Logger.info("The GCM service deleted " + intent.getStringExtra(GCMMessageHandler.EXTRA_GCM_TOTAL_DELETED) + " messages.");
        }
    }

    // PushManager

    /**
     * Gets the push token for the Urban Airship Google SDK.
     * @return The push token.
     */
    public static String getPushToken() {
        return PushManager.shared().getAPID();
    }

    // LocationManager

    /**
     * Enables location updates.
     * @param enabled If location updates are enabled or not.
     */
    public static void setLocationUpdatesEnabled(boolean enabled) {
        if (enabled) {
            UALocationManager.enableLocation();
            UALocationManager.enableForegroundLocation();
        } else {
            UALocationManager.disableLocation();
            UALocationManager.disableForegroundLocation();
        }
    }

    /**
     * Allows location updates to continue in the background.
     * @param enabled If background updates are allowed in the background or not.
     */
    public static void setBackgroundLocationAllowed(boolean enabled) {
        if (enabled) {
            UALocationManager.enableBackgroundLocation();
        } else {
            UALocationManager.disableBackgroundLocation();
        }
    }

    /**
     * Checks if continuous location updates is enabled or not.
     *
     * @return <code>true</code> if location updates are enabled, otherwise
     * <code>false</code>.
     */
    public static boolean isLocationUpdatesEnabled() {
        LocationPreferences preferences = UALocationManager.shared().getPreferences();
        return preferences.isLocationEnabled() && preferences.isForegroundLocationEnabled();
    }

    /**
     * Checks if continuous location updates are allowed to continue
     * when the application is in the background.
     *
     * @return <code>true</code> if continuous location update are allowed,
     * otherwise <code>false</code>.
     */
    public static boolean isBackgroundLocationAllowed() {
        return UALocationManager.shared().getPreferences().isBackgroundLocationEnabled();
    }
}
