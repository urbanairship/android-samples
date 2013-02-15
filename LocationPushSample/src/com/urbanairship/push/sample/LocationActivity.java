package com.urbanairship.push.sample;

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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import com.urbanairship.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.location.UALocationManager;
import com.urbanairship.util.ServiceNotBoundException;

public class LocationActivity extends MapActivity {

    Button networkUpdateButton;
    Button gpsUpdateButton;
    Criteria newCriteria;
    Drawable mapIcon;
    IntentFilter locationFilter;
    LinearLayout mapLayout;
    LocationItemizedOverlay overlay;
    MapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location);

        networkUpdateButton = (Button) findViewById(R.id.network_update_button);
        gpsUpdateButton = (Button) findViewById(R.id.gps_update_button);

        mapLayout = (LinearLayout) findViewById(R.id.map_layout);

        if ("ENTER YOUR GOOGLE MAPS API KEY HERE".equals(MyApplication.GOOGLE_MAPS_API_KEY)) {
            TextView textView = new TextView(this);
            textView.setText("If you enter your Google Maps API key in the MyApplication class, " +
                    "you could have a map view here instead of this message.");
            mapLayout.addView(textView);
        } else {
            this.createMap();

            mapIcon = this.getResources().getDrawable(R.drawable.icon_small);
            overlay = new LocationItemizedOverlay(mapIcon, this);

            initializeMap();
        }

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
            if (UALocationManager.getLocationIntentAction(UALocationManager.ACTION_SUFFIX_LOCATION_UPDATE)
                    .equals(intent.getAction())) {
                Location newLocation = (Location) intent.getExtras().get(UALocationManager.LOCATION_KEY);
                if (mapView != null) {
                    setNewLocationOnMap(newLocation);
                } else {
                    String text = String.format("lat: %s, lon: %s", newLocation.getLatitude(),
                            newLocation.getLongitude());
                    Toast.makeText(UAirship.shared().getApplicationContext(),
                            text, Toast.LENGTH_LONG).show();
                }
            }
        }

    };

    private void initializeMap() {
        Location currentLocation = null;
        try {
            currentLocation = UALocationManager.shared().getLocation();
        } catch (ServiceNotBoundException e) {
            Logger.debug(e.getMessage());
        } catch (RemoteException e) {
            Logger.debug(e.getMessage());
        }

        if (currentLocation != null)
            setNewLocationOnMap(currentLocation);
    }

    private void setNewLocationOnMap(Location location) {
        GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1E6),
                (int)(location.getLongitude() * 1E6));
        OverlayItem overlayItem = new OverlayItem(point, "Oh haiiii!",
                String.format("I'm at latitude %s, longitude %s",
                        location.getLatitude(), location.getLongitude()));

        overlay.setItem(overlayItem);
        mapView.getOverlays().clear();
        mapView.getOverlays().add(overlay);

        mapView.getController().animateTo(point);
    }

    private void createMap() {
        mapView = new MapView(this, MyApplication.GOOGLE_MAPS_API_KEY);
        mapView.setBuiltInZoomControls(true);
        mapView.setClickable(true);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, new GeoPoint(0,0), LayoutParams.CENTER);

        mapLayout.addView(mapView, params);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

}
