package com.kfugosic.expensetracker.ui;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.loaders.CategoriesLoader;
import com.kfugosic.expensetracker.recyclerviews.CategoriesAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CategoriesActivity extends AppCompatActivity {

    private static final String TAG = "CategoriesActivity";
    private static final int CATEGORIES_LOADER_ID = 101;

    @BindView(R.id.rv_categories)
    RecyclerView mCategoriesRecyclerView;
    @BindView(R.id.et_category_name)
    EditText mCategoryNameView;
    @BindView(R.id.color_picker)
    View mColorPickerView;
    @BindView(R.id.add_button)
    Button mAddButton;

    private CategoriesAdapter mAdapter;
    private CategoriesLoader mLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        ButterKnife.bind(this);

        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new CategoriesAdapter(this, null);
        mCategoriesRecyclerView.setAdapter(mAdapter);

        mLoader = new CategoriesLoader(this, mAdapter);
        getSupportLoaderManager().initLoader(CATEGORIES_LOADER_ID, null, mLoader);

    }

    @OnClick(R.id.color_picker)
    void onColorPickerClick() {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(((ColorDrawable)mColorPickerView.getBackground()).getColor())
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
        Integer color = ((ColorDrawable)mColorPickerView.getBackground()).getColor();
        if(TextUtils.isEmpty(name)){
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_NAME, name);
        contentValues.put(CategoriesContract.CategoriesEntry.COLUMN_COLOR, color);

        Uri uri = getContentResolver().insert(CategoriesContract.CategoriesEntry.CONTENT_URI, contentValues);
        getSupportLoaderManager().restartLoader(CATEGORIES_LOADER_ID, null, mLoader);
    }

}
