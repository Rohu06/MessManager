package com.example.messmanager.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;

/**
 * AlarmScheduler
 *
 * Schedules and cancels the daily exact alarms that trigger meal
 * reminders. Each alarm reschedules itself for the following day from
 * inside ReminderReceiver — a single AlarmManager.setExactAndAllowWhileIdle
 * call per day is more reliable across OEM battery optimizations than
 * setRepeating.
 */
public class AlarmScheduler {

    private static final String TAG = "AlarmScheduler";

    public static final String EXTRA_MEAL_TYPE = "extra_meal_type";
    public static final String MEAL_TYPE_LUNCH = "lunch";
    public static final String MEAL_TYPE_DINNER = "dinner";

    private static final int REQUEST_CODE_LUNCH = 2001;
    private static final int REQUEST_CODE_DINNER = 2002;

    /** True if the app can schedule exact alarms (always true below API 31). */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    public static void scheduleLunch(Context context, int hour, int minute) {
        schedule(context, MEAL_TYPE_LUNCH, REQUEST_CODE_LUNCH, hour, minute);
    }

    public static void scheduleDinner(Context context, int hour, int minute) {
        schedule(context, MEAL_TYPE_DINNER, REQUEST_CODE_DINNER, hour, minute);
    }

    public static void cancelLunch(Context context) {
        cancel(context, REQUEST_CODE_LUNCH);
    }

    public static void cancelDinner(Context context) {
        cancel(context, REQUEST_CODE_DINNER);
    }

    private static void schedule(Context context, String mealType, int requestCode, int hour, int minute) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            Log.w(TAG, "AlarmManager is null, cannot schedule " + mealType);
            return;
        }
        if (!canScheduleExactAlarms(context)) {
            Log.w(TAG, "Exact alarm permission NOT granted — cannot schedule " + mealType
                    + ". Direct user to Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM.");
            return;
        }

        long triggerAtMillis = nextOccurrence(hour, minute);
        PendingIntent pendingIntent = buildPendingIntent(context, mealType, requestCode);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        Log.d(TAG, mealType + " scheduled for " + new java.util.Date(triggerAtMillis));
    }

    private static void cancel(Context context, int requestCode) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) return;

        String mealType = requestCode == REQUEST_CODE_LUNCH ? MEAL_TYPE_LUNCH : MEAL_TYPE_DINNER;
        PendingIntent pendingIntent = buildPendingIntent(context, mealType, requestCode);
        alarmManager.cancel(pendingIntent);
        Log.d(TAG, mealType + " alarm cancelled");
    }

    private static PendingIntent buildPendingIntent(Context context, String mealType, int requestCode) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(EXTRA_MEAL_TYPE, mealType);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    /** Returns epoch millis for the next occurrence of hour:minute (today if still ahead, else tomorrow). */
    private static long nextOccurrence(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return cal.getTimeInMillis();
    }
}