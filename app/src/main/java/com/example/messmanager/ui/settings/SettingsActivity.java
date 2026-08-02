package com.example.messmanager.ui.settings;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmanager.BuildConfig;
import com.example.messmanager.R;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.databinding.ActivitySettingsBinding;

/**
 * SettingsActivity
 *
 * App configuration: total coupon count, dark mode, monthly reset,
 * and entry points to Reminders (Module 8) and Backup/Restore
 * (Module 9), plus static About/Privacy/Version info.
 */
public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        binding.tvTotalCouponsValue.setText(String.valueOf(viewModel.getTotalCoupons()));
        binding.switchDarkMode.setChecked(viewModel.isDarkModeEnabled());
        binding.tvVersionValue.setText(BuildConfig.VERSION_NAME);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.rowChangeTotalCoupons.setOnClickListener(v -> showChangeCouponsDialog());
        binding.rowResetMonth.setOnClickListener(v -> confirmResetMonth());

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setDarkModeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.rowReminders.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.reminders.ReminderSettingsActivity.class)));        binding.rowBackup.setOnClickListener(v -> showComingSoon());
        binding.rowRestore.setOnClickListener(v -> showComingSoon());

        binding.rowAbout.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.title_about)
                .setMessage(R.string.msg_about_body)
                .setPositiveButton(R.string.action_ok, null)
                .show());

        binding.rowPrivacy.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.title_privacy)
                .setMessage(R.string.msg_privacy_body)
                .setPositiveButton(R.string.action_ok, null)
                .show());

        binding.rowBackup.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.backup.BackupRestoreActivity.class)));
        binding.rowRestore.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, com.example.messmanager.ui.backup.BackupRestoreActivity.class)));
    }

    private void showChangeCouponsDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(viewModel.getTotalCoupons()));

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_change_total_coupons)
                .setView(input)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String text = input.getText().toString().trim();
                    if (text.isEmpty()) return;
                    int value = Integer.parseInt(text);
                    if (value <= 0) {
                        Toast.makeText(this, R.string.msg_invalid_coupon_count, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    viewModel.setTotalCoupons(value);
                    binding.tvTotalCouponsValue.setText(String.valueOf(value));
                    Toast.makeText(this, R.string.msg_settings_saved, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmResetMonth() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_reset_month)
                .setMessage(R.string.msg_confirm_reset_month)
                .setPositiveButton(R.string.action_reset, (dialog, which) ->
                        viewModel.resetCurrentMonth(new MealRepository.SaveCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(SettingsActivity.this, R.string.msg_month_reset, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showComingSoon() {
        Toast.makeText(this, R.string.msg_module_coming_soon, Toast.LENGTH_SHORT).show();
    }


}