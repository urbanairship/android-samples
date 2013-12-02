package com.urbanairship.push.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.util.ServiceNotBoundException;

public class LocationActivity extends Activity {

    Button networkUpdateButton;
    Button gpsUpdateButton;
    Criteria newCriteria;
    Drawable mapIcon;
    IntentFilter locationFilter;
    LinearLayout mapLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location);

        networkUpdateButton = (Button) findViewById(R.id.network_update_button);
        gpsUpdateButton = (Button) findViewById(R.id.gps_update_button);

        locationFilter = new IntentFilter();
        locationFilter.addAction(UALocationManager.getLocationIntentAction(UALocationManager.ACTION_SUFFIX_LOCATION_UPDATE));

        newCriteria = new Criteria();
        newCriteria.setAccuracy(Criteria.ACCURACY_FINE);

        networkUpdateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    UALocationManager.shared().recordCurrentLocation();
                } catch (ServiceNotBoundException e) {
                    Logger.debug(e.getMessage());
                } catch (RemoteException e) {
                    Logger.debug(e.getMessage());
                }
            }

        });

        gpsUpdateButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    UALocationManager.shared().recordCurrentLocation(newCriteria);
                } catch (ServiceNotBoundException e) {
                    Logger.debug(e.getMessage());
                } catch (RemoteException e) {
                    Logger.debug(e.getMessage());
                }
            }

        });


    }

    // Implementing onStart/onStop because we're not extending a UA activity
    @Override
    public void onStart() {
        super.onStart();
        UAirship.shared().getAnalytics().activityStarted(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        UAirship.shared().getAnalytics().activityStopped(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(locationUpdateReceiver, locationFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(locationUpdateReceiver);
    }

    BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (UALocationManager.getLocationIntentAction(UALocationManager.ACTION_SUFFIX_LOCATION_UPDATE).equals(intent.getAction())) {
                Location newLocation = (Location) intent.getExtras().get(UALocationManager.LOCATION_KEY);

                String text = String.format("lat: %s, lon: %s", newLocation.getLatitude(),
                        newLocation.getLongitude());

                Toast.makeText(UAirship.shared().getApplicationContext(),
                        text, Toast.LENGTH_LONG).show();

            }
        }

    };


}
