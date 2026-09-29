package com.example.flashcard_api.model.dto;

import com.example.flashcard_api.model.entity.DeckAttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullDeckAttemptDto {
    private Long deckAttemptId;
    private DeckDto deck;
    private Instant attemptedAt;
    private Instant endedAt;
    private DeckAttemptStatus status;
    private Integer score;
    private Integer maxScore;
    private Integer incorrectCount;
    private Integer unansweredCount;
    private List<FullCardAttemptDto> fullCardAttemptDtos;
}
