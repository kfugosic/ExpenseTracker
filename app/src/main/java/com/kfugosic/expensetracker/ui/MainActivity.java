package com.kfugosic.expensetracker.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
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
import com.kfugosic.expensetracker.util.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements IDataLoaderListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String IDTOCOLOR_MAP_KEY = "idtocolor";
    public static final String DEFAULT_SHOW_ADS_KEY = "show_ads";
    public static final boolean DEFAULT_SHOW_ADS_VALUE = true;
    public static final String SHOULD_RESTART_KEY = "should_restart";
    private static final int EXPENSE_LOADER_TODAY = 202;
    private static final int EXPENSE_LOADER_MONTH = 203;
    private static final int REQUEST_CODE_ADD = 3;
    private static final Integer GREY_COLOR_AS_INT = -7829368;


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
    @BindView(R.id.tv_income_minus_spent)
    TextView mIncomeSpentDifference;
    @BindView(R.id.piechart)
    PieChart mPieChart;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private SparseArray<String> mCategoryIdToName;
    private LinkedHashMap<Integer, Integer> mCategoryIdToColor;
    private boolean MyBoolean = false;
    private float mMontlyExpenses;

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
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

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
        String[] selectionArgs = new String[]{String.valueOf(CalendarUtils.getTodaysDateMillis())};
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
        String[] selectionArgs = new String[]{String.valueOf(CalendarUtils.getThisMonthMillis())};
        DataLoader expensesLoader = new DataLoader(this, this, ExpensesContract.ExpensesEntry.CONTENT_URI, selection, selectionArgs);
        Loader<String> loader = loaderManager.getLoader(EXPENSE_LOADER_MONTH);
        if (loader == null) {
            loaderManager.initLoader(EXPENSE_LOADER_MONTH, null, expensesLoader);
        } else {
            loaderManager.restartLoader(EXPENSE_LOADER_MONTH, null, expensesLoader);
        }
    }

        // https://stackoverflow.com/questions/38754490/get-current-day-in-milliseconds-in-java


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
            mMontlyExpenses = total;
            updateIncomeSpentDifferenceTV();
            fillPieChart(amountByCategory);
        } else if (id == CategoriesActivity.CATEGORIES_LOADER_ID) {
            initOrRestartMonthLoader();
            int idIndex = cursor.getColumnIndex("_id");
            int colorIndex = cursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_COLOR);
            int nameIndex = cursor.getColumnIndex(CategoriesContract.CategoriesEntry.COLUMN_NAME);
            cursor.moveToPosition(-1);
            mCategoryIdToName = new SparseArray<>();
            mCategoryIdToColor  = new LinkedHashMap<>();
            mCategoryIdToColor.put(-1, GREY_COLOR_AS_INT);
            while (cursor.moveToNext()) {
                mCategoryIdToName.append(cursor.getInt(idIndex), cursor.getString(nameIndex));
                mCategoryIdToColor.put(cursor.getInt(idIndex), cursor.getInt(colorIndex));
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
        List<Integer> colors = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry: mCategoryIdToColor.entrySet()) {
            if(amountByCategory.get(entry.getKey(), 0f) > 1E-10 ){
                colors.add(entry.getValue());
            }
        }
        dataSet.setColors(colors);
        mPieChart.setDescription(desc);
        mPieChart.setData(data);
        mPieChart.invalidate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdView.destroy();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
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
        Intent intent = new Intent(this, StatisticsActivity.class);
        intent.putExtra(IDTOCOLOR_MAP_KEY, mCategoryIdToColor);
        startActivity(intent);
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


    //
    // Preferences
    //

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_income_key))) {
            updateIncomeSpentDifferenceTV();
        }
    }

    private void updateIncomeSpentDifferenceTV() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        float income = Float.valueOf(sharedPreferences.getString("income", "0"));

        if(income < 1E-10) {
            mIncomeSpentDifference.setVisibility(View.GONE);
        } else {
            mIncomeSpentDifference.setVisibility(View.VISIBLE);
            mIncomeSpentDifference.setText("$"+(income-mMontlyExpenses));
        }
    }

}
