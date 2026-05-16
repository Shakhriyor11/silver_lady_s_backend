package com.portfolio.silver_lady_s;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class SilverLadySApplication {
    public static void main(String[] args) {
        SpringApplication.run(SilverLadySApplication.class, args);
    }
}