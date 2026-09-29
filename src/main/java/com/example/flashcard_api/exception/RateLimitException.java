package com.example.flashcard_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitException extends FlashCardApiException {

    private final long retryAfterSeconds;

    public RateLimitException(long retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Please wait " + retryAfterSeconds + " seconds and try again.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
