package fyp.samoleary.WildlifePrototype2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by ssjoleary on 10/03/14.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE="create table "+
            Constants.PERSON_TABLE+" ("+
            Constants.PERSON_ID+" integer primary key autoincrement, "+
            Constants.PERSON_NAME+" text not null, "+
            Constants.PERSON_PHONE+" text, "+
            Constants.PERSON_EMAIL+" text not null"+
            Constants.PERSON_MEMBER+" integer not null);";
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("DBHelper onCreate", "Creating all the tables");
        try {
            db.execSQL(CREATE_TABLE);
        } catch (SQLiteException e) {
            Log.v("Create Table exception", e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("TaskDBAdapter", "Upgraading from version "+oldVersion+" to "+newVersion+", which will destroy all old data");
        db.execSQL("drop table if exists " +Constants.PERSON_TABLE);
        onCreate(db);
    }
}
