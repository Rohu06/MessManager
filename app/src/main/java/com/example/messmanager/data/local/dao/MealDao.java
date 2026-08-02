package com.example.messmanager.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.messmanager.data.local.entity.MealEntry;

import java.util.List;

/**
 * MealDao
 *
 * Data Access Object for meal_entries table.
 * Provides both LiveData-observing queries (for UI screens that should
 * auto-update) and synchronous queries (for use inside background
 * transactions, e.g. checking for an existing entry before inserting).
 */
@Dao
public interface MealDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MealEntry mealEntry);

    @Update
    void update(MealEntry mealEntry);

    @Query("DELETE FROM meal_entries WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM meal_entries WHERE date = :date LIMIT 1")
    MealEntry getEntryForDateSync(String date);

    @Query("SELECT * FROM meal_entries WHERE date = :date LIMIT 1")
    LiveData<MealEntry> getEntryForDate(String date);

    @Query("SELECT * FROM meal_entries WHERE month = :month AND year = :year ORDER BY date ASC")
    LiveData<List<MealEntry>> getEntriesForMonth(int month, int year);

    @Query("SELECT * FROM meal_entries ORDER BY date DESC")
    LiveData<List<MealEntry>> getAllEntries();

    // Add to the existing MealDao interface:

    @Query("DELETE FROM meal_entries WHERE month = :month AND year = :year")
    void deleteEntriesForMonth(int month, int year);
}