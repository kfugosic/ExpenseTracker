package com.kfugosic.expensetracker.loaders;

import android.database.Cursor;

public interface IDataLoaderListener {

    public void onDataLoaded(Cursor cursor);

}
