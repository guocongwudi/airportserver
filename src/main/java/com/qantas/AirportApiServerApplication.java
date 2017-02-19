package com.qantas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching
public class AirportApiServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirportApiServerApplication.class, args);
    }
}
