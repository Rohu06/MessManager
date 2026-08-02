package com.example.messmanager.ui.statistics;

/**
 * MealStatistics
 *
 * Immutable snapshot of all computed statistics for the current month,
 * built by StatisticsViewModel from raw MealEntry data + preferences.
 */
public class MealStatistics {

    public final int totalCoupons;
    public final int couponsRemaining;
    public final int totalMealsUsed;
    public final int totalLunch;
    public final int totalDinner;
    public final int skippedMeals;
    public final float averageMealsPerDay;
    public final int monthlyProgressPercent;
    public final int[] weeklyMealCounts; // index 0-4, meals used per week-of-month
    public final boolean onTrackToLastMonth;
    public final int daysRemainingInMonth;

    public MealStatistics(int totalCoupons, int couponsRemaining, int totalMealsUsed,
                          int totalLunch, int totalDinner, int skippedMeals,
                          float averageMealsPerDay, int monthlyProgressPercent,
                          int[] weeklyMealCounts, boolean onTrackToLastMonth,
                          int daysRemainingInMonth) {
        this.totalCoupons = totalCoupons;
        this.couponsRemaining = couponsRemaining;
        this.totalMealsUsed = totalMealsUsed;
        this.totalLunch = totalLunch;
        this.totalDinner = totalDinner;
        this.skippedMeals = skippedMeals;
        this.averageMealsPerDay = averageMealsPerDay;
        this.monthlyProgressPercent = monthlyProgressPercent;
        this.weeklyMealCounts = weeklyMealCounts;
        this.onTrackToLastMonth = onTrackToLastMonth;
        this.daysRemainingInMonth = daysRemainingInMonth;
    }
}