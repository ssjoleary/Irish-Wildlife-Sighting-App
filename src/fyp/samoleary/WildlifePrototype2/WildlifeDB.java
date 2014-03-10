package fyp.samoleary.WildlifePrototype2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Created by ssjoleary on 10/03/14.
 */
public class WildlifeDB {
    private SQLiteDatabase db;
    private final Context context;
    private final DBHelper dbHelper;

    public WildlifeDB(Context c) {
        context = c;
        dbHelper = new DBHelper(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
    }
    public void close(){
        db.close();
    }
    public void open() throws SQLiteException {
        try {
            db = dbHelper.getWritableDatabase();
        } catch(SQLiteException e) {
            System.out.println("Open Database exception caught");
            Log.v("Open Database exception caught", e.getMessage());
            db = dbHelper.getReadableDatabase();
        }
    }
}
