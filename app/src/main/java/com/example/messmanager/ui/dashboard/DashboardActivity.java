package com.example.messmanager.ui.dashboard;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmanager.R;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.databinding.ActivityDashboardBinding;
import com.example.messmanager.util.DateUtils;

/**
 * DashboardActivity
 *
 * The app's home screen. Shows today's status, remaining coupons, and
 * monthly progress, and provides quick-mark buttons for lunch/dinner
 * plus navigation to the other modules.
 */
public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding.tvCurrentDate.setText(DateUtils.getDisplayDate());
        binding.tvCurrentMonth.setText(DateUtils.getDisplayMonthYear());

        observeViewModel();
        setupClickListeners();
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, state -> {
            if (state == null) return;

            binding.tvRemainingCoupons.setText(String.valueOf(state.remainingCoupons));
            binding.tvTotalCoupons.setText(getString(R.string.total_coupons_format, state.totalCoupons));
            binding.tvLunchToday.setText(state.lunchMarkedToday
                    ? getString(R.string.status_done) : getString(R.string.status_pending));
            binding.tvDinnerToday.setText(state.dinnerMarkedToday
                    ? getString(R.string.status_done) : getString(R.string.status_pending));

            int progress = state.totalCoupons == 0
                    ? 0 : (int) ((state.mealsUsed / (float) state.totalCoupons) * 100);
            binding.progressMonthly.setProgress(progress);

            binding.btnMarkLunch.setEnabled(!state.lunchMarkedToday);
            binding.btnMarkDinner.setEnabled(!state.dinnerMarkedToday);
            binding.pillLunch.setBackgroundResource(state.lunchMarkedToday
                    ? R.drawable.bg_status_pill_done : R.drawable.bg_status_pill_pending);
            binding.pillDinner.setBackgroundResource(state.dinnerMarkedToday
                    ? R.drawable.bg_status_pill_done : R.drawable.bg_status_pill_pending);
        });
    }

    private void setupClickListeners() {
        binding.btnMarkLunch.setOnClickListener(v -> viewModel.markLunch(new MealRepository.MarkMealCallback() {
            @Override
            public void onAlreadyMarked() {
                Toast.makeText(DashboardActivity.this, R.string.msg_already_marked, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(DashboardActivity.this, R.string.msg_lunch_marked, Toast.LENGTH_SHORT).show();
            }
        }));

        binding.btnMarkDinner.setOnClickListener(v -> viewModel.markDinner(new MealRepository.MarkMealCallback() {
            @Override
            public void onAlreadyMarked() {
                Toast.makeText(DashboardActivity.this, R.string.msg_already_marked, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(DashboardActivity.this, R.string.msg_dinner_marked, Toast.LENGTH_SHORT).show();
            }

        }));

        binding.btnHistory.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.history.HistoryActivity.class)));

        binding.btnStatistics.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.statistics.StatisticsActivity.class)));

        binding.btnSettings.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.settings.SettingsActivity.class)));
        binding.fabAddMeal.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.addmeal.AddMealActivity.class)));

        binding.btnCalendar.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.calendar.CalendarActivity.class)));
        binding.btnStatistics.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.statistics.StatisticsActivity.class)));

        binding.btnSettings.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.settings.SettingsActivity.class)));


    }

    private void showComingSoon() {
        Toast.makeText(this, R.string.msg_module_coming_soon, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshCycleStart();
    }


}