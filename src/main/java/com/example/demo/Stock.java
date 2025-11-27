package com.example.demo;

public class Stock {
    private String name;
    private String id;
    private double changePercent;
    private String trend; // "up" or "down"

    // Constructors
    public Stock() {
    }

    public Stock(String name, String id, double changePercent, String trend) {
        this.name = name;
        this.id = id;
        this.changePercent = changePercent;
        this.trend = trend;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }
}
