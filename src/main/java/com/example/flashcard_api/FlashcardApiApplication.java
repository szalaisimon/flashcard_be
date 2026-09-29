package com.example.flashcard_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware", dateTimeProviderRef = "auditDateTimeProvider")
public class FlashcardApiApplication {

    static void main(String[] args) {
        SpringApplication.run(FlashcardApiApplication.class, args);
    }

}
