package com.pepijndejong.ssj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlackspotApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlackspotApplication.class, args);
    }

}
