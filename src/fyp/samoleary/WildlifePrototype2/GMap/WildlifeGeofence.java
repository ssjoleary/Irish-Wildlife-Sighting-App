package fyp.samoleary.WildlifePrototype2.GMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import fyp.samoleary.WildlifePrototype2.GetConnectivityStatus;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WildlifeGeofence extends GMapActivity implements
        LocationClient.OnAddGeofencesResultListener {
    /*
         * Use to set an expiration time for a geofence. After this amount
         * of time Location Services will stop tracking the geofence.
         */
    private static final long SECONDS_PER_HOUR = 60;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    private static final long GEOFENCE_EXPIRATION_TIME =
            GEOFENCE_EXPIRATION_IN_HOURS *
                    SECONDS_PER_HOUR *
                    MILLISECONDS_PER_SECOND;

    private GetConnectivityStatus isConnected;
    /*
     * Note if updates have been turned on. Starts out as "false"; is ser to "true" in the
     * method handleRequestSuccess of LocationUpdateReceiver
     *
     */
    boolean mUpdatesRequested = false;
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;
    // Google Map
    private GoogleMap googleMap;

    private SeekBar seekBar;
    private HashMap<String, Marker> geofenceMkr;
    // Internal List of Geofence objects
    List<Geofence> mGeofenceList;
    // Persistent storage for geofences
    private SimpleGeofenceStore mGeofenceStorage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geofence);
        // Instantiate a new geofence storage area
        mGeofenceStorage = new SimpleGeofenceStore(this);
        // Instantiate the current List of geofences
        mGeofenceList = new ArrayList<Geofence>();

        geofenceMkr = new HashMap<String, Marker>();

        seekBar = (SeekBar) findViewById(R.id.hotspotSeekBar);
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SimpleGeofence simpleGeofence = mGeofenceStorage.getGeofence(mPrefs.getString("geofenceMkrID", null));
                simpleGeofence.setmRadius(progress);
                Log.d(LocationUtils.APPTAG, "Progress: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        isConnected = new GetConnectivityStatus();
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

        try {
            // Loading map
            initializeMapIfNeeded();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {

    }

    /**
     * A single Geofence object, defined by its center and radius.
     */
    public class SimpleGeofence {
        // Instance variables
        private final String mId;
        private final double mLatitude;
        private final double mLongitude;

        public void setmRadius(float mRadius) {
            this.mRadius = mRadius;
        }

        private float mRadius;
        private long mExpirationDuration;
        private int mTransitionType;

        /**
         * @param geofenceId The Geofence's request ID
         * @param latitude Latitude of the Geofence's center.
         * @param longitude Longitude of the Geofence's center.
         * @param radius Radius of the geofence circle.
         * @param expiration Geofence expiration duration
         * @param transition Type of Geofence transition.
         */
        public SimpleGeofence(String geofenceId, double latitude, double longitude, float radius, long expiration, int transition) {
            // Set the instance fields from the constructor
            this.mId = geofenceId;
            this.mLatitude = latitude;
            this.mLongitude = longitude;
            this.mRadius = radius;
            this.mExpirationDuration = expiration;
            this.mTransitionType = transition;
        }
        // Instance field getters
        public String getId() {
            return mId;
        }
        public double getLatitude() {
            return mLatitude;
        }
        public double getLongitude() {
            return mLongitude;
        }
        public float getRadius() {
            return mRadius;
        }
        public long getExpirationDuration() {
            return mExpirationDuration;
        }
        public int getTransitionType() {
            return mTransitionType;
        }
        /**
         * Creates a Location Services Geofence object from a
         * SimpleGeofence.
         *
         * @return A Geofence object
         */
        public Geofence toGeofence() {
            // Build a new Geofence object
            return new Geofence.Builder()
                    .setRequestId(getId())
                    .setTransitionTypes(mTransitionType)
                    .setCircularRegion(
                            getLatitude(), getLongitude(), getRadius())
                    .setExpirationDuration(mExpirationDuration)
                    .build();
        }
    }

    /**
     * Storage for geofence values, implemented in SharedPreferences.
     */
    public class SimpleGeofenceStore {

        // The SharedPreferences object in which geofences are stored
        private final SharedPreferences mPrefs;
        // Create the SharedPreferences storage with private access only
        public SimpleGeofenceStore(Context context) {
            mPrefs =
                    context.getSharedPreferences(
                            LocationUtils.SHARED_PREFERENCES,
                            Context.MODE_PRIVATE);
        }
        /**
         * Returns a stored geofence by its id, or returns null
         * if it's not found.
         *
         * @param id The ID of a stored geofence
         * @return A geofence defined by its center and radius. See
         */
        public SimpleGeofence getGeofence(String id) {
            /*
             * Get the latitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lat = mPrefs.getFloat(
                    getGeofenceFieldKey(id, LocationUtils.KEY_LATITUDE),
                    LocationUtils.INVALID_FLOAT_VALUE);
            /*
             * Get the longitude for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            double lng = mPrefs.getFloat(
                    getGeofenceFieldKey(id, LocationUtils.KEY_LONGITUDE),
                    LocationUtils.INVALID_FLOAT_VALUE);
            /*
             * Get the radius for the geofence identified by id, or
             * INVALID_FLOAT_VALUE if it doesn't exist
             */
            float radius = mPrefs.getFloat(
                    getGeofenceFieldKey(id, LocationUtils.KEY_RADIUS),
                    LocationUtils.INVALID_FLOAT_VALUE);
            /*
             * Get the expiration duration for the geofence identified
             * by id, or INVALID_LONG_VALUE if it doesn't exist
             */
            long expirationDuration = mPrefs.getLong(
                    getGeofenceFieldKey(id, LocationUtils.KEY_EXPIRATION_DURATION),
                    LocationUtils.INVALID_LONG_VALUE);
            /*
             * Get the transition type for the geofence identified by
             * id, or INVALID_INT_VALUE if it doesn't exist
             */
            int transitionType = mPrefs.getInt(
                    getGeofenceFieldKey(id, LocationUtils.KEY_TRANSITION_TYPE),
                    LocationUtils.INVALID_INT_VALUE);
            // If none of the values is incorrect, return the object
            if (
                    lat != LocationUtils.INVALID_FLOAT_VALUE &&
                            lng != LocationUtils.INVALID_FLOAT_VALUE &&
                            radius != LocationUtils.INVALID_FLOAT_VALUE &&
                            expirationDuration !=
                                    LocationUtils.INVALID_LONG_VALUE &&
                            transitionType != LocationUtils.INVALID_INT_VALUE) {

                // Return a true Geofence object
                return new SimpleGeofence(
                        id, lat, lng, radius, expirationDuration,
                        transitionType);
                // Otherwise, return null.
            } else {
                return null;
            }
        }
        /**
         * Save a geofence.
         * @param geofence The SimpleGeofence containing the
         * values you want to save in SharedPreferences
         */
        public void setGeofence(String id, SimpleGeofence geofence) {
            /*
             * Get a SharedPreferences editor instance. Among other
             * things, SharedPreferences ensures that updates are atomic
             * and non-concurrent
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            // Write the Geofence values to SharedPreferences
            editor.putFloat(
                    getGeofenceFieldKey(id, LocationUtils.KEY_LATITUDE),
                    (float) geofence.getLatitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, LocationUtils.KEY_LONGITUDE),
                    (float) geofence.getLongitude());
            editor.putFloat(
                    getGeofenceFieldKey(id, LocationUtils.KEY_RADIUS),
                    geofence.getRadius());
            editor.putLong(
                    getGeofenceFieldKey(id, LocationUtils.KEY_EXPIRATION_DURATION),
                    geofence.getExpirationDuration());
            editor.putInt(
                    getGeofenceFieldKey(id, LocationUtils.KEY_TRANSITION_TYPE),
                    geofence.getTransitionType());
            // Commit the changes
            editor.commit();
        }
        public void clearGeofence(String id) {
            /*
             * Remove a flattened geofence object from storage by
             * removing all of its keys
             */
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.remove(getGeofenceFieldKey(id, LocationUtils.KEY_LATITUDE));
            editor.remove(getGeofenceFieldKey(id, LocationUtils.KEY_LONGITUDE));
            editor.remove(getGeofenceFieldKey(id, LocationUtils.KEY_RADIUS));
            editor.remove(getGeofenceFieldKey(id,
                    LocationUtils.KEY_EXPIRATION_DURATION));
            editor.remove(getGeofenceFieldKey(id, LocationUtils.KEY_TRANSITION_TYPE));
            editor.commit();
        }
        /**
         * Given a Geofence object's ID and the name of a field
         * (for example, KEY_LATITUDE), return the key name of the
         * object's values in SharedPreferences.
         *
         * @param id The ID of a Geofence object
         * @param fieldName The field represented by the key
         * @return The full key name of a value in SharedPreferences
         */
        private String getGeofenceFieldKey(String id,
                                           String fieldName) {
            return LocationUtils.KEY_PREFIX + "_" + id + "_" + fieldName;
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
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });

        googleMap.setOnMapLongClickListener(this);

    }

    @Override
    public void onMapLongClick(LatLng point) {
        Marker mkrOld = geofenceMkr.get(mPrefs.getString("geofenceMkrID", "null"));
        if (mkrOld==null){
            Log.d("Placing Marker: ", "No Previous Markers Set");
        } else {
            mkrOld.remove();
            geofenceMkr.remove(mPrefs.getString("geofenceMkrID", "null"));
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 11));
        Marker mkr = googleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Adjust Seekbar")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mkr.showInfoWindow();
        geofenceMkr.put(mkr.getId(), mkr);
        mEditor.putString("geofenceMkrID", mkr.getId());

        SimpleGeofence myGeofence = new SimpleGeofence(
                mkr.getId(), point.latitude, point.longitude, 100.0f,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER
        );
        mGeofenceStorage.setGeofence(mkr.getId(), myGeofence);

        mEditor.commit();
        seekBar.setVisibility(View.VISIBLE);
    }
}