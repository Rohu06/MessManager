package com.example.messmanager.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.messmanager.data.preferences.AppPreferences;

/**
 * BootReceiver
 *
 * All alarms scheduled via AlarmManager are cleared when the device
 * reboots. This receiver re-arms any reminders the user had enabled,
 * so they survive a restart without the user having to reopen Settings.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        AppPreferences prefs = AppPreferences.getInstance(context);

        if (prefs.isLunchReminderEnabled()) {
            AlarmScheduler.scheduleLunch(context, prefs.getLunchReminderHour(), prefs.getLunchReminderMinute());
        }
        if (prefs.isDinnerReminderEnabled()) {
            AlarmScheduler.scheduleDinner(context, prefs.getDinnerReminderHour(), prefs.getDinnerReminderMinute());
        }
    }
}