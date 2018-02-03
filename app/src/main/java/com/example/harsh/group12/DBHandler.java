package com.example.harsh.group12;

/**
 * Created by Ravi Teja Pinnaka on 2/11/2017.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;
import static android.os.FileObserver.CREATE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NULL;

public class DBHandler extends SQLiteOpenHelper {


    public void onCreate(SQLiteDatabase db) {
        //super.onCreate();

    }

    private static final int DATABASE_VERSION = 2;
    // Database Name
    private static final String db_name = "accelerometer_data";
    private static String DB_PATH = Environment.getExternalStorageDirectory() + File.separator + "Mydatabases" + File.separator;
    //private static final String table_name = "Name_ID_Age_Sex";
    private String key_time_stamp = "time_stamp";
    private String key_x_values = "x_values";
    private String key_y_values = "y_values";
    private String key_z_values = "z_values";
    SQLiteDatabase db;
    public String recent_table;
    File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Mydatabases");
    //File folder=new File("/data/data/com.example.harsh.group12"+"/Mydatabases");

    public DBHandler(Context context) {

        super(context, db_name, null, DATABASE_VERSION);
    }


    public void create() {

        if (!(folder.exists()))
            folder.mkdir();
        String myPath = DB_PATH + db_name;
        db = SQLiteDatabase.openOrCreateDatabase(myPath, null);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void upgrade() {
        create();
    }


    public void addTotable(table_setup table, String table_name) {
        try {

            String CREATE_TABLE = "CREATE TABLE if not exists " + table_name + "(" + key_time_stamp + " TEXT PRIMARY KEY," +
                    key_x_values + " REAL," + key_y_values + " REAL," + key_z_values + " REAL" + " );";

            try {
                db.beginTransaction();
                db.execSQL(CREATE_TABLE);
                db.setTransactionSuccessful();
                db.endTransaction();
                Log.d("Database", "Table created" + table_name);
                System.out.println("Table created");
            } catch (SQLException s) {
                System.out.print("SQL Error");
            }

            //SQLiteDatabase db = this.getWritableDatabase();
            db.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(key_time_stamp, table.getTime_stamp());
            values.put(key_x_values, table.getX_values());
            values.put(key_y_values, table.getY_values());
            values.put(key_z_values, table.getZ_values());
            db.insert(table_name, null, values);
            db.setTransactionSuccessful();
            //db.close();
            db.endTransaction();
        } catch (SQLException s) {
            System.out.print("SQL Error");

        }
    }

    public void commit() {
        db.close();
    }

    public List<table_setup> getlastdata(String table_name) {

        List<table_setup> table_list = new ArrayList<table_setup>();
// Select All Query
        String selectQuery = "SELECT * FROM " + table_name;

        // SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        cursor.moveToLast();

// looping through all rows and adding to list

        table_setup t2 = new table_setup();
        t2.setX_values(cursor.getFloat(1));
        t2.setY_values(cursor.getFloat(2));
        t2.setZ_values(cursor.getFloat(3));
// Adding contact to list
        table_list.add(t2);
        recent_table = table_name;
        return table_list;

    }

    public List<table_setup> getlast10data() {
        List<table_setup> table_list = new ArrayList<table_setup>();
        db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + "Downloads/accelerometer_data", null);
   //        Cursor c=db.rawQuery("select name from sqlite_master where type='table';",null);//       c.moveToLast();
        String table_name=recent_table;
        Cursor c=db.rawQuery("SELECT * FROM "+ table_name+ ";" ,null);
        int n=c.getCount();
        if(n>10) n=10;
       // String table_name=c.getString(0);
        String selectQuery ="SELECT * FROM "+table_name+" LIMIT "+n +" OFFSET (SELECT COUNT(*) FROM "+table_name +" )-"+n+" ;";
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                table_setup t2 = new table_setup();
                t2.setTime_stamp(cursor.getString(0));
                t2.setX_values(cursor.getFloat(1));
                t2.setY_values(cursor.getFloat(2));
                t2.setZ_values(cursor.getFloat(3));
// Adding contact to list
                table_list.add(t2);
            } while (cursor.moveToNext());
        }
        return table_list;
    }

    public String gettablename(){
       // db = SQLiteDatabase.openOrCreateDatabase(DB_PATH + "Downloads/accelerometer_data", null);
        // String selectQuery = "SELECT * FROM  (select * from " + " harsha_25_21_male " + " order by time_stamp ASC limit 10) order by time_stamp DESC;\n";
        // SQLiteDatabase db = this.getReadableDatabase();
        //Cursor c=db.rawQuery("select name from sqlite_master where type='table';",null);
        //c.moveToLast();
        //String table_name=c.getString(0);
        String table_name=recent_table;
        return table_name;
    }

}





