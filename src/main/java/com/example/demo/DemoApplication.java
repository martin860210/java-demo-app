package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
@RestController
public class DemoApplication {

    @Autowired
    private StockService stockService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping(value = "/api/stocks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Stock> getStocks() {
        List<String> symbols = Arrays.asList("AAPL", "MSFT", "GOOG", "TSLA", "NVDA");
        return stockService.getStocks(symbols);
    }
}

@Service
class StockService {

    public Flux<Stock> getStocks(List<String> symbols) {
        return Flux.create(emitter -> {
            try {
                // This can throw IOException (e.g. for 429 Too Many Requests)
                Map<String, yahoofinance.Stock> yahooStocks = YahooFinance.get(symbols.toArray(new String[0]));
                
                if (yahooStocks == null || yahooStocks.isEmpty()) {
                    emitter.complete();
                    return;
                }

                yahooStocks.values().forEach(yahooStock -> {
                    if (yahooStock != null && yahooStock.getQuote() != null && yahooStock.getQuote().getPrice() != null) {
                        StockQuote quote = yahooStock.getQuote();
                        Stock stock = new Stock(
                                quote.getSymbol(),
                                quote.getPrice(),
                                quote.getChangeInPercent()
                        );
                        emitter.next(stock); // Emit each stock as it's processed
                    }
                });
                emitter.complete(); // Signal that we are done
            } catch (IOException e) {
                // If Yahoo Finance fails (e.g., rate limiting), log the error and complete the stream gracefully.
                // This prevents the 500 server error and allows the frontend to handle it.
                System.err.println("Failed to fetch stocks from Yahoo Finance: " + e.getMessage());
                emitter.complete(); // Instead of emitter.error(e), we complete the stream.
            }
        });
    }
}
