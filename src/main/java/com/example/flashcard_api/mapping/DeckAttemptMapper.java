package com.example.flashcard_api.mapping;

import com.example.flashcard_api.model.dto.DeckAttemptDto;
import com.example.flashcard_api.model.entity.DeckAttempt;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeckAttemptMapper {

    private final CardAttemptMapper cardAttemptMapper;

    public @NonNull DeckAttemptDto from(final @NonNull DeckAttempt entity) {
        final DeckAttemptDto dto = new DeckAttemptDto();
        dto.setId(entity.getId());
        dto.setDeckId(entity.getDeck().getId());
        dto.setAttemptedAt(entity.getAttemptedAt());
        dto.setEndedAt(entity.getEndedAt());
        dto.setStatus(entity.getStatus());
        dto.setCardAttempts(entity.getCardAttempts().stream().map(cardAttemptMapper::from).toList());
        return dto;
    }
}
