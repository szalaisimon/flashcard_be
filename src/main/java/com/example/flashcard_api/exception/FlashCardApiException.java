package com.example.flashcard_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FlashCardApiException extends RuntimeException {

    private final HttpStatus status;

    public FlashCardApiException(HttpStatus status, String message) {
        this.status = status;
        super(message);
    }

    public FlashCardApiException(HttpStatus status) {
        this.status = status;
    }
}
