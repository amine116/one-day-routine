package com.example.dailyroutine;


public class Task {
    private int startingHour, startingMin, endingHour, endingMin;
    private String title, details;
    private MyTime sTime, eTime;
    public Task(int startingHour, int startingMin, int endingHour, int endingMin, String title,
                String details){
        this.startingHour = startingHour;
        this.startingMin = startingMin;
        this.endingHour = endingHour;
        this.endingMin = endingMin;
        this.title = title;
        this.details = details;
    }



    public int getStartingHour() {
        return startingHour;
    }

    public void setStartingHour(int startingHour) {
        this.startingHour = startingHour;
    }

    public int getStartingMin() {
        return startingMin;
    }

    public void setStartingMin(int startingMin) {
        this.startingMin = startingMin;
    }

    public int getEndingHour() {
        return endingHour;
    }

    public void setEndingHour(int endingHour) {
        this.endingHour = endingHour;
    }

    public int getEndingMin() {
        return endingMin;
    }

    public void setEndingMin(int endingMin) {
        this.endingMin = endingMin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
