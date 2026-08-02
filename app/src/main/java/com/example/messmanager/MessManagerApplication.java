package com.example.messmanager;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.notification.NotificationHelper;

/**
 * MessManagerApplication
 *
 * Applies the saved theme and registers the notification channel
 * before any Activity is created.
 */
public class MessManagerApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        boolean darkModeEnabled = AppPreferences.getInstance(this).isDarkModeEnabled();
        AppCompatDelegate.setDefaultNightMode(darkModeEnabled
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);

        NotificationHelper.createNotificationChannel(this);
    }
}