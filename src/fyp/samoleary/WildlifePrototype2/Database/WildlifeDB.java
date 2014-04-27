package fyp.samoleary.WildlifePrototype2.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.Sighting.Sighting;import fyp.samoleary.WildlifePrototype2.WildlifeGeofencePkg.SimpleGeofence;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created: 10/03/14
 * Revision:
 * Revision History:
 *
 * Description:
 *      This class handles the opening, closing, inserting and retrieving of data and information from the SQLite
 *      Database.
 */
public class WildlifeDB {
    private SQLiteDatabase db;
    private final Context context;
    private final DBHelper dbHelper;

    public WildlifeDB(Context c) {
        context = c;
        dbHelper = new DBHelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }

    /**
     * Closes the connection to the Database
     */
    public void close(){
        db.close();
    }

    /**
     * Attempts to open a connection to a Database to write to. If this fails an exception is thrown and an attempt to
     * connect to a Database to read from is made.
     * @throws SQLiteException
     */
    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch(SQLiteException e) {
            System.out.println("Open Database exception caught");
            Log.v("Open Database exception caught", e.getMessage());
            db = dbHelper.getReadableDatabase();
        }
    }

    public int getMaxTableID(String tableName){
        int id = 0;
        Cursor cursor = db.rawQuery("SELECT MAX(_id) AS _id FROM "+tableName, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex("_id"));
        }
        return id;
    }

    public String getSpecies(String id){
        String species = "unknown species";
        Cursor cursor = db.rawQuery("SELECT "+ Constants.HOTSPOTS_SPECIES+" FROM "+Constants.TABLE_NAME_HOTSPOTS +" WHERE _id="+id, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            species = cursor.getString(cursor.getColumnIndex(Constants.HOTSPOTS_SPECIES));
        }
        return species;
    }

    public Cursor searchCounty(String county){
        return db.rawQuery("SELECT * FROM "+Constants.TABLE_NAME_RSS_SIGHTING +" WHERE "+Constants.SIGHTING_LOCATION +" like '%" + county + "%'", null);
    }

    public Cursor searchSpecies(String species){
        return db.rawQuery("SELECT * FROM "+Constants.TABLE_NAME_RSS_SIGHTING +" WHERE "+Constants.SIGHTING_SPECIES +" = '" + species+"'", null);
    }

    public Cursor searchCountySpecies(String county, String species){
        return db.rawQuery("SELECT * FROM "+Constants.TABLE_NAME_RSS_SIGHTING +" WHERE "+Constants.SIGHTING_LOCATION +" like '%" + county
                +"%' and " + Constants.SIGHTING_SPECIES + " = '" + species+"'", null);
    }

    public Cursor getHotspotBySpecies(String species) {
        return db.rawQuery("SELECT * FROM "+Constants.TABLE_NAME_HOTSPOTS +" WHERE " + Constants.HOTSPOTS_SPECIES +"="+"\""+species+"\"", null);
    }

    /**
     * This method, insertInfo(), is called once information has been retrieved and prepared elsewhere and is now ready
     * to be inserted into the Database.
     *
     * @param geofence species
     *
     * @return
     *      A return value of anything but -1 is good, -1 means an exception has occurred and the insert was unsuccessful.
     */
    public long insertInfoHotspots(SimpleGeofence geofence, String species) {
        try {
            ContentValues newTaskValue = new ContentValues();
            newTaskValue.put(Constants.HOTSPOTS_SPECIES, species);
            //newTaskValue.put(Constants.HOTSPOTS_ID, geofence.getId());
            newTaskValue.put(Constants.HOTSPOTS_RADIUS, geofence.getRadius());
            newTaskValue.put(Constants.HOTSPOTS_LAT, geofence.getLatitude());
            newTaskValue.put(Constants.HOTSPOTS_LNG, geofence.getLongitude());
            return db.insert(Constants.TABLE_NAME_HOTSPOTS, null, newTaskValue);
        }   catch (SQLiteException e) {
            System.out.println("Insert into database exception caught");
            Log.v("Insert into database exception caught", e.getMessage());
            return -1;
        }
    }

    public long insertInfoRssSighting(Sighting sighting) {
        try {
            ContentValues newTaskValue = new ContentValues();
            newTaskValue.put(Constants.SIGHTING_ID, sighting.getID());
            newTaskValue.put(Constants.SIGHTING_SPECIES, sighting.getSpecies());
            newTaskValue.put(Constants.SIGHTING_DATE, sighting.getDate());
            newTaskValue.put(Constants.SIGHTING_LOCATION, sighting.getLocation());
            newTaskValue.put(Constants.SIGHTING_ANIMALS, sighting.getAnimals());
            newTaskValue.put(Constants.SIGHTING_LAT, sighting.getSightingLat());
            newTaskValue.put(Constants.SIGHTING_LNG, sighting.getSightingLong());
            newTaskValue.put(Constants.SIGHTING_NAME, sighting.getName());
            newTaskValue.put(Constants.SIGHTING_IMAGE, sighting.getImgUrlString());
            Log.d(LocationUtils.APPTAG, "WildlifeDB: Inserted sighting");
            return db.insertOrThrow(Constants.TABLE_NAME_RSS_SIGHTING, null, newTaskValue);
        }   catch (SQLiteException e) {
            Log.d(LocationUtils.APPTAG,"Insert into database exception caught"+ e.getMessage());
            return -1;
        }
    }

    /**
     * This method, getInfo(), queries the Database and returns all the information it contains.
     *
     * @return
     *      Returns the Cursor containing the information retrieved.
     */
    public Cursor getInfoHotspots() {
        return db.query(Constants.TABLE_NAME_HOTSPOTS, null, null, null, null, null, null);
    }

    public Cursor getInfoRssSighting() {
        return db.query(Constants.TABLE_NAME_RSS_SIGHTING, null, null, null, null, null, null);
    }
    public Cursor getInfoRssSightingLimit() {
        return db.query(Constants.TABLE_NAME_RSS_SIGHTING, null, null, null, null, null, "_id DESC", "10");
    }
     /**
     * This method, dropTable(), is called when the user resets their profile. The table is dropped, or deleted, and all
     * the information gathered up to that point is lost.
     */
    public void dropTable(String tableName) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
        dbHelper.onCreate(db);
        Log.d(LocationUtils.APPTAG, "Table Dropped");
    }
}

