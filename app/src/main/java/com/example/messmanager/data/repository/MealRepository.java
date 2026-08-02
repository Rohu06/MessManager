package com.example.messmanager.data.repository;

import android.content.Context;
import android.service.autofill.SaveCallback;

import androidx.lifecycle.LiveData;

import com.example.messmanager.data.local.AppDatabase;
import com.example.messmanager.data.local.dao.MealDao;
import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.util.AppExecutors;
import com.example.messmanager.util.DateUtils;

import java.util.List;

/**
 * MealRepository
 *
 * Single source of truth for meal data. ViewModels only ever talk to
 * this class, never directly to Room — this isolation is what lets
 * future versions swap in remote/cloud data sources without touching
 * any ViewModel or UI code.
 */
public class MealRepository {

    private final MealDao mealDao;
    private final AppExecutors executors;

    public MealRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        mealDao = db.mealDao();
        executors = AppExecutors.getInstance();
    }

    public LiveData<List<MealEntry>> getEntriesForMonth(int month, int year) {
        return mealDao.getEntriesForMonth(month, year);
    }

    public LiveData<MealEntry> getEntryForDate(String date) {
        return mealDao.getEntryForDate(date);


    }



    /** Callback for quick-mark actions, since the DB write happens on a background thread. */
    public interface MarkMealCallback {
        void onAlreadyMarked();
        void onSuccess();
    }

    public void markLunchForToday(MarkMealCallback callback) {
        markMealForToday(true, false, callback);
    }

    public void markDinnerForToday(MarkMealCallback callback) {
        markMealForToday(false, true, callback);
    }

    private void markMealForToday(boolean markLunch, boolean markDinner, MarkMealCallback callback) {
        executors.diskIO().execute(() -> {
            String today = DateUtils.getTodayDateString();
            MealEntry existing = mealDao.getEntryForDateSync(today);
            long now = System.currentTimeMillis();

            if (existing == null) {
                MealEntry entry = new MealEntry(
                        today, markLunch, markDinner, false, null,
                        now, now, DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()
                );
                mealDao.insert(entry);
                executors.mainThread().post(callback::onSuccess);
            } else {
                boolean alreadyMarked = (markLunch && existing.isLunch()) || (markDinner && existing.isDinner());
                if (alreadyMarked) {
                    executors.mainThread().post(callback::onAlreadyMarked);
                    return;
                }
                if (markLunch) existing.setLunch(true);
                if (markDinner) existing.setDinner(true);
                existing.setSkipped(false);
                existing.setUpdatedTime(now);
                mealDao.update(existing);
                executors.mainThread().post(callback::onSuccess);
            }
        });
    }

    // Add to the existing MealRepository class:

    public void resetCurrentMonth(int month, int year, SaveCallback callback) {
        executors.diskIO().execute(() -> {
            mealDao.deleteEntriesForMonth(month, year);
            executors.mainThread().post(callback::onSuccess);
        });
    }

    public LiveData<List<MealEntry>> getAllEntries() {
        return mealDao.getAllEntries();
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String message);
    }

    public void saveMeal(MealEntry entry, boolean isUpdate, SaveCallback callback) {
        executors.diskIO().execute(() -> {
            if (isUpdate) {
                mealDao.update(entry);
            } else {
                mealDao.insert(entry);
            }
            executors.mainThread().post(callback::onSuccess);
        });
    }

    public void deleteMeal(MealEntry entry, SaveCallback callback) {
        executors.diskIO().execute(() -> {
            mealDao.deleteById(entry.getId());
            executors.mainThread().post(callback::onSuccess);
        });
    }

    public LiveData<List<MealEntry>> getEntriesFromCycleStart(String cycleStartDate) {
        return mealDao.getEntriesFromCycleStart(cycleStartDate);
    }

}