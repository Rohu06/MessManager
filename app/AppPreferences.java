package com.example.messmanager.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * AppPreferences
 *
 * Wraps SharedPreferences for simple app-wide settings such as the
 * total monthly coupon count. Settings screen (later module) will
 * write to this; Dashboard and Statistics read from it.
 */
public class AppPreferences {

    private static final String PREFS_NAME = "mess_manager_prefs";
    private static final String KEY_TOTAL_COUPONS = "total_coupons";
    private static final int DEFAULT_TOTAL_COUPONS = 60;

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
}