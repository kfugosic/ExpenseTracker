package com.kfugosic.expensetracker.data.expenses;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ExpensesContentProvider extends ContentProvider {

    private static final int EXPENSES = 200;
    private static final int SPECIFIC_EXPENSE = 201;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private ExpensesHelper mExpensesHelper;

    @Override
    public boolean onCreate() {
        mExpensesHelper = new ExpensesHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mExpensesHelper.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        Cursor resultCursor = null;
        switch (match) {
            case EXPENSES:
                resultCursor = db.query(
                        ExpensesContract.ExpensesEntry.TABLE_NAME,
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
        final SQLiteDatabase db = mExpensesHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        Uri resultUri = null;
        switch (match) {
            case EXPENSES:
                long id = db.insert(ExpensesContract.ExpensesEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    resultUri = ContentUris.withAppendedId(ExpensesContract.ExpensesEntry.CONTENT_URI, id);
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
        final SQLiteDatabase db = mExpensesHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksDeleted = 0;
        switch (match) {
            case SPECIFIC_EXPENSE:
            case EXPENSES:
                if (selection != null && selectionArgs != null) {
                    tasksDeleted = db.delete(
                            ExpensesContract.ExpensesEntry.TABLE_NAME,
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
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mExpensesHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int tasksUpdated = 0;
        switch (match) {
            case SPECIFIC_EXPENSE:
                String id = uri.getPathSegments().get(1);
                tasksUpdated = db.update(
                        ExpensesContract.ExpensesEntry.TABLE_NAME,
                        contentValues,
                        "_id=?",
                        new String[]{id}
                );
                break;
            case EXPENSES:
                tasksUpdated = db.update(
                        ExpensesContract.ExpensesEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs
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
            case EXPENSES:
                return "vnd.android.cursor.dir" + "/" + ExpensesContract.AUTHORITY + "/" + ExpensesContract.PATH_EXPENSES;
            case SPECIFIC_EXPENSE:
                return "vnd.android.cursor.item" + "/" + ExpensesContract.AUTHORITY + "/" + ExpensesContract.PATH_EXPENSES;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(ExpensesContract.AUTHORITY, ExpensesContract.PATH_EXPENSES, EXPENSES);
        uriMatcher.addURI(ExpensesContract.AUTHORITY, ExpensesContract.PATH_EXPENSES + "/#", SPECIFIC_EXPENSE);
        return uriMatcher;
    }

}
