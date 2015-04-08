package com.urbanairship.push.sample;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.urbanairship.PendingResult;
import com.urbanairship.UAirship;
import com.urbanairship.analytics.Analytics;
import com.urbanairship.location.LocationRequestOptions;

public class LocationActivity extends Activity {

    private PendingResult<Location> pendingRequest;
    private RadioGroup priorityGroup;
    private View progress;

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


    @SuppressWarnings("UnusedParameters")
    public void onRequestLocationClicked(View view) {
        if (pendingRequest != null) {
            pendingRequest.cancel();
        }

        progress.setVisibility(View.VISIBLE);

        LocationRequestOptions options = new LocationRequestOptions.Builder()
                .setPriority(getPriority())
                .create();

        //noinspection unchecked
        pendingRequest = UAirship.shared().getLocationManager().requestSingleLocation(options);

        pendingRequest.onResult(new PendingResult.ResultCallback<Location>() {
            @Override
            public void onResult(Location location) {
                progress.setVisibility(View.INVISIBLE);

                if (location != null) {
                    Toast.makeText(getApplicationContext(), formatLocation(location), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to get location", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
     * @return The location request options priority.
     */
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
