package com.example.messmanager.ui.reminders;

import android.Manifest;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.messmanager.R;
import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.notification.AlarmScheduler;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.Calendar;
import java.util.Locale;

public class ReminderSettingsActivity extends AppCompatActivity {

    private AppPreferences prefs;

    private MaterialSwitch switchLunchReminder;
    private MaterialSwitch switchDinnerReminder;
    private android.widget.TextView tvLunchTime;
    private android.widget.TextView tvDinnerTime;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Whether granted or not, re-sync switch states with the real permission state.
                refreshSwitchStates();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        prefs = AppPreferences.getInstance(this);

        switchLunchReminder = findViewById(R.id.switchLunchReminder);
        switchDinnerReminder = findViewById(R.id.switchDinnerReminder);
        tvLunchTime = findViewById(R.id.tvLunchTime);
        tvDinnerTime = findViewById(R.id.tvDinnerTime);

        setupLunch();
        setupDinner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // User may have come back from system Settings (exact alarm / notification permission).
        refreshSwitchStates();
    }

    private void setupLunch() {
        switchLunchReminder.setChecked(prefs.isLunchReminderEnabled());
        updateTimeLabel(tvLunchTime, prefs.getLunchReminderHour(), prefs.getLunchReminderMinute());

        tvLunchTime.setOnClickListener(v -> showTimePicker(true));

        switchLunchReminder.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked && !ensurePermissions()) {
                // Permission missing — revert the switch and stop; user must grant first.
                button.setChecked(false);
                return;
            }
            prefs.setLunchReminder(isChecked, prefs.getLunchReminderHour(), prefs.getLunchReminderMinute());
            if (isChecked) {
                AlarmScheduler.scheduleLunch(this, prefs.getLunchReminderHour(), prefs.getLunchReminderMinute());
            } else {
                AlarmScheduler.cancelLunch(this);
            }
        });
    }

    private void setupDinner() {
        switchDinnerReminder.setChecked(prefs.isDinnerReminderEnabled());
        updateTimeLabel(tvDinnerTime, prefs.getDinnerReminderHour(), prefs.getDinnerReminderMinute());

        tvDinnerTime.setOnClickListener(v -> showTimePicker(false));

        switchDinnerReminder.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked && !ensurePermissions()) {
                button.setChecked(false);
                return;
            }
            prefs.setDinnerReminder(isChecked, prefs.getDinnerReminderHour(), prefs.getDinnerReminderMinute());
            if (isChecked) {
                AlarmScheduler.scheduleDinner(this, prefs.getDinnerReminderHour(), prefs.getDinnerReminderMinute());
            } else {
                AlarmScheduler.cancelDinner(this);
            }
        });
    }

    private void showTimePicker(boolean isLunch) {
        int currentHour = isLunch ? prefs.getLunchReminderHour() : prefs.getDinnerReminderHour();
        int currentMinute = isLunch ? prefs.getLunchReminderMinute() : prefs.getDinnerReminderMinute();

        TimePickerDialog dialog = new TimePickerDialog(this, (view, hour, minute) -> {
            if (isLunch) {
                prefs.setLunchReminder(prefs.isLunchReminderEnabled(), hour, minute);
                updateTimeLabel(tvLunchTime, hour, minute);
                if (prefs.isLunchReminderEnabled()) {
                    AlarmScheduler.scheduleLunch(this, hour, minute);
                }
            } else {
                prefs.setDinnerReminder(prefs.isDinnerReminderEnabled(), hour, minute);
                updateTimeLabel(tvDinnerTime, hour, minute);
                if (prefs.isDinnerReminderEnabled()) {
                    AlarmScheduler.scheduleDinner(this, hour, minute);
                }
            }
        }, currentHour, currentMinute, false);

        dialog.show();
    }

    private void updateTimeLabel(android.widget.TextView label, int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("h:mm a", Locale.getDefault());
        label.setText(format.format(cal.getTime()));
    }

    /**
     * Returns true only if both POST_NOTIFICATIONS and exact-alarm permission
     * are already granted. If either is missing, kicks off the request flow
     * and returns false so the caller can revert the switch for now.
     */
    private boolean ensurePermissions() {
        // 1. Notification permission (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return false;
            }
        }

        // 2. Exact alarm permission (API 31+)
        if (!AlarmScheduler.canScheduleExactAlarms(this)) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return false;
        }

        return true;
    }

    /** Called on resume in case the user granted permission via system Settings and came back. */
    private void refreshSwitchStates() {
        boolean canSchedule = AlarmScheduler.canScheduleExactAlarms(this);
        boolean hasNotifPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;

        // If permissions are now available and the switch is on but no alarm is
        // actually scheduled yet (e.g. user just granted exact-alarm access), re-arm it.
        if (canSchedule && hasNotifPermission) {
            if (prefs.isLunchReminderEnabled()) {
                AlarmScheduler.scheduleLunch(this, prefs.getLunchReminderHour(), prefs.getLunchReminderMinute());
            }
            if (prefs.isDinnerReminderEnabled()) {
                AlarmScheduler.scheduleDinner(this, prefs.getDinnerReminderHour(), prefs.getDinnerReminderMinute());
            }
        }
    }
}