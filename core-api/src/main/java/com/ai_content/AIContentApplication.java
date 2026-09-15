package com.ai_content;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AIContentApplication {
    public static void main(String[] args) {
        SpringApplication.run(AIContentApplication.class, args);
    }
}