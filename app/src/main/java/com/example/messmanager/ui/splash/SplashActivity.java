package com.example.messmanager.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.databinding.ActivitySplashBinding;
import com.example.messmanager.notification.NotificationHelper;
import com.example.messmanager.ui.dashboard.DashboardActivity;

/**
 * SplashActivity
 *
 * Polished entrance screen featuring Material 3 branding, ambient aura,
 * smooth choreographed micro-animations, and seamless transition to DashboardActivity.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_DURATION_MS = 1400;

    private ActivitySplashBinding binding;
    private final Handler navHandler = new Handler(Looper.getMainLooper());
    private final Runnable navRunnable = this::navigateToDashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply dark mode preference before view inflation to avoid theme flicker
        AppPreferences.getInstance(this).applyDarkMode();

        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Notification channel initialization
        NotificationHelper.createNotificationChannel(this);

        // Adjust system insets so bottom version & elements don't get clipped
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootSplash, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        setupInitialViewState();
        startChoreographedAnimations();

        navHandler.postDelayed(navRunnable, SPLASH_DISPLAY_DURATION_MS);
    }

    /**
     * Sets initial alpha, scale, and translation properties for entrance animation.
     */
    private void setupInitialViewState() {
        binding.cardLogo.setScaleX(0.7f);
        binding.cardLogo.setScaleY(0.7f);
        binding.cardLogo.setAlpha(0f);

        binding.viewGlow.setScaleX(0.5f);
        binding.viewGlow.setScaleY(0.5f);
        binding.viewGlow.setAlpha(0f);

        binding.tvAppName.setAlpha(0f);
        binding.tvAppName.setTranslationY(24f);

        binding.tvTagline.setAlpha(0f);
        binding.tvTagline.setTranslationY(20f);

        binding.layoutBadge.setAlpha(0f);
        binding.layoutBadge.setScaleX(0.85f);
        binding.layoutBadge.setScaleY(0.85f);

        binding.progressBar.setAlpha(0f);
        binding.tvVersion.setAlpha(0f);
    }

    /**
     * Plays smooth, staggered entrance animations across splash elements.
     */
    private void startChoreographedAnimations() {
        // 1. Ambient Glow Aura
        binding.viewGlow.animate()
                .alpha(0.85f)
                .scaleX(1.15f)
                .scaleY(1.15f)
                .setDuration(700)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 2. Branded Logo Card (Overshoot bounce)
        binding.cardLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator(1.25f))
                .start();

        // 3. App Title
        binding.tvAppName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(480)
                .setStartDelay(160)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 4. Tagline
        binding.tvTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setStartDelay(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 5. Feature Badge Capsule
        binding.layoutBadge.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setStartDelay(360)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // 6. Linear Progress Bar & Version Footer
        binding.progressBar.animate()
                .alpha(1f)
                .setDuration(350)
                .setStartDelay(420)
                .start();

        binding.tvVersion.animate()
                .alpha(1f)
                .setDuration(350)
                .setStartDelay(480)
                .start();
    }

    /**
     * Navigates to DashboardActivity with a smooth cross-fade animation.
     */
    private void navigateToDashboard() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        Intent intent = new Intent(this, DashboardActivity.class);
        Bundle options = ActivityOptionsCompat.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
        ).toBundle();

        startActivity(intent, options);
        finish();
    }

    @Override
    protected void onDestroy() {
        navHandler.removeCallbacks(navRunnable);
        super.onDestroy();
    }
}