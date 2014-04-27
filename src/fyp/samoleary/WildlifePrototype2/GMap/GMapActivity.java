package fyp.samoleary.WildlifePrototype2.GMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.*;

import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import android.view.*;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import android.support.v4.app.DialogFragment;

import com.google.android.gms.maps.model.*;
import com.google.maps.android.clustering.ClusterManager;
import fyp.samoleary.WildlifePrototype2.*;
import fyp.samoleary.WildlifePrototype2.Database.Constants;import fyp.samoleary.WildlifePrototype2.Database.WildlifeDB;
import fyp.samoleary.WildlifePrototype2.Imgur.ImgurUploadTask;
import fyp.samoleary.WildlifePrototype2.NavDrawer.NavDrawer;
import fyp.samoleary.WildlifePrototype2.RSSFeed.NewsFeedActivity;
import fyp.samoleary.WildlifePrototype2.RSSFeed.RSSItem;
import fyp.samoleary.WildlifePrototype2.RSSFeed.RSSParser;
import fyp.samoleary.WildlifePrototype2.Search.SearchActivity;
import fyp.samoleary.WildlifePrototype2.Sighting.Sighting;
import fyp.samoleary.WildlifePrototype2.Sighting.SightingDialog;
import fyp.samoleary.WildlifePrototype2.Sighting.SightingStore;
import fyp.samoleary.WildlifePrototype2.Sighting.SubmitActivity;
import fyp.samoleary.WildlifePrototype2.SpeciesGuide.SpeciesGuide;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 02/02/14
 * Revision: 3
 * Revision History:
 * 1: 02/02/14
 * 2: 11/02/14
 * 3: 12/02/14
 * Description:
 */

public class GMapActivity extends NavDrawer implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMapLongClickListener {

    private final static String USER_COORDINATES = "fyp.samoleary.WildlifePrototype2.COORDINATES";
    private final static String SIGHTING = "fyp.samoleary.WildlifePrototype2.SIGHTING";
    private final static int SUBMIT_REQUEST = 1000;
    private final static int SEARCH_REQUEST = 2000;
    LatLng userTouchPoint;
    String userMarkerID;

    private WildlifeDB wildlifeDB;

    // Marker Clustering
    private ClusterManager<Sighting> mClusterManager;

    // Object to display an AlertDialog
    private ConfirmSightingLocationDialog confirmSightingLocationDialog;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    /**
     * Note if updates have been turned on. Starts out as "false"; is ser to "true" in the
     * method handleRequestSuccess of LocationUpdateReceiver
     *
     */
    boolean mUpdatesRequested = false;

    // Google Map
    private GoogleMap googleMap;

    private HashMap<String, Sighting> mkrObjects;
    private HashMap<String, Marker> sightingMkr;
    private GetConnectivityStatus isConnected;

    private SightingStore sightingStore;
    private int nextID;
    private MyImgurUploadTask mImgurUploadTask;
    private String mImgurUrl;
    private boolean imgurDone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sightingStore = new SightingStore(this);
        isConnected = new GetConnectivityStatus();
        setTitle(R.string.app_name);

        mkrObjects = new HashMap<String, Sighting>();
        sightingMkr = new HashMap<String, Marker>();
        userTouchPoint = new LatLng(0, 0);

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;

        // Open Shared Preferences
        mPrefs = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Get an editor
        mEditor = mPrefs.edit();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);


        // Initialize ClusterManager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<Sighting>(this, ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap());
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap().setOnCameraChangeListener(mClusterManager);
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap().setOnMarkerClickListener(mClusterManager);

        wildlifeDB = new WildlifeDB(this);

        try {
            // Loading map
            initializeMapIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent i = getIntent();
        if (i.getStringExtra("result") == null) {
            //getRecentSightings();
/**            getLocalSightings();*/
        } //else {
        //plotMarkers(i.getStringExtra("result"));
        //getLocalSightings();
        //}

        if (i.getIntExtra("groupPosition", 999) != 999){
            selectItem(i.getIntExtra("groupPosition", 999), i.getIntExtra("childPosition", 999));
            i.putExtra("groupPosition", 999);
        }

        if (savedInstanceState!=null){
            Double lat = savedInstanceState.getDouble("lat", 0);
            Double lng = savedInstanceState.getDouble("lng", 0);

            if(lat != 0 && lng != 0){
                onMapLongClick(new LatLng(lat, lng));
            } else {
                getLocalSightings();
            }
        }
        if (mImgurUrl == null)
            mImgurUrl = "image";
        if(isConnected.isConnected(getApplicationContext())) {
            Log.d(LocationUtils.APPTAG, "GMapActivity: connected");
            if (sightingStore.getSightings() != null) {
                Log.d(LocationUtils.APPTAG, "GMapActivity: create dialog");
                SubmitUnsubmittedSightingsDialog submitUnsubmittedSightingsDialog = SubmitUnsubmittedSightingsDialog.newInstance(
                        R.string.unsub_msg);
                submitUnsubmittedSightingsDialog.show(getSupportFragmentManager(), "dialog");
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (userTouchPoint!=null){
            outState.putDouble("lat", userTouchPoint.latitude);
            outState.putDouble("lng", userTouchPoint.longitude);
        }

    }

    private void checkForNewSightings()  {
        setProgressBarIndeterminateVisibility(true);
        if (isConnected.isConnected(getApplicationContext())) {
            new HttpHandler() {
                @Override
                public HttpUriRequest getHttpRequestMethod() {
                    return new HttpGet("http://fyp-irish-wildlife.herokuapp.com/sightings/getsighting/");

                }
                @Override
                public void onResponse(String result) {
                    getLatestJSONSighting(result);
                }
            }.execute();
            //new RssSightingAsyncTask().execute(getString(R.string.rssfeed_sightings));
        } else {
            Toast.makeText(this, "Cannot check for new sightings, are you connected to the internet?", Toast.LENGTH_LONG).show();
        }

    }

    private void getLocalSightings() {
        googleMap.clear();
        mClusterManager.clearItems();

        wildlifeDB.open();

        Cursor cursor = wildlifeDB.getInfoRssSightingLimit();
        startManagingCursor(cursor);
        if(cursor.moveToFirst()){
            do {
                int ID = cursor.getInt(cursor.getColumnIndex(Constants.SIGHTING_ID));
                int animals = cursor.getInt(cursor.getColumnIndex(Constants.SIGHTING_ANIMALS));
                String sub_date = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_DATE));
                double latitude  = cursor.getDouble(cursor.getColumnIndex(Constants.SIGHTING_LAT));
                double longitude = cursor.getDouble(cursor.getColumnIndex(Constants.SIGHTING_LNG));
                String location = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_LOCATION));
                String species = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_SPECIES));
                String name = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_NAME));
                String imgurl = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_IMAGE));

                //MarkerOptions mo = new MarkerOptions()
                //        .position(new LatLng(latitude, longitude))
                //        .flat(true)
                //        .snippet("Click here for details")
                //        .rotation(0)
                //        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                //Marker myMarker = googleMap.addMarker(mo);
                Sighting mySighting = new Sighting(ID, species, sub_date, latitude, longitude, location, animals, name, imgurl);//, imgUri);
                mClusterManager.addItem(mySighting);
                //mkrObjects.put(myMarker.getId(), mySighting);

            } while(cursor.moveToNext());
            mClusterManager.cluster();
        }
        stopManagingCursor(cursor);
        wildlifeDB.close();
        setProgressBarIndeterminateVisibility(false);
    }

    private int getNextID() {
        nextID = mPrefs.getInt("ID", 0);
        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {
//                setProgressBarIndeterminateVisibility(true);

                return new HttpGet("http://fyp-irish-wildlife.herokuapp.com/sightings/getsighting/");

            }
            @Override
            public void onResponse(String result) {


                JSONArray json;
                try {
                    json = new JSONArray(result);
                    for (int i = 0; i < json.length(); i++) {
                        JSONObject c;
                        try {
                            c = json.getJSONObject(i);

                            int ID = c.getInt("pk");
                            if(ID >= nextID){
                                nextID = ID + 1;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mEditor.putInt("ID", nextID);
                mEditor.commit();
                setProgressBarIndeterminateVisibility(false);
            }
        }.execute();

        return nextID;
    }

    private void plotMarkers(String result) {
        googleMap.clear();
        mClusterManager.clearItems();
        //mkrObjects.clear();
        JSONArray json;
        try {
            json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject c;
                try {
                    c = json.getJSONObject(i);

                    int ID = c.getInt("pk");
                    JSONObject fields = c.getJSONObject("fields");
                    int animals = fields.getInt("animals");
                    String sub_date = fields.getString("sub_date");
                    double latitude = fields.getDouble("latitude");
                    double longitude = fields.getDouble("longitude");
                    String location = fields.getString("location");
                    String species = fields.getString("species");
                    String name = fields.getString("name");
                    String imgurl = fields.getString("imageurl");
                    Sighting sighting = new Sighting(ID, species, sub_date, latitude, longitude, location, animals, name, imgurl);

                    mClusterManager.addItem(sighting);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mClusterManager.cluster();
    }

    @Override
    public void selectItem(int groupPosition, int childPosition) {
        //Log.d(LocationUtils.APPTAG, groupPosition + " : " + childPosition);
        switch (groupPosition) {
            case 0:
                closeDrawer();
                gotoSpeciesGuide();
                break;
            case 1:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        setProgressBarIndeterminateVisibility(true);
                        getLocalSightings();
                        break;
                    case 1:
                        closeDrawer();
                        createReportDialog();
                        getLocalSightings();
                        break;
                    case 2:
                        closeDrawer();
                        gotoSearchActivity();
                        getLocalSightings();
                        break;
                    case 3:
                        closeDrawer();
                        checkForNewSightings();
                    default:
                        break;
                }
                break;
            case 2:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoNewsFeed("http://www.iwdg.ie/index.php?option=com_k2&view=itemlist&task=category&id=1&Itemid=93&format=feed");
                        break;
                    case 1:
                        closeDrawer();
                        gotoNewsFeed("http://www.iwdg.ie/_customphp/iscope/rss_sightings.php");
                        break;
                    case 2:
                        closeDrawer();
                        gotoNewsFeed("http://www.iwdg.ie/_customphp/iscope/rss_strandings.php");
                        break;
                }
                break;
            case 3:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoWildlifeGeofence(groupPosition, childPosition);
                        break;
                    case 1:
                        closeDrawer();
                        gotoWildlifeGeofence(groupPosition, childPosition);
                        break;
                    case 2:
                        closeDrawer();
                        gotoWildlifeGeofence(groupPosition, childPosition);
                        break;
                }
                break;
            case 10:
                switch (childPosition) {
                    case 0:
                        closeDrawer();
                        gotoProfile();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void selectGroup(int groupPosition) {
        switch (groupPosition) {
            case 0:
                closeDrawer();
                gotoSpeciesGuide();
                break;
            default:
                break;
        }
    }

    private void createReportDialog() {
        ReportSightingDialog reportSightingDialog = ReportSightingDialog.newInstance(
                R.string.dialog_report_sighting_selected);
        reportSightingDialog.show(getSupportFragmentManager(), "dialog");
    }


    /**
     * Method to create a map.
     */
    private void initializeMapIfNeeded() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

            // check if map is created successfully or not
            if (googleMap != null) {
                //Toast.makeText(getApplicationContext(), R.string.unable_to_create_maps, Toast.LENGTH_SHORT).show();
                initializeMap();
            }

        }
    }

    private void initializeMap() {
        googleMap.setMyLocationEnabled(true);

        /**googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
        if (mkrObjects.containsKey(marker.getId())) {
        Sighting sighting = mkrObjects.get(marker.getId());
        gotoDisplaySightingDialog(sighting);
        return true;
        } else {
        marker.showInfoWindow();
        return true;
        }
        }
        });**/

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                confirmSightingLocationDialog = ConfirmSightingLocationDialog.newInstance(
                        R.string.dialog_create_sighting_msg);
                confirmSightingLocationDialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                if(marker.isInfoWindowShown()){
                    marker.hideInfoWindow();
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                userTouchPoint = marker.getPosition();
                marker.setTitle("Tap to confirm location and begin submission");
                marker.showInfoWindow();
            }
        });

        googleMap.setOnMapLongClickListener(this);

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<Sighting>() {
            @Override
            public boolean onClusterItemClick(Sighting sighting) {
                gotoDisplaySightingDialog(sighting);
                return false;
            }
        });
        googleMap.getUiSettings().setZoomControlsEnabled(false);
    }

    private void gotoDisplaySightingDialog(Sighting sighting) {
        Intent intent = new Intent(this, SightingDialog.class);
        intent.putExtra(SIGHTING, sighting);
        startActivity(intent);
    }

    @Override
    public void onMapLongClick(LatLng point) {
        dropSightingMarker(point);
    }

    public void doPositiveClick() {
        gotoSubmitActivity();
    }

    public void doNegativeClick() {
    }

    private void gotoSubmitActivity() {
        Intent intent = new Intent(this, SubmitActivity.class);
        intent.putExtra(USER_COORDINATES, userTouchPoint);
        startActivityForResult(intent, SUBMIT_REQUEST);
    }

    private void gotoSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivityForResult(intent, SEARCH_REQUEST);
    }

    /**
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }

    /**
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {

        // Save the current setting for updates
        mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();

        super.onPause();
    }

    /**
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {
        super.onStart();
        //Log.d(LocationUtils.APPTAG, "onStart");
        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        //Log.d(LocationUtils.APPTAG, "onRestart");
    }

    /**
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(LocationUtils.APPTAG, "onResume");
        //getRecentSightings();
/**        getLocalSightings();*/
        //LatLng point = new LatLng(53.41608, -7.93396);
        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 6));

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);

            // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode,intent);
        String county;
        String species;
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));

                        // Display the result
                        Toast.makeText(this, R.string.connected, Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, R.string.resolved, Toast.LENGTH_SHORT).show();
                        Log.d(LocationUtils.APPTAG, getString(R.string.connected));
                        Log.d(LocationUtils.APPTAG, getString(R.string.resolved));
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));

                        // Display the result
                        Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, R.string.no_resolution, Toast.LENGTH_SHORT).show();
                        Log.d(LocationUtils.APPTAG, getString(R.string.disconnected));
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));

                        break;
                }
                break;

            case SEARCH_REQUEST:
                switch (resultCode) {
                    case 10:
                        getLocalSightings();
                        setProgressBarIndeterminateVisibility(false);
                        break;
                    case 11:
                        setProgressBarIndeterminateVisibility(false);
                        Toast.makeText(this,"No result matched your query!" , Toast.LENGTH_SHORT).show();
                        Log.d(LocationUtils.APPTAG, "No result matched your query!");
                        break;
                    case 12:
                        species = intent.getStringExtra("species");
                        searchSpecies(species);
                        break;
                    case 13:
                        county = intent.getStringExtra("county");
                        searchCounty(county);
                        break;
                    case 14:
                        county = intent.getStringExtra("county");
                        species = intent.getStringExtra("species");
                        searchSpeciesCounty(county, species);
                        break;
                }
                break;

            case SUBMIT_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //Sighting mySighting = (Sighting) intent.getSerializableExtra("userSighting");
                        //mClusterManager.addItem(mySighting);
                        //mkrObjects.put(mPrefs.getString("mkrID", "null"), sighting);
                        Marker mkr = sightingMkr.get(mPrefs.getString("mkrID", "null"));
                        sightingMkr.remove(mPrefs.getString("mkrID", "null"));

                        if (mkr != null)
                            mkr.remove();

                        getLocalSightings();

                        /*if (mkr != null) {
                            mkr.setDraggable(false);
                            mkr.hideInfoWindow();
                            sightingMkr.remove(mPrefs.getString("mkrID", "null"));
                        }*/
                        break;

                    case RESULT_CANCELED:
                        Log.d(LocationUtils.APPTAG, "onActivityResult");
                        mkr = sightingMkr.get(mPrefs.getString("mkrID", "null"));
                        sightingMkr.remove(mPrefs.getString("mkrID", "null"));

                        if (mkr != null)
                            mkr.remove();

                        getLocalSightings();
                        LatLng point = new LatLng(53.41608, -7.93396);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 6));
                        break;
                }
                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(LocationUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }

    private void searchSpeciesCounty(String county, String species) {
        wildlifeDB.open();
        plotSearchedMarkers(wildlifeDB.searchCountySpecies(county, species));
        wildlifeDB.close();
    }

    private void searchCounty(String county) {
        wildlifeDB.open();
        plotSearchedMarkers(wildlifeDB.searchCounty(county));
        wildlifeDB.close();
    }

    private void searchSpecies(String species) {
        wildlifeDB.open();
        plotSearchedMarkers(wildlifeDB.searchSpecies(species));
        wildlifeDB.close();
    }

    private void plotSearchedMarkers(Cursor cursor) {
        googleMap.clear();
        mClusterManager.clearItems();

        startManagingCursor(cursor);
        if(cursor.moveToFirst()){
            do {
                int ID = cursor.getInt(cursor.getColumnIndex(Constants.SIGHTING_ID));
                int animals = cursor.getInt(cursor.getColumnIndex(Constants.SIGHTING_ANIMALS));
                String sub_date = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_DATE));
                double latitude  = cursor.getDouble(cursor.getColumnIndex(Constants.SIGHTING_LAT));
                double longitude = cursor.getDouble(cursor.getColumnIndex(Constants.SIGHTING_LNG));
                String location = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_LOCATION));
                String species = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_SPECIES));
                String name = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_NAME));
                String imgurl = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_IMAGE));

                Sighting mySighting = new Sighting(ID, species, sub_date, latitude, longitude, location, animals, name, imgurl);//, imgUri);
                mClusterManager.addItem(mySighting);

            } while(cursor.moveToNext());
            mClusterManager.cluster();
        }
        stopManagingCursor(cursor);
        setProgressBarIndeterminateVisibility(false);
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    protected boolean servicesConnected() {

        // Check that Google Play Services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play Services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
            // Google Play Services was not available for some reason
        } else {
            /* Get the error code
            int errorCode = ConnectionResult.getErrorCode(); */

            // Get the error dialog from Google Play Services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            // If Google Play Services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Invoked by the "Get Location" button.
     *
     * Calls getLastLocation() to get the current location
     *
     */
    public LatLng getLocation() {

        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            if (currentLocation != null) {
                return new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            } else {
                return new LatLng(0, 0);
            }


            // Display the current location in the UI
            //mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
            //Toast.makeText(this, "You're current location:", Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, LocationUtils.getLatLng(this, currentLocation), Toast.LENGTH_SHORT).show();

        } else {
            Log.d(LocationUtils.APPTAG, "Google Play Services Unavailable");
            return new LatLng(0,0);
        }
    }

    /**
     * Invoked by the "Start Updates" button
     * Sends a request to start location updates
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void startUpdates(View v) {
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    /**
     * Invoked by the "Stop Updates" button
     * Sends a request to remove location updates
     * request them.
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void stopUpdates(View v) {
        mUpdatesRequested = false;

        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }

    /**
     * Called by Location Services when the request to connect the client finishes successfully
     * At this point, you can request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        //Toast.makeText(this, R.string.connected, Toast.LENGTH_SHORT).show();
        Log.d(LocationUtils.APPTAG, getString(R.string.connected));

        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
    }

    /**
     * Called by Location Services if the connection to the location client drops
     * because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        //Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
        Log.d(LocationUtils.APPTAG, getString(R.string.disconnected));
    }

    /**
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                 * Thrown if Google Play services cancelled the original
                 * PendingIntent
                 */

            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error
             */
            showErrorDialog(connectionResult.getErrorCode());
        }

    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {

        // Report to the UI that the location was updated
        Toast.makeText(this, R.string.location_updated, Toast.LENGTH_SHORT).show();
        Log.d(LocationUtils.APPTAG, getString(R.string.location_updated));

        // In the UI, set the latitude and longitude to the value received
        Toast.makeText(this, LocationUtils.getLatLng(this, location), Toast.LENGTH_SHORT).show();
        Log.d(LocationUtils.APPTAG, LocationUtils.getLatLng(this, location));
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        //Toast.makeText(this, R.string.location_requested, Toast.LENGTH_SHORT).show();
        Log.d(LocationUtils.APPTAG, getString(R.string.location_requested));
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        //Toast.makeText(this, R.string.location_updates_stopped, Toast.LENGTH_SHORT).show();
        Log.d(LocationUtils.APPTAG, getString(R.string.location_updates_stopped));
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                errorCode,
                this,
                LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), LocationUtils.APPTAG);
        }
    }

    public static class ConfirmSightingLocationDialog extends DialogFragment {

        public static ConfirmSightingLocationDialog newInstance(int title) {
            ConfirmSightingLocationDialog frag = new ConfirmSightingLocationDialog();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(title)
                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((GMapActivity)getActivity()).doPositiveClick();
                        }
                    })
                    /*.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((GMapActivity)getActivity()).doNegativeClick();
                        }
                    })*/
                    .setNeutralButton(R.string.dialog_remove_marker, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((GMapActivity)getActivity()).doNeutralClick();
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private void doNeutralClick() {
        Marker mkr = sightingMkr.get(mPrefs.getString("mkrID", "null"));
        mkr.remove();
        userTouchPoint = null;
    }

    public static class ReportSightingDialog extends DialogFragment {

        public static ReportSightingDialog newInstance(int title) {
            ReportSightingDialog frag = new ReportSightingDialog();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(title)
                    .setPositiveButton(R.string.dialog_current_location, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((GMapActivity)getActivity()).doReportAtCurrentLocationClick();
                        }
                    })
                    .setNeutralButton(R.string.dialog_chosen_location, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((GMapActivity) getActivity()).doReportAtChosenLocationClick();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((GMapActivity) getActivity()).doCancelReportClick();
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private void doReportAtChosenLocationClick() {
        Toast.makeText(GMapActivity.this, "Long Press anywhere on the map to create the Sighting", Toast.LENGTH_LONG).show();
    }

    private void doCancelReportClick() {

    }

    private void doReportAtCurrentLocationClick() {
        LatLng userLocation= getLocation();
        if (userLocation.latitude == 0 && userLocation.longitude == 0) {
            Toast.makeText(GMapActivity.this, "Unable to fetch current location, please try again!", Toast.LENGTH_LONG).show();
        } else {
            dropSightingMarker(userLocation);
        }
    }

    public void dropSightingMarker(LatLng point) {
        Marker mkrOld = sightingMkr.get(mPrefs.getString("mkrID", "null"));
        if (mkrOld==null){
            Log.d("Placing Marker: ", "No Previous Markers Set");
        } else {
            mkrOld.remove();
            sightingMkr.remove(mPrefs.getString("mkrID", "null"));
        }
        userTouchPoint = point;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 11));
        Marker mkr = googleMap.addMarker(new MarkerOptions()
                .position(userTouchPoint)
                .title("Long Press to Drag me or tap to begin submission!")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mkr.showInfoWindow();
        userMarkerID = mkr.getId();
        sightingMkr.put(userMarkerID, mkr);
        mEditor.putString("mkrID", userMarkerID);
        mEditor.commit();
    }

    private class RssSightingAsyncTask extends AsyncTask<String, Void, String> {
        RSSParser rssParser = new RSSParser();
        List<RSSItem> rssItems = new ArrayList<RSSItem>();

        @Override
        protected void onPreExecute() {
            wildlifeDB.open();
            //wildlifeDB.dropTableRssSighting();
            /*pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setMessage("Checking for recent sightings");
            pDialog.show();*/

            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String... urls) {
            String url = urls[0];
            // list of rss items
            rssItems = rssParser.getRSSFeedItems(url);

            Document doc;//Jsoup.parse(rssItems.get(0).getLink());
            try {
                for(RSSItem rssItem: rssItems) {
                    doc = Jsoup.connect(rssItem.getLink())
                            .userAgent("Mozilla/5.0 Gecko/20100101 Firefox/21.0")
                            .timeout(0)
                            .get();

                    Element id = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(0);
                    //Log.d(LocationUtils.APPTAG, id.ownText());

                    if(!checkSightingExists(id.ownText())){
                        Element name = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(30);
                        //Log.d(LocationUtils.APPTAG, name.ownText());

                        Element species = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td a").first();
                        Element date = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(5);
                        Element lat = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(18);
                        Element lng = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(19);
                        Element location = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(3);
                        Element animals = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(7);
                        if(species == null) {
                            Element nametwo = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(29);
                            //Log.d(LocationUtils.APPTAG, nametwo.ownText());

                            Element speciesText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(2);
                            Element latText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(16);
                            Element lngText = doc.select("html body#bd.fs3 div#ja-wrapper div#ja-containerwrap-fr div#ja-containerwrap2 div#ja-container div#ja-container2.clearfix div#ja-mainbody-fr.clearfix div#ja-contentwrap div#ja-content div#k2Container.itemView div.itemBody div.itemFullText div table.table1 tbody tr td").get(17);

                            Sighting sighting = new Sighting(Integer.parseInt(id.ownText()), speciesText.ownText(), date.ownText(), latText.ownText(), lngText.ownText(), location.ownText(), animals.ownText(), nametwo.ownText());
                            wildlifeDB.insertInfoRssSighting(sighting);
                            //Log.d(LocationUtils.APPTAG, "Sighting added: species = null");
                        } else {
                            Sighting sighting = new Sighting(Integer.parseInt(id.ownText()), species.ownText(), date.ownText(), lat.ownText(), lng.ownText(), location.ownText(), animals.ownText(), name.ownText());
                            wildlifeDB.insertInfoRssSighting(sighting);
                            //Log.d(LocationUtils.APPTAG, "Sighting added: species != null");
                        }
                    } else {
                        //Log.d(LocationUtils.APPTAG, "Sighting not added");
                        return null;
                    }

                    /*for(Element element: elements){
                        Log.d(LocationUtils.APPTAG, element.ownText());
                    }*/
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // dismiss the dialog after getting all products
            //pDialog.dismiss();
            wildlifeDB.close();
/*            getLocalSightings();*/
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private boolean checkSightingExists(String id) {

        Cursor cursor = wildlifeDB.getInfoRssSighting();
        startManagingCursor(cursor);
        if(cursor.moveToFirst()){
            do {
                int existingID = cursor.getInt(cursor.getColumnIndex(Constants.SIGHTING_ID));
                if(id.equals(Integer.toString(existingID))){
                    return true;
                }
            } while(cursor.moveToNext());
        }
        stopManagingCursor(cursor);
        return false;
    }

    private void getLatestJSONSighting(String result) {

        JSONArray json;
        try {
            json = new JSONArray(result);
            wildlifeDB.open();
            for (int i = 0; i < json.length(); i++) {
                JSONObject c;
                try {
                    c = json.getJSONObject(i);


                    int ID = c.getInt("pk");

                    if(!checkSightingExists(String.valueOf(ID))) {
                        JSONObject fields = c.getJSONObject("fields");
                        int animals = fields.getInt("animals");
                        String sub_date = fields.getString("sub_date");
                        double latitude = fields.getDouble("latitude");
                        double longitude = fields.getDouble("longitude");
                        String location = fields.getString("location");
                        String species = fields.getString("species");
                        String name = fields.getString("name");
                        String imgurl = fields.getString("imageurl");
                        Sighting sighting = new Sighting(ID, species, sub_date, latitude, longitude, location, animals, name, imgurl);
                        wildlifeDB.insertInfoRssSighting(sighting);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            wildlifeDB.close();
            getLocalSightings();
            setProgressBarIndeterminateVisibility(false);
            Toast.makeText(this, "Map Refreshed", Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static class SubmitUnsubmittedSightingsDialog extends DialogFragment {

        public static SubmitUnsubmittedSightingsDialog newInstance(int title) {
            SubmitUnsubmittedSightingsDialog frag = new SubmitUnsubmittedSightingsDialog();
            Bundle args = new Bundle();
            args.putInt("title", title);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int title = getArguments().getInt("title");

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(title)
                    .setPositiveButton(R.string.unsub_submit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((GMapActivity)getActivity()).doSubmitClick();
                        }
                    })
                    .setNegativeButton(R.string.unsub_dontsubmit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((GMapActivity)getActivity()).doNotSubmitClick();
                        }
                    })
                    .setNeutralButton(R.string.unsub_edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((GMapActivity)getActivity()).doEditClick();
                        }
                    });

            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private void doSubmitClick() {
        ArrayList<Sighting> sightings = new ArrayList<Sighting>(sightingStore.getSightings());
        for (Sighting sighting : sightings) {
            Log.d(LocationUtils.APPTAG, "image uri: " + sighting.getImgUrlString());
            sightingStore.clearSighting(String.valueOf(sighting.getID()));
            sighting.setID(getNextID());
            mkrObjects.put(String.valueOf(sighting.getID()), sighting);
            if (!sighting.getImgUrlString().equals("image")) {
                new MyImgurUploadTask(Uri.parse(sighting.getImgUrlString()), sighting).execute();
            } else {
                new HttpAsyncTask().execute("http://fyp-irish-wildlife.herokuapp.com/sightings/postsighting/", String.valueOf(sighting.getID()));
            }
        }
    }

    private void doNotSubmitClick() {

    }

    private void doEditClick() {

    }
    public static String POST(String url, Sighting sightingSubmit){
        InputStream inputStream;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json;

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("species", sightingSubmit.getSpecies());
            jsonObject.accumulate("date", sightingSubmit.getDate());
            jsonObject.accumulate("lat", sightingSubmit.getSightingLat());
            jsonObject.accumulate("lng", sightingSubmit.getSightingLong());
            jsonObject.accumulate("location", sightingSubmit.getLocation());
            jsonObject.accumulate("animals", sightingSubmit.getAnimals());
            jsonObject.accumulate("name", sightingSubmit.getName());
            jsonObject.accumulate("imageurl", sightingSubmit.getImgUrlString());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();
            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(searchActivity);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                Log.d(LocationUtils.APPTAG, "SubmitActivity: POST: inputStream != null");
                result = convertInputStreamToString(inputStream);
            }
            else {
                Log.d(LocationUtils.APPTAG, "SubmitActivity: POST: inputStream = null: did not work");
                result = "Did not work!";
            }

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected String doInBackground(String... urls) {
            Log.d(LocationUtils.APPTAG, "GMapActivity: HttpAsyncTask: doInBackground");
            Sighting sighting = mkrObjects.get(urls[1]);
            return POST(urls[0], sighting);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            if (showSuccess(result)) {

            }
            setProgressBarIndeterminateVisibility(false);
            getLocalSightings();
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        StringBuilder result = new StringBuilder();
        while((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }

        inputStream.close();
        Log.d(LocationUtils.APPTAG, "SubmitActivity: convertInputStreamToString: result.To.String(): " + result.toString());

        return result.toString();

    }

    private boolean showSuccess(String result) {
        //generateNoteOnSD("ErrorHtml", result);
        JSONArray json;
        try {
            json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject c;
                try {
                    c = json.getJSONObject(i);

                    String success = c.getString("success");
                    if (success.equals("success")){
                        Log.d(LocationUtils.APPTAG, "isSuccessful: successful");
                        return true;
                    } else {
                        Log.d(LocationUtils.APPTAG, "isSuccessful: unsuccessful");
                        return false;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    private class MyImgurUploadTask extends ImgurUploadTask {
        private Sighting imgurSighting;
        public MyImgurUploadTask(Uri imageUri, Sighting sighting) {
            super(imageUri, getContentResolver());
            imgurSighting = sighting;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mImgurUploadTask != null) {
                boolean cancelled = mImgurUploadTask.cancel(false);
                if (!cancelled)
                    this.cancel(true);
            }
            mImgurUploadTask = this;
            mImgurUrl = null;
            //getView().findViewById(R.id.choose_image_button).setEnabled(false);
            //setImgurUploadStatus(R.string.choose_image_upload_status_uploading);
        }
        @Override
        protected void onPostExecute(String imageId) {
            super.onPostExecute(imageId);
            mImgurUploadTask = null;
            if (imageId != null) {
                mImgurUrl = "" + imageId;
                Log.d(LocationUtils.APPTAG, "imgur upload success: " + mImgurUrl);
                //setImgurUploadStatus(R.string.choose_image_upload_status_success);
                //if (isResumed()) {
                //    getView().findViewById(R.id.imgur_link_layout).setVisibility(View.VISIBLE);
                //    ((TextView) getView().findViewById(R.id.link_url)).setText(mImgurUrl);
                //}
            } else {
                mImgurUrl = "image";
                Log.d(LocationUtils.APPTAG, "imgur upload error");
                //setImgurUploadStatus(R.string.choose_image_upload_status_failure);
                /*if (isResumed()) {
                    getView().findViewById(R.id.imgur_link_layout).setVisibility(View.GONE);
                    if (isVisible()) {
                        ((ImageView) getView().findViewById(R.id.choose_image_preview)).setImageBitmap(null);
                        Toast.makeText(getActivity(), R.string.imgur_upload_error, Toast.LENGTH_LONG).show();
                    }
                }*/
            }
            imgurSighting.setImgUrl(mImgurUrl);
            new HttpAsyncTask().execute("http://fyp-irish-wildlife.herokuapp.com/sightings/postsighting/", String.valueOf(imgurSighting.getID()));

            /*submitSighting();
            if (publish){
                publish = false;
                publishStory();
            }
            if (isVisible())
                getView().findViewById(R.id.choose_image_button).setEnabled(true);*/
        }
    }

}
