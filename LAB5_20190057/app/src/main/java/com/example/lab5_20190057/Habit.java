package com.example.lab5_20190057;

import java.io.Serializable;

public class Habit implements Serializable {
    private String id;
    private String name;
    private String category;
    private int frequencyHours;
    private String startDate;
    private String startTime;
    private boolean isActive;

    public Habit() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.isActive = true;
    }

    public Habit(String name, String category, int frequencyHours, String startDate, String startTime) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.category = category;
        this.frequencyHours = frequencyHours;
        this.startDate = startDate;
        this.startTime = startTime;
        this.isActive = true;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getFrequencyHours() {
        return frequencyHours;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setFrequencyHours(int frequencyHours) {
        this.frequencyHours = frequencyHours;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getCategoryIcon() {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return R.drawable.ic_exercise;
            case "alimentación":
                return R.drawable.ic_food;
            case "sueño":
                return R.drawable.ic_sleep;
            case "lectura":
                return R.drawable.ic_book;
            case "trabajo":
                return R.drawable.ic_work;
            case "salud":
                return R.drawable.ic_health;
            default:
                return R.drawable.ic_default_habit;
        }
    }

    public int getCategoryColor() {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return android.R.color.holo_orange_dark;
            case "alimentación":
                return android.R.color.holo_green_dark;
            case "sueño":
                return android.R.color.holo_purple;
            case "lectura":
                return android.R.color.holo_blue_dark;
            case "trabajo":
                return android.R.color.darker_gray;
            case "salud":
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.holo_blue_bright;
        }
    }

    @Override
    public String toString() {
        return "Habit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", frequencyHours=" + frequencyHours +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}