package fyp.samoleary.WildlifePrototype2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;

import android.content.*;

import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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

    // Object to display an AlertDialog
    private SightingAlertDialogFragment sightingAlertDialog;

    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    /*
     * Note if updates have been turned on. Starts out as "false"; is ser to "true" in the
     * method handleRequestSuccess of LocationUpdateReceiver
     *
     */
    boolean mUpdatesRequested = false;

    // Google Map
    private GoogleMap googleMap;

    private HashMap<String, Sighting> mkrObjects;
    private HashMap<String, Marker> sightingMkr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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

        try {
            // Loading map
            initializeMapIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent i = getIntent();
        if (i.getStringExtra("result") == null) {
            getRecentSightings();
        } else {
            plotMarkers(i.getStringExtra("result"));
            getLocalSightings();
        }

    }

    private void getLocalSightings() {
        WildlifeDB wildlifeDB = new WildlifeDB(this);
        wildlifeDB.open();

        Cursor cursor = wildlifeDB.getInfo();
        startManagingCursor(cursor);
        if(cursor.moveToFirst()){
            do {
                int animals = cursor.getInt(cursor.getColumnIndex(Constants.SIGHTING_ANIMALS));
                String sub_date = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_DATE));
                double latitude  = cursor.getDouble(cursor.getColumnIndex(Constants.SIGHTING_LAT));
                double longitude = cursor.getDouble(cursor.getColumnIndex(Constants.SIGHTING_LNG));
                String location = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_LOCATION));
                String species = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_SPECIES));
                String imgUri = cursor.getString(cursor.getColumnIndex(Constants.SIGHTING_IMGURI));

                MarkerOptions mo = new MarkerOptions()
                        .position(new LatLng(latitude, longitude))
                        .flat(true)
                        .snippet("Click here for details")
                        .rotation(0)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                Marker myMarker = googleMap.addMarker(mo);
                Sighting mySighting = new Sighting(species, sub_date, latitude, longitude, location, animals, imgUri);
                mkrObjects.put(myMarker.getId(), mySighting);

            } while(cursor.moveToNext());
        }
    }

    private void getRecentSightings() {
        new HttpHandler() {
            @Override
            public HttpUriRequest getHttpRequestMethod() {

                return new HttpGet("http://fyp-irish-wildlife.herokuapp.com/sightings/getsighting/");

            }
            @Override
            public void onResponse(String result) {
                plotMarkers(result);
                getLocalSightings();
            }
        }.execute();
    }

    private void plotMarkers(String result) {
        googleMap.clear();
        JSONArray json;
        try {
            json = new JSONArray(result);
            for (int i = 0; i < json.length(); i++) {
                JSONObject c;
                try {
                    c = json.getJSONObject(i);

                    JSONObject fields = c.getJSONObject("fields");
                    int animals = fields.getInt("animals");
                    String sub_date = fields.getString("sub_date");
                    double latitude = fields.getDouble("latitude");
                    double longitude = fields.getDouble("longitude");
                    String location = fields.getString("location");
                    String species = fields.getString("species");

                    MarkerOptions mo = new MarkerOptions()
                            .position(new LatLng(latitude, longitude))
                            .flat(true)
                            .snippet("Click here for details")
                            .rotation(0);

                    Marker myMarker = googleMap.addMarker(mo);
                    Sighting mySighting = new Sighting(species, sub_date, latitude, longitude, location, animals);
                    mkrObjects.put(myMarker.getId(), mySighting);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gmapcontextmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.gmap_refresh).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.gmap_refresh:
                getRecentSightings();
                return true;
            case R.id.gmap_help:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void selectItem(int position) {
        switch (position) {
            case 0:
                closeDrawer(position);
                gotoProfile();
                break;
            case 1:
                closeDrawer(position);
                getRecentSightings();
                break;
            case 2:
                break;
            case 3:
                closeDrawer(position);
                gotoSearchActivity();
                break;
            case 4:
                closeDrawer(position);
                gotoSpeciesGuide();
                break;
            default:
                break;
        }
    }

    private void gotoSpeciesGuide() {
        Intent intent = new Intent(this, SpeciesGuide.class);
        startActivity(intent);
    }

    private void gotoProfile() {
        Intent i = new Intent(this, Profile.class);
        startActivity(i);
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

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
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
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                sightingAlertDialog = SightingAlertDialogFragment.newInstance(
                        R.string.dialog_create_sighting_msg);
                sightingAlertDialog.show(getSupportFragmentManager(), "dialog");
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
                marker.setTitle("Confirm location and begin submission");
                marker.showInfoWindow();
            }
        });

        googleMap.setOnMapLongClickListener(this);
    }

    private void gotoDisplaySightingDialog(Sighting sighting) {
        Intent intent = new Intent(this, SightingDialog.class);
        intent.putExtra(SIGHTING, sighting);
        startActivity(intent);
    }

    @Override
    public void onMapLongClick(LatLng point) {
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
                .title("Drag me or tap to begin submission!")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mkr.showInfoWindow();
        userMarkerID = mkr.getId();
        sightingMkr.put(userMarkerID, mkr);
        mEditor.putString("mkrID", userMarkerID);
        mEditor.commit();
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
    /*
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

    /*
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

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();

    }

    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    protected void onResume() {
        super.onResume();
        initializeMapIfNeeded();

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

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

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
                        break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(LocationUtils.APPTAG, getString(R.string.no_resolution));

                        // Display the result
                        Toast.makeText(this, R.string.disconnected, Toast.LENGTH_SHORT).show();
                        Toast.makeText(this, R.string.no_resolution, Toast.LENGTH_SHORT).show();

                        break;
                }

            case SEARCH_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        String result = intent.getStringExtra("result");
                        plotMarkers(result);
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(this,"No result matched your query!" , Toast.LENGTH_SHORT).show();
                        break;
                }

            case SUBMIT_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Sighting sighting = (Sighting) intent.getSerializableExtra("userSighting");
                        mkrObjects.put(mPrefs.getString("mkrID", "null"), sighting);
                        Marker mkr = sightingMkr.get(mPrefs.getString("mkrID", "null"));
                        if (mkr != null) {
                            mkr.setDraggable(false);
                            mkr.hideInfoWindow();
                            sightingMkr.remove(mPrefs.getString("mkrID", "null"));
                        }
                        break;
                    case Activity.RESULT_CANCELED:

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

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

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
    public void getLocation() {

        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            // Display the current location in the UI
            //mLatLng.setText(LocationUtils.getLatLng(this, currentLocation));
            Toast.makeText(this, "You're current location:", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, LocationUtils.getLatLng(this, currentLocation), Toast.LENGTH_SHORT).show();
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

    /*
     * Called by Location Services when the request to connect the client finishes successfully
     * At this point, you can request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, R.string.connected, Toast.LENGTH_SHORT).show();

        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
    }

    /*
     * Called by Location Services if the connection to the location client drops
     * because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, R.string.disconnected,
                Toast.LENGTH_SHORT).show();
    }

    /*
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

        // In the UI, set the latitude and longitude to the value received
        Toast.makeText(this, LocationUtils.getLatLng(this, location), Toast.LENGTH_SHORT).show();
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        Toast.makeText(this, R.string.location_requested, Toast.LENGTH_SHORT).show();
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        Toast.makeText(this, R.string.location_updates_stopped, Toast.LENGTH_SHORT).show();
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


    public static class SightingAlertDialogFragment extends DialogFragment {

        public static SightingAlertDialogFragment newInstance(int title) {
            SightingAlertDialogFragment frag = new SightingAlertDialogFragment();
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
                    .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((GMapActivity)getActivity()).doNegativeClick();
                        }
                    })
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
    }
}
