package com.example.flashcard_api.service;

import com.example.flashcard_api.mapping.DeckMapper;
import com.example.flashcard_api.model.dto.DeckDto;
import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.model.entity.FlashCard;
import com.example.flashcard_api.repository.DeckAttemptRepository;
import com.example.flashcard_api.repository.DeckRepository;
import com.example.flashcard_api.repository.FlashCardRepository;
import com.example.flashcard_api.security.service.CurrentUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeckService {

    private final DeckRepository deckRepository;
    private final DeckAttemptRepository deckAttemptRepository;
    private final FlashCardRepository flashCardRepository;
    private final DeckMapper deckMapper;
    private final CurrentUserService currentUserService;
    private final DeckAccessService deckAccessService;

    public @NonNull Page<DeckDto> getAllDecks(final @NonNull Pageable pageable) {
        final Pageable ordered = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return deckRepository.findSummariesByUserId(currentUserService.getId(), ordered).map(deckMapper::from);
    }

    public @NonNull DeckDto getDeckById(final @NonNull Long id) {
        return toDto(deckAccessService.getOwnedDeck(id));
    }

    @Transactional
    public @NonNull DeckDto createDeck(final @NonNull DeckDto dto) {
        final Deck deck = deckMapper.from(dto, currentUserService.getUser());
        return deckMapper.from(deckRepository.save(deck));
    }

    @Transactional
    public @NonNull DeckDto updateDeck(final @NonNull Long id, final @NonNull DeckDto dto) {
        final Deck deck = deckAccessService.getOwnedDeckForUpdate(id);
        deck.setName(dto.getName().strip());
        return toDto(deck);
    }

    @Transactional
    public void deleteDeck(final @NonNull Long id) {
        deckAccessService.getOwnedDeckForUpdate(id).setDeleted(true);
    }

    private DeckDto toDto(final Deck deck) {
        final List<FlashCard> cards = flashCardRepository.findAllByDeckIdAndDeletedFalseOrderByCreatedAtAscIdAsc(deck.getId());
        final DeckDto dto = deckMapper.fromEntityWithCards(deck, cards, deckAttemptRepository.countByDeckId(deck.getId()));
        dto.setActiveAttemptId(deckAttemptRepository.findActiveIdByDeckId(deck.getId()).orElse(null));
        return dto;
    }
}
