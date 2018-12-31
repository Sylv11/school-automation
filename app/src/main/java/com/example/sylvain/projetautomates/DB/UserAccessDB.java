package com.example.sylvain.projetautomates.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class UserAccessDB {
    // Table version
    private static final int VERSION = 1;

    // Base name
    private static final String NAME_DB = "User.db";

    // Table name
    private static final String TABLE_USER = "table_user";

    // Id and name of each column
    private static final String COL_ID = "ID";
    private static final int NUM_COL_ID = 0;

    private static final String COL_LASTNAME = "LASTNAME";
    private static final int NUM_COL_LASTNAME = 1;

    private static final String COL_FIRSTNAME = "FIRSTNAME";
    private static final int NUM_COL_FIRSTNAME = 2;

    private static final String COL_EMAIL = "EMAIL";
    private static final int NUM_COL_EMAIL = 3;

    private static final String COL_PASSWORD = "PASSWORD";
    private static final int NUM_COL_PASSWORD = 4;

    private static final String COL_RANK = "RANK";
    private static final int NUM_COL_RANK = 5;


    // DB
    private SQLiteDatabase db;
    private UserDBSqlite userdb;

    public UserAccessDB(Context c) {
        this.userdb = new UserDBSqlite(c, NAME_DB, null, VERSION);
    }

    // Open for write in DB
    public void openForWrite() {
        this.db = this.userdb.getWritableDatabase();
    }

    // Open for read in DB
    public void openForRead() {
        this.db = this.userdb.getReadableDatabase();
    }

    // Close DB
    public void Close() {
        this.db.close();
    }

    // Insert user
    public long insertUser(User u) {
        ContentValues content = new ContentValues();
        content.put(COL_LASTNAME, u.getLastname());
        content.put(COL_FIRSTNAME, u.getFirstname());
        content.put(COL_EMAIL, u.getEmail());
        content.put(COL_PASSWORD, u.getPassword());
        content.put(COL_RANK, u.getRank());

        return this.db.insert(TABLE_USER, null, content);
    }

    // Update a user by id
    public int updateUser(int i, User u) {
        ContentValues content = new ContentValues();
        content.put(COL_LASTNAME, u.getLastname());
        content.put(COL_FIRSTNAME, u.getFirstname());
        content.put(COL_EMAIL, u.getEmail());
        content.put(COL_PASSWORD, u.getPassword());
        content.put(COL_RANK, u.getRank());

        return this.db.update(TABLE_USER, content, COL_ID + " = " + i, null);
    }

    // Remove user by email
    public int removeUser(String email) {
        return this.db.delete(TABLE_USER, COL_EMAIL + " = \"" + email + "\"", null);
    }

    // Get all users
    public ArrayList<User> getUsers() {
        // Result of request
        Cursor c = this.db.query(TABLE_USER, new String[]{
                        COL_ID, COL_LASTNAME, COL_FIRSTNAME, COL_EMAIL, COL_PASSWORD, COL_RANK}, null, null, null, null,
                COL_EMAIL);

        // If there is no user
        ArrayList<User> users = new ArrayList<User>();
        if (c.getCount() == 0) {
            c.close();
            return users;
        }

        // Read all lines of the cursor (line = user)
        while (c.moveToNext()) {
            User user1 = new User(c.getString(NUM_COL_LASTNAME), c.getString(NUM_COL_FIRSTNAME), c.getString(NUM_COL_EMAIL), c.getString(NUM_COL_PASSWORD), c.getInt(NUM_COL_RANK));
            users.add(user1);
        }
        c.close();

        // Return the user ArrayList
        return users;
    }

    // Get user by his email
    public User getUser(String email) {
        Cursor c = this.db.query(TABLE_USER,
                new String[]{COL_ID, COL_LASTNAME, COL_FIRSTNAME, COL_EMAIL, COL_PASSWORD, COL_RANK},
                COL_EMAIL + " LIKE \"" + email + "\"",
                null, null, null, COL_EMAIL
        );

        return cursorToUser(c);
    }

    // Return user by cursor
    private User cursorToUser(Cursor c) {
        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();

        User user = new User(c.getString(NUM_COL_LASTNAME), c.getString(NUM_COL_FIRSTNAME), c.getString(NUM_COL_EMAIL), c.getString(NUM_COL_PASSWORD), c.getInt(NUM_COL_RANK));
        c.close();

        return user;
    }

    // Delete the table
    public int deleteTableDB() {
        return this.db.delete(TABLE_USER, null, null);
    }


}
