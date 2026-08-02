package com.example.messmanager.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * MealEntry
 *
 * Room entity representing a single day's meal record.
 * One row exists per calendar date (enforced by a unique index on "date").
 * Stores whether lunch and/or dinner were consumed, whether the day was
 * explicitly marked as skipped, and optional notes.
 */
@Entity(tableName = "meal_entries", indices = {@androidx.room.Index(value = "date", unique = true)})
public class MealEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "date")
    private String date; // format: yyyy-MM-dd

    @ColumnInfo(name = "lunch")
    private boolean lunch;

    @ColumnInfo(name = "dinner")
    private boolean dinner;

    @ColumnInfo(name = "skipped")
    private boolean skipped;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "created_time")
    private long createdTime;

    @ColumnInfo(name = "updated_time")
    private long updatedTime;

    @ColumnInfo(name = "month")
    private int month; // 1-12

    @ColumnInfo(name = "year")
    private int year;

    public MealEntry(@NonNull String date, boolean lunch, boolean dinner, boolean skipped,
                     String notes, long createdTime, long updatedTime, int month, int year) {
        this.date = date;
        this.lunch = lunch;
        this.dinner = dinner;
        this.skipped = skipped;
        this.notes = notes;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.month = month;
        this.year = year;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getDate() { return date; }
    public void setDate(@NonNull String date) { this.date = date; }

    public boolean isLunch() { return lunch; }
    public void setLunch(boolean lunch) { this.lunch = lunch; }

    public boolean isDinner() { return dinner; }
    public void setDinner(boolean dinner) { this.dinner = dinner; }

    public boolean isSkipped() { return skipped; }
    public void setSkipped(boolean skipped) { this.skipped = skipped; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public long getCreatedTime() { return createdTime; }
    public void setCreatedTime(long createdTime) { this.createdTime = createdTime; }

    public long getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(long updatedTime) { this.updatedTime = updatedTime; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}