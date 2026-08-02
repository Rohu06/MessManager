package com.example.messmanager.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmanager.R;
import com.example.messmanager.databinding.ActivityStatisticsBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * StatisticsActivity
 *
 * Displays monthly meal statistics as numeric cards plus a pie chart
 * (lunch/dinner/skipped distribution) and a bar chart (meals used per
 * week), using MPAndroidChart. All numbers come pre-computed from
 * StatisticsViewModel — this class only renders.
 */
public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private StatisticsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        viewModel.getStatistics().observe(this, this::render);
    }

    private void render(MealStatistics stats) {
        if (stats == null) return;

        binding.tvTotalMealsUsed.setText(String.valueOf(stats.totalMealsUsed));
        binding.tvTotalLunch.setText(String.valueOf(stats.totalLunch));
        binding.tvTotalDinner.setText(String.valueOf(stats.totalDinner));
        binding.tvCouponsRemaining.setText(String.valueOf(stats.couponsRemaining));
        binding.tvSkippedMeals.setText(String.valueOf(stats.skippedMeals));
        binding.tvAveragePerDay.setText(String.format(java.util.Locale.US, "%.1f", stats.averageMealsPerDay));
        binding.progressMonthly.setProgress(stats.monthlyProgressPercent);
        binding.tvProgressPercent.setText(stats.monthlyProgressPercent + "%");

        if (stats.onTrackToLastMonth) {
            binding.tvPrediction.setText(getString(R.string.prediction_on_track, stats.daysRemainingInMonth));
            binding.tvPrediction.setTextColor(getColor(R.color.status_green));
        } else {
            binding.tvPrediction.setText(getString(R.string.prediction_will_run_short));
            binding.tvPrediction.setTextColor(getColor(R.color.status_red));
        }

        renderPieChart(stats);
        renderBarChart(stats);
    }

    private void renderPieChart(MealStatistics stats) {
        List<PieEntry> entries = new ArrayList<>();
        if (stats.totalLunch > 0) entries.add(new PieEntry(stats.totalLunch, getString(R.string.label_lunch)));
        if (stats.totalDinner > 0) entries.add(new PieEntry(stats.totalDinner, getString(R.string.label_dinner)));
        if (stats.skippedMeals > 0) entries.add(new PieEntry(stats.skippedMeals, getString(R.string.status_skipped)));

        if (entries.isEmpty()) {
            binding.pieChart.clear();
            binding.pieChart.setNoDataText(getString(R.string.empty_no_data_yet));
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                getColor(R.color.status_yellow),
                getColor(R.color.status_green),
                getColor(R.color.status_red)
        );
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setEntryLabelColor(Color.BLACK);
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        binding.pieChart.animateY(600);
        binding.pieChart.invalidate();
    }

    private void renderBarChart(MealStatistics stats) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < stats.weeklyMealCounts.length; i++) {
            entries.add(new BarEntry(i, stats.weeklyMealCounts[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.label_meals_per_week));
        dataSet.setColor(getColor(R.color.status_green));
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        binding.barChart.setData(data);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getAxisRight().setEnabled(false);
        binding.barChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return getString(R.string.label_week_short) + " " + ((int) value + 1);
            }
        });
        binding.barChart.animateY(600);
        binding.barChart.invalidate();
    }
}