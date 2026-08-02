package com.example.messmanager.ui.calendar;

/**
 * CalendarDay
 *
 * Represents a single cell in the calendar grid. Not a Room entity —
 * purely a UI model built by CalendarViewModel from MealEntry data
 * (or the absence of it) for one date.
 */
public class CalendarDay {

    public enum Status { GREEN, YELLOW, RED, NEUTRAL, PADDING }

    private final String date;       // yyyy-MM-dd, null for padding cells
    private final int dayOfMonth;    // 0 for padding cells
    private final Status status;

    public CalendarDay(String date, int dayOfMonth, Status status) {
        this.date = date;
        this.dayOfMonth = dayOfMonth;
        this.status = status;
    }

    public static CalendarDay padding() {
        return new CalendarDay(null, 0, Status.PADDING);
    }

    public String getDate() { return date; }
    public int getDayOfMonth() { return dayOfMonth; }
    public Status getStatus() { return status; }
    public boolean isPadding() { return status == Status.PADDING; }
}