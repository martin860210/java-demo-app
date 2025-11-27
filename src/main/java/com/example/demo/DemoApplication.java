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
                Map<String, yahoofinance.Stock> yahooStocks = YahooFinance.get(symbols.toArray(new String[0]));
                yahooStocks.values().forEach(yahooStock -> {
                    StockQuote quote = yahooStock.getQuote();
                    Stock stock = new Stock(
                            quote.getSymbol(),
                            quote.getPrice(),
                            quote.getChangeInPercent()
                    );
                    emitter.next(stock); // Emit each stock as it's processed
                });
                emitter.complete(); // Signal that we are done
            } catch (IOException e) {
                emitter.error(e); // Propagate errors
            }
        });
    }
}
