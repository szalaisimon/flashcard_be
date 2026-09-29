package com.example.flashcard_api.mapping;

import com.example.flashcard_api.model.dto.FlashCardDto;
import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.model.entity.FlashCard;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class FlashCardMapper {

    public @NonNull FlashCard from(final @NonNull FlashCardDto dto, final @NonNull Deck deck) {
        final @NonNull FlashCard entity = new FlashCard();

        entity.setQuestion(dto.getQuestion());
        entity.setAnswer(dto.getAnswer());
        entity.setDeck(deck);
        entity.setDeleted(false);

        return entity;
    }

    public @NonNull FlashCardDto from(final @NonNull FlashCard entity) {
        final @NonNull FlashCardDto dto = new FlashCardDto();

        dto.setId(entity.getId());
        dto.setDeckId(entity.getDeck().getId());
        dto.setQuestion(entity.getQuestion());
        dto.setAnswer(entity.getAnswer());

        return dto;
    }
}
