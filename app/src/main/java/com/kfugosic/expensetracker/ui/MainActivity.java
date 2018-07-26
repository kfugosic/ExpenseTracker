package com.kfugosic.expensetracker.ui;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT_SHOW_ADS_KEY = "show_ads";
    public static final boolean DEFAULT_SHOW_ADS_VALUE = true;

    @BindView(R.id.adView)
    AdView mAdView;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

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
