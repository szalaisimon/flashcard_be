package com.example.flashcard_api.controller;

import com.example.flashcard_api.model.dto.CardAttemptDto;
import com.example.flashcard_api.model.dto.DeckAttemptDto;
import com.example.flashcard_api.model.dto.DeckAttemptScoreDto;
import com.example.flashcard_api.model.dto.FullDeckAttemptDto;
import com.example.flashcard_api.model.response.PaginatedResponse;
import com.example.flashcard_api.service.DeckAttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/{version}/deckattempt", version = "1")
public class DeckAttemptController {

    private final DeckAttemptService deckAttemptService;

    @GetMapping
    public PaginatedResponse<DeckAttemptDto> getDeckAttempts(final Pageable pageable) {
        Page<DeckAttemptDto> page = deckAttemptService.getDeckAttempts(pageable);
        return PaginatedResponse.buildPaginatedResponse(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckAttemptDto> getDeckAttemptById(final @PathVariable Long id) {
        return ResponseEntity.ok(deckAttemptService.getDeckAttempt(id));
    }

    @GetMapping("/{id}/full")
    public ResponseEntity<FullDeckAttemptDto> getFullDeckAttemptById(final @PathVariable Long id) {
        return ResponseEntity.ok(deckAttemptService.getFullDeckAttempt(id));
    }

    @PostMapping
    public ResponseEntity<DeckAttemptDto> createDeckAttempt(final @Valid @RequestBody DeckAttemptDto deckAttempt) {
        return ResponseEntity.ok(deckAttemptService.createDeckAttempt(deckAttempt));
    }

    @PostMapping("/{id}/abort")
    public ResponseEntity<FullDeckAttemptDto> abortDeckAttempt(final @PathVariable Long id) {
        return ResponseEntity.ok(deckAttemptService.abortDeckAttempt(id));
    }

    @GetMapping("/{id}/score")
    public ResponseEntity<DeckAttemptScoreDto> getDeckAttemptScore(final @PathVariable Long id) {
        return ResponseEntity.ok(deckAttemptService.getDeckAttemptScore(id));
    }

    @GetMapping("/history")
    public PaginatedResponse<DeckAttemptScoreDto> getDeckAttemptHistory(final Pageable pageable) {
        return PaginatedResponse.buildPaginatedResponse(deckAttemptService.getDeckAttemptScores(pageable));
    }

    // ========================================================

    @PostMapping("/{id}/cardattempt")
    public ResponseEntity<CardAttemptDto> createCardAttempt(final @PathVariable Long id, final @Valid @RequestBody CardAttemptDto cardAttempt) {
        return ResponseEntity.ok(deckAttemptService.createCardAttempt(id, cardAttempt));
    }
}
