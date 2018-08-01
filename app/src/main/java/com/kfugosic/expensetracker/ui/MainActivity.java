package com.kfugosic.expensetracker.ui;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.kfugosic.expensetracker.BuildConfig;
import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.data.categories.CategoriesContract;
import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.loaders.DataLoader;
import com.kfugosic.expensetracker.loaders.IDataLoaderListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements IDataLoaderListener {

    public static final String DEFAULT_SHOW_ADS_KEY = "show_ads";
    public static final boolean DEFAULT_SHOW_ADS_VALUE = true;
    public static final String SHOULD_RESTART_KEY = "should_restart";
    private static final int EXPENSE_LOADER_TODAY = 202;
    private static final int EXPENSE_LOADER_MONTH = 203;
    private static final int REQUEST_CODE_ADD = 3;


    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.btn_add)
    Button mAddButton;
    @BindView(R.id.test)
    View mExpandable;
    @BindView(R.id.tv_amount_today)
    TextView mAmountToday;
    @BindView(R.id.tv_amount_month)
    TextView mAmountMonth;
    @BindView(R.id.piechart)
    PieChart mPieChart;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SparseArray<String> mCategoryIdToName;
    private boolean MyBoolean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        FirebaseApp.initializeApp(this);

        setupRemoteConfig();

        if (mFirebaseRemoteConfig.getBoolean(DEFAULT_SHOW_ADS_KEY)) {
            setupAds();
        }

        initOrRestartLoader();
    }

    public void initOrRestartLoader() {
        LoaderManager loaderManager = getSupportLoaderManager();

        DataLoader categoriesLoader = new DataLoader(this, this, CategoriesContract.CategoriesEntry.CONTENT_URI);
        Loader<String> loader = loaderManager.getLoader(CategoriesActivity.CATEGORIES_LOADER_ID);
        if (loader == null) {
            loaderManager.initLoader(CategoriesActivity.CATEGORIES_LOADER_ID, null, categoriesLoader);
        } else {
            loaderManager.restartLoader(CategoriesActivity.CATEGORIES_LOADER_ID, null, categoriesLoader);
        }

        String selection = ExpensesContract.ExpensesEntry.COLUMN_DATE + ">=?";
        String[] selectionArgs = new String[]{String.valueOf(getTodaysDateMillis())};
        DataLoader expensesLoader = new DataLoader(this, this, ExpensesContract.ExpensesEntry.CONTENT_URI, selection, selectionArgs);

        loader = loaderManager.getLoader(EXPENSE_LOADER_TODAY);
        if (loader == null) {
            loaderManager.initLoader(EXPENSE_LOADER_TODAY, null, expensesLoader);
        } else {
            loaderManager.restartLoader(EXPENSE_LOADER_TODAY, null, expensesLoader);
        }
    }

    public void initOrRestartMonthLoader() {
        LoaderManager loaderManager = getSupportLoaderManager();
        String selection = ExpensesContract.ExpensesEntry.COLUMN_DATE + ">=?";
        String[] selectionArgs = new String[]{String.valueOf(getThisMonthMillis())};
        DataLoader expensesLoader = new DataLoader(this, this, ExpensesContract.ExpensesEntry.CONTENT_URI, selection, selectionArgs);
        Loader<String> loader = loaderManager.getLoader(EXPENSE_LOADER_MONTH);
        if (loader == null) {
            loaderManager.initLoader(EXPENSE_LOADER_MONTH, null, expensesLoader);
        } else {
            loaderManager.restartLoader(EXPENSE_LOADER_MONTH, null, expensesLoader);
        }
    }

        // https://stackoverflow.com/questions/38754490/get-current-day-in-milliseconds-in-java
    private long getTodaysDateMillis() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        return cal.getTimeInMillis();
    }

    private long getThisMonthMillis() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.clear();
        cal.set(year, month, 1);
        return cal.getTimeInMillis();
    }

    @Override
    public void onDataLoaded(int id, Cursor cursor) {
        Log.d("TAG123", "onDataLoaded: " + id+" "+cursor.getCount());

        float total = 0.0f;
        int amountIndex = cursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_AMOUNT);
        int categoryIndex = cursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_CATEGORY);
        if (id == EXPENSE_LOADER_TODAY) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                total += cursor.getFloat(amountIndex);
            }
            mAmountToday.setText("$" + total);
        } else if (id == EXPENSE_LOADER_MONTH) {
            cursor.moveToPosition(-1);
            //Map<Integer, Float> amountByCategory = new HashMap<>();
            SparseArray<Float> amountByCategory = new SparseArray<>();
            while (cursor.moveToNext()) {
                int category = cursor.getInt(categoryIndex);
                float amount = cursor.getFloat(amountIndex);
                float old = amountByCategory.get(category, 0f);
                amountByCategory.append(category, old + amount);
                total += amount;
            }
            mAmountMonth.setText("$" + total);
            fillPieChart(amountByCategory);
        } else if (id == CategoriesActivity.CATEGORIES_LOADER_ID) {
            initOrRestartMonthLoader();
            int idIndex = cursor.getColumnIndex("_id");
            int nameIndex = cursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_NAME);
            cursor.moveToPosition(-1);
            mCategoryIdToName = new SparseArray<>();
            while (cursor.moveToNext()) {
                mCategoryIdToName.append(cursor.getInt(idIndex), cursor.getString(nameIndex));
            }
        }
    }

    private void fillPieChart(SparseArray<Float> amountByCategory) {
        ArrayList<PieEntry> values = new ArrayList<PieEntry>();
        for(int i = 0; i < amountByCategory.size(); i++) {
            int key = amountByCategory.keyAt(i);
            Float amount = amountByCategory.get(key);
            String name = mCategoryIdToName.get(key);
            if(TextUtils.isEmpty(name)) {
                name = "None";
            }
            values.add(new PieEntry(amount, name));
            Log.d("TAG123", "fillPieChart: "+amount+" "+key);
        }

        PieDataSet dataSet = new PieDataSet(values, null);
        PieData data = new PieData(dataSet);
        //data.setValueFormatter(new PercentFormatter());
        Description desc = new Description();
        desc.setText("Expenses by category for this month");
        dataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        mPieChart.setDescription(desc);
        mPieChart.setData(data);
        mPieChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdView.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdView.resume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("TAG123", "onActivityResult: "+resultCode);
        switch (requestCode) {
            case REQUEST_CODE_ADD:
                if(data != null) {
                    boolean res = data.getBooleanExtra(SHOULD_RESTART_KEY, false);
                    Log.d("TAG123", "onActivityResult: "+res);
                    if (res) {
                        initOrRestartLoader();
                    }
                    break;
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //
    //
    //

    @OnClick(R.id.btn_add)
    void onAddButtonClick() {
        startActivityForResult(new Intent(this, NewExpenseActivity.class), REQUEST_CODE_ADD);
    }

    @OnClick(R.id.btn_statistics)
    void onStatisticsButtonClick() {
        startActivity(new Intent(this, StatisticsActivity.class));
    }

    ///
    /// MENU
    ///

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    ///
    /// ADS AND REMOTE CONFIG
    ///

    private void setupAds() {

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("33ADD80C74AAACE7B0DDE64ED58F8D19") // Add your device if you are not testing on emulator
                .build();
        mAdView.loadAd(adRequest);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("MyBoolean", true);
        Log.d("TAG123", "onSaveInstanceState: ");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        MyBoolean = savedInstanceState.getBoolean("MyBoolean");
        Log.d("TAG123", "onRestoreInstanceState: ");
    }

    private void setupRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings
                .Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(DEFAULT_SHOW_ADS_KEY, DEFAULT_SHOW_ADS_VALUE);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        long cacheExpiration = 86400;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        mFirebaseRemoteConfig
                .fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        updateAdsView();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        updateAdsView();
                    }
                });
    }

    private void updateAdsView() {
        boolean showAds = mFirebaseRemoteConfig.getBoolean(DEFAULT_SHOW_ADS_KEY);
        if (showAds) {
            mAdView.setVisibility(View.VISIBLE);
        } else {
            mAdView.setVisibility(View.GONE);
        }
    }

}
