package com.kfugosic.expensetracker.data.expenses;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kfugosic.expensetracker.data.expenses.ExpensesContract.ExpensesEntry;

public class ExpensesHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expensesDb.db";
    private static final int VERSION = 1;

    ExpensesHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_TABLE = "CREATE TABLE " + ExpensesEntry.TABLE_NAME + " (" +
                ExpensesEntry._ID + " INTEGER PRIMARY KEY, " +
                ExpensesEntry.COLUMN_AMOUNT + " REAL NOT NULL, " +
                ExpensesEntry.COLUMN_DATE + " INT NOT NULL, " +
                ExpensesEntry.COLUMN_CATEGORY + " INT, " +
                ExpensesEntry.COLUMN_PHOTO_LOCATION + " TEXT);";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // TODO Implement ALTER if version changes https://thebhwgroup.com/blog/how-android-sqlite-onupgrade
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ExpensesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

}
