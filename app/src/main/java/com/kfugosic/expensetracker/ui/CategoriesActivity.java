package com.kfugosic.expensetracker.ui;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.loaders.DataLoader;
import com.kfugosic.expensetracker.loaders.IDataLoaderListener;
import com.kfugosic.expensetracker.recyclerviews.CategoriesAdapter;
import com.kfugosic.expensetracker.util.ToolbarUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CategoriesActivity extends AppCompatActivity implements IDataLoaderListener {

    private static final String KEY_INSTANCE_STATE_RV_POSITION = "rv_position";
    public static final int CATEGORIES_LOADER_ID = 101;
    private static final String CACHED_COLOR_KEY = "color_picker";

    @BindView(R.id.rv_categories)
    RecyclerView mCategoriesRecyclerView;
    @BindView(R.id.et_category_name)
    EditText mCategoryNameView;
    @BindView(R.id.color_picker)
    View mColorPickerView;
    @BindView(R.id.add_button)
    Button mAddButton;

    private CategoriesAdapter mAdapter;
    private DataLoader mLoader;
    private Parcelable mLayoutManagerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        ToolbarUtil.setupToolbar(this);
        findViewById(R.id.action_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ButterKnife.bind(this);

        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new CategoriesAdapter(this, null);
        mCategoriesRecyclerView.setAdapter(mAdapter);
        mCategoriesRecyclerView.setHasFixedSize(true);

        mLoader = new DataLoader(this, this, CategoriesContract.CategoriesEntry.CONTENT_URI);
        initOrRestartLoader();

        if (savedInstanceState != null) {
            mLayoutManagerState = savedInstanceState.getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
        }
    }

    private void initOrRestartLoader() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(CATEGORIES_LOADER_ID);

        if (loader == null) {
            loaderManager.initLoader(CATEGORIES_LOADER_ID, null, mLoader);
        } else {
            loaderManager.restartLoader(CATEGORIES_LOADER_ID, null, mLoader);
        }
    }

    @OnTextChanged(R.id.et_category_name)
    protected void handleTextChange(Editable editable) {
        if (editable == null || editable.toString().trim().isEmpty()) {
            mAddButton.setEnabled(false);
        } else {
            mAddButton.setEnabled(true);
        }
    }

    @OnClick(R.id.color_picker)
    void onColorPickerClick() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(((ColorDrawable) mColorPickerView.getBackground()).getColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("Select", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        mColorPickerView.setBackgroundColor(selectedColor);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    @OnClick(R.id.add_button)
    void onAddButtonClick() {
        String name = mCategoryNameView.getText().toString();
        Integer color = ((ColorDrawable) mColorPickerView.getBackground()).getColor();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please set category name.", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_NAME, name);
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_COLOR, color);

        getContentResolver().insert(CategoriesContract.CategoriesEntry.CONTENT_URI, contentValues);
        getSupportLoaderManager().restartLoader(CATEGORIES_LOADER_ID, null, mLoader);

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // ignorable
        }

        mCategoryNameView.setText("");
        mColorPickerView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        Toast.makeText(this, "New category created!", Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        int color = ((ColorDrawable) mColorPickerView.getBackground()).getColor();
        outState.putInt(CACHED_COLOR_KEY, color);
        outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION, mLayoutManagerState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(CACHED_COLOR_KEY)) {
            int color = savedInstanceState.getInt(CACHED_COLOR_KEY);
            mColorPickerView.setBackgroundColor(color);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public DataLoader getLoader() {
        return mLoader;
    }

    @Override
    public void onDataLoaded(int id, Cursor cursor) {
        mAdapter.onDataLoaded(id, cursor);
        if(mLayoutManagerState != null) {
            mCategoriesRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutManagerState);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLayoutManagerState = mCategoriesRecyclerView.getLayoutManager().onSaveInstanceState();
    }

}
