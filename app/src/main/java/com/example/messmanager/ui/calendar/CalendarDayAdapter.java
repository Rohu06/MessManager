package com.example.messmanager.ui.calendar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmanager.R;
import com.example.messmanager.databinding.ItemCalendarDayBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * CalendarDayAdapter
 *
 * Renders the 7-column calendar grid. Padding cells (before day 1 of
 * the month) render blank and are non-clickable; real day cells show
 * a colored dot indicating meal status and are clickable.
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {

    public interface Listener {
        void onDayClick(CalendarDay day);
    }

    private final Listener listener;
    private List<CalendarDay> days = new ArrayList<>();

    public CalendarDayAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<CalendarDay> newDays) {
        days = newDays != null ? newDays : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCalendarDayBinding binding = ItemCalendarDayBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(days.get(position));
    }

    @Override
    public int getItemCount() { return days.size(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCalendarDayBinding binding;

        ViewHolder(ItemCalendarDayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CalendarDay day) {
            if (day.isPadding()) {
                binding.tvDayNumber.setText("");
                binding.viewStatusDot.setVisibility(View.INVISIBLE);
                itemView.setClickable(false);
                itemView.setOnClickListener(null);
                return;
            }

            binding.tvDayNumber.setText(String.valueOf(day.getDayOfMonth()));
            binding.viewStatusDot.setVisibility(View.VISIBLE);

            int colorRes;
            switch (day.getStatus()) {
                case GREEN:
                    colorRes = R.color.status_green;
                    break;
                case YELLOW:
                    colorRes = R.color.status_yellow;
                    break;
                case RED:
                    colorRes = R.color.status_red;
                    break;
                default:
                    colorRes = R.color.status_neutral;
            }
            binding.viewStatusDot.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            itemView.getContext().getColor(colorRes)));

            itemView.setClickable(true);
            itemView.setOnClickListener(v -> listener.onDayClick(day));
        }
    }
}