package fyp.samoleary.WildlifePrototype2.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import fyp.samoleary.WildlifePrototype2.Constants;

/**
 * Author: Sam O'Leary
 * Email: somhairle.olaoire@mycit.ie
 * Created:
 * Revision:
 * Revision History:
 *
 * Description:
 *      This Class is a helper class for the WildlifeDB class. Its methods deal with the creation of tables and handling of
 *      upgrades to the Database.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE="create table "+
            Constants.TABLE_NAME+" ("+
            Constants.SIGHTING_ID+" integer primary key autoincrement, "+
            Constants.SIGHTING_SPECIES+" text not null, "+
            Constants.SIGHTING_DATE+" text not null, "+
            Constants.SIGHTING_LOCATION+" text not null,"+
            Constants.SIGHTING_LNG+" double not null,"+
            Constants.SIGHTING_LAT+" double not null,"+
            Constants.SIGHTING_IMGURI+" text not null, "+
            Constants.SIGHTING_ANIMALS+" integer not null);";

    private static final String CREATE_TABLE_RSS_SIGHTING="create table "+
            Constants.TABLE_NAME_RSS_SIGHTING+" ("+
            Constants.SIGHTING_ID+" integer primary key, "+
            Constants.SIGHTING_NAME+" text not null, "+
            Constants.SIGHTING_IMAGE+" text not null, "+
            Constants.SIGHTING_SPECIES+" text not null, "+
            Constants.SIGHTING_DATE+" text not null, "+
            Constants.SIGHTING_LOCATION+" text not null,"+
            Constants.SIGHTING_LNG+" double not null,"+
            Constants.SIGHTING_LAT+" double not null,"+
            Constants.SIGHTING_ANIMALS+" integer not null);";

    /**
     * The constructor for the DBHelper class.
     * @param context
     * @param name
     * @param factory
     * @param version
     */
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * This method, onCreate(), creates the table using the command shown above.
     *
     * @param db
     *      The Database in which the table is to be created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("DBHelper onCreate", "Creating all the tables");
        try {
            db.execSQL(CREATE_TABLE_RSS_SIGHTING);
            //db.execSQL(CREATE_TABLE);
        } catch (SQLiteException e) {
            Log.v("Create Table exception", e.getMessage());
        }
    }

    /**
     * This method, onUpgrade(), drops the existing table to allow for the upgrade to take place. Afterwards it creates
     * the table again. If certain precautions aren't taken before the upgrade to backup the table then it will be lost
     * during the uprade. The new table created is blank.
     *
     * @param db
     *      The Database in which the table is stored.
     *
     * @param oldVersion
     *      The old version number.
     *
     * @param newVersion
     *      The new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("TaskDBAdapter", "Upgrading from version "+oldVersion+" to "+newVersion+", which will destroy all old data");
        db.execSQL("drop table if exists " +Constants.TABLE_NAME);
        db.execSQL("drop table if exists " +Constants.TABLE_NAME_RSS_SIGHTING);
        onCreate(db);
    }
}
