package com.example.flashcard_api.model.dto;

import com.example.flashcard_api.model.entity.DeckAttemptStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckAttemptDto {
    private Long id;

    @NotNull(message = "Deck ID is required")
    private Long deckId;

    private Instant attemptedAt;
    private Instant endedAt;
    private DeckAttemptStatus status;

    @Valid
    private List<CardAttemptDto> cardAttempts;
}
