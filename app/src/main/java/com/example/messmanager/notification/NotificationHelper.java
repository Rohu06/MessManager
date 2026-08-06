package com.example.messmanager.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.messmanager.R;
import com.example.messmanager.ui.dashboard.DashboardActivity;

/**
 * NotificationHelper
 *
 * Owns the notification channel definition and builds/shows the two
 * reminder notifications (lunch, dinner). Tapping a notification opens
 * the Dashboard, where the user can mark the meal directly.
 */
public class NotificationHelper {

    public static final String CHANNEL_ID = "meal_reminders";
    public static final int NOTIFICATION_ID_LUNCH = 1001;
    public static final int NOTIFICATION_ID_DINNER = 1002;

    /** Call once at app startup. Creating an existing channel again is a safe no-op. */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.channel_name_meal_reminders),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(context.getString(R.string.channel_desc_meal_reminders));
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showLunchReminder(Context context) {
        show(context, NOTIFICATION_ID_LUNCH,
                context.getString(R.string.notif_title_lunch),
                context.getString(R.string.notif_body_lunch),
                NotificationActionReceiver.ACTION_MARK_LUNCH_EATEN);
    }

    public static void showDinnerReminder(Context context) {
        show(context, NOTIFICATION_ID_DINNER,
                context.getString(R.string.notif_title_dinner),
                context.getString(R.string.notif_body_dinner),
                NotificationActionReceiver.ACTION_MARK_DINNER_EATEN);
    }

    private static void show(Context context, int notificationId, String title, String body, String markAction) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, intent, flags);

        Intent markIntent = new Intent(context, NotificationActionReceiver.class);
        markIntent.setAction(markAction);
        markIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent markPendingIntent = PendingIntent.getBroadcast(context, notificationId, markIntent, flags);

        Intent skipIntent = new Intent(context, NotificationActionReceiver.class);
        skipIntent.setAction(NotificationActionReceiver.ACTION_SKIP);
        skipIntent.putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(context, notificationId + 100, skipIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(0, context.getString(R.string.action_mark_eaten), markPendingIntent)
                .addAction(0, context.getString(R.string.action_skip), skipPendingIntent);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        // Caller (ReminderReceiver) has already checked POST_NOTIFICATIONS is granted
        // before invoking this method — see ReminderReceiver.onReceive().
        try {
            managerCompat.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            // Permission was revoked between the check and this call (rare race);
            // fail silently rather than crash, since there's no user-facing action to take here.
        }
    }
}