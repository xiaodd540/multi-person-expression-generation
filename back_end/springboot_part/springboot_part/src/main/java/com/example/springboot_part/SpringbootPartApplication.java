package com.example.springboot_part;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 开启 Spring 定时任务
public class SpringbootPartApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootPartApplication.class, args);
    }

}
