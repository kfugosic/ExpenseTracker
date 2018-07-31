package com.kfugosic.expensetracker.data.categories;

import android.net.Uri;
import android.provider.BaseColumns;

public class CategoriesContract {

    public static final String AUTHORITY = "com.kfugosic.expensetracker.data.categories";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_CATEGORIES = "categories";

    private CategoriesContract() {
    }

    public static final class CategoriesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();

        public static final String TABLE_NAME = "categories";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color";
    }

}
