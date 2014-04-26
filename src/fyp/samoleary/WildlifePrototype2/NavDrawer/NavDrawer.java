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

package fyp.samoleary.WildlifePrototype2.NavDrawer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;import android.view.MenuItem;import android.view.View;
import android.view.Window;
import android.widget.*;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import fyp.samoleary.WildlifePrototype2.GMap.GMapActivity;import fyp.samoleary.WildlifePrototype2.GetConnectivityStatus;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.Profile;
import fyp.samoleary.WildlifePrototype2.R;import fyp.samoleary.WildlifePrototype2.RSSFeed.NewsFeedActivity;import fyp.samoleary.WildlifePrototype2.SpeciesGuide.SpeciesGuide;import fyp.samoleary.WildlifePrototype2.WildlifeGeofencePkg.WildlifeGeofence;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private Session session;
    private String user_ID;
    private String profileName;

    protected DrawerLayout fullLayout;
    protected FrameLayout actContent;

    protected DrawerLayout mDrawerLayout;

    protected ExpandableListAdapter listAdapter;
    protected ExpandableListView mDrawerList;
    protected List<String> listDataHeader;
    protected HashMap<String, List<String>> listDataChild;

    protected ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String app_ver;

    private TextView imgText;
    private ImageView imageView;

    private GetConnectivityStatus isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        isConnected = new GetConnectivityStatus();

        try
        {
            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            Log.v(LocationUtils.APPTAG, e.getMessage());
        }

        if (savedInstanceState == null) {
            selectItem(-1, -1);
        }
        Log.d(LocationUtils.APPTAG, "onCreate");

    }

    @Override
    public void setContentView(final int layoutResID) {
        fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_main, null);
        assert fullLayout != null;
        actContent = (FrameLayout) fullLayout.findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, actContent, true);
        super.setContentView(fullLayout);

        mTitle = mDrawerTitle = getTitle();

        //String[] navDrawerMenuItems = getResources().getStringArray(R.array.menu_items_array);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // Get the ListView
        mDrawerList = (ExpandableListView) findViewById(R.id.left_drawer);

        // Preparing List Data
        prepareListDataInit();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        View imgView = getLayoutInflater().inflate(R.layout.header, null);
        mDrawerList.addHeaderView(imgView);

        // Setting List Adapter
        mDrawerList.setAdapter(listAdapter);

        // Listview on child click listener
        mDrawerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                selectItem(groupPosition, childPosition);
                return false;
            }
        });

        mDrawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                selectGroup(groupPosition);
                return false;
            }
        });

        // set up the drawer's list view with items and click listener
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //        R.layout.drawer_list_item, navDrawerMenuItems));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

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
                setUpNavDrawer();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                imgText = (TextView) findViewById(R.id.myImageViewText);
                SharedPreferences settings = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, 0);
                String name = settings.getString("name", "Click to set up Profile");
                imgText.setText(name);
                setUpNavDrawer();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setUpNavDrawer() {
        // Preparing List Data
        prepareListData();
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // Setting List Adapter
        mDrawerList.setAdapter(listAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void selectGroup(int groupPosition) {

    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding Group Header Data
        listDataHeader.add("Species Guide");
        listDataHeader.add("IWDG Data");
        listDataHeader.add("News Feed");
        listDataHeader.add("Hotspots");

        // Adding Child Data
        List<String> iwdgData = new ArrayList<String>();
        iwdgData.add("View Sightings");
        iwdgData.add("Report A Sighting");
        iwdgData.add("Search Sightings");

        List<String> species = new ArrayList<String>();
        //species.add("Species Guide");

        List<String> newsFeed = new ArrayList<String>();
        newsFeed.add("Cetacean News");
        newsFeed.add("Latest Sightings");
        newsFeed.add("Latest Strandings");

        List<String> hotspots = new ArrayList<String>();
        hotspots.add("View Hotspots");
        hotspots.add("Create a Hotspot");
        hotspots.add("Search Hotspots");

        listDataChild.put(listDataHeader.get(0), species);
        listDataChild.put(listDataHeader.get(1), iwdgData);
        listDataChild.put(listDataHeader.get(2), newsFeed);
        listDataChild.put(listDataHeader.get(3), hotspots);

        if(!isConnected.isConnected(this.getApplicationContext())){
            listDataHeader.add("No Internet Connectivity!");
            List<String> error = new ArrayList<String>();
            listDataChild.put(listDataHeader.get(listDataHeader.size() - 1), error);
        }

    }

    private void prepareListDataInit(){
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding Group Header Data
        listDataHeader.add("Species Guide");
        listDataHeader.add("IWDG Data");
        listDataHeader.add("News Feed");
        listDataHeader.add("Hotspots");

        // Adding Child Data
        List<String> iwdgData = new ArrayList<String>();
        iwdgData.add("View Sightings");
        iwdgData.add("Report A Sighting");
        iwdgData.add("Search Sightings");

        List<String> species = new ArrayList<String>();
        //species.add("Species Guide");

        List<String> newsFeed = new ArrayList<String>();
        newsFeed.add("Cetacean News");
        newsFeed.add("Latest Sightings");
        newsFeed.add("Latest Strandings");

        List<String> hotspots = new ArrayList<String>();
        hotspots.add("View Hotspots");
        hotspots.add("Create a Hotspot");
        hotspots.add("Search Hotspots");


        listDataChild.put(listDataHeader.get(0), species);
        listDataChild.put(listDataHeader.get(1), iwdgData);
        listDataChild.put(listDataHeader.get(2), newsFeed);
        listDataChild.put(listDataHeader.get(3), hotspots);
    }

    /* The click listener for ListView in the navigation drawer*/
    private class DrawerItemClickListener implements ExpandableListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            gotoProfile();
        }
    }

    public void selectItem(int groupPosition, int childPosition){

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

    public void closeDrawer(){//}int position){
        //mDrawerList.setItemChecked(position, false);
        mDrawerLayout.closeDrawers();
    }

    protected void gotoGMapActivity(int groupPosition, int childPosition) {
        Intent intent = new Intent(this, GMapActivity.class);
        intent.putExtra("groupPosition", groupPosition);
        intent.putExtra("childPosition", childPosition);
        startActivity(intent);
    }

    protected void gotoWildlifeGeofence(int groupPosition, int childPosition) {
        Intent intent = new Intent(this, WildlifeGeofence.class);
        intent.putExtra("groupPosition", groupPosition);
        intent.putExtra("childPosition", childPosition);
        startActivity(intent);
    }

    protected void gotoNewsFeed(String rssUrl) {
        Intent intent = new Intent(this, NewsFeedActivity.class);
        intent.putExtra("rssUrl", rssUrl);
        startActivity(intent);
    }

    protected void gotoSpeciesGuide() {
        Intent intent = new Intent(this, SpeciesGuide.class);
        startActivity(intent);
    }

    protected void gotoProfile() {
        Intent i = new Intent(this, Profile.class);
        startActivity(i);
    }

    private boolean isLoggedIn() {
        session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    // If the response is successful
                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            user_ID = user.getId();//user id
                            profileName = user.getName();//user's profile name
                        }
                    }
                }
            });
            Request.executeBatchAsync(request);
            return true;
        } else {
            return false;
        }
    }

    private void setProfilePic() {
        new AsyncTask<Void, Void, Bitmap>()
        {
            @Override
            protected Bitmap doInBackground(Void... params)
            {
                // safety check
                if (user_ID == null)
                    return null;

                String url = String.format(
                        "https://graph.facebook.com/%s/picture",
                        user_ID);

                // you'll need to wrap the two method calls
                // which follow in try-catch-finally blocks
                // and remember to close your input stream

                InputStream inputStream = null;
                try {
                    inputStream = new URL(url).openStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap)
            {
                // safety check
                if (bitmap != null
                        && !isChangingConfigurations()
                        && !isFinishing()) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }.execute();
    }
}