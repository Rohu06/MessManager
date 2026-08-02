package com.example.messmanager.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.util.DateUtils;

import java.util.Calendar;
import java.util.List;

/**
 * StatisticsViewModel
 *
 * Computes all monthly statistics — totals, averages, weekly breakdown,
 * and a simple end-of-month coupon projection — from the current
 * month's MealEntry list and the total-coupons preference.
 */
public class StatisticsViewModel extends AndroidViewModel {

    private final MealRepository repository;
    private final AppPreferences preferences;
    private final LiveData<List<MealEntry>> monthEntries;
    private final MediatorLiveData<MealStatistics> statistics = new MediatorLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new MealRepository(application);
        preferences = AppPreferences.getInstance(application);
        monthEntries = repository.getEntriesForMonth(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear());

        statistics.addSource(monthEntries, entries -> statistics.setValue(compute(entries)));
    }

    public LiveData<MealStatistics> getStatistics() {
        return statistics;
    }

    private MealStatistics compute(List<MealEntry> entries) {
        int totalCoupons = preferences.getTotalCoupons();
        int lunch = 0, dinner = 0, skipped = 0;
        int[] weekly = new int[5];

        Calendar today = Calendar.getInstance();
        int daysElapsed = today.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysRemaining = daysInMonth - daysElapsed;

        if (entries != null) {
            for (MealEntry entry : entries) {
                if (entry.isLunch()) lunch++;
                if (entry.isDinner()) dinner++;
                if (entry.isSkipped()) skipped++;

                int dayOfMonth = DateUtils.getCalendarFromDateString(entry.getDate()).get(Calendar.DAY_OF_MONTH);
                int weekIndex = Math.min((dayOfMonth - 1) / 7, 4);
                weekly[weekIndex] += (entry.isLunch() ? 1 : 0) + (entry.isDinner() ? 1 : 0);
            }
        }

        int totalUsed = lunch + dinner;
        int remaining = totalCoupons - totalUsed;
        float avgPerDay = daysElapsed > 0 ? totalUsed / (float) daysElapsed : 0f;
        int progressPercent = totalCoupons == 0 ? 0 : (int) ((totalUsed / (float) totalCoupons) * 100);

        float projectedAdditionalUse = avgPerDay * daysRemaining;
        boolean onTrack = projectedAdditionalUse <= remaining;

        return new MealStatistics(totalCoupons, remaining, totalUsed, lunch, dinner, skipped,
                avgPerDay, progressPercent, weekly, onTrack, daysRemaining);
    }
}