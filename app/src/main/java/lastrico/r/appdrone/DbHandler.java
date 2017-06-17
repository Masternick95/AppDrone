package lastrico.r.appdrone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Nick on 13/05/2017.
 */

public class DbHandler {
    public static final String DB_NAME = "iref.db";
    public static final int DB_VERSION = 1;

    public static final String IMG_TABLE = "immagini";

    public static final String IMG_ID = "id";
    public static final int IMG_ID_COL = 0;

    public static final String IMG_PATH = "path";
    public static final int IMG_PATH_COL = 1;

    public static final String IMG_LABEL = "label";
    public static final int IMG_LABEL_COL = 2;

    private long last_record_id;

    //Create and drop statements
    public static final String CREATE_IMG_TABLE =
            "CREATE TABLE " + IMG_TABLE + " (" +
                    IMG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    IMG_PATH + "TEXT NOT NULL, " +
                    IMG_LABEL + "TEXT NOT NULL);";

    public static final String DROP_IMG_TABLE =
            "DROP TABLE IF EXISTS " + IMG_TABLE;

    private static  class DBHelper extends SQLiteOpenHelper{
        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db){
            db.execSQL(CREATE_IMG_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
            Log.d("DB", "Upgrading db from  version " + oldVersion + " to " + newVersion);
            db.execSQL(DROP_IMG_TABLE);
        }
    }

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    //Constructor
    public DbHandler(Context context){
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    //Private methods
    private void openReadableDB(){
        db = dbHelper.getReadableDatabase();
    }

    private void openWriteableDB(){
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB(){
        if(db != null){
            db.close();
        }
    }

    //METODI PUBBLICI INTERAZIONE CON IL DB
    public ArrayList<Immagine> getImages(){
        ArrayList<Immagine> immagini = new ArrayList<Immagine>();
        openReadableDB();
        Cursor cursor = db.query(IMG_TABLE, null, null, null, null, null, null);
        while(cursor.moveToNext()){
            Immagine img = new Immagine();
            img.id = cursor.getInt(IMG_ID_COL);
            img.imgPath = cursor.getString(IMG_PATH_COL);
            img.imgLabel = cursor.getString(IMG_LABEL_COL);
            immagini.add(img);
        }
        if(cursor != null){
            cursor.close();
        }
        closeDB();
        return immagini;
    }

    public long insertImage(String imgPath, String label){
        ContentValues cv = new ContentValues();
        cv.put(IMG_PATH, imgPath);
        cv.put(IMG_LABEL, label);
        this.openReadableDB();
        long rowId = db.insert(IMG_TABLE, null, cv);
        this.closeDB();
        return rowId;
    }


}
