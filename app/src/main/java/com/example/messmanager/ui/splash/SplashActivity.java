package com.example.messmanager.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.databinding.ActivitySplashBinding;
import com.example.messmanager.notification.NotificationHelper;
import com.example.messmanager.ui.dashboard.DashboardActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DISPLAY_DURATION_MS = 1200;

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppPreferences.getInstance(this).applyDarkMode();
        NotificationHelper.createNotificationChannel(this);


        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToDashboard, SPLASH_DISPLAY_DURATION_MS);
    }

    private void navigateToDashboard() {
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}