package com.doapps.habits.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.doapps.habits.adapter.TimeLineAdapter;
import com.doapps.habits.helper.HabitListManager;
import com.doapps.habits.helper.HomeDayManager;
import com.doapps.habits.models.DayManager;
import com.doapps.habits.models.Habit;
import com.doapps.habits.models.HabitListProvider;
import com.doapps.habits.models.StringSelector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {
    private TextView weather;
    private TextView weatherBot;

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String URL_WEATHER_API = "http://habbitsapp.esy.es/weather.php";

    private static boolean isConnected(final Context context) {
        final ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.timeline);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final HabitListProvider habitListManager =
                HabitListManager.getInstance(getContext());

        final List<Habit> habitList = new ArrayList<>(habitListManager.getList());
        // get day of week
        final int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1;
        // filter list for today
        HomeDayManager.filterListForToday(habitList, dayOfWeek + 1);

        // set due count of filtered list
        final TextView tasksDue = (TextView) view.findViewById(R.id.tasks_due);
        tasksDue.setText(getDueCount(habitList));

        weather = (TextView) view.findViewById(R.id.weather);
        weatherBot = (TextView) view.findViewById(R.id.weatherBot);
        if (isConnected(getContext())) {
            getWeather(getContext());
        } else {
            weather.setText(String.valueOf(habitList.size()));
            weatherBot.setText("All tasks");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final ConnectivityManager conMan = (ConnectivityManager)
                        getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                Log.i("Lol", "addDefaultNetworkActiveListener");
                conMan.addDefaultNetworkActiveListener(() -> getWeather(getContext()));
            }
        }

        // set filtered list to adapter
        final TimeLineAdapter timeLineAdapter = new TimeLineAdapter(habitList);
        timeLineAdapter.setHasStableIds(true);
        recyclerView.setAdapter(timeLineAdapter);

        final StringSelector swipeSelector = (StringSelector) view.findViewById(R.id.sliding_tabs);

        swipeSelector.setItems(getDaysOfWeekFromToday(dayOfWeek));

        initDaysTabs(
                dayOfWeek,
                swipeSelector,
                HabitListManager.getInstance(getContext()),
                new HomeDayManager(timeLineAdapter));

        return view;

    }

    private String[] getDaysOfWeekFromToday(final int dayOfWeek) {
        final String[] daysOfWeekNames = getActivity().getResources().getStringArray(R.array.days_of_week_array);
        final String[] daysOfWeek = new String[7];
        for (int i = 0; i < daysOfWeekNames.length; i++) {
            daysOfWeek[i] = daysOfWeekNames[(dayOfWeek + i) % 7];
        }
        return daysOfWeek;
    }

    private static void initDaysTabs(
            final int dayOfWeek,
            final StringSelector swipeStringSelector,
            final HabitListProvider habitListProvider,
            final DayManager<Habit> habitDayManager) {

        swipeStringSelector.setOnItemSelectedListener(item -> {
            final int value = swipeStringSelector.getAdapter().getCurrentPosition();
            if (value == 0) {
                habitDayManager.updateForToday(habitListProvider.getList(), dayOfWeek + 1);

                if (BuildConfig.DEBUG) {
                    Log.i("HomeFragment", "Selected day (today) = " + dayOfWeek);
                }

            } else {
                final int day = (dayOfWeek + value) % 7;

                if (BuildConfig.DEBUG) {
                    Log.i("HomeFragment", "Selected day = " + day);
                }

                habitDayManager.updateListByDay(habitListProvider.getList(), day);
            }
        });
    }

    private static CharSequence getDueCount(final Iterable<Habit> habitsList) {
        final Calendar calendar = Calendar.getInstance();
        final int date = calendar.get(Calendar.DATE);
        final int month = calendar.get(Calendar.MONTH);
        final int year = calendar.get(Calendar.YEAR);

        int counter = 0;
        for (final Habit habit : habitsList) {
            if (!habit.isDone(date, month, year)) {
                counter++;
            }
        }
        return String.valueOf(counter);
    }

    private void getWeather(final Context context) {
        Volley.newRequestQueue(context.getApplicationContext()).add(
                new StringRequest(Request.Method.GET, URL_WEATHER_API,
                        response -> {
                            try {
                                final JSONObject o = new JSONObject(response);

                                if (o.has("error")) {
                                    Log.e("JSONException", "Response got: " + o.getString("error"));
                                } else {
                                    if (getActivity() != null) {
                                        getActivity()
                                                .getSharedPreferences("pref", Context.MODE_PRIVATE)
                                                .edit()
                                                .putString("location", o.getString("location"))
                                                .apply();
                                    }

                                    weather.setText(o.getString("celsius") + "˚C");
                                    weatherBot.setText(o.getString("location"));
                                }

                            } catch (final JSONException e) {
                                Log.e("JSONException", "Response got: " + response, e);
                            }

                        },
                        error -> Log.e("StringRequest error", error.toString()))
        );
    }
}