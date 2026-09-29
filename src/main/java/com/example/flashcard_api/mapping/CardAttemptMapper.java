package com.example.flashcard_api.mapping;

import com.example.flashcard_api.model.dto.CardAttemptDto;
import com.example.flashcard_api.model.entity.CardAttempt;
import com.example.flashcard_api.model.entity.DeckAttempt;
import com.example.flashcard_api.model.entity.FlashCard;
import lombok.NonNull;
import org.springframework.stereotype.Service;

@Service
public class CardAttemptMapper {

    public @NonNull CardAttemptDto from(final @NonNull CardAttempt entity) {
        return new CardAttemptDto(entity.getId(), entity.getFlashCard().getId(), entity.getCorrect());
    }

    public @NonNull CardAttempt snapshot(final @NonNull FlashCard flashCard,
                                         final @NonNull DeckAttempt deckAttempt, final int position) {
        final CardAttempt entity = new CardAttempt();
        entity.setFlashCard(flashCard);
        entity.setDeckAttempt(deckAttempt);
        entity.setQuestion(flashCard.getQuestion());
        entity.setAnswer(flashCard.getAnswer());
        entity.setPosition(position);
        return entity;
    }
}
