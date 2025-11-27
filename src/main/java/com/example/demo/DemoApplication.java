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
import java.util.Random;

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
        // For this test, we will use the mock service directly.
        return stockService.getMockStocks(Arrays.asList("MOCK1", "MOCK2", "MOCK3"));
    }
}

@Service
class StockService {

    // This flag allows us to easily switch between real and mock data.
    private static final boolean USE_MOCK_DATA = true;

    @Value("${ALPHAVANTAGE_API_KEY:}")
    private String apiKey;

    private final WebClient webClient;

    public StockService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
    }

    public Flux<StockData> getStocks(List<String> symbols) {
        if (USE_MOCK_DATA) {
            return getMockStocks(symbols);
        }
        return getRealStocks(symbols);
    }

    // This is our new method to generate mock data for testing
    public Flux<StockData> getMockStocks(List<String> symbols) {
        return Flux.fromIterable(symbols)
                   .delayElements(Duration.ofSeconds(5)) // Faster delay for testing
                   .map(this::createMockStockData);
    }
    
    private StockData createMockStockData(String symbol) {
        Random random = new Random();
        StockData stock = new StockData();
        stock.setSymbol(symbol);
        stock.setPrice(String.valueOf(100 + random.nextDouble() * 50)); // Price between 100-150
        double change = (random.nextDouble() - 0.5) * 10; // Change between -5% and +5%
        stock.setChangePercent(String.format("%.4f%%", change));
        return stock;
    }

    private Flux<StockData> getRealStocks(List<String> symbols) {
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
