package com.kfugosic.expensetracker.loaders;

import android.database.Cursor;

public interface IDataLoaderListener {

    public void onDataLoaded(int id, Cursor cursor);

}
