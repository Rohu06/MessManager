package com.example.messmanager.ui.dashboard;

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

import java.util.List;

/**
 * DashboardViewModel
 *
 * Exposes DashboardUiState combining meal entries since the user's
 * current mess cycle start date (not the calendar month) with the
 * total coupon setting. The cycle start is re-checked on every
 * refreshCycleStart() call (invoked from onResume) so a change made
 * in Settings is picked up without restarting the app.
 */
public class DashboardViewModel extends AndroidViewModel {

    private final MealRepository repository;
    private final AppPreferences preferences;
    private final MutableLiveData<String> cycleStartDate = new MutableLiveData<>();
    private final LiveData<List<MealEntry>> cycleEntries;
    private final MediatorLiveData<DashboardUiState> uiState = new MediatorLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new MealRepository(application);
        preferences = AppPreferences.getInstance(application);

        cycleStartDate.setValue(preferences.getCycleStartDate());
        cycleEntries = Transformations.switchMap(cycleStartDate, repository::getEntriesFromCycleStart);

        uiState.addSource(cycleEntries, entries -> uiState.setValue(buildState(entries)));
    }

    /** Call from Activity.onResume() so a Settings change to cycle start is reflected without restart. */
    public void refreshCycleStart() {
        String latest = preferences.getCycleStartDate();
        if (!latest.equals(cycleStartDate.getValue())) {
            cycleStartDate.setValue(latest);
        }
    }

    private DashboardUiState buildState(List<MealEntry> entries) {
        int totalCoupons = preferences.getTotalCoupons();
        int mealsUsed = 0;
        int lunchCount = 0;
        int dinnerCount = 0;
        boolean lunchToday = false;
        boolean dinnerToday = false;
        String today = DateUtils.getTodayDateString();

        if (entries != null) {
            for (MealEntry entry : entries) {
                if (entry.isLunch()) { mealsUsed++; lunchCount++; }
                if (entry.isDinner()) { mealsUsed++; dinnerCount++; }
                if (entry.getDate().equals(today)) {
                    lunchToday = entry.isLunch();
                    dinnerToday = entry.isDinner();
                }
            }
        }

        int remaining = totalCoupons - mealsUsed;
        return new DashboardUiState(totalCoupons, remaining, mealsUsed, lunchCount, dinnerCount, lunchToday, dinnerToday);
    }

    public LiveData<DashboardUiState> getUiState() {
        return uiState;
    }

    public void markLunch(MealRepository.MarkMealCallback callback) {
        repository.markLunchForToday(callback);
    }

    public void markDinner(MealRepository.MarkMealCallback callback) {
        repository.markDinnerForToday(callback);
    }

    public static class DashboardUiState {
        public final int totalCoupons;
        public final int remainingCoupons;
        public final int mealsUsed;
        public final int lunchCount;
        public final int dinnerCount;
        public final boolean lunchMarkedToday;
        public final boolean dinnerMarkedToday;

        public DashboardUiState(int totalCoupons, int remainingCoupons, int mealsUsed,
                                int lunchCount, int dinnerCount,
                                boolean lunchMarkedToday, boolean dinnerMarkedToday) {
            this.totalCoupons = totalCoupons;
            this.remainingCoupons = remainingCoupons;
            this.mealsUsed = mealsUsed;
            this.lunchCount = lunchCount;
            this.dinnerCount = dinnerCount;
            this.lunchMarkedToday = lunchMarkedToday;
            this.dinnerMarkedToday = dinnerMarkedToday;
        }
    }
}