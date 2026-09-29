package com.example.flashcard_api.service;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.repository.DeckRepository;
import com.example.flashcard_api.security.service.CurrentUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DeckAccessService {

    private final DeckRepository deckRepository;
    private final CurrentUserService currentUserService;

    public @NonNull Deck getOwnedDeck(final @NonNull Long deckId) {
        return deckRepository.findByIdAndUserIdAndDeletedFalse(deckId, currentUserService.getId())
                .orElseThrow(() -> new FlashCardApiException(NOT_FOUND, "Deck not found"));
    }

    public @NonNull Deck getOwnedDeckForUpdate(final @NonNull Long deckId) {
        return deckRepository.findOwnedActiveByIdForUpdate(deckId, currentUserService.getId())
                .orElseThrow(() -> new FlashCardApiException(NOT_FOUND, "Deck not found"));
    }
}
