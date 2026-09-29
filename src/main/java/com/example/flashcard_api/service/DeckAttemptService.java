package com.example.flashcard_api.service;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.mapping.CardAttemptMapper;
import com.example.flashcard_api.mapping.DeckAttemptMapper;
import com.example.flashcard_api.mapping.DeckMapper;
import com.example.flashcard_api.model.dto.*;
import com.example.flashcard_api.model.entity.CardAttempt;
import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.model.entity.DeckAttempt;
import com.example.flashcard_api.model.entity.DeckAttemptStatus;
import com.example.flashcard_api.model.entity.FlashCard;
import com.example.flashcard_api.model.projection.DeckAttemptScore;
import com.example.flashcard_api.repository.DeckAttemptRepository;
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

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeckAttemptService {

    private final DeckAttemptRepository deckAttemptRepository;
    private final DeckAttemptMapper deckAttemptMapper;
    private final CardAttemptMapper cardAttemptMapper;
    private final FlashCardRepository flashCardRepository;
    private final DeckMapper deckMapper;
    private final CurrentUserService currentUserService;
    private final DeckAccessService deckAccessService;

    public @NonNull Page<DeckAttemptDto> getDeckAttempts(final @NonNull Pageable pageable) {
        return deckAttemptRepository.findAllByDeckUserId(currentUserService.getId(), ordered(pageable))
                .map(deckAttemptMapper::from);
    }

    public @NonNull DeckAttemptDto getDeckAttempt(final @NonNull Long id) {
        return deckAttemptMapper.from(getOwnedAttempt(id));
    }

    public @NonNull FullDeckAttemptDto getFullDeckAttempt(final @NonNull Long id) {
        return toFullDto(getOwnedAttempt(id));
    }

    @Transactional
    public @NonNull DeckAttemptDto createDeckAttempt(final @NonNull DeckAttemptDto dto) {
        if (dto.getCardAttempts() != null && !dto.getCardAttempts().isEmpty()) {
            throw new FlashCardApiException(BAD_REQUEST, "A practice session must start without assessments");
        }

        final Deck deck = deckAccessService.getOwnedDeckForUpdate(dto.getDeckId());
        final DeckAttempt active = deckAttemptRepository.findByActiveDeckId(deck.getId()).orElse(null);
        if (active != null) {
            return deckAttemptMapper.from(active);
        }

        final List<FlashCard> cards = flashCardRepository.findAllByDeckIdAndDeletedFalseOrderByCreatedAtAscIdAsc(deck.getId());
        if (cards.isEmpty()) {
            throw new FlashCardApiException(CONFLICT, "Add at least one card before starting practice");
        }

        final DeckAttempt attempt = new DeckAttempt();
        attempt.setDeck(deck);
        attempt.setDeckName(deck.getName());
        attempt.setActiveDeckId(deck.getId());
        attempt.setStatus(DeckAttemptStatus.IN_PROGRESS);
        attempt.setAttemptedAt(Instant.now());
        for (int position = 0; position < cards.size(); position++) {
            attempt.getCardAttempts().add(cardAttemptMapper.snapshot(cards.get(position), attempt, position));
        }
        return deckAttemptMapper.from(deckAttemptRepository.save(attempt));
    }

    public @NonNull DeckAttemptScoreDto getDeckAttemptScore(final @NonNull Long id) {
        return toScoreDto(deckAttemptRepository.findOwnedScoreById(id, currentUserService.getId())
                .orElseThrow(() -> new FlashCardApiException(NOT_FOUND, "Practice session not found")));
    }

    public @NonNull Page<DeckAttemptScoreDto> getDeckAttemptScores(final @NonNull Pageable pageable) {
        return deckAttemptRepository.findScoresByUserId(currentUserService.getId(), ordered(pageable))
                .map(this::toScoreDto);
    }

    @Transactional
    public @NonNull CardAttemptDto createCardAttempt(final @NonNull Long id, final @NonNull CardAttemptDto dto) {
        final DeckAttempt attempt = getOwnedAttemptForUpdate(id);
        if (attempt.getStatus() == DeckAttemptStatus.ABORTED) {
            throw new FlashCardApiException(CONFLICT, "This practice session has been aborted");
        }
        final CardAttempt card = attempt.getCardAttempts().stream()
                .filter(item -> item.getFlashCard().getId().equals(dto.getFlashCardId()))
                .findFirst().orElseThrow(() -> new FlashCardApiException(NOT_FOUND, "Card not found in this practice session"));

        if (card.getCorrect() != null) {
            if (card.getCorrect().equals(dto.getCorrect())) {
                return cardAttemptMapper.from(card);
            }
            throw new FlashCardApiException(CONFLICT, "This card has already been assessed");
        }
        if (attempt.getStatus() != DeckAttemptStatus.IN_PROGRESS) {
            throw new FlashCardApiException(CONFLICT, "This practice session has already ended");
        }
        final CardAttempt nextCard = attempt.getCardAttempts().stream()
                .filter(item -> item.getCorrect() == null)
                .findFirst().orElseThrow(() -> new FlashCardApiException(CONFLICT, "There are no unanswered cards"));
        if (!nextCard.getId().equals(card.getId())) {
            throw new FlashCardApiException(CONFLICT, "Assess the current card before continuing");
        }

        card.setCorrect(dto.getCorrect());
        card.setAttemptedAt(Instant.now());
        if (attempt.getCardAttempts().stream().allMatch(item -> item.getCorrect() != null)) {
            closeAttempt(attempt, DeckAttemptStatus.COMPLETED);
        }
        return cardAttemptMapper.from(card);
    }

    @Transactional
    public @NonNull FullDeckAttemptDto abortDeckAttempt(final @NonNull Long id) {
        final DeckAttempt attempt = getOwnedAttemptForUpdate(id);
        if (attempt.getStatus() == DeckAttemptStatus.COMPLETED) {
            throw new FlashCardApiException(CONFLICT, "A completed practice session cannot be aborted");
        }
        if (attempt.getStatus() == DeckAttemptStatus.IN_PROGRESS) {
            closeAttempt(attempt, DeckAttemptStatus.ABORTED);
        }
        return toFullDto(attempt);
    }

    private DeckAttempt getOwnedAttempt(final Long id) {
        return deckAttemptRepository.findOwnedByIdWithCards(id, currentUserService.getId())
                .orElseThrow(() -> new FlashCardApiException(NOT_FOUND, "Practice session not found"));
    }

    private DeckAttempt getOwnedAttemptForUpdate(final Long id) {
        return deckAttemptRepository.findOwnedByIdForUpdate(id, currentUserService.getId())
                .orElseThrow(() -> new FlashCardApiException(NOT_FOUND, "Practice session not found"));
    }

    private void closeAttempt(final DeckAttempt attempt, final DeckAttemptStatus status) {
        attempt.setStatus(status);
        attempt.setEndedAt(Instant.now());
        attempt.setActiveDeckId(null);
    }

    private FullDeckAttemptDto toFullDto(final DeckAttempt attempt) {
        final List<CardAttempt> cards = attempt.getCardAttempts();
        final int score = (int) cards.stream().filter(card -> Boolean.TRUE.equals(card.getCorrect())).count();
        final int incorrect = (int) cards.stream().filter(card -> Boolean.FALSE.equals(card.getCorrect())).count();
        final DeckDto deck = new DeckDto();
        deck.setId(attempt.getDeck().getId());
        deck.setUserId(attempt.getDeck().getUser().getId());
        deck.setName(attempt.getDeckName());
        deck.setNumberOfCards(cards.size());
        deck.setAttempts((int) deckAttemptRepository.countByDeckId(attempt.getDeck().getId()));
        deck.setCards(cards.stream().map(card -> new FlashCardDto(card.getFlashCard().getId(),
                attempt.getDeck().getId(), card.getQuestion(), card.getAnswer())).toList());

        final FullDeckAttemptDto dto = new FullDeckAttemptDto();
        dto.setDeckAttemptId(attempt.getId());
        dto.setDeck(deck);
        dto.setAttemptedAt(attempt.getAttemptedAt());
        dto.setEndedAt(attempt.getEndedAt());
        dto.setStatus(attempt.getStatus());
        dto.setScore(score);
        dto.setMaxScore(cards.size());
        dto.setIncorrectCount(incorrect);
        dto.setUnansweredCount(cards.size() - score - incorrect);
        dto.setFullCardAttemptDtos(cards.stream().map(card -> new FullCardAttemptDto(card.getId(),
                card.getFlashCard().getId(), card.getCorrect(), card.getQuestion(), card.getAnswer(), card.getPosition())).toList());
        return dto;
    }

    private DeckAttemptScoreDto toScoreDto(final DeckAttemptScore score) {
        return DeckAttemptScoreDto.builder()
                .deckAttemptId(score.deckAttemptId())
                .deckDto(deckMapper.from(score.deckSummary()))
                .attemptedAt(score.attemptedAt())
                .endedAt(score.endedAt())
                .status(score.status())
                .score((int) score.score())
                .maxScore((int) score.maxScore())
                .incorrectCount((int) score.incorrectCount())
                .unansweredCount((int) score.unansweredCount())
                .build();
    }

    private Pageable ordered(final Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "attemptedAt", "id"));
    }
}
