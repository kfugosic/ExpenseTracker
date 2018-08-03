package com.kfugosic.expensetracker.util;

import java.util.Calendar;

public class CalendarUtils {

    /**
     * Returns current date in milliseconds
     * @return current date in milliseconds
     */
    // https://stackoverflow.com/questions/38754490/get-current-day-in-milliseconds-in-java
    public static long getTodaysDateMillis() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int date = cal.get(Calendar.DATE);
        cal.clear();
        cal.set(year, month, date);
        return cal.getTimeInMillis();
    }

    /**
     * Returns first day of current week in milliseconds
     * @return first day of current week in milliseconds
     */
    public static long getThisWeekMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return cal.getTimeInMillis();
    }

    /**
     * Returns first day of current month in milliseconds
     * @return first day of current month in milliseconds
     */
    public static long getThisMonthMillis() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        cal.clear();
        cal.set(year, month, 1);
        return cal.getTimeInMillis();
    }

}
