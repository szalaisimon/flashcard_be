package com.example.flashcard_api.controller;

import com.example.flashcard_api.model.dto.DeckDto;
import com.example.flashcard_api.model.dto.FlashCardDto;
import com.example.flashcard_api.model.response.PaginatedResponse;
import com.example.flashcard_api.service.DeckService;
import com.example.flashcard_api.service.FlashCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.flashcard_api.model.response.PaginatedResponse.buildPaginatedResponse;

@RestController
@RequestMapping(value = "/api/{version}/deck", version = "1")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;
    private final FlashCardService flashCardService;

    @GetMapping
    public PaginatedResponse<DeckDto> getAllDecks(final Pageable pageable) {
        return buildPaginatedResponse(deckService.getAllDecks(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckDto> getDeckById(final @PathVariable Long id) {
        return ResponseEntity.ok(deckService.getDeckById(id));
    }

    @PostMapping
    public ResponseEntity<DeckDto> createDeck(final @Valid @RequestBody DeckDto deck) {
        return ResponseEntity.ok(deckService.createDeck(deck));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckDto> updateDeck(final @PathVariable Long id, final @Valid @RequestBody DeckDto deck) {
        return ResponseEntity.ok(deckService.updateDeck(id, deck));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeck(final @PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }

    // ========================= FLASHCARD ==============================

    @GetMapping("{id}/flashcard")
    public PaginatedResponse<FlashCardDto> getAllFlashCards(final @PathVariable Long id, final Pageable pageable) {
        Page<FlashCardDto> page = flashCardService.getAllFlashCards(id, pageable);
        return buildPaginatedResponse(page);
    }

    @PostMapping("{id}/flashcard")
    public ResponseEntity<FlashCardDto> createFlashCard(final @PathVariable Long id, final @Valid @RequestBody FlashCardDto flashCard) {
        return ResponseEntity.ok(flashCardService.createFlashCard(id, flashCard));
    }

    @PutMapping("{deckId}/flashcard/{id}")
    public ResponseEntity<FlashCardDto> updateFlashCard(final @PathVariable Long deckId, final @PathVariable Long id,
                                                      final @Valid @RequestBody FlashCardDto flashCard) {
        return ResponseEntity.ok(flashCardService.updateFlashCard(deckId, id, flashCard));
    }

    @DeleteMapping("{deckId}/flashcard/{id}")
    public ResponseEntity<Void> deleteFlashCard(final @PathVariable Long deckId, final @PathVariable Long id) {
        flashCardService.deleteFlashCard(deckId, id);
        return ResponseEntity.noContent().build();
    }
}
