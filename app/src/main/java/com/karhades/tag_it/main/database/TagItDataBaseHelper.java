package com.karhades.tag_it.main.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class to handle the database related operations.
 */
public class TagItDataBaseHelper extends SQLiteOpenHelper {
    /**
     * Database version number.
     */
    private static final int VERSION = 1;

    /**
     * Database name.
     */
    private static final String DATABASE_NAME = "tagBase.db";

    public TagItDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TagItDatabaseSchema.TagTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                TagItDatabaseSchema.TagTable.Columns.TAG_ID + ", " +
                TagItDatabaseSchema.TagTable.Columns.TITLE + ", " +
                TagItDatabaseSchema.TagTable.Columns.FILE_PATH + ", " +
                TagItDatabaseSchema.TagTable.Columns.DIFFICULTY + ", " +
                TagItDatabaseSchema.TagTable.Columns.DISCOVERED + ", " +
                TagItDatabaseSchema.TagTable.Columns.DATE_DISCOVERED +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DO NOTHING.
    }
}
