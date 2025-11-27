package com.example.demo;

import java.math.BigDecimal;

public class Stock {
    private String symbol;
    private BigDecimal price;
    private BigDecimal changePercent;

    public Stock(String symbol, BigDecimal price, BigDecimal changePercent) {
        this.symbol = symbol;
        this.price = price;
        this.changePercent = changePercent;
    }

    // Getters and setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }
}
