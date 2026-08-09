package com.example.messmanager.ui.calendar;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmanager.R;
import com.example.messmanager.databinding.ItemCalendarDayBinding;
import com.example.messmanager.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * CalendarDayAdapter
 *
 * Renders the 7-column calendar grid. Padding cells (before day 1 of
 * the month) render blank and are non-clickable; real day cells show
 * a colored circular background indicating meal status and are clickable.
 * Today's date gets an additional primary-color ring highlight.
 */
public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.ViewHolder> {

    public interface Listener {
        void onDayClick(CalendarDay day, View sharedElement);
    }

    private final Listener listener;
    private List<CalendarDay> days = new ArrayList<>();
    private final String today = DateUtils.getTodayDateString();

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
                binding.viewStatusCircle.setVisibility(View.INVISIBLE);
                binding.viewTodayRing.setVisibility(View.INVISIBLE);
                binding.viewStatusCircle.setTransitionName(null);
                itemView.setClickable(false);
                itemView.setOnClickListener(null);
                return;
            }

            binding.tvDayNumber.setText(String.valueOf(day.getDayOfMonth()));

            // Set a unique transition name per day for shared element transitions
            binding.viewStatusCircle.setTransitionName("transition_day_" + day.getDate());

            // Determine status color and apply tinted circular background
            int colorRes;
            int bgAlpha = 40; // ~15% opacity for the circle fill
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
                    bgAlpha = 30;
            }

            int baseColor = itemView.getContext().getColor(colorRes);
            int tintedColor = Color.argb(bgAlpha,
                    Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));

            binding.viewStatusCircle.setVisibility(View.VISIBLE);
            binding.viewStatusCircle.setBackgroundTintList(
                    ColorStateList.valueOf(tintedColor));

            // Set text color: darker for status days, lighter for neutral
            if (day.getStatus() != CalendarDay.Status.NEUTRAL) {
                binding.tvDayNumber.setTextColor(itemView.getContext().getColor(colorRes));
            } else {
                binding.tvDayNumber.setTextColor(
                        itemView.getContext().getColorStateList(R.color.md_on_surface).getDefaultColor());
            }

            // Highlight today with a primary-color ring
            boolean isToday = day.getDate() != null && day.getDate().equals(today);
            binding.viewTodayRing.setVisibility(isToday ? View.VISIBLE : View.INVISIBLE);
            if (isToday) {
                binding.tvDayNumber.setTextColor(
                        itemView.getContext().getColor(R.color.md_primary));
            }

            itemView.setClickable(true);
            itemView.setOnClickListener(v -> listener.onDayClick(day, binding.viewStatusCircle));
        }
    }
}