package fyp.samoleary.WildlifePrototype2.WildlifeGeofencePkg;

import android.app.Activity;import android.app.Dialog;import android.content.*;import android.database.Cursor;import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;import android.support.v4.content.LocalBroadcastManager;import android.text.TextUtils;import android.text.format.DateUtils;import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;import com.google.android.gms.common.ConnectionResult;import com.google.android.gms.common.GooglePlayServicesUtil;import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;
import fyp.samoleary.WildlifePrototype2.Database.Constants;import fyp.samoleary.WildlifePrototype2.Database.WildlifeDB;import fyp.samoleary.WildlifePrototype2.GMap.GMapActivity;import fyp.samoleary.WildlifePrototype2.LocationUtils;import fyp.samoleary.WildlifePrototype2.NavDrawer.NavDrawer;import fyp.samoleary.WildlifePrototype2.R;

import java.util.ArrayList;import java.util.Collections;import java.util.HashMap;import java.util.List;

public class WildlifeGeofence extends NavDrawer implements
        GoogleMap.OnMapLongClickListener {

    /*
     * Use to set an expiration time for a geofence. After this amount
     * of time Location Services will stop tracking the geofence.
     * Remember to unregister a geofence when you're finished with it.
     * Otherwise, your app will use up battery. To continue monitoring
     * a geofence indefinitely, set the expiration time to
     * Geofence#NEVER_EXPIRE.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;

    // Store the current request
    private GeofenceUtils.REQUEST_TYPE mRequestType;

    // Store the current type of removal
    private GeofenceUtils.REMOVE_TYPE mRemoveType;

    // Persistent storage for geofences
    private SimpleGeofenceStore mPrefs;

    // Store a list of geofences to add
    List<Geofence> mCurrentGeofences;

    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;
    // Remove geofences handler
    private GeofenceRemover mGeofenceRemover;

    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;

    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    // Store the list of geofences to remove
    private List<String> mGeofenceIdsToRemove;

    // Google Map
    private GoogleMap googleMap;
    private SeekBar seekBar;
    private Button submitBtn;
    private HashMap<String, Marker> geofenceMkr;
    private HashMap<String, Circle> geofenceCircles;
    private HashMap<String, String> markerIDs;
    private Circle myCircle;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor mEditor;
    private WildlifeDB wildlifeDB;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geofence);

        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceSampleReceiver();

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Instantiate a new geofence storage area
        mPrefs = new SimpleGeofenceStore(this);

        // Instantiate the current List of geofences
        mCurrentGeofences = new ArrayList<Geofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

        // Instantiate a Geofence remover
        mGeofenceRemover = new GeofenceRemover(this);

        // Open Shared Preferences
        sharedPreferences = getSharedPreferences(LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        // Get an editor
        mEditor = sharedPreferences.edit();

        geofenceMkr = new HashMap<String, Marker>();
        geofenceCircles = new HashMap<String, Circle>();
        markerIDs = new HashMap<String, String>();
        wildlifeDB = new WildlifeDB(this);
        geofenceMkr = new HashMap<String, Marker>();
        submitBtn = (Button) findViewById(R.id.submitBtn);
        submitBtn.setVisibility(View.INVISIBLE);
        submitBtn.setText("Create Hotspot");
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRegisterClicked(v);
            }
        });

        seekBar = (SeekBar) findViewById(R.id.hotspotSeekBar);
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                myCircle.setRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        try {
            // Loading map
            initializeMapIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLocalHotspots();
        googleMap.setPadding(0,0,0,15);
    }

    /*
    * Handle results returned to this Activity by other Activities started with
    * startActivityForResult(). In particular, the method onConnectionFailed() in
    * GeofenceRemover and GeofenceRequester may call startResolutionForResult() to
    * start an Activity that handles Google Play services problems. The result of this
    * call returns here, to onActivityResult.
    * calls
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case GeofenceUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to add geofences
                        if (GeofenceUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Toggle the request flag and send a new request
                            mGeofenceRequester.setInProgressFlag(false);

                            // Restart the process of adding the current geofences
                            mGeofenceRequester.addGeofences(mCurrentGeofences);

                            // If the request was to remove geofences
                        } else if (GeofenceUtils.REQUEST_TYPE.REMOVE == mRequestType ){

                            // Toggle the removal flag and send a new removal request
                            mGeofenceRemover.setInProgressFlag(false);

                            // If the removal was by Intent
                            if (GeofenceUtils.REMOVE_TYPE.INTENT == mRemoveType) {

                                // Restart the removal of all geofences for the PendingIntent
                                mGeofenceRemover.removeGeofencesByIntent(
                                        mGeofenceRequester.getRequestPendingIntent());

                                // If the removal was by a List of geofence IDs
                            } else {

                                // Restart the removal of the geofence list
                                mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);
                            }
                        }
                        break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(GeofenceUtils.APPTAG, getString(R.string.no_resolution));
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(GeofenceUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }

    /*
     * Whenever the Activity resumes, reconnect the client to Location
     * Services and reload the last geofences that were set
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, mIntentFilter);
        /*
         * Get existing geofences from the latitude, longitude, and
         * radius values stored in SharedPreferences. If no values
         * exist, null is returned.
         */

        /*
         * If the returned geofences have values, use them to set
         * values in the UI, using the previously-defined number
         * formats.
         *
        if (mUIGeofence1 != null) {

        }*/
    }

    /*
     * Save the current geofence settings in SharedPreferences.
     */
    @Override
    protected void onPause() {
        super.onPause();

    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(GeofenceUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Called when the user clicks the "Remove geofences" button
     *
     * @param view The view that triggered this callback
     */
    public void onUnregisterByPendingIntentClicked(View view) {
        /*
         * Remove all geofences set by this app. To do this, get the
         * PendingIntent that was added when the geofences were added
         * and use it as an argument to removeGeofences(). The removal
         * happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done
         */

        /*
         * Record the removal as remove by Intent. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        // Record the type of removal
        mRemoveType = GeofenceUtils.REMOVE_TYPE.INTENT;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        // Try to make a removal request
        try {
        /*
         * Remove the geofences represented by the currently-active PendingIntent. If the
         * PendingIntent was removed for some reason, re-create it; since it's always
         * created with FLAG_UPDATE_CURRENT, an identical PendingIntent is always created.
         */
            mGeofenceRemover.removeGeofencesByIntent(mGeofenceRequester.getRequestPendingIntent());

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
                    Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Called when the user clicks the "Remove geofence 1" button
     * @param view The view that triggered this callback
     */
    public void onUnregisterGeofence1Clicked(View view) {
        /*
         * Remove the geofence by creating a List of geofences to
         * remove and sending it to Location Services. The List
         * contains the id of geofence 1 ("1").
         * The removal happens asynchronously; Location Services calls
         * onRemoveGeofencesByPendingIntentResult() (implemented in
         * the current Activity) when the removal is done.
         */

        // Create a List of 1 Geofence with the ID "1" and store it in the global list
        mGeofenceIdsToRemove = Collections.singletonList("1");

        /*
         * Record the removal as remove by list. If a connection error occurs,
         * the app can automatically restart the removal if Google Play services
         * can fix the error
         */
        mRemoveType = GeofenceUtils.REMOVE_TYPE.LIST;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        // Try to remove the geofence
        try {
            mGeofenceRemover.removeGeofencesById(mGeofenceIdsToRemove);

            // Catch errors with the provided geofence IDs
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.remove_geofences_already_requested_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when the user clicks the "Register geofences" button.
     * Get the geofence parameters for each geofence and add them to
     * a List. Create the PendingIntent containing an Intent that
     * Location Services sends to this app's broadcast receiver when
     * Location Services detects a geofence transition. Send the List
     * and the PendingIntent to Location Services.
     */
    public void onRegisterClicked(View view) {

        /*
         * Record the request as an ADD. If a connection error occurs,
         * the app can automatically restart the add request if Google Play services
         * can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

        /*
         * Check for Google Play services. Do this after
         * setting the request type. If connecting to Google Play services
         * fails, onActivityResult is eventually called, and it needs to
         * know what type of request was in progress.
         */
        if (!servicesConnected()) {

            return;
        }

        /*
         * Create a version of geofence 1 that is "flattened" into individual fields. This
         * allows it to be stored in SharedPreferences.
         */

        wildlifeDB.open();
        int idInt = wildlifeDB.getMaxTableID(Constants.TABLE_NAME_HOTSPOTS);
        String id = String.valueOf(idInt) + 1;
        // sharedPreferences.getString("geofenceMkrID", null);
        SimpleGeofence myGeofence = new SimpleGeofence(
                id,
                myCircle.getCenter().latitude,
                myCircle.getCenter().longitude,
                (float) myCircle.getRadius(),
                GEOFENCE_EXPIRATION_IN_MILLISECONDS,
                Geofence.GEOFENCE_TRANSITION_ENTER
        );

        mPrefs.setGeofence(id, myGeofence);

        Marker mkr = geofenceMkr.get(id);

        markerIDs.put(mkr.getId(), id);

        Circle newCircle = googleMap.addCircle(new CircleOptions()
                .radius(myCircle.getRadius())
                .center(myCircle.getCenter())
                .strokeWidth(myCircle.getStrokeWidth())
                .fillColor(myCircle.getFillColor()));

        geofenceCircles.put(id, newCircle);

        if (mkr != null) {
            mkr.setDraggable(false);
            mkr.hideInfoWindow();
            mkr.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            geofenceMkr.remove(id);
        }

        /*
         * Add Geofence objects to a List. toGeofence()
         * creates a Location Services Geofence object from a
         * flat object
         */
        mCurrentGeofences.add(myGeofence.toGeofence());

        submitBtn.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);

        // Start the request. Fail if there's already a request in progress
        try {
            wildlifeDB.insertInfoHotspots(myGeofence, "Blue Whale");
            wildlifeDB.close();

            // Try to add geofences
            mGeofenceRequester.addGeofences(mCurrentGeofences);
        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            Toast.makeText(this, R.string.add_geofences_already_requested_error,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

                // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                            ||
                            TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

                // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

                // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }
    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Method to create a map.
     */
    private void initializeMapIfNeeded() {
        if (googleMap == null) {
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.geofenceMap)).getMap();

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
            String id = markerIDs.get(marker.getId());
            wildlifeDB.open();
            String species = wildlifeDB.getSpecies(id);
            Log.d(LocationUtils.APPTAG, "Species: " + species);
            wildlifeDB.close();

            marker.setTitle(species);
            marker.showInfoWindow();
            return true;
        }
        });

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {
                myCircle.setCenter(marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });

        googleMap.setOnMapLongClickListener(this);

    }

    @Override
    public void onMapLongClick(LatLng point) {
        wildlifeDB.open();
        int idInt = wildlifeDB.getMaxTableID(Constants.TABLE_NAME_HOTSPOTS);
        String id = String.valueOf(idInt) + 1;
        wildlifeDB.close();

        Marker mkrOld = null;
        try {
            mkrOld = geofenceMkr.get(id);
        } catch (NullPointerException e) {
            Log.e(LocationUtils.APPTAG, "mkrOld = null");
        }

        if (mkrOld != null) {
            mkrOld.remove();
            geofenceMkr.remove(id);
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 11));

        Marker mkr = googleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Move Seekbar to adjust size of Hotspot")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        mkr.showInfoWindow();

        geofenceMkr.put(id, mkr);

        if (myCircle == null) {
            Log.d("SettingCircle: ", "No Previous Circles Set");
        } else {
            myCircle.remove();
        }

        myCircle = googleMap.addCircle(new CircleOptions()
                .center(point)
                .radius(750)
                .fillColor(Color.parseColor("#40576494"))
                .strokeWidth(1.5f));

        submitBtn.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(750);
    }

    private void getLocalHotspots() {
        googleMap.clear();
        mCurrentGeofences.clear();

        wildlifeDB.open();
        //wildlifeDB.dropTable(Constants.TABLE_NAME_HOTSPOTS);

        Cursor cursor = wildlifeDB.getInfoHotspots();
        stopManagingCursor(cursor);
        if(cursor.moveToFirst()) {
            do {
                SimpleGeofence myGeofence = new SimpleGeofence(
                        String.valueOf(cursor.getInt(cursor.getColumnIndex(Constants.HOTSPOTS_ID))),
                        cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_LAT)),
                        cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_LNG)),
                        (float) cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_RADIUS)),
                        GEOFENCE_EXPIRATION_IN_MILLISECONDS,
                        Geofence.GEOFENCE_TRANSITION_ENTER
                );

                Circle circle = googleMap.addCircle(new CircleOptions()
                        .center(new LatLng(cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_LAT)),
                                cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_LNG))))
                        .radius(cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_RADIUS)))
                        .fillColor(Color.parseColor("#40576494"))
                        .strokeWidth(1.5f));

                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .draggable(false)
                        .position(new LatLng(cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_LAT)),
                                cursor.getDouble(cursor.getColumnIndex(Constants.HOTSPOTS_LNG)))));

                markerIDs.put(marker.getId(), String.valueOf(cursor.getInt(cursor.getColumnIndex(Constants.HOTSPOTS_ID))));
                geofenceCircles.put(String.valueOf(cursor.getInt(cursor.getColumnIndex(Constants.HOTSPOTS_ID))), circle);

                mPrefs.setGeofence(String.valueOf(cursor.getInt(cursor.getColumnIndex(Constants.HOTSPOTS_ID))), myGeofence);

                mCurrentGeofences.add(myGeofence.toGeofence());
            } while(cursor.moveToNext());
        }
        wildlifeDB.close();
        stopManagingCursor(cursor);
    }
}