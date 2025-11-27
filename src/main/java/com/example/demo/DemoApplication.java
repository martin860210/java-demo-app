package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RestController
public class DemoApplication {

    @Autowired
    private StockService stockService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/api/stocks")
    public Flux<StockData> getStocks() {
        // Now we fetch real data for a predefined list of stocks
        List<String> symbols = Arrays.asList("AAPL", "MSFT", "GOOG");
        return stockService.getStocks(symbols);
    }
}

@Service
class StockService {

    // Spring will inject the value of the environment variable ALPHAVANTAGE_API_KEY here
    @Value("${ALPHAVANTAGE_API_KEY}")
    private String apiKey;

    private final WebClient webClient;

    public StockService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
    }

    public Flux<StockData> getStocks(List<String> symbols) {
        return Flux.fromIterable(symbols)
                .flatMap(this::fetchStockData);
    }

    private Flux<StockData> fetchStockData(String symbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/query")
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GlobalQuote.class)
                .map(GlobalQuote::getStockData)
                .flux();
    }
}
