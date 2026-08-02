package com.example.messmanager.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * DateUtils
 *
 * Centralized date formatting/parsing. Handles both "today" helpers
 * (used by Dashboard) and arbitrary date string parsing (used by
 * Add Meal, and later History/Calendar/Statistics).
 */
public class DateUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static String getTodayDateString() {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(Calendar.getInstance().getTime());
    }

    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static String getDisplayDate() {
        return new SimpleDateFormat("EEEE, d MMMM", Locale.US).format(Calendar.getInstance().getTime());
    }

    public static String getDisplayMonthYear() {
        return new SimpleDateFormat("MMMM yyyy", Locale.US).format(Calendar.getInstance().getTime());
    }

    /** Converts a yyyy-MM-dd string into a friendly display string, e.g. "22 July 2026". */
    public static String formatForDisplay(String dateString) {
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(dateString);
            return new SimpleDateFormat("d MMMM yyyy", Locale.US).format(date);
        } catch (ParseException e) {
            return dateString;
        }
    }

    public static int getMonthFromDateString(String dateString) {
        Calendar cal = getCalendarFromDateString(dateString);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getYearFromDateString(String dateString) {
        Calendar cal = getCalendarFromDateString(dateString);
        return cal.get(Calendar.YEAR);
    }

    public static Calendar getCalendarFromDateString(String dateString) {
        Calendar cal = Calendar.getInstance();
        try {
            Date date = new SimpleDateFormat(DATE_FORMAT, Locale.US).parse(dateString);
            cal.setTime(date);
        } catch (ParseException e) {
            // Fall back to today if parsing fails — should not happen since
            // dates only ever come from our own DatePickerDialog output.
        }
        return cal;
    }

    public static String buildDateString(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(cal.getTime());
    }
}