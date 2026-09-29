package com.example.flashcard_api.service;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.mapping.FlashCardMapper;
import com.example.flashcard_api.model.dto.FlashCardDto;
import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.model.entity.FlashCard;
import com.example.flashcard_api.repository.FlashCardRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlashCardService {

    private final FlashCardMapper flashCardMapper;
    private final FlashCardRepository flashCardRepository;
    private final DeckAccessService deckAccessService;

    public @NonNull Page<FlashCardDto> getAllFlashCards(final @NonNull Long deckId, final @NonNull Pageable pageable) {
        deckAccessService.getOwnedDeck(deckId);
        final Pageable ordered = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "createdAt", "id"));
        return flashCardRepository.findAllByDeckIdAndDeletedFalse(deckId, ordered).map(flashCardMapper::from);
    }

    @Transactional
    public @NonNull FlashCardDto createFlashCard(final @NonNull Long deckId, final @NonNull FlashCardDto dto) {
        final Deck deck = deckAccessService.getOwnedDeckForUpdate(deckId);
        return flashCardMapper.from(flashCardRepository.save(flashCardMapper.from(dto, deck)));
    }

    @Transactional
    public @NonNull FlashCardDto updateFlashCard(final @NonNull Long deckId, final @NonNull Long flashCardId,
                                               final @NonNull FlashCardDto dto) {
        deckAccessService.getOwnedDeckForUpdate(deckId);
        final FlashCard card = getActiveCard(deckId, flashCardId);
        card.setQuestion(dto.getQuestion());
        card.setAnswer(dto.getAnswer());
        return flashCardMapper.from(card);
    }

    @Transactional
    public void deleteFlashCard(final @NonNull Long deckId, final @NonNull Long flashCardId) {
        deckAccessService.getOwnedDeckForUpdate(deckId);
        getActiveCard(deckId, flashCardId).setDeleted(true);
    }

    private FlashCard getActiveCard(final Long deckId, final Long flashCardId) {
        return flashCardRepository.findByIdAndDeckIdAndDeletedFalse(flashCardId, deckId)
                .orElseThrow(() -> new FlashCardApiException(NOT_FOUND, "Flash card not found"));
    }
}
