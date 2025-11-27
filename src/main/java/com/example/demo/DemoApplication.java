package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/api/stocks")
    public List<Stock> getStocks() {
        // Create some mock stock data
        List<Stock> stocks = Arrays.asList(
            new Stock("研發魔人", "VIP", 8.09, "up"),
            new Stock("精選00733強勢股", "VIP", 5.56, "up"),
            new Stock("可能恢復信用交易", "", -3.76, "down")
        );
        return stocks;
    }
}
