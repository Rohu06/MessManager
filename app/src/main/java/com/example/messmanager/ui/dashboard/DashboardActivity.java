package com.example.messmanager.ui.dashboard;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmanager.R;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.databinding.ActivityDashboardBinding;
import com.example.messmanager.util.AnimationUtils;
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

    /** Previous values for count-up animations; -1 means "first load, don't animate". */
    private int prevRemainingCoupons = -1;
    private int prevMealsUsed = -1;
    private int prevLunchCount = -1;
    private int prevDinnerCount = -1;

    private static final long COUNT_UP_DURATION_MS = 600;

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

            // ── Count-up animations on hero ring & stat chips ──
            if (prevRemainingCoupons == -1) {
                // First load — set immediately, no animation
                binding.tvRemainingCoupons.setText(String.valueOf(state.remainingCoupons));
                binding.tvUsedCount.setText(String.valueOf(state.mealsUsed));
                binding.tvHeroLunchCount.setText(String.valueOf(state.lunchCount));
                binding.tvHeroDinnerCount.setText(String.valueOf(state.dinnerCount));
            } else {
                // Subsequent updates — animate the count change
                if (prevRemainingCoupons != state.remainingCoupons) {
                    ValueAnimator anim = AnimationUtils.animateCountUp(
                            binding.tvRemainingCoupons, prevRemainingCoupons,
                            state.remainingCoupons, COUNT_UP_DURATION_MS);
                    anim.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            AnimationUtils.pulseView(binding.tvRemainingCoupons);
                        }
                    });
                }
                if (prevMealsUsed != state.mealsUsed) {
                    AnimationUtils.animateCountUp(
                            binding.tvUsedCount, prevMealsUsed,
                            state.mealsUsed, COUNT_UP_DURATION_MS);
                }
                if (prevLunchCount != state.lunchCount) {
                    AnimationUtils.animateCountUp(
                            binding.tvHeroLunchCount, prevLunchCount,
                            state.lunchCount, COUNT_UP_DURATION_MS);
                }
                if (prevDinnerCount != state.dinnerCount) {
                    AnimationUtils.animateCountUp(
                            binding.tvHeroDinnerCount, prevDinnerCount,
                            state.dinnerCount, COUNT_UP_DURATION_MS);
                }
            }

            // Store for next comparison
            prevRemainingCoupons = state.remainingCoupons;
            prevMealsUsed = state.mealsUsed;
            prevLunchCount = state.lunchCount;
            prevDinnerCount = state.dinnerCount;

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

            // Update pill card background colors
            binding.pillLunch.setCardBackgroundColor(ContextCompat.getColor(this,
                    state.lunchMarkedToday ? R.color.status_green_container : R.color.md_surface_container_high));
            binding.pillDinner.setCardBackgroundColor(ContextCompat.getColor(this,
                    state.dinnerMarkedToday ? R.color.status_green_container : R.color.md_surface_container_high));
        });
    }

    private void setupClickListeners() {
        binding.btnMarkLunch.setOnClickListener(v -> {
            AnimationUtils.pressScaleAnimation(v);
            viewModel.markLunch(new MealRepository.MarkMealCallback() {
                @Override
                public void onAlreadyMarked() {
                    Toast.makeText(DashboardActivity.this, R.string.msg_already_marked, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(DashboardActivity.this, R.string.msg_lunch_marked, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.btnMarkDinner.setOnClickListener(v -> {
            AnimationUtils.pressScaleAnimation(v);
            viewModel.markDinner(new MealRepository.MarkMealCallback() {
                @Override
                public void onAlreadyMarked() {
                    Toast.makeText(DashboardActivity.this, R.string.msg_already_marked, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess() {
                    Toast.makeText(DashboardActivity.this, R.string.msg_dinner_marked, Toast.LENGTH_SHORT).show();
                }
            });
        });

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