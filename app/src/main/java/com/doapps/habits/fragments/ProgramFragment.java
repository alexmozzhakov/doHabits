package com.doapps.habits.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.doapps.habits.BuildConfig;
import com.doapps.habits.R;
import com.doapps.habits.helper.HabitListManager;
import com.doapps.habits.models.Achievement;
import com.doapps.habits.models.HabitsDatabase;
import com.doapps.habits.models.Program;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProgramFragment extends Fragment {

    private static final SparseArray<Program> programHashMap = new SparseArray<>();
    private DataSnapshot mSnapshot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View result = inflater.inflate(R.layout.fragment_program, container, false);

        FloatingActionButton fab = (FloatingActionButton) result.findViewById(R.id.fab);

        TextView description = (TextView) result.findViewById(R.id.description);

        description.setText(mSnapshot.child("habit").child("description").getValue(String.class));
        if (BuildConfig.DEBUG) {
            Log.i("Program fragment", mSnapshot.toString());
        }

        if (fab != null) {
            fab.setOnClickListener(view -> {
                int id = Integer.valueOf(mSnapshot.getKey());

                if (programHashMap.get(id) == null) {
                    Program program = onProgramApply(mSnapshot, getContext());
                    programHashMap.put(id, program);
                    Toast.makeText(getActivity(), "New program added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Program already added", Toast.LENGTH_SHORT).show();
                }

            });
        }

        return result;
    }

    void setSnapshot(DataSnapshot snapshot) {
        mSnapshot = snapshot;
    }

    private static Program onProgramApply(DataSnapshot dataSnapshot, Context context) {
        HabitsDatabase habitsDatabase = HabitListManager.getInstance(context).getDatabase();
        long habitId = habitsDatabase.addHabit(
                dataSnapshot.child("habit").child("title").getValue(String.class),
                dataSnapshot.child("habit").child("question").getValue(String.class),
                dataSnapshot.child("habit").child("time").getValue(Integer.class),
                Calendar.getInstance(),
                dataSnapshot.child("habit").child("cost").getValue(Integer.class),
                dataSnapshot.child("habit").child("frequency").getValue(String.class)
        );

        List<Achievement> achievements =
                createAchievementList(dataSnapshot.child("achievements"));

        return new Program(
                Integer.valueOf(dataSnapshot.getKey()),
                dataSnapshot.child("name").getValue(String.class),
                String.format("%s SUCCESS", dataSnapshot.child("success").getValue(String.class)),
                habitId,
                achievements
        );
    }

    private static List<Achievement> createAchievementList(DataSnapshot dataSnapshot) {
        List<Achievement> achievements = new ArrayList<>((int) dataSnapshot.getChildrenCount());
        for (DataSnapshot achievementSnapshot : dataSnapshot.getChildren()) {
            List<String> templates =
                    new ArrayList<>((int) achievementSnapshot.getChildrenCount());
            for (DataSnapshot templatesSnapshot : achievementSnapshot.child("templates").getChildren()) {
                templates.add(templatesSnapshot.child("name").getValue(String.class));
            }
            Achievement achievement = new Achievement(
                    achievementSnapshot.child("rating").getValue(Integer.class),
                    templates);
            achievements.add(achievement);
        }
        return achievements;
    }
}