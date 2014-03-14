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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import com.urbanairship.Logger;
import com.urbanairship.push.sample.preference.PreferencesActivity;
import com.urbanairship.util.UriUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An activity that creates and launches a task stack from a deep link uri.
 *
 * The activity will operate on any URI that it is started with, only parsing
 * the URI's path and query parameters.  URI filtering should be defined in the
 * AndroidManifest.xml for the ParseDeepLinkActivity entry by defining an intent
 * filter.
 *
 * PushSample example:
 *
 * vnd.urbanairship.push://deeplink/home/preferences/location
 *
 * Will open the application to the location activity with the preferences and
 * main activity on the task stack.
 *
 */
public class ParseDeepLinkActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        List<String> deepLinks = null;
        Bundle options = null;
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Parse the URI for deep links and options
        if (intent != null && intent.getData() != null) {
            Uri uri = intent.getData();
            deepLinks = uri.getPathSegments();
            options = parseOptions(uri);
        }

        // Create a task stack from the deep links
        if (deepLinks != null) {
            for (String deepLink : deepLinks) {
                Logger.info("Parsing deep link: " + deepLink);
                Intent route = parseDeepLink(deepLink);
                if (route != null) {
                    stackBuilder.addNextIntentWithParentStack(route);
                }
            }
        }

        // If we failed to parse any deep links still start the app at the main
        // activity
        if (stackBuilder.getIntents().length == 0) {
            stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        }

        // Launch the activities
        if (options != null) {
            stackBuilder.startActivities(options);
        } else {
            stackBuilder.startActivities();
        }

        finish();
    }

    /**
     * Parses a deep link (a path segment) to an intent that starts an activity
     *
     * @param deepLink The deep link
     * @return An intent that corresponds to the deepLink, or null if matching no
     * activity is found.
     */
    private Intent parseDeepLink(String deepLink) {
        if ("preferences".equals(deepLink)) {
            return new Intent(getApplicationContext(), PreferencesActivity.class);
        } else if ("home".equals(deepLink)) {
            return new Intent(getApplicationContext(), MainActivity.class);
        } else if ("location".equals(deepLink)) {
            return new Intent(getApplicationContext(), LocationActivity.class);
        } else {
            return null;
        }
    }

    /**
     * Helper method that parses the uri's query parameters as a bundle
     * @param uri The uri to parse
     * @return A bundle of the uri's query parameters
     */
    private Bundle parseOptions(Uri uri) {
        Bundle options = new Bundle();
        Map <String, List<String>> queryParameters = UriUtils.getQueryParameters(uri);

        if (queryParameters == null) {
            return options;
        }

        for (String key : queryParameters.keySet()) {
            List<String> values = queryParameters.get(key);
            if (values.size() == 1) {
                options.putString(key, values.get(0));
            } else if (values.size() > 1) {
                options.putStringArrayList(key, new ArrayList<String>(values));
            }
        }

        return options;
    }
}
