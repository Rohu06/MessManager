package com.example.messmanager.ui.calendar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.messmanager.databinding.ActivityCalendarBinding;
import com.example.messmanager.ui.addmeal.AddMealActivity;
import com.example.messmanager.util.DateUtils;

import java.util.Calendar;

/**
 * CalendarActivity
 *
 * Shows a monthly grid with color-coded status dots per day. Prev/Next
 * buttons navigate months; tapping any real (non-padding) day opens
 * AddMealActivity for that date, reusing its existing edit/view form.
 */
public class CalendarActivity extends AppCompatActivity {

    private ActivityCalendarBinding binding;
    private CalendarViewModel viewModel;
    private CalendarDayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CalendarViewModel.class);

        adapter = new CalendarDayAdapter(day -> {
            android.content.Intent intent = new android.content.Intent(this, AddMealActivity.class);
            intent.putExtra(AddMealActivity.EXTRA_DATE, day.getDate());
            startActivity(intent);
        });

        binding.recyclerCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        binding.recyclerCalendar.setAdapter(adapter);

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.goToPreviousMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.goToNextMonth());

        viewModel.getCalendarGrid().observe(this, days -> {
            adapter.submitList(days);
            updateMonthLabel();
        });
    }

    private void updateMonthLabel() {
        Calendar cal = Calendar.getInstance();
        cal.set(viewModel.getDisplayedYear(), viewModel.getDisplayedMonth() - 1, 1);
        binding.tvMonthLabel.setText(
                new java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.US).format(cal.getTime()));
    }
}