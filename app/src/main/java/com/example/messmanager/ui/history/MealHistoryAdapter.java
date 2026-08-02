package com.example.messmanager.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmanager.R;
import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.databinding.ItemMealHistoryBinding;
import com.example.messmanager.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * MealHistoryAdapter
 *
 * Displays meal entries in a RecyclerView. Uses DiffUtil for efficient
 * updates when the underlying filtered/sorted list changes. Edit and
 * delete actions are delegated to the hosting Activity via a listener
 * interface, keeping the adapter free of navigation/dialog logic.
 */
public class MealHistoryAdapter extends RecyclerView.Adapter<MealHistoryAdapter.ViewHolder> {

    public interface Listener {
        void onEditClick(MealEntry entry);
        void onDeleteClick(MealEntry entry);
    }

    private final Listener listener;
    private List<MealEntry> entries = new ArrayList<>();

    public MealHistoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<MealEntry> newEntries) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() { return entries.size(); }

            @Override
            public int getNewListSize() { return newEntries.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return entries.get(oldPos).getId() == newEntries.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                MealEntry a = entries.get(oldPos);
                MealEntry b = newEntries.get(newPos);
                return a.isLunch() == b.isLunch()
                        && a.isDinner() == b.isDinner()
                        && a.isSkipped() == b.isSkipped()
                        && java.util.Objects.equals(a.getNotes(), b.getNotes());
            }
        });
        entries = newEntries;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMealHistoryBinding binding = ItemMealHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemMealHistoryBinding binding;

        ViewHolder(ItemMealHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MealEntry entry) {
            binding.tvDate.setText(DateUtils.formatForDisplay(entry.getDate()));

            if (entry.isSkipped()) {
                binding.tvStatus.setText(R.string.status_skipped);
            } else {
                String lunch = entry.isLunch() ? "Lunch ✓" : "Lunch ✗";
                String dinner = entry.isDinner() ? "Dinner ✓" : "Dinner ✗";
                binding.tvStatus.setText(lunch + "   " + dinner);
            }

            if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
                binding.tvNotes.setVisibility(View.VISIBLE);
                binding.tvNotes.setText(entry.getNotes());
            } else {
                binding.tvNotes.setVisibility(View.GONE);
            }

            binding.btnEdit.setOnClickListener(v -> listener.onEditClick(entry));
            binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(entry));
        }
    }
}