package com.kfugosic.expensetracker.data.categories;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.kfugosic.expensetracker.data.categories.CategoriesContract.CategoriesEntry;

public class CategoriesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "categoriesDb.db";
    private static final int VERSION = 1;

    CategoriesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE = "CREATE TABLE " + CategoriesEntry.TABLE_NAME + " (" +
                CategoriesEntry._ID + " INTEGER PRIMARY KEY, " +
                CategoriesEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                CategoriesEntry.COLUMN_COLOR + " INTEGER NOT NULL);";
        sqLiteDatabase.execSQL(CREATE_TABLE);

        // TODO Optimize bulk insert
        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_NAME, "Groceries");
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_COLOR, -10163947);
        sqLiteDatabase.insert(CategoriesEntry.TABLE_NAME, null, contentValues);
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_NAME, "Vehicle");
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_COLOR, -15374871);
        sqLiteDatabase.insert(CategoriesEntry.TABLE_NAME, null, contentValues);
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_NAME, "Bills");
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_COLOR, -7829368);
        sqLiteDatabase.insert(CategoriesEntry.TABLE_NAME, null, contentValues);
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_NAME, "Medical");
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_COLOR, -1496534);
        sqLiteDatabase.insert(CategoriesEntry.TABLE_NAME, null, contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO Implement ALTER if version changes https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
