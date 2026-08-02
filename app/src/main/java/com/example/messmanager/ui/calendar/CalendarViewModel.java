package com.example.messmanager.ui.calendar;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CalendarViewModel
 *
 * Tracks the currently viewed month/year and builds a full grid of
 * CalendarDay cells (with leading padding for weekday alignment) by
 * combining that month's MealEntry records with today's date to
 * distinguish "no data yet" (future) from "missed" (past, unmarked).
 */
public class CalendarViewModel extends AndroidViewModel {

    private final MealRepository repository;
    private final MutableLiveData<int[]> monthYear = new MutableLiveData<>(); // [month(1-12), year]
    private final LiveData<List<MealEntry>> monthEntries;
    private final MediatorLiveData<List<CalendarDay>> calendarGrid = new MediatorLiveData<>();

    public CalendarViewModel(@NonNull Application application) {
        super(application);
        repository = new MealRepository(application);

        monthYear.setValue(new int[]{DateUtils.getCurrentMonth(), DateUtils.getCurrentYear()});

        monthEntries = Transformations.switchMap(monthYear,
                my -> repository.getEntriesForMonth(my[0], my[1]));

        calendarGrid.addSource(monthEntries, entries -> rebuildGrid(entries));
    }

    public LiveData<List<CalendarDay>> getCalendarGrid() {
        return calendarGrid;
    }

    public int getDisplayedMonth() { return monthYear.getValue()[0]; }
    public int getDisplayedYear() { return monthYear.getValue()[1]; }

    public void goToPreviousMonth() {
        int[] current = monthYear.getValue();
        int month = current[0] - 1;
        int year = current[1];
        if (month < 1) { month = 12; year--; }
        monthYear.setValue(new int[]{month, year});
    }

    public void goToNextMonth() {
        int[] current = monthYear.getValue();
        int month = current[0] + 1;
        int year = current[1];
        if (month > 12) { month = 1; year++; }
        monthYear.setValue(new int[]{month, year});
    }

    private void rebuildGrid(List<MealEntry> entries) {
        int[] my = monthYear.getValue();
        int month = my[0];
        int year = my[1];

        Map<String, MealEntry> entryByDate = new HashMap<>();
        if (entries != null) {
            for (MealEntry entry : entries) {
                entryByDate.put(entry.getDate(), entry);
            }
        }

        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1);
        int firstWeekday = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String today = DateUtils.getTodayDateString();

        List<CalendarDay> grid = new ArrayList<>();

        // Leading padding so day 1 lands under the correct weekday column
        for (int i = 1; i < firstWeekday; i++) {
            grid.add(CalendarDay.padding());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            String dateStr = DateUtils.buildDateString(year, month - 1, day);
            MealEntry entry = entryByDate.get(dateStr);
            CalendarDay.Status status = resolveStatus(entry, dateStr, today);
            grid.add(new CalendarDay(dateStr, day, status));
        }

        calendarGrid.setValue(grid);
    }

    private CalendarDay.Status resolveStatus(MealEntry entry, String dateStr, String today) {
        if (entry != null && !entry.isSkipped()) {
            if (entry.isLunch() && entry.isDinner()) return CalendarDay.Status.GREEN;
            if (entry.isLunch() || entry.isDinner()) return CalendarDay.Status.YELLOW;
        }
        // Explicitly skipped, or an entry with neither meal marked, counts as "no meals"
        if (entry != null && entry.isSkipped()) return CalendarDay.Status.RED;

        // No entry at all: red if the day has already passed, neutral if today/future
        return dateStr.compareTo(today) < 0 ? CalendarDay.Status.RED : CalendarDay.Status.NEUTRAL;
    }
}