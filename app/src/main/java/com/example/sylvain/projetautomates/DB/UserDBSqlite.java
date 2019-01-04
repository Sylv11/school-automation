package com.example.sylvain.projetautomates.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDBSqlite extends SQLiteOpenHelper {
    private static final String TABLE_USER = "table_user";
    private static final String COL_ID = "ID";
    private static final String COL_LASTNAME = "LASTNAME";
    private static final String COL_FIRSTNAME = "FIRSTNAME";
    private static final String COL_EMAIL = "EMAIL";
    private static final String COL_PASSWORD = "PASSWORD";
    private static final String COL_RANK = "RANK";

    // database creation request
    private static final String CREATE_DB = "CREATE TABLE " + TABLE_USER
            + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_LASTNAME + " TEXT NOT NULL, "
            + COL_FIRSTNAME + " TEXT NOT NULL, "
            + COL_EMAIL + " TEXT NOT NULL, "
            + COL_PASSWORD + " TEXT NOT NULL, "
            + COL_RANK + " TINYINT NOT NULL);";

    public UserDBSqlite(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // If the database doesn't exist, the SQLiteOpenHelper object run onCreate()
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DB);
    }

    // If the version change, onUpgrade is called
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + TABLE_USER);
        onCreate(db);
    }
}
