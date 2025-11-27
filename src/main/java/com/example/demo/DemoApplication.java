package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
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

    @GetMapping(value = "/api/stocks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<StockData> getStocks() {
        List<String> symbols = Arrays.asList("AAPL", "MSFT", "GOOG", "TSLA", "NVDA");
        return stockService.getStocks(symbols);
    }
}

@Service
class StockService {

    @Value("${ALPHAVANTAGE_API_KEY:}")
    private String apiKey;

    private final WebClient webClient;

    public StockService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
    }

    public Flux<StockData> getStocks(List<String> symbols) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("API Key is not configured. Returning empty flux.");
            return Flux.empty();
        }

        return Flux.fromIterable(symbols)
                .delayElements(Duration.ofSeconds(15))
                .flatMap(this::fetchStockData);
    }

    private Mono<StockData> fetchStockData(String symbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/query")
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(GlobalQuote.class)
                .flatMap(globalQuote -> {
                    if (globalQuote != null && globalQuote.getStockData() != null && globalQuote.getStockData().getSymbol() != null) {
                        return Mono.just(globalQuote.getStockData());
                    }
                    return Mono.empty();
                })
                .doOnError(error -> System.err.println("Error fetching stock data for " + symbol + ": " + error.getMessage()));
    }
}
