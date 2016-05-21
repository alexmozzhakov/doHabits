package com.habit_track.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.habit_track.R;
import com.habit_track.fragments.ListFragment;
import com.habit_track.models.Habit;
import com.habit_track.models.MovableItem;
import com.habit_track.viewholder.HabitViewHolder;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class HabitRecycleAdapter extends RecyclerView.Adapter implements MovableItem {

    final private List<Habit> mHabitList;

    public HabitRecycleAdapter(final List<Habit> data) {
        super();
        this.mHabitList = data;
    }

    @Override
    public HabitViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_list_item, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder recyclerHolder, final int position) {

        final HabitViewHolder holder = (HabitViewHolder) recyclerHolder;
        final Habit habit = mHabitList.get(position);
        holder.txtTitle.setText(habit.title);
        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                mHabitList.get(position).setDoneMarker(true);

                ListFragment.mHabitsDatabase.updateHabit(
                        position,
                        habit.markerUpdatedDay,
                        habit.markerUpdatedMonth,
                        habit.markerUpdatedYear,
                        1);

                holder.txtTitle.setTextColor(Color.GRAY);
            } else {

                ListFragment.mHabitsDatabase.updateHabit(
                        position,
                        habit.markerUpdatedDay,
                        habit.markerUpdatedMonth,
                        habit.markerUpdatedYear,
                        0);

                holder.txtTitle.setTextColor(Color.BLACK);
                mHabitList.get(position).setDoneMarker(false);
            }
        });

        final Calendar calendar = Calendar.getInstance();

        holder.checkBox.setChecked(habit.isDone(
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.YEAR)));

    }

    @Override
    public int getItemCount() {
        return mHabitList.size();
    }

    @Override
    public void onItemDismiss(final int position) {
        ListFragment.mHabitsDatabase.delete(mHabitList.get(position).id);
        mHabitList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(final int fromPosition, final int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mHabitList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mHabitList, i, i - 1);
            }
        }
        Log.i("kek", "onItemMove: " + (fromPosition + 1));
        ListFragment.mHabitsDatabase.move(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }
}