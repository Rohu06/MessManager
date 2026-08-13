package com.example.messmanager.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.messmanager.R;
import com.example.messmanager.ui.dashboard.DashboardActivity;

/**
 * MealWidgetProvider
 *
 * AppWidgetProvider that drives the Mess Manager home screen widget.
 * Periodic updates are triggered by the system every 30 minutes
 * (set in meal_widget_info.xml). Immediate updates happen whenever
 * the user marks a meal — either from within the app (MealRepository
 * calls WidgetUpdateHelper) or from the widget itself
 * (WidgetActionReceiver calls WidgetUpdateHelper).
 */
public class MealWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // onUpdate() runs on the MAIN thread, so we must move synchronous
        // Room queries to a background thread. goAsync() keeps the broadcast
        // alive until we call pendingResult.finish().
        final PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                for (int appWidgetId : appWidgetIds) {
                    WidgetUpdateHelper.updateWidget(context, appWidgetManager, appWidgetId);
                }
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    @Override
    public void onEnabled(Context context) {
        // First widget placed — nothing extra needed.
    }

    @Override
    public void onDisabled(Context context) {
        // Last widget removed — nothing to clean up.
    }

    /**
     * Wires up all click PendingIntents on the widget's RemoteViews.
     * Called from WidgetUpdateHelper so every update has fresh intents.
     */
    static void setClickIntents(Context context, RemoteViews views, int appWidgetId) {
        // ── Widget body → open Dashboard ────────────────────────────
        Intent dashboardIntent = new Intent(context, DashboardActivity.class);
        dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent dashboardPending = PendingIntent.getActivity(
                context, appWidgetId,
                dashboardIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, dashboardPending);

        // ── Mark Lunch button ───────────────────────────────────────
        Intent lunchIntent = new Intent(context, WidgetActionReceiver.class);
        lunchIntent.setAction(WidgetActionReceiver.ACTION_WIDGET_MARK_LUNCH);
        lunchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent lunchPending = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 1,
                lunchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_btn_mark_lunch, lunchPending);

        // ── Mark Dinner button ──────────────────────────────────────
        Intent dinnerIntent = new Intent(context, WidgetActionReceiver.class);
        dinnerIntent.setAction(WidgetActionReceiver.ACTION_WIDGET_MARK_DINNER);
        dinnerIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent dinnerPending = PendingIntent.getBroadcast(
                context, appWidgetId * 10 + 2,
                dinnerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_btn_mark_dinner, dinnerPending);
    }
}
