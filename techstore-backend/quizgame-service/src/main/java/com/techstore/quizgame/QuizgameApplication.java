package com.techstore.quizgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class QuizgameApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizgameApplication.class, args);
    }
}
