package com.example.flashcard_api.model.projection;

public record DeckSummary(
        Long id,
        Long userId,
        String name,
        long activeCardCount,
        long attemptCount,
        Long activeAttemptId
) {
}
