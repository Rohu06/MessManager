package com.example.messmanager.ui.reminders;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.notification.AlarmScheduler;

/**
 * ReminderSettingsViewModel
 *
 * Reads/writes reminder preferences and drives the AlarmScheduler so
 * toggling a switch or changing a time immediately re-arms (or cancels)
 * the underlying alarm — the Activity never touches AlarmManager directly.
 */
public class ReminderSettingsViewModel extends AndroidViewModel {

    private final AppPreferences preferences;

    public ReminderSettingsViewModel(@NonNull Application application) {
        super(application);
        preferences = AppPreferences.getInstance(application);
    }

    public boolean isLunchEnabled() { return preferences.isLunchReminderEnabled(); }
    public int getLunchHour() { return preferences.getLunchReminderHour(); }
    public int getLunchMinute() { return preferences.getLunchReminderMinute(); }

    public boolean isDinnerEnabled() { return preferences.isDinnerReminderEnabled(); }
    public int getDinnerHour() { return preferences.getDinnerReminderHour(); }
    public int getDinnerMinute() { return preferences.getDinnerReminderMinute(); }

    public void setLunchReminder(android.content.Context context, boolean enabled, int hour, int minute) {
        preferences.setLunchReminder(enabled, hour, minute);
        if (enabled) {
            AlarmScheduler.scheduleLunch(context, hour, minute);
        } else {
            AlarmScheduler.cancelLunch(context);
        }
    }

    public void setDinnerReminder(android.content.Context context, boolean enabled, int hour, int minute) {
        preferences.setDinnerReminder(enabled, hour, minute);
        if (enabled) {
            AlarmScheduler.scheduleDinner(context, hour, minute);
        } else {
            AlarmScheduler.cancelDinner(context);
        }
    }
}