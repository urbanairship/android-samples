/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
import android.support.v4.content.LocalBroadcastManager;
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
        locationFilter.addAction(UALocationManager.ACTION_LOCATION_UPDATE);

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
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver, locationFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
    }

    BroadcastReceiver locationUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (UALocationManager.ACTION_LOCATION_UPDATE.equals(intent.getAction())) {
                Location newLocation = (Location) intent.getExtras().get(UALocationManager.LOCATION_KEY);

                String text = String.format("lat: %s, lon: %s", newLocation.getLatitude(),
                        newLocation.getLongitude());

                Toast.makeText(UAirship.shared().getApplicationContext(),
                        text, Toast.LENGTH_LONG).show();

            }
        }

    };


}
