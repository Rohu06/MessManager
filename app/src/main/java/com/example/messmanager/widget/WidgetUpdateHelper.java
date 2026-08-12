package com.example.messmanager.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
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
 * from a background thread (which AppWidgetProvider.onUpdate() and
 * BroadcastReceiver.onReceive() already run on for widget providers).
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

        // Remaining badge
        views.setTextViewText(R.id.widget_remaining_badge,
                context.getString(R.string.widget_remaining_format, remaining));

        // Progress bar: we simulate it by adjusting the layout weight of the fill
        // RemoteViews doesn't support setLayoutWeight directly, so we use
        // ViewStub-like trick: set the fill width as a percentage via
        // setInt(viewId, "setMaxWidth", px). Instead, we hide/show approach won't
        // work well. The simplest reliable approach for RemoteViews: use a
        // ProgressBar view in the layout. But we already have FrameLayout approach.
        // We'll use setViewPadding to effectively shrink the fill from the right.
        //
        // Strategy: fill FrameLayout is match_parent. We set its right padding
        // to (1 - percentage) * estimated_width. Since we don't know exact width,
        // we'll use a scale that works visually.
        float percentage = totalCoupons > 0 ? (float) mealsUsed / totalCoupons : 0f;
        // Clamp to [0, 1]
        percentage = Math.min(1f, Math.max(0f, percentage));
        // We'll use a large virtual width and compute right padding
        // Actually, a simpler approach: use setInt to setMaxWidth doesn't exist.
        // Best approach for RemoteViews: use android.widget.ProgressBar in layout.
        // But let's use a different reliable technique:
        // We can use ViewFlipper or just update text. For visual appeal,
        // let's use a simpler text-based representation alongside the bar.
        //
        // Actually the cleanest solution: calculate padding. The fill view is
        // match_parent inside a weighted FrameLayout. By adding right padding
        // we effectively "shrink" the visible fill.
        // 1000dp is our "virtual full width". Right padding = (1-pct) * 1000dp.
        // But we don't know dp-to-px conversion here. Let's use dp calculation.
        float density = context.getResources().getDisplayMetrics().density;
        int maxWidthDp = 500; // generous virtual max
        int rightPaddingPx = (int) ((1f - percentage) * maxWidthDp * density);
        views.setViewPadding(R.id.widget_progress_fill, 0, 0, rightPaddingPx, 0);

        // Coupon text
        views.setTextViewText(R.id.widget_coupon_text,
                context.getString(R.string.widget_coupons_format, mealsUsed, totalCoupons));

        // ── Lunch status ────────────────────────────────────────────
        if (lunchDone) {
            views.setTextViewText(R.id.widget_lunch_status,
                    context.getString(R.string.widget_status_done));
            views.setInt(R.id.widget_lunch_status, "setBackgroundResource",
                    R.drawable.bg_widget_status_done);
        } else {
            views.setTextViewText(R.id.widget_lunch_status,
                    context.getString(R.string.widget_status_pending));
            views.setInt(R.id.widget_lunch_status, "setBackgroundResource",
                    R.drawable.bg_widget_status_pending);
        }

        // ── Dinner status ───────────────────────────────────────────
        if (dinnerDone) {
            views.setTextViewText(R.id.widget_dinner_status,
                    context.getString(R.string.widget_status_done));
            views.setInt(R.id.widget_dinner_status, "setBackgroundResource",
                    R.drawable.bg_widget_status_done);
        } else {
            views.setTextViewText(R.id.widget_dinner_status,
                    context.getString(R.string.widget_status_pending));
            views.setInt(R.id.widget_dinner_status, "setBackgroundResource",
                    R.drawable.bg_widget_status_pending);
        }

        // ── Click intents ───────────────────────────────────────────
        MealWidgetProvider.setClickIntents(context, views, appWidgetId);

        // Push update
        manager.updateAppWidget(appWidgetId, views);
    }
}
