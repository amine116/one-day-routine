package com.example.dailyroutine;

import java.util.Comparator;

public class SortTasksList implements Comparator<Task> {

    @Override
    public int compare(Task task1, Task task2) {
        int first = task1.getStartingHour()*60 + task1.getStartingMin(),
                second = task2.getStartingHour()*60 + task2.getStartingMin();

        return first - second;
    }
}
