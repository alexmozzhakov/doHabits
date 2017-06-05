package com.doapps.habits.helper;

import com.doapps.habits.models.Habit;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.doapps.habits.helper.DayFilter.filterListByDay;
import static com.doapps.habits.helper.DayFilter.filterListForToday;

@SuppressWarnings("HardCodedStringLiteral")
public class HomeDayManagerTest {

    private static List<Habit> generateListOfHabits() {
        final List<Habit> list = new ArrayList<>(15);
        //once types
        list.add(new Habit(1, "once", "", false, false, 12, 12, 12, 60, 30, 30, 1, 0));
        list.add(new Habit(1, "once a week", "", false, false, 12, 12, 12, 60, 30, 30, 1, 7));
        list.add(new Habit(1, "once a year", "", false, false, 12, 12, 12, 60, 30, 30, 1, 365));
        //every ... types
        list.add(new Habit(1, "every sunday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 1, 0));
        list.add(new Habit(1, "every monday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 2, 0));
        list.add(new Habit(1, "every tuesday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 3, 0));
        list.add(new Habit(1, "every wednesday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 4, 0));
        list.add(new Habit(1, "every thursday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 5, 0));
        list.add(new Habit(1, "every friday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 6, 0));
        list.add(new Habit(1, "every saturday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 7, 0));
        list.add(new Habit(1, "every day", "", false, false, 12, 12, 12, 60, 30, 30, 1, 1));
        //every ... and ... types
        list.add(new Habit(1, "every sunday and saturday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 1, 7));
        list.add(new Habit(1, "every monday and friday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 2, 6));
        list.add(new Habit(1, "every tuesday and thursday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 3, 5));
        list.add(new Habit(1, "every wednesday and thursday", "", false, false, 12, 12, 12, 60, 30, 30, 1, 4, 5));
        return list;
    }

    @Test
    public void filterForToday() {
        final List<Habit> list = generateListOfHabits();
        filterListForToday(list);
        String dayOfWeek = Calendar.getInstance()
                .getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US).toLowerCase();

        System.out.println("today : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()),
                    habit.getTitle().contains("once") || habit.getTitle().contains(dayOfWeek)
                            || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 6, CoreMatchers.is(true));
        System.out.println("}");
    }


    @Test
    public void filterListForSun() {
        final List<Habit> list = generateListOfHabits();
        filterListByDay(list, 1);
        System.out.println("sun : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()), habit.getTitle().contains("sunday")
                    || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 3, CoreMatchers.is(true));
        System.out.println("}");
    }

    @Test
    public void filterListForMon() {
        final List<Habit> list = generateListOfHabits();
        filterListByDay(list, 2);
        System.out.println("mon : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()),
                    habit.getTitle().contains("monday")
                            || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 3, CoreMatchers.is(true));
        System.out.println("}");
    }

    @Test
    public void filterListForTue() {
        final List<Habit> list = generateListOfHabits();
        filterListByDay(list, 3);
        System.out.println("tue : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()),
                    habit.getTitle().contains("tuesday")
                            || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 3, CoreMatchers.is(true));
        System.out.println("}");
    }

    @Test
    public void filterListForWed() {
        final List<Habit> list = generateListOfHabits();
        filterListByDay(list, 4);
        System.out.println("wed : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()),
                    habit.getTitle().contains("wednesday")
                            || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 3, CoreMatchers.is(true));
        System.out.println("}");
    }

    @Test
    public void filterListForThu() {
        final List<Habit> list = generateListOfHabits();
        filterListByDay(list, 5);
        System.out.println("thu : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()),
                    habit.getTitle().contains("thursday")
                            || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 4, CoreMatchers.is(true));
        System.out.println("}");
    }

    @Test
    public void filterListForFri() {
        final List<Habit> list = generateListOfHabits();
        filterListByDay(list, 6);
        System.out.println("fri : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()),
                    habit.getTitle().contains("friday")
                            || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 3, CoreMatchers.is(true));
        System.out.println("}");
    }

    @Test
    public void filterListForSat() {
        final List<Habit> list = generateListOfHabits();
        filterListByDay(list, 7);
        System.out.println("sat : {");
        for (final Habit habit : list) {
            System.out.println("\t" + habit);
            Assert.assertThat(String.format("  %s%n", habit.getTitle()),
                    habit.getTitle().contains("saturday")
                            || habit.getTitle().contains("every day"), CoreMatchers.is(true));
        }
        Assert.assertThat(String.format(Locale.ENGLISH, "List size = %d, here are them: %s",
                list.size(), list), list.size() == 3, CoreMatchers.is(true));
        System.out.println("}");
    }
}