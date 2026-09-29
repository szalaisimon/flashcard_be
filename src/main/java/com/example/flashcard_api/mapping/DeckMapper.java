package com.example.flashcard_api.mapping;

import com.example.flashcard_api.model.dto.DeckDto;
import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.model.entity.FlashCard;
import com.example.flashcard_api.model.entity.User;
import com.example.flashcard_api.model.projection.DeckSummary;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DeckMapper {

    private final FlashCardMapper flashCardMapper;

    public @NonNull Deck from(final @NonNull DeckDto deckDto, final @NonNull User user) {
        final @NonNull Deck deck = new Deck();

        deck.setName(deckDto.getName().strip());
        deck.setUser(user);

        return deck;
    }

    public @NonNull DeckDto from(final @NonNull Deck entity) {
        final @NonNull DeckDto dto = new DeckDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setUserId(entity.getUser().getId());
        dto.setAttempts(entity.getAttempts().size());
        dto.setNumberOfCards((int) entity.getCards().stream().filter(card -> !card.isDeleted()).count());
        dto.setCards(entity.getCards().stream().filter(card -> !card.isDeleted()).map(flashCardMapper::from).toList());

        return dto;
    }

    public @NonNull DeckDto from(final @NonNull DeckSummary summary) {
        final @NonNull DeckDto dto = new DeckDto();

        dto.setId(summary.id());
        dto.setName(summary.name());
        dto.setUserId(summary.userId());
        dto.setAttempts((int) summary.attemptCount());
        dto.setNumberOfCards((int) summary.activeCardCount());
        dto.setActiveAttemptId(summary.activeAttemptId());

        return dto;
    }

    public @NonNull DeckDto fromEntityWithCards(
            final @NonNull Deck entity,
            final @NonNull List<FlashCard> activeCards,
            final long attemptCount
    ) {
        final @NonNull DeckDto dto = new DeckDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setUserId(entity.getUser().getId());
        dto.setAttempts((int) attemptCount);
        dto.setNumberOfCards(activeCards.size());

        dto.setCards(activeCards.stream().map(flashCardMapper::from).toList());

        return dto;
    }
}
