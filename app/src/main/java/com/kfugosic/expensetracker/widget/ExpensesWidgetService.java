package com.kfugosic.expensetracker.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.kfugosic.expensetracker.data.expenses.ExpensesContract;
import com.kfugosic.expensetracker.util.CalendarUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpensesWidgetService extends IntentService {

    public static final String ACTION_UPDATE_EXPENSES_TEXTVIEWS = "com.kfugosic.expensetracker.action.updatewidget";

    public ExpensesWidgetService() {
        super("ExpensesWidgetService");
    }

    public static void startActionUpdateExpensesTextviews(Context context) {
        Intent intent = new Intent(context, ExpensesWidgetService.class);
        intent.setAction(ACTION_UPDATE_EXPENSES_TEXTVIEWS);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent == null) {
            return;
        }
        final String action = intent.getAction();
        if(ACTION_UPDATE_EXPENSES_TEXTVIEWS.equals(action)) {
            handleActionUpdateExpensesTextViews();
        }

    }

    private void handleActionUpdateExpensesTextViews() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, ExpensesWidgetProvider.class));

        String selection = ExpensesContract.ExpensesEntry.COLUMN_DATE + ">=?";
        String[] selectionArgs = new String[]{String.valueOf(CalendarUtils.getThisMonthMillis())};
        Cursor cursor = getContentResolver().query(ExpensesContract.ExpensesEntry.CONTENT_URI, null, selection, selectionArgs, null);
        if(cursor == null || cursor.getCount() <= 0) {
            List<Float> amounts = new ArrayList<>();
            amounts.add(0.0f);
            amounts.add(0.0f);
            amounts.add(0.0f);
            ExpensesWidgetProvider.updateAllTextViews(this, appWidgetManager, appWidgetIds, amounts);
        }
        cursor.moveToPosition(-1);
        float thisDay = 0.0f;
        float thisWeek = 0.0f;
        float thisMonth = 0.0f;
        int amountIndex = cursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_AMOUNT);
        int dateIndex = cursor.getColumnIndex(ExpensesContract.ExpensesEntry.COLUMN_DATE);
        long weekStartMillis = CalendarUtils.getThisWeekMillis();
        long currDayMillis = CalendarUtils.getTodaysDateMillis();
        while (cursor.moveToNext()) {
            float amount = cursor.getFloat(amountIndex);
            long date = cursor.getLong(dateIndex);
            thisMonth += amount;
            if(date >= currDayMillis) {
                thisDay += amount;
                thisWeek += amount;
                continue;
            }
            if(date >= weekStartMillis); {
                thisWeek += amount;
            }
         }

        List<Float> amounts = new ArrayList<>();
        amounts.add(thisDay);
        amounts.add(thisWeek);
        amounts.add(thisMonth);
        ExpensesWidgetProvider.updateAllTextViews(this, appWidgetManager, appWidgetIds, amounts);
        cursor.close();
    }

}
