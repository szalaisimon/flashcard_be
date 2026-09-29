package com.example.flashcard_api.repository;

import com.example.flashcard_api.model.entity.FlashCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashCardRepository extends JpaRepository<FlashCard, Long> {

    Page<FlashCard> findAllByDeckIdAndDeletedFalse(Long deckId, Pageable pageable);

    Optional<FlashCard> findByIdAndDeckIdAndDeletedFalse(Long id, Long deckId);

    List<FlashCard> findAllByDeckIdAndDeletedFalseOrderByCreatedAtAscIdAsc(Long deckId);
}
