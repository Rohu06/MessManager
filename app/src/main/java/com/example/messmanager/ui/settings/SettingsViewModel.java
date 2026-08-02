package com.example.messmanager.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.messmanager.data.preferences.AppPreferences;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.util.DateUtils;

/**
 * SettingsViewModel
 *
 * Exposes preference reads/writes and the "reset current month"
 * action. Kept thin — most of this screen is direct preference
 * access, so there's little state to hold beyond what's read on demand.
 */
public class SettingsViewModel extends AndroidViewModel {

    private final AppPreferences preferences;
    private final MealRepository repository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        preferences = AppPreferences.getInstance(application);
        repository = new MealRepository(application);
    }

    public int getTotalCoupons() {
        return preferences.getTotalCoupons();
    }

    public void setTotalCoupons(int value) {
        preferences.setTotalCoupons(value);
    }

    public boolean isDarkModeEnabled() {
        return preferences.isDarkModeEnabled();
    }

    public void setDarkModeEnabled(boolean enabled) {
        preferences.setDarkModeEnabled(enabled);
    }

    public void resetCurrentMonth(MealRepository.SaveCallback callback) {
        repository.resetCurrentMonth(DateUtils.getCurrentMonth(), DateUtils.getCurrentYear(), callback);
    }
}