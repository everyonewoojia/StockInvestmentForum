package com.medicine.assistant;

import com.medicine.assistant.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class MedicineAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicineAssistantApplication.class, args);
    }
}
