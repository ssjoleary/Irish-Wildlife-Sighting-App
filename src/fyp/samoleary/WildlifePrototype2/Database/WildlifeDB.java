package fyp.samoleary.WildlifePrototype2.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import fyp.samoleary.WildlifePrototype2.Constants;
import fyp.samoleary.WildlifePrototype2.LocationUtils;
import fyp.samoleary.WildlifePrototype2.Sighting.Sighting;

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

    /**
     * This method, insertInfo(), is called once information has been retrieved and prepared elsewhere and is now ready
     * to be inserted into the Database.
     *
     * @param sighting
     *
     * @return
     *      A return value of anything but -1 is good, -1 means an exception has occurred and the insert was unsuccessful.
     */
    public long insertInfo(Sighting sighting) {
        try {
            ContentValues newTaskValue = new ContentValues();
            newTaskValue.put(Constants.SIGHTING_SPECIES, sighting.getSpecies());
            newTaskValue.put(Constants.SIGHTING_DATE, sighting.getDate());
            newTaskValue.put(Constants.SIGHTING_LOCATION, sighting.getLocation());
            newTaskValue.put(Constants.SIGHTING_ANIMALS, sighting.getAnimals());
            newTaskValue.put(Constants.SIGHTING_LAT, sighting.getSightingLat());
            newTaskValue.put(Constants.SIGHTING_LNG, sighting.getSightingLong());
            newTaskValue.put(Constants.SIGHTING_IMGURI, sighting.getImgUriString());
            return db.insert(Constants.TABLE_NAME, null, newTaskValue);
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
    public Cursor getInfo() {
        return db.query(Constants.TABLE_NAME, null, null, null, null, null, null);
    }

    public Cursor getInfoRssSighting() {
        return db.query(Constants.TABLE_NAME_RSS_SIGHTING, null, null, null, null, null, null);
    }
     /**
     * This method, dropTable(), is called when the user resets their profile. The table is dropped, or deleted, and all
     * the information gathered up to that point is lost.
     */
    public void dropTable() {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME);
        dbHelper.onCreate(db);
    }

    public void dropTableRssSighting() {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_NAME_RSS_SIGHTING);
        dbHelper.onCreate(db);
        Log.d(LocationUtils.APPTAG, "Table Dropped");
    }
}
