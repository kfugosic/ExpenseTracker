package com.kfugosic.expensetracker.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.loaders.DataLoader;
import com.kfugosic.expensetracker.loaders.IDataLoaderListener;
import com.kfugosic.expensetracker.recyclerviews.ExpensesAdapter;
import com.kfugosic.expensetracker.util.ToolbarUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StatisticsActivity extends AppCompatActivity implements IDataLoaderListener {

    private static final String KEY_INSTANCE_STATE_RV_POSITION = "rv_position";
    public static final int EXPENSES_LOADER_ID = 201;

    @BindView(R.id.rv_expenses)
    RecyclerView mExpensesRecyclerView;

    private DataLoader mLoader;
    private ExpensesAdapter mAdapter;
    private HashMap<Integer, Integer> mCategoryIdToColor;
    private Parcelable mLayoutManagerState;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        ToolbarUtil.setupToolbar(this);
        findViewById(R.id.action_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ButterKnife.bind(this);
        Intent passedIntent = getIntent();
        if(passedIntent != null) {
            mCategoryIdToColor = (HashMap<Integer, Integer>) passedIntent.getSerializableExtra(MainActivity.IDTOCOLOR_MAP_KEY);
        }

        mExpensesRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new ExpensesAdapter(this, null, mCategoryIdToColor);
        mExpensesRecyclerView.setAdapter(mAdapter);
        mExpensesRecyclerView.setHasFixedSize(true);
        if(savedInstanceState != null) {
            mLayoutManagerState = savedInstanceState.getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
        }

        mLoader = new DataLoader(this, this, ExpensesContract.ExpensesEntry.CONTENT_URI);
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
        outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION, mLayoutManagerState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLayoutManagerState = mExpensesRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    public DataLoader getLoader() {
        return mLoader;
    }

    @Override
    public void onDataLoaded(int id, Cursor cursor) {
        mAdapter.onDataLoaded(id, cursor);
        if(mLayoutManagerState != null) {
            mExpensesRecyclerView.getLayoutManager().onRestoreInstanceState(mLayoutManagerState);
        }
    }
}
