package com.kfugosic.expensetracker.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.kfugosic.expensetracker.R;
import com.kfugosic.expensetracker.ui.MainActivity;

import java.util.List;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 */
public class ExpensesWidgetProvider extends AppWidgetProvider {

    private static final int ID_TODAY = 0;
    private static final int ID_THIS_WEEK = 1;
    private static final int ID_THIS_MONTH = 2;

    static List<Float> mAmounts;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.expenses_widget_provider);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        if (mAmounts != null) {
            views.setTextViewText(R.id.appwidget_today, String.format(Locale.ENGLISH, "$%.2f", mAmounts.get(ID_TODAY)));
            views.setTextViewText(R.id.appwidget_week, String.format(Locale.ENGLISH, "$%.2f", mAmounts.get(ID_THIS_WEEK)));
            views.setTextViewText(R.id.appwidget_month, String.format(Locale.ENGLISH, "$%.2f", mAmounts.get(ID_THIS_MONTH)));
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAllTextViews(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds, List<Float> amounts) {
        mAmounts = amounts;
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        ExpensesWidgetService.startActionUpdateExpensesTextviews(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

