package com.habit_track.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.habit_track.R;
import com.habit_track.adapter.ProgramRecycleAdapter;
import com.habit_track.database.HabitDBHandler;
import com.habit_track.models.Achievement;
import com.habit_track.models.Program;
import com.habit_track.models.ProgramView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ProgramsFragment extends Fragment {
    private static final String TAG = ProgramsFragment.class.getSimpleName();
    public static boolean isShowing;
    private ProgramRecycleAdapter mAdapter;
    private List<ProgramView> mProgramData;
    private TextView mTitleTop;
    private TextView mSuccessTop;
    private Typeface mFaceLight;
    private Typeface mFace;
    private ImageView mImageTop;
    private DatabaseReference mRootRef;

    private static ProgramView createProgramView(DataSnapshot dataSnapshot) {
        return new ProgramView(
                dataSnapshot.child("name").getValue(String.class),
                dataSnapshot.child("success").getValue(String.class) + " SUCCESS",
                dataSnapshot.child("habit").child("description").getValue(String.class));
    }

    static Program onProgramApply(DataSnapshot dataSnapshot, Activity activity) {
        long habitId = HabitDBHandler.getInstance(activity.getApplicationContext()).addHabit(
                dataSnapshot.child("habit").child("title").getValue(String.class),
                dataSnapshot.child("habit").child("description").getValue(String.class),
                dataSnapshot.child("habit").child("time").getValue(Integer.class),
                false,
                Calendar.getInstance(),
                0,
                dataSnapshot.child("habit").child("cost").getValue(Integer.class),
                dataSnapshot.child("habit").child("frequency").getValue(Integer.class)
        );

        final ArrayList<Achievement> achievements = createAchievementList(dataSnapshot);

        return new Program(
                Integer.valueOf(dataSnapshot.getKey()),
                dataSnapshot.child("name").getValue(String.class),
                dataSnapshot.child("success").getValue(String.class) + " SUCCESS",
                habitId,
                achievements
        );
    }

    private static ArrayList<Achievement> createAchievementList(DataSnapshot dataSnapshot) {
        final ArrayList<Achievement> achievements = new ArrayList<>();
        for (DataSnapshot achievementSnapshot : dataSnapshot.child("achievements").getChildren()) {
            ArrayList<String> templates = new ArrayList<>((int) achievementSnapshot.getChildrenCount());
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFaceLight = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Light.otf");
        mFace = Typeface.createFromAsset(context.getAssets(), "fonts/Montserrat-Regular.ttf");
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View result = inflater.inflate(R.layout.fragment_programs, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRootRef = database.getReference().child("programs");
        mProgramData = new ArrayList<>();
        mAdapter = new ProgramRecycleAdapter(mProgramData);

        final RecyclerView recyclerView = (RecyclerView) result.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        final LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);

        recyclerView.setAdapter(mAdapter);

        mSuccessTop = (TextView) result.findViewById(R.id.percentTop);
        mImageTop = (ImageView) result.findViewById(R.id.imageViewTop);
        mTitleTop = (TextView) result.findViewById(R.id.titleTop);

        mRootRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final int position = Integer.parseInt(dataSnapshot.getKey());

                Log.i(TAG, "onChildAdded:()");
                if (position == 0) {
                    generateTopProgram();
                } else {
                    mProgramData.add(position - 1, createProgramView(dataSnapshot));
                }

                mAdapter.notifyItemInserted(position - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.i(TAG, "onChildChanged: ()");
                int position = Integer.parseInt(dataSnapshot.getKey()) - 1;

                if (position >= 0) {
                    mProgramData.remove(position);
                    mProgramData.add(position, createProgramView(dataSnapshot));
                }

                mAdapter.notifyItemChanged(position);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onChildRemoved()");
                int position = Integer.parseInt(dataSnapshot.getKey()) - 1;
                if (position >= 0) {
                    mProgramData.remove(position);
                }
                mAdapter.notifyItemRemoved(position);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // mAdapter.notifyItemMoved();
                Log.i(TAG, "onChildMoved: ()");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DatabaseError", databaseError.toString());
            }
        });

        return result;
    }

    private void generateTopProgram() {
        DatabaseReference programRef = mRootRef.child("0");

        programRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mTitleTop.setText(snapshot.child("name").getValue(String.class));
                mTitleTop.setTextColor(Color.WHITE);
                final String imageLink = "http://habbitsapp.esy.es/img_progs/"
                        + snapshot.child("image").getValue(String.class) + ".jpg";
                Log.v("IMG_URL", imageLink);
                if (getActivity() != null) {
                    Picasso.with(getActivity().getApplicationContext())
                            .load(imageLink)
                            .into(mImageTop);
                }
                mTitleTop.setTypeface(mFaceLight);

                if (isShowing) {
                    createProgramApplyFragment(snapshot);
                }

                mTitleTop.setOnClickListener((view) ->
                        onClick(snapshot));

                //Style percent view
                mSuccessTop.setText(snapshot.child("success").getValue(String.class));
                mSuccessTop.setTypeface(mFace);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.toString());
            }
        });
    }

    private void onClick(DataSnapshot snapshot) {
        if (!isShowing) {
            createProgramApplyFragment(snapshot);
        } else {
            final FragmentManager fm = getChildFragmentManager();
            fm.beginTransaction()
                    .remove(fm.findFragmentById(R.id.recyclerLayout))
                    .commit();
            isShowing = false;
        }
    }

    private void createProgramApplyFragment(DataSnapshot snapshot) {

        // delete previous fragment if showing
        final FragmentManager fm = getChildFragmentManager();
        if (isShowing) {
            fm.beginTransaction()
                    .remove(fm.findFragmentById(R.id.recyclerLayout))
                    .commit();
        }

        final ProgramFragment programFragment = new ProgramFragment();
        programFragment.snapshot = snapshot;

        fm.beginTransaction()
                .replace(R.id.recyclerLayout, programFragment)
                .commit();

        isShowing = true;
    }

}
