package com.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;

// This class is a wrapper because the Alpha Vantage API returns a nested JSON object.
public class GlobalQuote {

    @JsonProperty("Global Quote")
    private StockData stockData;

    public StockData getStockData() {
        return stockData;
    }

    public void setStockData(StockData stockData) {
        this.stockData = stockData;
    }
}
