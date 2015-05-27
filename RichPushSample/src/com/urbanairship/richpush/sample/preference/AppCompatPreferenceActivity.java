/*
Copyright 2009-2015 Urban Airship Inc. All rights reserved.

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

package com.urbanairship.richpush.sample.preference;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * PreferenceActivity that implements the AppCompatDelegate.
 */
public class AppCompatPreferenceActivity extends PreferenceActivity {

    private AppCompatDelegate appCompatDelegate;

    @Override
    public MenuInflater getMenuInflater() {
        return this.getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(int layoutResID) {
        this.getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        this.getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        this.getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        this.getDelegate().addContentView(view, params);
    }

    @Override
    public void invalidateOptionsMenu() {
        this.getDelegate().invalidateOptionsMenu();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        this.getDelegate().setTitle(title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.getDelegate().installViewFactory();
        this.getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.getDelegate().onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        this.getDelegate().onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getDelegate().onDestroy();
    }

    public boolean supportRequestWindowFeature(int featureId) {
        return this.getDelegate().requestWindowFeature(featureId);
    }

    public void supportInvalidateOptionsMenu() {
        this.getDelegate().invalidateOptionsMenu();
    }

    public ActionBar getSupportActionBar() {
        return this.getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(Toolbar toolbar) {
        this.getDelegate().setSupportActionBar(toolbar);
    }

    @Nullable
    public ActionBarDrawerToggle.Delegate getDrawerToggleDelegate() {
        return this.getDelegate().getDrawerToggleDelegate();
    }

    public AppCompatDelegate getDelegate() {
        if(this.appCompatDelegate == null) {
            this.appCompatDelegate = AppCompatDelegate.create(this, null);
        }

        return this.appCompatDelegate;
    }
}
