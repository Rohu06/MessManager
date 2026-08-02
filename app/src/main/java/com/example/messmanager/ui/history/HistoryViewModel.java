package com.example.messmanager.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.repository.MealRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * HistoryViewModel
 *
 * Loads all meal entries and reactively applies search, filter, and sort
 * criteria on top of them. Recomputation happens whenever the source
 * list changes (new/edited entries) or any criterion changes, via
 * MediatorLiveData watching four sources at once.
 */
public class HistoryViewModel extends AndroidViewModel {

    public enum FilterType { ALL, LUNCH_ONLY, DINNER_ONLY, SKIPPED_ONLY }
    public enum SortOrder { NEWEST_FIRST, OLDEST_FIRST }

    private final MealRepository repository;
    private final LiveData<List<MealEntry>> allEntries;

    private final MediatorLiveData<List<MealEntry>> filteredEntries = new MediatorLiveData<>();

    private String currentQuery = "";
    private FilterType currentFilter = FilterType.ALL;
    private SortOrder currentSort = SortOrder.NEWEST_FIRST;
    private List<MealEntry> latestSourceList = new ArrayList<>();

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new MealRepository(application);
        allEntries = repository.getAllEntries();

        filteredEntries.addSource(allEntries, entries -> {
            latestSourceList = entries != null ? entries : new ArrayList<>();
            recompute();
        });
    }

    public LiveData<List<MealEntry>> getFilteredEntries() {
        return filteredEntries;
    }

    public void setSearchQuery(String query) {
        currentQuery = query == null ? "" : query.trim().toLowerCase();
        recompute();
    }

    public void setFilter(FilterType filter) {
        currentFilter = filter;
        recompute();
    }

    public void setSortOrder(SortOrder order) {
        currentSort = order;
        recompute();
    }

    public void deleteEntry(MealEntry entry, MealRepository.SaveCallback callback) {
        repository.deleteMeal(entry, callback);
    }

    private void recompute() {
        List<MealEntry> result = new ArrayList<>();

        for (MealEntry entry : latestSourceList) {
            if (!matchesFilter(entry)) continue;
            if (!matchesSearch(entry)) continue;
            result.add(entry);
        }

        Comparator<MealEntry> comparator = Comparator.comparing(MealEntry::getDate);
        if (currentSort == SortOrder.NEWEST_FIRST) {
            comparator = comparator.reversed();
        }
        Collections.sort(result, comparator);

        filteredEntries.setValue(result);
    }

    private boolean matchesFilter(MealEntry entry) {
        switch (currentFilter) {
            case LUNCH_ONLY:
                return entry.isLunch();
            case DINNER_ONLY:
                return entry.isDinner();
            case SKIPPED_ONLY:
                return entry.isSkipped();
            case ALL:
            default:
                return true;
        }
    }

    private boolean matchesSearch(MealEntry entry) {
        if (currentQuery.isEmpty()) return true;
        boolean dateMatch = entry.getDate().toLowerCase().contains(currentQuery);
        boolean notesMatch = entry.getNotes() != null && entry.getNotes().toLowerCase().contains(currentQuery);
        return dateMatch || notesMatch;
    }
}