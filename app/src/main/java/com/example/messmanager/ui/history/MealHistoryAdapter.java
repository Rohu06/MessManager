package com.example.messmanager.ui.history;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmanager.R;
import com.example.messmanager.data.local.entity.MealEntry;
import com.example.messmanager.databinding.ItemMealHistoryBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    private static final SimpleDateFormat SOURCE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DAY_FORMAT =
            new SimpleDateFormat("d", Locale.US);
    private static final SimpleDateFormat MONTH_YEAR_FORMAT =
            new SimpleDateFormat("MMM yyyy", Locale.US);
    private static final SimpleDateFormat FULL_DATE_FORMAT =
            new SimpleDateFormat("EEEE, d MMMM yyyy", Locale.US);

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
            // ── Date parsing ──────────────────────────────────────
            try {
                Date date = SOURCE_FORMAT.parse(entry.getDate());
                binding.tvDayNumber.setText(DAY_FORMAT.format(date));
                binding.tvMonthYear.setText(MONTH_YEAR_FORMAT.format(date).toUpperCase(Locale.US));
                binding.tvDate.setText(FULL_DATE_FORMAT.format(date));
            } catch (ParseException e) {
                binding.tvDayNumber.setText("–");
                binding.tvMonthYear.setText("");
                binding.tvDate.setText(entry.getDate());
            }

            // ── Status pills ─────────────────────────────────────
            if (entry.isSkipped()) {
                // Show only the skipped pill
                binding.pillLunch.setVisibility(View.GONE);
                binding.pillDinner.setVisibility(View.GONE);
                binding.pillSkipped.setVisibility(View.VISIBLE);

                // Tint accent strip amber
                binding.viewAccent.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(
                                itemView.getContext(), R.color.status_yellow)));
            } else {
                binding.pillSkipped.setVisibility(View.GONE);
                binding.pillLunch.setVisibility(View.VISIBLE);
                binding.pillDinner.setVisibility(View.VISIBLE);

                // Lunch pill
                setupMealPill(
                        entry.isLunch(),
                        binding.pillLunch,
                        binding.tvLunchStatus,
                        binding.ivLunchIcon,
                        itemView.getContext().getString(R.string.label_lunch));

                // Dinner pill
                setupMealPill(
                        entry.isDinner(),
                        binding.pillDinner,
                        binding.tvDinnerStatus,
                        binding.ivDinnerIcon,
                        itemView.getContext().getString(R.string.label_dinner));

                // Accent strip: green if both taken, red if both missed, primary otherwise
                int accentColor;
                if (entry.isLunch() && entry.isDinner()) {
                    accentColor = R.color.status_green;
                } else if (!entry.isLunch() && !entry.isDinner()) {
                    accentColor = R.color.status_red;
                } else {
                    accentColor = R.color.md_primary;
                }
                binding.viewAccent.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(
                                itemView.getContext(), accentColor)));
            }

            // ── Notes ────────────────────────────────────────────
            if (entry.getNotes() != null && !entry.getNotes().isEmpty()) {
                binding.layoutNotes.setVisibility(View.VISIBLE);
                binding.tvNotes.setText(entry.getNotes());
            } else {
                binding.layoutNotes.setVisibility(View.GONE);
            }

            // ── Action buttons ───────────────────────────────────
            binding.btnEdit.setOnClickListener(v -> listener.onEditClick(entry));
            binding.btnDelete.setOnClickListener(v -> listener.onDeleteClick(entry));
        }

        private void setupMealPill(boolean isTaken,
                                   View pillContainer,
                                   android.widget.TextView statusText,
                                   android.widget.ImageView icon,
                                   String mealLabel) {
            if (isTaken) {
                pillContainer.setBackgroundResource(R.drawable.bg_pill_taken);
                statusText.setText(mealLabel + " ✓");
                int greenColor = ContextCompat.getColor(itemView.getContext(), R.color.status_green);
                statusText.setTextColor(greenColor);
                icon.setImageTintList(ColorStateList.valueOf(greenColor));
            } else {
                pillContainer.setBackgroundResource(R.drawable.bg_pill_missed);
                statusText.setText(mealLabel + " ✗");
                int redColor = ContextCompat.getColor(itemView.getContext(), R.color.status_red);
                statusText.setTextColor(redColor);
                icon.setImageTintList(ColorStateList.valueOf(redColor));
            }
        }
    }
}