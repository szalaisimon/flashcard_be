package com.example.flashcard_api.model.projection;

import com.example.flashcard_api.model.entity.DeckAttemptStatus;

import java.time.Instant;

public record DeckAttemptScore(
        Long deckAttemptId,
        Long deckId,
        Long deckUserId,
        String deckName,
        long deckAttemptCount,
        Instant attemptedAt,
        Instant endedAt,
        DeckAttemptStatus status,
        long score,
        long incorrectCount,
        long unansweredCount,
        long maxScore
) {

    public DeckSummary deckSummary() {
        return new DeckSummary(deckId, deckUserId, deckName, maxScore, deckAttemptCount, null);
    }
}
