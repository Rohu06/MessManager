package com.example.messmanager.ui.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmanager.R;
import com.example.messmanager.databinding.ActivityStatisticsBinding;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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
 * Displays monthly meal statistics as numeric cards plus a donut chart
 * (lunch/dinner/skipped distribution) and a bar chart (meals used per
 * week), using MPAndroidChart. All numbers come pre-computed from
 * StatisticsViewModel — this class handles rendering and theme adaptations.
 */
public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private StatisticsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBackButton();

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);
        viewModel.getStatistics().observe(this, this::render);
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void render(MealStatistics stats) {
        if (stats == null) return;

        // Top cap badge
        binding.tvCouponCapBadge.setText(getString(R.string.label_coupons_cap_badge, stats.totalCoupons));

        // Stat cards
        binding.tvTotalMealsUsed.setText(String.valueOf(stats.totalMealsUsed));
        binding.tvTotalLunch.setText(String.valueOf(stats.totalLunch));
        binding.tvTotalDinner.setText(String.valueOf(stats.totalDinner));
        binding.tvCouponsRemaining.setText(String.valueOf(stats.couponsRemaining));
        binding.tvSkippedMeals.setText(String.valueOf(stats.skippedMeals));
        binding.tvAveragePerDay.setText(String.format(java.util.Locale.US, "%.1f", stats.averageMealsPerDay));

        // Monthly progress section
        binding.progressMonthly.setProgress(stats.monthlyProgressPercent);
        binding.tvProgressPercent.setText(stats.monthlyProgressPercent + "%");
        binding.tvProgressRatio.setText(getString(R.string.label_coupons_usage_format, stats.totalMealsUsed, stats.totalCoupons));

        if (stats.onTrackToLastMonth) {
            binding.tvPrediction.setText(getString(R.string.prediction_on_track, stats.daysRemainingInMonth));
            binding.tvPredictionBadge.setText("On Track");
            binding.tvPredictionBadge.setBackgroundResource(R.drawable.bg_pill_taken);
            binding.tvPredictionBadge.setTextColor(getColor(R.color.status_green));
        } else {
            binding.tvPrediction.setText(getString(R.string.prediction_will_run_short));
            binding.tvPredictionBadge.setText("At Risk");
            binding.tvPredictionBadge.setBackgroundResource(R.drawable.bg_pill_missed);
            binding.tvPredictionBadge.setTextColor(getColor(R.color.status_red));
        }

        renderPieChart(stats);
        renderBarChart(stats);
    }

    private void renderPieChart(MealStatistics stats) {
        List<PieEntry> entries = new ArrayList<>();
        if (stats.totalLunch > 0) entries.add(new PieEntry(stats.totalLunch, getString(R.string.label_lunch)));
        if (stats.totalDinner > 0) entries.add(new PieEntry(stats.totalDinner, getString(R.string.label_dinner)));
        if (stats.skippedMeals > 0) entries.add(new PieEntry(stats.skippedMeals, getString(R.string.status_skipped)));

        int colorOnSurface = ContextCompat.getColor(this, R.color.md_on_surface);
        int colorOnSurfaceVariant = ContextCompat.getColor(this, R.color.md_on_surface_variant);

        if (entries.isEmpty()) {
            binding.pieChart.clear();
            binding.pieChart.setNoDataText(getString(R.string.empty_no_data_yet));
            binding.pieChart.setNoDataTextColor(colorOnSurfaceVariant);
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                getColor(R.color.status_green),
                getColor(R.color.md_primary),
                getColor(R.color.status_yellow)
        );
        dataSet.setValueTextSize(13f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + " (" + String.format(java.util.Locale.US, "%.0f%%", (value / stats.totalMealsUsed) * 100) + ")";
            }
        });

        binding.pieChart.setData(data);
        binding.pieChart.setUsePercentValues(false);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleColor(Color.TRANSPARENT);
        binding.pieChart.setTransparentCircleRadius(55f);
        binding.pieChart.setHoleRadius(50f);
        binding.pieChart.setCenterText(stats.totalMealsUsed + "\nTotal Meals");
        binding.pieChart.setCenterTextColor(colorOnSurface);
        binding.pieChart.setCenterTextSize(14f);

        binding.pieChart.setEntryLabelColor(colorOnSurface);
        binding.pieChart.setEntryLabelTextSize(11f);
        binding.pieChart.getDescription().setEnabled(false);

        Legend legend = binding.pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setTextColor(colorOnSurface);
        legend.setTextSize(12f);
        legend.setFormSize(10f);

        binding.pieChart.animateY(700);
        binding.pieChart.invalidate();
    }

    private void renderBarChart(MealStatistics stats) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < stats.weeklyMealCounts.length; i++) {
            entries.add(new BarEntry(i, stats.weeklyMealCounts[i]));
        }

        int colorPrimary = ContextCompat.getColor(this, R.color.md_primary);
        int colorOnSurface = ContextCompat.getColor(this, R.color.md_on_surface);
        int colorOnSurfaceVariant = ContextCompat.getColor(this, R.color.md_on_surface_variant);

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.label_meals_per_week));
        dataSet.setColor(colorPrimary);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(colorOnSurface);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return value > 0 ? String.valueOf((int) value) : "";
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        binding.barChart.setData(data);
        binding.barChart.getDescription().setEnabled(false);
        binding.barChart.getAxisRight().setEnabled(false);

        // X Axis configuration
        XAxis xAxis = binding.barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(colorOnSurfaceVariant);
        xAxis.setTextSize(11f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return getString(R.string.label_week_short) + " " + ((int) value + 1);
            }
        });

        // Left Y Axis configuration
        binding.barChart.getAxisLeft().setDrawGridLines(false);
        binding.barChart.getAxisLeft().setTextColor(colorOnSurfaceVariant);
        binding.barChart.getAxisLeft().setTextSize(11f);
        binding.barChart.getAxisLeft().setAxisMinimum(0f);

        // Legend configuration
        Legend legend = binding.barChart.getLegend();
        legend.setTextColor(colorOnSurface);
        legend.setTextSize(12f);

        binding.barChart.animateY(700);
        binding.barChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshCycleStart();
    }
}