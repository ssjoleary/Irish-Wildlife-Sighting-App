package fyp.samoleary.WildlifePrototype2.Sighting;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import fyp.samoleary.WildlifePrototype2.LocationUtils;

import java.util.ArrayList;

public class SightingStore {
    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;

    private int ID = 5000;

    // The name of the resulting SharedPreferences
    private static final String SHARED_PREFERENCE_NAME =
            SubmitActivity.class.getSimpleName();

    // Create the SharedPreferences storage with private access only
    public SightingStore(Context context) {
        mPrefs =
                context.getSharedPreferences(
                        SHARED_PREFERENCE_NAME,
                        Context.MODE_PRIVATE);
    }

    public ArrayList<Sighting> getSightings(){
        ArrayList<Sighting> sightings = new ArrayList<Sighting>();
        int sightingID = mPrefs.getInt("storeID", 0);

        Log.d(LocationUtils.APPTAG, "Sightingstore: get id = " +sightingID);
        if (!mPrefs.getString(sightingID+"_id", "0").equals("0")) {
            for (int localID = sightingID; localID>5000;localID--){
                String sighting_id = mPrefs.getString(sightingID + "_id", "0");
                String name = mPrefs.getString(sightingID+"_name", "name");
                String species = mPrefs.getString(sightingID+"_species", "species");
                String date = mPrefs.getString(sightingID+"_date", "date");
                double lat = (double) mPrefs.getFloat(sightingID+"_lat", 0);
                double lng = mPrefs.getFloat(sightingID+"_lng", 0);
                String location = mPrefs.getString(sightingID+"_location", "location");
                int animals = mPrefs.getInt(sightingID+"_animals", 0);
                String imgUrl = mPrefs.getString(sightingID+"_imgurl", "imgUrl");
                sightings.add(new Sighting(Integer.parseInt(sighting_id), species, date, lat, lng, location, animals, name, imgUrl));
            }
        }
        if (sightings.size() != 0) {
            Log.d(LocationUtils.APPTAG, "Sightingstore: get != null");
            return sightings;
        } else {
            Log.d(LocationUtils.APPTAG, "Sightingstore: Add get = null");
            return null;
        }

    }

    public void setSighting(Sighting sighting) {
        /*
         * Get a SharedPreferences editor instance. Among other
         * things, SharedPreferences ensures that updates are atomic
         * and non-concurrent
         */
        SharedPreferences.Editor editor = mPrefs.edit();

        int storeID = mPrefs.getInt("storeID", 5000);
        String sightingID = String.valueOf(++storeID);
        Log.d(LocationUtils.APPTAG, "Sightingstore: add id = " +sightingID);
        editor.putString(sightingID + "_id", sightingID);
        editor.putString(sightingID+"_name", sighting.getName());
        editor.putString(sightingID+"_species", sighting.getSpecies());
        editor.putString(sightingID+"_date", sighting.getDate());
        editor.putFloat(sightingID+"_lat", (float)sighting.getSightingLat());
        editor.putFloat(sightingID+"_lng", (float)sighting.getSightingLong());
        editor.putString(sightingID+"_location", sighting.getLocation());
        editor.putInt(sightingID+"_animals", sighting.getAnimals());
        editor.putString(sightingID+"_imgurl", sighting.getImgUrlString());

        editor.putInt("storeID", storeID);
        // Commit the changes
        editor.commit();
        Log.d(LocationUtils.APPTAG, "Sightingstore: Add");
    }
      
    public void clearSighting(String id) {
        SharedPreferences.Editor editor = mPrefs.edit();

        editor.remove(id+"_id");
        editor.remove(id+"_name");
        editor.remove(id+"_species");
        editor.remove(id+"_date");
        editor.remove(id+"_lat");
        editor.remove(id+"_lng");
        editor.remove(id+"_location");
        editor.remove(id+"_animals");
        editor.remove(id+"_imgurl");
        editor.commit();
    }
}
