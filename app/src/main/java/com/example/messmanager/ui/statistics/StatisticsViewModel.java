package com.example.messmanager.ui.statistics;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.util.DateUtils;

import java.util.Calendar;
import java.util.List;

/**
 * StatisticsViewModel
 *
 * Computes statistics over the user's current mess cycle (since
 * cycleStartDate) rather than the calendar month, so numbers stay
 * correct across cycles that don't align to the 1st of the month.
 */
public class StatisticsViewModel extends AndroidViewModel {

    private final MealRepository repository;
    private final AppPreferences preferences;
    private final MutableLiveData<String> cycleStartDate = new MutableLiveData<>();
    private final LiveData<List<MealEntry>> cycleEntries;
    private final MediatorLiveData<MealStatistics> statistics = new MediatorLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        repository = new MealRepository(application);
        preferences = AppPreferences.getInstance(application);

        cycleStartDate.setValue(preferences.getCycleStartDate());
        cycleEntries = Transformations.switchMap(cycleStartDate, repository::getEntriesFromCycleStart);

        statistics.addSource(cycleEntries, entries -> statistics.setValue(compute(entries)));
    }

    public void refreshCycleStart() {
        String latest = preferences.getCycleStartDate();
        if (!latest.equals(cycleStartDate.getValue())) {
            cycleStartDate.setValue(latest);
        }
    }

    public LiveData<MealStatistics> getStatistics() {
        return statistics;
    }

    private MealStatistics compute(List<MealEntry> entries) {
        int totalCoupons = preferences.getTotalCoupons();
        int lunch = 0, dinner = 0, skipped = 0;
        int[] weekly = new int[6]; // widened slightly since a cycle can run >31 days

        String cycleStart = preferences.getCycleStartDate();
        Calendar startCal = DateUtils.getCalendarFromDateString(cycleStart);
        Calendar today = Calendar.getInstance();

        long daysElapsed = ((today.getTimeInMillis() - startCal.getTimeInMillis()) / (1000L * 60 * 60 * 24)) + 1;
        if (daysElapsed < 1) daysElapsed = 1;

        if (entries != null) {
            for (MealEntry entry : entries) {
                if (entry.isLunch()) lunch++;
                if (entry.isDinner()) dinner++;
                if (entry.isSkipped()) skipped++;

                long dayOffset = (DateUtils.getCalendarFromDateString(entry.getDate()).getTimeInMillis()
                        - startCal.getTimeInMillis()) / (1000L * 60 * 60 * 24);
                int weekIndex = Math.min((int) (dayOffset / 7), 5);
                if (weekIndex >= 0) {
                    weekly[weekIndex] += (entry.isLunch() ? 1 : 0) + (entry.isDinner() ? 1 : 0);
                }
            }
        }

        int totalUsed = lunch + dinner;
        int remaining = totalCoupons - totalUsed;
        float avgPerDay = totalUsed / (float) daysElapsed;
        int progressPercent = totalCoupons == 0 ? 0 : (int) ((totalUsed / (float) totalCoupons) * 100);

        // Assume a cycle roughly matches how many days totalCoupons/avg-use implies isn't known,
        // so "days remaining" isn't calendar-bound here — instead project until coupons run out.
        int daysRemainingEstimate = avgPerDay > 0 ? (int) (remaining / avgPerDay) : Integer.MAX_VALUE;
        boolean onTrack = remaining >= 0;

        return new MealStatistics(totalCoupons, remaining, totalUsed, lunch, dinner, skipped,
                avgPerDay, progressPercent, weekly, onTrack, Math.max(daysRemainingEstimate, 0));
    }
}