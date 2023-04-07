package com.sparta.parknav;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@EnableScheduling
@RestController
@SpringBootApplication
public class ParknavApplication {
    @RequestMapping("/api/users/hello")
    String hello() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(ParknavApplication.class, args);
    }

}
