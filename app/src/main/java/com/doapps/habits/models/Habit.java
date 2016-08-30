package com.doapps.habits.models;

import java.util.Arrays;
import java.util.Calendar;

public final class Habit {

    //Constant Habit values
    public final String title;
    private final String question;
    private final int time;
    private final int cost;
    private final short[] frequencyArray;

    // Changeable Habit values
    public int id;
    private boolean doneMarker;
    private int markerUpdatedDay;
    private int markerUpdatedMonth;
    private int markerUpdatedYear;
    private final int daysFollowing;

    public Habit(int id, String title, String question, boolean doneMarker,
                 int markerUpdatedDay, int markerUpdatedMonth, int markerUpdatedYear,
                 int time, int followingFrom, int cost, short... frequencyArray) {

        this.id = id;
        this.title = title;
        this.question = question;
        this.doneMarker = doneMarker;
        this.markerUpdatedDay = markerUpdatedDay;
        this.markerUpdatedMonth = markerUpdatedMonth;
        this.markerUpdatedYear = markerUpdatedYear;
        this.time = time;
        daysFollowing = followingFrom;
        this.cost = cost;
        this.frequencyArray = frequencyArray;
    }

    public short[] getFrequencyArray() {
        return frequencyArray;
    }

    public void setDoneMarker(boolean doneMarker) {
        this.doneMarker = doneMarker;
        if (doneMarker) {
            Calendar calendar = Calendar.getInstance();
            markerUpdatedDay = calendar.get(Calendar.DATE);
            markerUpdatedMonth = calendar.get(Calendar.MONTH);
            markerUpdatedYear = calendar.get(Calendar.YEAR);
        }
    }

    public boolean isDone(int markerUpdatedDay, int markerUpdatedMonth, int markerUpdatedYear) {
        return markerUpdatedDay == this.markerUpdatedDay &&
                markerUpdatedMonth == this.markerUpdatedMonth &&
                markerUpdatedYear == this.markerUpdatedYear &&
                doneMarker;
    }

    @Override
    public String toString() {
        return String.format("Habit{id=%d, title='%s', question='%s', time=%d, cost=%d," +
                " frequencyArray=%s, doneMarker=%s, markerUpdatedDay=%d, markerUpdatedMonth=%d," +
                " markerUpdatedYear=%d, daysFollowing=%d}",
                id, title, question, time, cost, Arrays.toString(frequencyArray), doneMarker,
                markerUpdatedDay, markerUpdatedMonth, markerUpdatedYear, daysFollowing);
    }

    public int getMarkerUpdatedDay() {
        return markerUpdatedDay;
    }

    public int getMarkerUpdatedMonth() {
        return markerUpdatedMonth;
    }

    public int getMarkerUpdatedYear() {
        return markerUpdatedYear;
    }

}