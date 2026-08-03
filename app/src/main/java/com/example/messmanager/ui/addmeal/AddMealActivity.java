package com.example.messmanager.ui.addmeal;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.messmanager.R;
import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.databinding.ActivityAddMealBinding;
import com.example.messmanager.util.DateUtils;

import java.util.Calendar;

/**
 * AddMealActivity
 *
 * Lets the user create, edit, or delete a meal entry for any date.
 * Launched from Dashboard's FAB (defaults to today) — later, History's
 * edit action will also launch this with a specific date pre-filled.
 */
public class AddMealActivity extends AppCompatActivity {

    public static final String EXTRA_DATE = "extra_date";

    private ActivityAddMealBinding binding;
    private AddMealViewModel viewModel;
    private MealEntry currentEntry; // null when adding a new entry

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddMealBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AddMealViewModel.class);

        String initialDate = getIntent().getStringExtra(EXTRA_DATE);
        if (initialDate == null) {
            initialDate = DateUtils.getTodayDateString();
        }
        viewModel.setSelectedDate(initialDate);

        setupSkippedToggle();
        setupDatePicker();
        setupSaveButton();
        setupDeleteButton();

        binding.btnBack.setOnClickListener(v -> finish());

        observeExistingEntry();
    }

    private void setupDatePicker() {
        updateDateButtonLabel(viewModel.getSelectedDateValue());

        binding.btnPickDate.setOnClickListener(v -> {
            Calendar cal = DateUtils.getCalendarFromDateString(viewModel.getSelectedDateValue());
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String picked = DateUtils.buildDateString(year, month, dayOfMonth);
                viewModel.setSelectedDate(picked);
                updateDateButtonLabel(picked);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void updateDateButtonLabel(String date) {
        binding.btnPickDate.setText(DateUtils.formatForDisplay(date));
    }

    /** Skipped and Lunch/Dinner are mutually exclusive — enforce that in the UI. */
    private void setupSkippedToggle() {
        binding.cbSkipped.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.cbLunch.setEnabled(!isChecked);
            binding.cbDinner.setEnabled(!isChecked);
            if (isChecked) {
                binding.cbLunch.setChecked(false);
                binding.cbDinner.setChecked(false);
            }
        });
    }

    /**
     * Reactively fills the form whenever the selected date's existing
     * entry changes — this is the "auto edit-mode" behavior that
     * prevents duplicate entries for the same date.
     */
    private void observeExistingEntry() {
        viewModel.getExistingEntry().observe(this, entry -> {
            currentEntry = entry;
            if (entry != null) {
                binding.tvScreenTitle.setText(R.string.title_edit_meal);
                binding.cbSkipped.setChecked(entry.isSkipped());
                binding.cbLunch.setChecked(entry.isLunch());
                binding.cbDinner.setChecked(entry.isDinner());
                binding.etNotes.setText(entry.getNotes());
                binding.btnDelete.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvScreenTitle.setText(R.string.title_add_meal);
                binding.cbSkipped.setChecked(false);
                binding.cbLunch.setChecked(false);
                binding.cbDinner.setChecked(false);
                binding.etNotes.setText("");
                binding.btnDelete.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void setupSaveButton() {
        binding.btnSave.setOnClickListener(v -> {
            String notes = binding.etNotes.getText().toString().trim();
            viewModel.save(
                    currentEntry,
                    viewModel.getSelectedDateValue(),
                    binding.cbLunch.isChecked(),
                    binding.cbDinner.isChecked(),
                    binding.cbSkipped.isChecked(),
                    notes.isEmpty() ? null : notes,
                    new MealRepository.SaveCallback() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(AddMealActivity.this, R.string.msg_meal_saved, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(AddMealActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void setupDeleteButton() {
        binding.btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.title_delete_entry)
                .setMessage(R.string.msg_confirm_delete)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (currentEntry != null) {
                        viewModel.delete(currentEntry, new MealRepository.SaveCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(AddMealActivity.this, R.string.msg_meal_deleted, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(AddMealActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show());
    }
}