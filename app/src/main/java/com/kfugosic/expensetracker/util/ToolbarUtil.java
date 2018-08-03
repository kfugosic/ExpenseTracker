package com.kfugosic.expensetracker.util;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.kfugosic.expensetracker.R;

import java.util.Objects;

public class ToolbarUtil {

    public static void setupToolbar(AppCompatActivity activity) {
        activity.setSupportActionBar((Toolbar) activity.findViewById(R.id.toolbar));
        try {
            Objects.requireNonNull(activity.getSupportActionBar()).setDisplayShowTitleEnabled(false);
        } catch (Exception ignorable){
            // do nothing
        };
    }

}
