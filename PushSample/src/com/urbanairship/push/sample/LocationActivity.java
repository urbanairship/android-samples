package com.urbanairship.push.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.urbanairship.Cancelable;
import com.urbanairship.UAirship;
import com.urbanairship.analytics.Analytics;
import com.urbanairship.location.LocationCallback;
import com.urbanairship.location.LocationRequestOptions;

public class LocationActivity extends Activity {

    private Cancelable pendingRequest;
    private RadioGroup priorityGroup;
    private View progress;
    static final int PERMISSIONS_REQUEST_LOCATION = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_activity);

        priorityGroup = (RadioGroup) findViewById(R.id.location_priority);
        progress = findViewById(R.id.request_progress);
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Required for analytics
        Analytics.activityStarted(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Required for analytics
        Analytics.activityStopped(this);

        // Cancel the request
        if (pendingRequest != null) {
            pendingRequest.cancel();
            progress.setVisibility(View.INVISIBLE);
        }
    }

    public void requestLocation() {
        if (pendingRequest != null) {
            pendingRequest.cancel();
        }

        progress.setVisibility(View.VISIBLE);

        LocationRequestOptions options = new LocationRequestOptions.Builder()
                .setPriority(getPriority())
                .create();

        pendingRequest = UAirship.shared().getLocationManager().requestSingleLocation(new LocationCallback() {
            @Override
            public void onResult(Location location) {
                progress.setVisibility(View.INVISIBLE);

                if (location != null) {
                    Toast.makeText(getApplicationContext(), formatLocation(location), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                }
            }
        }, options);
    }

    @SuppressWarnings("UnusedParameters")
    public void onRequestLocationClicked(View view) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_REQUEST_LOCATION);
        } else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, now request location.
                    requestLocation();
                } else {
                    // permission denied, let them know location permissions is required.
                    Toast.makeText(getApplicationContext(), "Enable location permissions and try again.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String formatLocation(Location location) {
        return String.format("provider: %s lat: %s, lon: %s, accuracy: %s",
                location.getProvider(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy());
    }

    /**
     * Gets the LocationRequestOptions priority from the radio group.
     *
     * @return The location request options priority.
     */
    @LocationRequestOptions.Priority
    private int getPriority() {
        switch (priorityGroup.getCheckedRadioButtonId()) {
            case R.id.priority_high_accuracy:
                return LocationRequestOptions.PRIORITY_HIGH_ACCURACY;
            case R.id.priority_balanced:
                return LocationRequestOptions.PRIORITY_BALANCED_POWER_ACCURACY;
            case R.id.priority_low_power:
                return LocationRequestOptions.PRIORITY_LOW_POWER;
            case R.id.priority_no_power:
                return LocationRequestOptions.PRIORITY_NO_POWER;
        }

        return LocationRequestOptions.PRIORITY_BALANCED_POWER_ACCURACY;
    }
}
