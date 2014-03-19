/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fyp.samoleary.WildlifePrototype2;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.*;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 11/02/14
 * Revision: 1
 * Revision History:
 * 1: 11/02/14
 * Description:
 */
public class NavDrawer extends FragmentActivity {
    protected DrawerLayout fullLayout;
    protected FrameLayout actContent;

    protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String app_ver;

    private TextView imgText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try
        {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.v(LocationUtils.APPTAG, e.getMessage());
        }

        if (savedInstanceState == null) {
            selectItem(-1);
        }
    }

    @Override
    public void setContentView(final int layoutResID) {
        fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
        assert fullLayout != null;
        actContent = (FrameLayout) fullLayout.findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, actContent, true);
        super.setContentView(fullLayout);

        mTitle = mDrawerTitle = getTitle();

        String[] navDrawerMenuItems = getResources().getStringArray(R.array.menu_items_array);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        View imgView = getLayoutInflater().inflate(R.layout.header, null);
        mDrawerList.addHeaderView(imgView);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navDrawerMenuItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                imgText = (TextView) findViewById(R.id.myImageViewText);
                SharedPreferences settings = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
                String name = settings.getString("name", "Click to set up Profile");
                imgText.setText(name);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public void selectItem(int position){

    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title + " " + app_ver;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void closeDrawer(int position){
        mDrawerList.setItemChecked(position, false);
        mDrawerLayout.closeDrawers();
    }
}