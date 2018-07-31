package com.kfugosic.expensetracker.data.expenses;

import android.net.Uri;
import android.provider.BaseColumns;

public class ExpensesContract {

    public static final String AUTHORITY = "com.kfugosic.expensetracker.data.expenses";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_EXPENSES = "expenses";

    private ExpensesContract() {
    }

    public static final class ExpensesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_EXPENSES).build();

        public static final String TABLE_NAME = "expenses";

        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_PHOTO_LOCATION = "photo_location";

    }

}
