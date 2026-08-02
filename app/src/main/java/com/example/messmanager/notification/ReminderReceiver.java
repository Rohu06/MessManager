package com.example.messmanager.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.messmanager.data.preferences.AppPreferences;

/**
 * ReminderReceiver
 *
 * Fired by AlarmManager at the scheduled lunch/dinner time. Shows the
 * notification (if POST_NOTIFICATIONS is granted) and immediately
 * reschedules itself for the same time tomorrow — this is what makes
 * the reminder recur daily using only single-shot exact alarms.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String mealType = intent.getStringExtra(AlarmScheduler.EXTRA_MEAL_TYPE);
        if (mealType == null) return;

        Log.d(TAG, "onReceive fired for mealType=" + mealType);

        AppPreferences prefs = AppPreferences.getInstance(context);
        boolean hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;

        Log.d(TAG, "hasPermission=" + hasPermission);

        if (AlarmScheduler.MEAL_TYPE_LUNCH.equals(mealType)) {
            if (!prefs.isLunchReminderEnabled()) return; // user disabled it since this alarm was set
            if (hasPermission) NotificationHelper.showLunchReminder(context);
            AlarmScheduler.scheduleLunch(context, prefs.getLunchReminderHour(), prefs.getLunchReminderMinute());

        } else if (AlarmScheduler.MEAL_TYPE_DINNER.equals(mealType)) {
            if (!prefs.isDinnerReminderEnabled()) return;
            if (hasPermission) NotificationHelper.showDinnerReminder(context);
            AlarmScheduler.scheduleDinner(context, prefs.getDinnerReminderHour(), prefs.getDinnerReminderMinute());
        }
    }
}