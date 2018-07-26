package com.kfugosic.expensetracker.data.categories;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class CategoriesContentProvider extends ContentProvider {

    private static final int CATEGORIES = 100;
    private static final int SPECIFIC_CATEGORY = 101;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private CategoriesDbHelper mCategoriesDbHelper;

    @Override
    public boolean onCreate() {
        mCategoriesDbHelper = new CategoriesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mCategoriesDbHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor resultCursor = null;
        switch (match) {
            case CATEGORIES:
                resultCursor = db.query(
                        CategoriesContract.CategoriesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        resultCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return resultCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mCategoriesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri resultUri = null;
        switch (match) {
            case CATEGORIES:
                long id = db.insert(CategoriesContract.CategoriesEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    resultUri = ContentUris.withAppendedId(CategoriesContract.CategoriesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return resultUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mCategoriesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksDeleted = 0;
        switch (match) {
            case SPECIFIC_CATEGORY:
                String id = uri.getPathSegments().get(1);
                tasksDeleted = db.delete(
                        CategoriesContract.CategoriesEntry.TABLE_NAME,
                        "_id=?",
                        new String[]{id}
                );
                break;
            case CATEGORIES:
                if (selection != null && selectionArgs != null) {
                    tasksDeleted = db.delete(
                            CategoriesContract.CategoriesEntry.TABLE_NAME,
                            selection,
                            selectionArgs
                    );
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (tasksDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return tasksDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = mCategoriesDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksUpdated = 0;
        switch (match) {
            case SPECIFIC_CATEGORY:
                String id = uri.getPathSegments().get(1);
                tasksUpdated = db.update(
                        CategoriesContract.CategoriesEntry.TABLE_NAME,
                        contentValues,
                        "_id=?",
                        new String[]{id}
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (tasksUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return tasksUpdated;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CATEGORIES:
                return "vnd.android.cursor.dir" + "/" + CategoriesContract.AUTHORITY + "/" + CategoriesContract.PATH_CATEGORIES;
            case SPECIFIC_CATEGORY:
                return "vnd.android.cursor.item" + "/" + CategoriesContract.AUTHORITY + "/" + CategoriesContract.PATH_CATEGORIES;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(CategoriesContract.AUTHORITY, CategoriesContract.PATH_CATEGORIES, CATEGORIES);
        uriMatcher.addURI(CategoriesContract.AUTHORITY, CategoriesContract.PATH_CATEGORIES + "/#", SPECIFIC_CATEGORY);
        return uriMatcher;
    }

}
