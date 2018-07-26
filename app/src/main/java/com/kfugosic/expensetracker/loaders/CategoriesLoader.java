package com.kfugosic.expensetracker.loaders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.recyclerviews.CategoriesAdapter;

import java.lang.ref.WeakReference;

public class CategoriesLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CategoriesLoader";
    private WeakReference<Context> mContext;
    private CategoriesAdapter mAdapter;

    public CategoriesLoader(Context context, CategoriesAdapter adapter) {
        mContext = new WeakReference<>(context);
        mAdapter = adapter;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(mContext.get()) {

            Cursor mData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mData != null) {
                    deliverResult(mData);
                    return;
                }
                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return mContext.get().getContentResolver().query(CategoriesContract.CategoriesEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mData = data;
                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
