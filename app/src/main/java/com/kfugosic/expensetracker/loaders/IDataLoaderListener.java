package com.kfugosic.expensetracker.loaders;

import android.database.Cursor;

public interface IDataLoaderListener {

    void onDataLoaded(int id, Cursor cursor);

}
