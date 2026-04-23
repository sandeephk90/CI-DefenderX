package com.cidefenderx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CiDefenderXApplication {
    public static void main(String[] args) {
        SpringApplication.run(CiDefenderXApplication.class, args);
    }
}
