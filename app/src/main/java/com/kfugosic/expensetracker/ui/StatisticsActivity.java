package com.kfugosic.expensetracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.loaders.DataLoader;
import com.kfugosic.expensetracker.recyclerviews.ExpensesAdapter;

import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "TAG12345";
    public static final int EXPENSES_LOADER_ID = 201;
    //private static final String CACHED_COLOR_KEY = "color_picker";

    @BindView(R.id.rv_expenses)
    RecyclerView mExpensesRecyclerView;

    private ExpensesAdapter mAdapter;
    private DataLoader mLoader;
    private HashMap<Integer, Integer> mCategoryIdToColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ButterKnife.bind(this);
        Intent passedIntent = getIntent();
        if(passedIntent != null) {
            mCategoryIdToColor = (HashMap<Integer, Integer>) passedIntent.getSerializableExtra(MainActivity.IDTOCOLOR_MAP_KEY);
        }

        mExpensesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ExpensesAdapter(this, null, mCategoryIdToColor);
        mExpensesRecyclerView.setAdapter(mAdapter);
        mExpensesRecyclerView.setHasFixedSize(true);

        mLoader = new DataLoader(this, mAdapter, ExpensesContract.ExpensesEntry.CONTENT_URI);
        initOrRestartLoader();
    }

    private void initOrRestartLoader() {
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> loader = loaderManager.getLoader(EXPENSES_LOADER_ID);

        if (loader == null) {
            loaderManager.initLoader(EXPENSES_LOADER_ID, null, mLoader);
        } else {
            loaderManager.restartLoader(EXPENSES_LOADER_ID, null, mLoader);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        if(savedInstanceState != null && savedInstanceState.containsKey()) {
//
//        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    public DataLoader getLoader() {
        return mLoader;
    }

}
