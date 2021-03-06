package com.kfugosic.expensetracker.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import java.lang.ref.WeakReference;

public class DataLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DataLoader";
    private WeakReference<Context> mContext;
    private IDataLoaderListener mListener;

    private Uri mUri;
    private String mSelection;
    private String[] mSelectionArgs;
    private boolean mCaching = true;

    public DataLoader(Context context, IDataLoaderListener listener, Uri uri) {
        mContext = new WeakReference<>(context);
        mListener = listener;
        mUri = uri;
    }

    public DataLoader(Context context, IDataLoaderListener listener, Uri uri, String selection, String[] selectionArgs) {
        mContext = new WeakReference<>(context);
        mListener = listener;
        mUri = uri;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
    }

    public void setCachingEnabled(boolean caching) {
        this.mCaching = caching;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(mContext.get()) {

            Cursor mData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mCaching && mData != null) {
                    deliverResult(mData);
                    return;
                }
                forceLoad();
            }

            @Override
            public Cursor loadInBackground() {
                try {
                    return mContext.get().getContentResolver().query(mUri,
                            null,
                            mSelection,
                            mSelectionArgs,
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
        mListener.onDataLoaded(loader.getId(), data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // do nothing
    }

}
