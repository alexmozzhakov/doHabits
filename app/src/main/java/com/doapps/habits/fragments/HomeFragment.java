package com.doapps.habits.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.doapps.habits.BuildConfig;
import com.doapps.habits.R;
import com.doapps.habits.activity.MainActivity;
import com.doapps.habits.adapter.TimeLineAdapter;
import com.doapps.habits.helper.ConnectionManager;
import com.doapps.habits.helper.HabitListManager;
import com.doapps.habits.helper.HomeDayManager;
import com.doapps.habits.models.IDayManager;
import com.doapps.habits.models.IHabitListProvider;
import com.doapps.habits.models.IStringSelector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class HomeFragment extends Fragment {
    private TextView weather;
    private TextView weatherBot;
    private static final String URL_WEATHER_API = "http://habbitsapp.esy.es/weather.php";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.timeline);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        IHabitListProvider habitListManager =
                HabitListManager.getInstance(getContext());

        // filter list for today
        HomeDayManager.filterListForToday(habitListManager.getList());

        // get list size
        int habitListSize = habitListManager.getList().size();
        // set due count of filtered list
        TextView tasksDue = (TextView) view.findViewById(R.id.tasks_due);
        tasksDue.setText(habitListManager.getDueCount());

        weather = (TextView) view.findViewById(R.id.weather);
        weatherBot = (TextView) view.findViewById(R.id.weatherBot);
        if (ConnectionManager.isConnected(getContext())) {
            getWeather(getContext(), habitListSize);
        } else {
            weather.setText(String.valueOf(habitListSize));
            weatherBot.setText(R.string.all_tasks);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ConnectivityManager conMan = (ConnectivityManager)
                        getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                Log.i("HomeFragment", "addDefaultNetworkActiveListener");
                ConnectivityManager.OnNetworkActiveListener activeListener =
                        new ConnectivityManager.OnNetworkActiveListener() {
                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public void onNetworkActive() {
                                Log.i("HF", "onNetworkActive");
                                getWeather(getContext(), habitListSize);

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user == null) {
                                    FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(task -> {
                                        MainActivity ac = (MainActivity) getActivity();
                                        ac.onSetupNavigationDrawer(
                                                FirebaseAuth.getInstance().getCurrentUser());
                                    });
                                } else {
                                    ((MainActivity) getActivity()).onSetupNavigationDrawer(user);
                                }
                                conMan.removeDefaultNetworkActiveListener(this);
                            }

                        };
                conMan.addDefaultNetworkActiveListener(activeListener);
            }
        }

        // set filtered list to adapter
        TimeLineAdapter timeLineAdapter = new TimeLineAdapter(habitListManager.getList());
        timeLineAdapter.setHasStableIds(true);
        recyclerView.setAdapter(timeLineAdapter);

        // get day of week
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        IStringSelector swipeSelector = (IStringSelector) view.findViewById(R.id.sliding_tabs);
        swipeSelector.setItems(getDaysOfWeekFromToday(dayOfWeek - 1));

        initDaysTabs(
                dayOfWeek,
                swipeSelector,
                new HomeDayManager(timeLineAdapter,
                        HabitListManager.getInstance(getContext()).getList()));
        return view;

    }

    private String[] getDaysOfWeekFromToday(int dayOfWeek) {
        String[] daysOfWeekNames = getActivity().getResources().getStringArray(R.array.days_of_week_array);
        String[] daysOfWeek = new String[7];
        for (int i = 0; i < daysOfWeekNames.length; i++) {
            daysOfWeek[i] = daysOfWeekNames[(dayOfWeek + i) % 7];
        }
        return daysOfWeek;
    }

    /**
     * Initiates
     *
     * @param swipeStringSelector by using
     * @param habitDayManager     for filtering list
     * @param dayOfWeek           for getting dayOfWeek from number of selected item
     */
    private static void initDaysTabs(
            int dayOfWeek,
            IStringSelector swipeStringSelector,
            IDayManager habitDayManager) {

        swipeStringSelector.setOnItemSelectedListener(item -> {
            int value = swipeStringSelector.getAdapter().getCurrentPosition();
            if (value == 0) {
                habitDayManager.updateForToday();

                if (BuildConfig.DEBUG) {
                    Log.i("HomeFragment", "Selected day (today) = " + dayOfWeek);
                }

            } else {
                int daysFromWeekStart = dayOfWeek + value;
                int day = daysFromWeekStart > 7 ? daysFromWeekStart % 7 : daysFromWeekStart;

                if (BuildConfig.DEBUG) {
                    Log.i("HomeFragment", "Selected day = " + day);
                }

                habitDayManager.updateListByDayOfWeek(day);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getWeather(Context context, int listSize) {
        Volley.newRequestQueue(context.getApplicationContext()).add(
                new StringRequest(Request.Method.GET, URL_WEATHER_API,
                        response -> {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);

                                if (jsonResponse.has("error")) {
                                    onNetworkFail(
                                            new Exception(jsonResponse.getString("error")), listSize);
                                } else {
                                    if (getActivity() != null) {
                                        getActivity()
                                                .getSharedPreferences("pref", Context.MODE_PRIVATE)
                                                .edit()
                                                .putString("location", jsonResponse.getString("location"))
                                                .apply();
                                    }

                                    weather.setText(jsonResponse.getString("celsius") + "˚C");
                                    weatherBot.setText(jsonResponse.getString("location"));
                                }

                            } catch (JSONException error) {
                                onNetworkFail(error, listSize);
                            }

                        },
                        error -> onNetworkFail(error, listSize))
        );
    }

    private void onNetworkFail(Exception error, int listSize) {
        if (getActivity() == null) return;
        Log.e("StringRequest error", error.toString());
        weather.setText(String.valueOf(listSize));
        weatherBot.setText(R.string.all_tasks);
        ((MainActivity) getActivity()).setAvatarInvisible();
    }
}