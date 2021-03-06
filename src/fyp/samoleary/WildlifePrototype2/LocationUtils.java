package fyp.samoleary.WildlifePrototype2;

import android.content.Context;
import android.location.Location;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 1/01/14
 * Revision: 1
 * Revision History:
 * 1: 1/01/14
 * Description:
 */
public class LocationUtils {
    public static final String MY_IMGUR_CLIENT_ID = "097252c7c1aae8e";
    public static final String MY_IMGUR_CLIENT_SECRET = "cb0a2a462b79545d4e4b136cdb5a01e5fdcd6d8e";
    //The arbitrary redirect url (Authorization callback URL), ex. awesome://imgur or http://android,
    // declared when registering your app with Imgur API
    public static final String MY_IMGUR_REDIRECT_URL = "abc";

    // Debugging tag for the application
    public static final String APPTAG = "DEBUGWildifePrototype";

    // Name of shared preferences repository that stores persistent state
    public static final String SHARED_PREFERENCES =
            "fyp.samoleary.WildlifePrototype2.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED =
            "fyp.samoleary.WildlifePrototype2.KEY_UPDATES_REQUESTED";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(Context context, Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return context.getString(
                    R.string.latitude_longitude,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }

    /*
         * Invalid values, used to test geofence storage when
         * retrieving geofences
         */
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;

    // Keys for flattened geofences stored in SharedPreferences
    public static final String KEY_LATITUDE =
            "com.example.android.geofence.KEY_LATITUDE";
    public static final String KEY_LONGITUDE =
            "com.example.android.geofence.KEY_LONGITUDE";
    public static final String KEY_RADIUS =
            "com.example.android.geofence.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION =
            "com.example.android.geofence.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE =
            "com.example.android.geofence.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX =
            "com.example.android.geofence.KEY";

}
