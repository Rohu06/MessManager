package com.example.messmanager.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * AppPreferences
 *
 * Wraps SharedPreferences for app-wide settings: total coupons, dark
 * mode, and per-meal reminder configuration (enabled flag + time).
 */
public class AppPreferences {

    private static final String PREFS_NAME = "mess_manager_prefs";
    private static final String KEY_TOTAL_COUPONS = "total_coupons";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private static final String KEY_LUNCH_REMINDER_ENABLED = "lunch_reminder_enabled";
    private static final String KEY_LUNCH_REMINDER_HOUR = "lunch_reminder_hour";
    private static final String KEY_LUNCH_REMINDER_MINUTE = "lunch_reminder_minute";

    private static final String KEY_DINNER_REMINDER_ENABLED = "dinner_reminder_enabled";
    private static final String KEY_DINNER_REMINDER_HOUR = "dinner_reminder_hour";
    private static final String KEY_DINNER_REMINDER_MINUTE = "dinner_reminder_minute";

    private static final int DEFAULT_TOTAL_COUPONS = 60;
    private static final int DEFAULT_LUNCH_HOUR = 12;
    private static final int DEFAULT_DINNER_HOUR = 20;

    private static volatile AppPreferences INSTANCE;
    private final SharedPreferences prefs;

    private AppPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static AppPreferences getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppPreferences.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AppPreferences(context);
                }
            }
        }
        return INSTANCE;
    }

    public int getTotalCoupons() {
        return prefs.getInt(KEY_TOTAL_COUPONS, DEFAULT_TOTAL_COUPONS);
    }

    public void setTotalCoupons(int totalCoupons) {
        prefs.edit().putInt(KEY_TOTAL_COUPONS, totalCoupons).apply();
    }

    public boolean isDarkModeEnabled() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    /** Persists the flag AND applies it immediately via AppCompatDelegate. */
    public void setDarkModeEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        applyDarkMode();
    }

    /** Call at app startup (Application.onCreate) and after every change. */
    public void applyDarkMode() {
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled()
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public boolean isLunchReminderEnabled() {
        return prefs.getBoolean(KEY_LUNCH_REMINDER_ENABLED, false);
    }

    public int getLunchReminderHour() {
        return prefs.getInt(KEY_LUNCH_REMINDER_HOUR, DEFAULT_LUNCH_HOUR);
    }

    public int getLunchReminderMinute() {
        return prefs.getInt(KEY_LUNCH_REMINDER_MINUTE, 0);
    }

    public void setLunchReminder(boolean enabled, int hour, int minute) {
        prefs.edit()
                .putBoolean(KEY_LUNCH_REMINDER_ENABLED, enabled)
                .putInt(KEY_LUNCH_REMINDER_HOUR, hour)
                .putInt(KEY_LUNCH_REMINDER_MINUTE, minute)
                .apply();
    }

    public boolean isDinnerReminderEnabled() {
        return prefs.getBoolean(KEY_DINNER_REMINDER_ENABLED, false);
    }

    public int getDinnerReminderHour() {
        return prefs.getInt(KEY_DINNER_REMINDER_HOUR, DEFAULT_DINNER_HOUR);
    }

    public int getDinnerReminderMinute() {
        return prefs.getInt(KEY_DINNER_REMINDER_MINUTE, 0);
    }

    public void setDinnerReminder(boolean enabled, int hour, int minute) {
        prefs.edit()
                .putBoolean(KEY_DINNER_REMINDER_ENABLED, enabled)
                .putInt(KEY_DINNER_REMINDER_HOUR, hour)
                .putInt(KEY_DINNER_REMINDER_MINUTE, minute)
                .apply();
    }

    private static final String KEY_CYCLE_START_DATE = "cycle_start_date";

    public String getCycleStartDate() {
        String stored = prefs.getString(KEY_CYCLE_START_DATE, null);
        if (stored == null) {
            // First launch: default the cycle to start today.
            String today = com.example.messmanager.util.DateUtils.getTodayDateString();
            setCycleStartDate(today);
            return today;
        }
        return stored;
    }

    public void setCycleStartDate(String date) {
        prefs.edit().putString(KEY_CYCLE_START_DATE, date).apply();
    }
}