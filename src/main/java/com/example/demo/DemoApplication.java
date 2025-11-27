package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        List<String> symbols = Arrays.asList("AAPL", "MSFT", "GOOG", "TSLA", "NVDA"); // Added more stocks
        return stockService.getStocks(symbols);
    }
}

@Service
class StockService {

    @Value("${ALPHAVANTAGE_API_KEY:}") // Added default empty value to avoid startup failure if not set
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
                .delayElements(java.time.Duration.ofSeconds(15)) // Add delay to respect API rate limits (e.g., 5 calls/min)
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
                    // Defensive check: Alpha Vantage returns an empty object or a note on rate limiting.
                    if (globalQuote != null && globalQuote.getStockData() != null && globalQuote.getStockData().getSymbol() != null) {
                        return Mono.just(globalQuote.getStockData());
                    }
                    // If data is invalid or we hit a rate limit, return an empty Mono.
                    // This prevents the NullPointerException and allows the stream to continue.
                    return Mono.empty();
                })
                .doOnError(error -> System.err.println("Error fetching stock data for " + symbol + ": " + error.getMessage()));
    }
}
