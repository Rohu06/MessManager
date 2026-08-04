package com.example.messmanager.ui.history;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.messmanager.R;
import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.data.repository.MealRepository;
import com.example.messmanager.databinding.ActivityHistoryBinding;
import com.example.messmanager.ui.addmeal.AddMealActivity;

import java.util.List;

/**
 * HistoryActivity
 *
 * Shows every meal record with search, filter (chips), and sort
 * (newest/oldest) controls. Tapping edit on a row opens AddMealActivity
 * pre-loaded with that date; delete asks for confirmation first.
 */
public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private HistoryViewModel viewModel;
    private MealHistoryAdapter adapter;
    private boolean isFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        setupBackButton();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupSortButton();
        observeList();
    }

    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MealHistoryAdapter(new MealHistoryAdapter.Listener() {
            @Override
            public void onEditClick(MealEntry entry) {
                android.content.Intent intent = new android.content.Intent(HistoryActivity.this, AddMealActivity.class);
                intent.putExtra(AddMealActivity.EXTRA_DATE, entry.getDate());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(MealEntry entry) {
                confirmDelete(entry);
            }
        });
        binding.recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerHistory.setAdapter(adapter);

        // Set layout animation for staggered item entrance
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(
                this, R.anim.layout_animation_slide_up);
        binding.recyclerHistory.setLayoutAnimation(animation);
    }

    private void confirmDelete(MealEntry entry) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_delete_entry)
                .setMessage(R.string.msg_confirm_delete)
                .setPositiveButton(R.string.action_delete, (dialog, which) ->
                        viewModel.deleteEntry(entry, new MealRepository.SaveCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(HistoryActivity.this, R.string.msg_meal_deleted, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String message) {
                                Toast.makeText(HistoryActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                viewModel.setFilter(HistoryViewModel.FilterType.ALL);
                updateFilterIndicator("All entries");
                return;
            }
            int id = checkedIds.get(0);
            if (id == binding.chipLunch.getId()) {
                viewModel.setFilter(HistoryViewModel.FilterType.LUNCH_ONLY);
                updateFilterIndicator("Lunch only");
            } else if (id == binding.chipDinner.getId()) {
                viewModel.setFilter(HistoryViewModel.FilterType.DINNER_ONLY);
                updateFilterIndicator("Dinner only");
            } else if (id == binding.chipSkipped.getId()) {
                viewModel.setFilter(HistoryViewModel.FilterType.SKIPPED_ONLY);
                updateFilterIndicator("Skipped only");
            } else {
                viewModel.setFilter(HistoryViewModel.FilterType.ALL);
                updateFilterIndicator("All entries");
            }
        });
    }

    private void updateFilterIndicator(String filterText) {
        binding.tvFilterIndicator.setText("Showing: " + filterText);
        binding.tvFilterIndicator.setVisibility(View.VISIBLE);
        // Hide after 2 seconds
        binding.tvFilterIndicator.postDelayed(() ->
                binding.tvFilterIndicator.animate().alpha(0f).withEndAction(() ->
                        binding.tvFilterIndicator.setVisibility(View.GONE)), 2000);
    }
    private void setupSortButton() {
        binding.btnSort.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, binding.btnSort);
            popup.getMenu().add(0, 0, 0, R.string.sort_newest_first);
            popup.getMenu().add(0, 1, 1, R.string.sort_oldest_first);
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0) {
                    viewModel.setSortOrder(HistoryViewModel.SortOrder.NEWEST_FIRST);
                } else {
                    viewModel.setSortOrder(HistoryViewModel.SortOrder.OLDEST_FIRST);
                }
                return true;
            });
            popup.show();
        });
    }

    private void observeList() {
        viewModel.getFilteredEntries().observe(this, this::renderList);
    }

    private void renderList(List<MealEntry> entries) {
        adapter.submitList(entries);
        boolean isEmpty = entries == null || entries.isEmpty();
        binding.recyclerHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // Update entry count badge in the header
        updateEntryCount(entries);

        // Play layout animation on first load
        if (isFirstLoad && !isEmpty) {
            binding.recyclerHistory.scheduleLayoutAnimation();
            isFirstLoad = false;
        }
    }

    private void updateEntryCount(List<MealEntry> entries) {
        int count = (entries != null) ? entries.size() : 0;
        if (count == 0) {
            binding.tvEntryCount.setText(R.string.label_no_entries);
        } else if (count == 1) {
            binding.tvEntryCount.setText(R.string.label_one_entry);
        } else {
            binding.tvEntryCount.setText(getString(R.string.label_entries_count, count));
        }
    }
}