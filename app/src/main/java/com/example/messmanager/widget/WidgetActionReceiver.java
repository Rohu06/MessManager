package com.example.messmanager.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.messmanager.R;
import com.example.messmanager.data.repository.MealRepository;

/**
 * WidgetActionReceiver
 *
 * Handles the "Mark Lunch" and "Mark Dinner" quick-action buttons
 * tapped directly on the home screen widget. Reuses the existing
 * MealRepository.markLunchForToday / markDinnerForToday methods,
 * then triggers a full widget refresh so the status pills update
 * immediately.
 */
public class WidgetActionReceiver extends BroadcastReceiver {

    public static final String ACTION_WIDGET_MARK_LUNCH =
            "com.example.messmanager.ACTION_WIDGET_MARK_LUNCH";
    public static final String ACTION_WIDGET_MARK_DINNER =
            "com.example.messmanager.ACTION_WIDGET_MARK_DINNER";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        Context appContext = context.getApplicationContext();
        MealRepository repository = new MealRepository(appContext);
        Handler mainHandler = new Handler(Looper.getMainLooper());

        switch (action) {
            case ACTION_WIDGET_MARK_LUNCH:
                repository.markLunchForToday(new MealRepository.MarkMealCallback() {
                    @Override
                    public void onAlreadyMarked() {
                        mainHandler.post(() ->
                                Toast.makeText(appContext,
                                        R.string.widget_already_marked_toast,
                                        Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onSuccess() {
                        mainHandler.post(() ->
                                Toast.makeText(appContext,
                                        R.string.widget_lunch_marked_toast,
                                        Toast.LENGTH_SHORT).show());
                        WidgetUpdateHelper.requestUpdate(appContext);
                    }
                });
                break;

            case ACTION_WIDGET_MARK_DINNER:
                repository.markDinnerForToday(new MealRepository.MarkMealCallback() {
                    @Override
                    public void onAlreadyMarked() {
                        mainHandler.post(() ->
                                Toast.makeText(appContext,
                                        R.string.widget_already_marked_toast,
                                        Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onSuccess() {
                        mainHandler.post(() ->
                                Toast.makeText(appContext,
                                        R.string.widget_dinner_marked_toast,
                                        Toast.LENGTH_SHORT).show());
                        WidgetUpdateHelper.requestUpdate(appContext);
                    }
                });
                break;
        }
    }
}
