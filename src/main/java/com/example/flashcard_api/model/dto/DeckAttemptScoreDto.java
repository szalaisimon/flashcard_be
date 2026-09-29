package com.example.flashcard_api.model.dto;

import com.example.flashcard_api.model.entity.DeckAttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckAttemptScoreDto {
    private Long deckAttemptId;
    private DeckDto deckDto;
    private Integer score;
    private Integer maxScore;
    private Instant attemptedAt;
    private Instant endedAt;
    private DeckAttemptStatus status;
    private Integer incorrectCount;
    private Integer unansweredCount;
}
