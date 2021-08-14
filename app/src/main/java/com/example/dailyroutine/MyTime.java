package com.example.dailyroutine;

public class MyTime {
    private int hour, min;

    public MyTime(int hour, int min) {
        this.hour = hour;
        this.min = min;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int toMinute(){
        return hour*60 + min;
    }

    public float toHour(){
        return hour + (min/(float)60);
    }

    public String toAmPm(){
        String time = getTwoDigitHour() + ":" + getTwoDigitMin();
        if(getHour() < 12) return time + " AM";
        else return time + "PM";
    }

    public long toMills(){
        return toSecond()*1000;
    }

    public long toSecond(){
        return toMinute()*60;
    }

    private String getTwoDigitHour(){
        if(getHour() == 0 || getHour() - 12 == 0) return "12";
        if(getHour() < 10) return "0" + getHour();
        if(getHour() <= 12) return getHour() + "";
        if(getHour() - 12 < 10) return "0" + getHour();
        return getHour() + "";
    }
    private String getTwoDigitMin(){
        if(getMin() < 10) return "0" + getMin();
        return getMin() + "";
    }
}
