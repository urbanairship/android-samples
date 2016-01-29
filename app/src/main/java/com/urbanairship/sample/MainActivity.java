/*
Copyright 2009-2016 Urban Airship Inc. All rights reserved.


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

package com.urbanairship.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.urbanairship.google.PlayServicesUtils;
import com.urbanairship.messagecenter.MessageCenterFragment;
import com.urbanairship.richpush.RichPushInbox;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /**
     * Remember the ID of the selected item for the navigation drawer.
     */
    private static final String NAV_ID = "NAV_ID";

    /**
     * Remember the last title of the activity.
     */
    private static final String TITLE = "TITLE";

    private DrawerLayout drawer;
    private int currentNavPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // App drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Navigation view
        NavigationView navigation = (NavigationView) findViewById(R.id.nav_view);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                navigate(item.getItemId());
                return true;
            }
        });

        if (savedInstanceState != null) {
            navigate(savedInstanceState.getInt(NAV_ID));
            setTitle(savedInstanceState.getString(TITLE));
        } else {
            navigation.setCheckedItem(R.id.nav_home);
            navigate(R.id.nav_home);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ID, currentNavPosition);
        outState.putString(TITLE, String.valueOf(getTitle()));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Handle any Google Play services errors
        if (PlayServicesUtils.isGooglePlayStoreAvailable(this)) {
            PlayServicesUtils.handleAnyPlayServicesError(this);
        }

        // Handle the "com.urbanairship.VIEW_RICH_PUSH_INBOX" intent action.
        if (RichPushInbox.VIEW_INBOX_INTENT_ACTION.equals(getIntent().getAction())) {
            navigate(R.id.nav_location);

            // Clear the action so we don't handle it again
            getIntent().setAction(null);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (currentNavPosition != R.id.nav_home) {
            navigate(R.id.nav_home);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void navigate(int id) {
        currentNavPosition = id;

        if (getSupportFragmentManager().findFragmentByTag("content_frag" + id) != null) {
            return;
        }

        Fragment fragment;
        switch (id) {
            case R.id.nav_home:
                setTitle(R.string.home_title);
                fragment = new HomeFragment();
                break;
            case R.id.nav_message_center:
                setTitle(R.string.message_center_title);
                fragment = new MessageCenterFragment();
                break;
            case R.id.nav_location:
                setTitle(R.string.location_title);
                fragment = new LocationFragment();
                break;
            default:
                Log.e(TAG, "Unexpected navigation item");
                return;
        }

        currentNavPosition = id;

        getSupportFragmentManager().beginTransaction()
                                   .replace(R.id.content_frame, fragment, "content_frag" + id)
                                   .commit();

        drawer.closeDrawer(GravityCompat.START);
    }
}
