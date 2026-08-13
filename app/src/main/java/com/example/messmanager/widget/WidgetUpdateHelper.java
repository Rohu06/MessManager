package com.example.messmanager.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import com.example.messmanager.R;
import com.example.messmanager.data.local.AppDatabase;
import com.example.messmanager.data.local.dao.MealDao;
import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.util.DateUtils;

/**
 * WidgetUpdateHelper
 *
 * Centralizes the data-fetching and RemoteViews-building logic for
 * the home screen widget. Called from MealWidgetProvider (periodic
 * updates), WidgetActionReceiver (after a meal is marked from the
 * widget), and MealRepository (after any in-app data change).
 *
 * All database queries are synchronous — callers must invoke this
 * from a background thread.
 */
public class WidgetUpdateHelper {

    /**
     * Convenience method: resolves all widget IDs and updates each one.
     * Safe to call from any thread (spawns its own background work).
     */
    public static void requestUpdate(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName widget = new ComponentName(context, MealWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(widget);
        if (ids != null && ids.length > 0) {
            new Thread(() -> {
                for (int id : ids) {
                    updateWidget(context, manager, id);
                }
            }).start();
        }
    }

    /**
     * Builds a fully populated RemoteViews for a single widget instance
     * and pushes it to the AppWidgetManager.
     *
     * Must be called from a background thread because it does synchronous
     * database reads.
     */
    public static void updateWidget(Context context, AppWidgetManager manager, int appWidgetId) {
        // ── Fetch data ──────────────────────────────────────────────
        AppPreferences prefs = AppPreferences.getInstance(context);
        int totalCoupons = prefs.getTotalCoupons();
        String cycleStartDate = prefs.getCycleStartDate();
        String today = DateUtils.getTodayDateString();

        MealDao dao = AppDatabase.getInstance(context).mealDao();
        MealEntry todayEntry = dao.getEntryForDateSync(today);

        // Count meals used in this cycle (synchronous full-table scan, but
        // the dataset is tiny — at most ~60 rows per cycle).
        java.util.List<MealEntry> entries = dao.getAllEntriesSync();
        int mealsUsed = 0;
        if (entries != null) {
            for (MealEntry e : entries) {
                if (e.getDate().compareTo(cycleStartDate) >= 0) {
                    if (e.isLunch()) mealsUsed++;
                    if (e.isDinner()) mealsUsed++;
                }
            }
        }

        int remaining = Math.max(0, totalCoupons - mealsUsed);
        boolean lunchDone = todayEntry != null && todayEntry.isLunch();
        boolean dinnerDone = todayEntry != null && todayEntry.isDinner();

        // ── Build RemoteViews ───────────────────────────────────────
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_meal_layout);

        // ── Header: Date ────────────────────────────────────────────
        String displayDate = DateUtils.getDisplayDate();
        views.setTextViewText(R.id.widget_date_text, displayDate);

        // ── Remaining coupon count (large number) ───────────────────
        views.setTextViewText(R.id.widget_remaining_count,
                context.getString(R.string.widget_remaining_count_format, remaining));

        // ── Used / total label ──────────────────────────────────────
        views.setTextViewText(R.id.widget_coupon_text,
                context.getString(R.string.widget_coupons_format, mealsUsed, totalCoupons));

        // ── Lunch status ────────────────────────────────────────────
        if (lunchDone) {
            views.setTextViewText(R.id.widget_lunch_status,
                    context.getString(R.string.widget_status_done));
            views.setTextColor(R.id.widget_lunch_status, 0xCC66BB6A);  // green tint
            views.setInt(R.id.widget_lunch_dot, "setBackgroundResource",
                    R.drawable.bg_widget_dot_done);
            // Hide the button since already marked
            views.setViewVisibility(R.id.widget_btn_mark_lunch, View.GONE);
        } else {
            views.setTextViewText(R.id.widget_lunch_status,
                    context.getString(R.string.widget_status_pending));
            views.setTextColor(R.id.widget_lunch_status, 0xB0FFFFFF);
            views.setInt(R.id.widget_lunch_dot, "setBackgroundResource",
                    R.drawable.bg_widget_dot_pending);
            views.setViewVisibility(R.id.widget_btn_mark_lunch, View.VISIBLE);
        }

        // ── Dinner status ───────────────────────────────────────────
        if (dinnerDone) {
            views.setTextViewText(R.id.widget_dinner_status,
                    context.getString(R.string.widget_status_done));
            views.setTextColor(R.id.widget_dinner_status, 0xCC66BB6A);
            views.setInt(R.id.widget_dinner_dot, "setBackgroundResource",
                    R.drawable.bg_widget_dot_done);
            views.setViewVisibility(R.id.widget_btn_mark_dinner, View.GONE);
        } else {
            views.setTextViewText(R.id.widget_dinner_status,
                    context.getString(R.string.widget_status_pending));
            views.setTextColor(R.id.widget_dinner_status, 0xB0FFFFFF);
            views.setInt(R.id.widget_dinner_dot, "setBackgroundResource",
                    R.drawable.bg_widget_dot_pending);
            views.setViewVisibility(R.id.widget_btn_mark_dinner, View.VISIBLE);
        }

        // ── Click intents ───────────────────────────────────────────
        MealWidgetProvider.setClickIntents(context, views, appWidgetId);

        // Push update
        manager.updateAppWidget(appWidgetId, views);
    }
}
