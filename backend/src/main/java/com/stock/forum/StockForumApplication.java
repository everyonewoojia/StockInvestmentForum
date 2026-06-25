package com.stock.forum;

import com.stock.forum.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class StockForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockForumApplication.class, args);
    }
}
