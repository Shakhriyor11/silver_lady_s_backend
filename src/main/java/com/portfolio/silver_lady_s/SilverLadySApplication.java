package com.portfolio.silver_lady_s;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SilverLadySApplication {
    public static void main(String[] args) {
        SpringApplication.run(SilverLadySApplication.class, args);
    }
}

