package com.example.messmanager.ui.addmeal;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.util.DateUtils;

/**
 * AddMealViewModel
 *
 * Tracks the currently selected date and reactively loads any existing
 * MealEntry for that date — this is what powers the "auto edit-mode"
 * behavior: picking a date that already has a record surfaces it
 * automatically instead of allowing a silent duplicate.
 */
public class AddMealViewModel extends AndroidViewModel {

    private final MealRepository repository;
    private final MutableLiveData<String> selectedDate = new MutableLiveData<>();
    private final LiveData<MealEntry> existingEntry;

    public AddMealViewModel(@NonNull Application application) {
        super(application);
        repository = new MealRepository(application);
        existingEntry = Transformations.switchMap(selectedDate, repository::getEntryForDate);
    }

    public void setSelectedDate(String date) {
        if (!date.equals(selectedDate.getValue())) {
            selectedDate.setValue(date);
        }
    }

    public String getSelectedDateValue() {
        return selectedDate.getValue();
    }

    public LiveData<MealEntry> getExistingEntry() {
        return existingEntry;
    }

    /**
     * Validates and saves the entry. existingEntry is the currently loaded
     * record for the selected date (null if this is a new entry) — the
     * caller (Activity) passes back whatever the LiveData last emitted.
     */
    public void save(MealEntry existingEntry, String date, boolean lunch, boolean dinner,
                     boolean skipped, String notes, MealRepository.SaveCallback callback) {

        if (!skipped && !lunch && !dinner) {
            callback.onError("Select Lunch, Dinner, or mark the day as Skipped.");
            return;
        }

        long now = System.currentTimeMillis();
        boolean isUpdate = existingEntry != null;

        MealEntry entry;
        if (isUpdate) {
            entry = existingEntry;
            entry.setLunch(!skipped && lunch);
            entry.setDinner(!skipped && dinner);
            entry.setSkipped(skipped);
            entry.setNotes(notes);
            entry.setUpdatedTime(now);
        } else {
            entry = new MealEntry(
                    date,
                    !skipped && lunch,
                    !skipped && dinner,
                    skipped,
                    notes,
                    now,
                    now,
                    DateUtils.getMonthFromDateString(date),
                    DateUtils.getYearFromDateString(date)
            );
        }

        repository.saveMeal(entry, isUpdate, callback);
    }

    public void delete(MealEntry entry, MealRepository.SaveCallback callback) {
        repository.deleteMeal(entry, callback);
    }
}