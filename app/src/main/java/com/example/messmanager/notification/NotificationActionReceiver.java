package com.example.messmanager.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.messmanager.R;
import com.example.messmanager.data.repository.MealRepository;

/**
 * Handles action buttons from the meal reminder notifications.
 */
public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotifActionReceiver";

    public static final String ACTION_MARK_LUNCH_EATEN = "com.example.messmanager.ACTION_MARK_LUNCH_EATEN";
    public static final String ACTION_MARK_DINNER_EATEN = "com.example.messmanager.ACTION_MARK_DINNER_EATEN";
    public static final String ACTION_SKIP = "com.example.messmanager.ACTION_SKIP";

    public static final String EXTRA_NOTIFICATION_ID = "notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);

        Log.d(TAG, "onReceive: action=" + action + ", notifId=" + notificationId);

        if (action == null) return;

        MealRepository repository = new MealRepository(context.getApplicationContext());

        switch (action) {
            case ACTION_MARK_LUNCH_EATEN:
                repository.markLunchForToday(new MealRepository.MarkMealCallback() {
                    @Override
                    public void onAlreadyMarked() {
                        Toast.makeText(context, R.string.msg_lunch_already_marked, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, R.string.msg_lunch_marked, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case ACTION_MARK_DINNER_EATEN:
                repository.markDinnerForToday(new MealRepository.MarkMealCallback() {
                    @Override
                    public void onAlreadyMarked() {
                        Toast.makeText(context, R.string.msg_dinner_already_marked, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, R.string.msg_dinner_marked, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case ACTION_SKIP:
                // No DB action for skip from notification, just dismiss
                break;
        }

        // Always dismiss the notification
        if (notificationId != -1) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.cancel(notificationId);
            }
        }
    }
}
