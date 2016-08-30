package com.doapps.habits.helper;

import android.util.Log;

import com.doapps.habits.models.DayManager;
import com.doapps.habits.models.Habit;
import com.doapps.habits.models.UpdatableList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class HomeDayManager implements DayManager {
    private final UpdatableList<Habit> mTimeLineAdapter;
    private final List<Habit> mHabitList;

    public HomeDayManager(UpdatableList<Habit> timeLineAdapter, List<Habit> habitList) {
        mTimeLineAdapter = timeLineAdapter;
        mHabitList = habitList;
    }

    @Override
    public void updateListByDay(int dayOfWeek) {
        List<Habit> dayHabits = new ArrayList<>(mHabitList);
        filterListByDay(dayHabits, dayOfWeek);
        mTimeLineAdapter.updateList(dayHabits);
    }

    @Override
    public void updateForToday() {
        List<Habit> todayHabits = new ArrayList<>(mHabitList);
        filterListForToday(todayHabits);
        mTimeLineAdapter.updateList(todayHabits);
    }

    public static void filterListForToday(Iterable<Habit> todayHabits) {
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        Iterator<Habit> habitIterator = todayHabits.iterator();
        while (habitIterator.hasNext()) {
            short[] freq = habitIterator.next().getFrequencyArray();
            if (freq.length == 0) {
                Log.w("filterListByDay()", "frequency not set");
            } else {
                if (freq.length > 2) { // not once type
                    boolean today = false;
                    for (int i = 0; i < freq.length - 1; i++) {
                        if (freq[i] == dayOfWeek) {
                            today = true;
                            break;
                        }
                    }
                    if (!today) {
                        // if not found days markers
                        habitIterator.remove();
                    }
                }
            }
        }
    }

    @SuppressWarnings("LocalVariableOfConcreteClass")
    static void filterListByDay(Iterable<Habit> dayHabits, int dayOfWeek) {
        Iterator<Habit> habitIterator = dayHabits.iterator();
        while (habitIterator.hasNext()) {
            short[] freq = habitIterator.next().getFrequencyArray();
            if (freq.length == 0) {
                Log.w("filterListByDay()", "frequency not set");
            } else if (freq[0] != freq[1]) {
                // if it's not every day
                if (freq.length > 2) {
                    boolean today = false;
                    for (int j = 0; j < freq.length - 1; j++) {
                        if (freq[j] == dayOfWeek) {
                            today = true;
                            break;
                        }
                    }
                    if (!today) {
                        // if not found days markers
                        habitIterator.remove();
                    }
                } else {
                    // once type
                    habitIterator.remove();
                }
            }
        }
    }
}