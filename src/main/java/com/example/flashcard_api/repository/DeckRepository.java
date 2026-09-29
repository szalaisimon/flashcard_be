package com.example.flashcard_api.repository;

import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.model.projection.DeckSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {

    Optional<Deck> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Deck d WHERE d.id = :id AND d.user.id = :userId AND d.deleted = false")
    Optional<Deck> findOwnedActiveByIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    @Query(value = """
            SELECT new com.example.flashcard_api.model.projection.DeckSummary(
                d.id,
                d.user.id,
                d.name,
                (SELECT COUNT(c) FROM FlashCard c WHERE c.deck = d AND c.deleted = false),
                (SELECT COUNT(a) FROM DeckAttempt a WHERE a.deck = d),
                (SELECT active.id FROM DeckAttempt active WHERE active.activeDeckId = d.id)
            )
            FROM Deck d
            WHERE d.user.id = :userId AND d.deleted = false
            """,
            countQuery = "SELECT COUNT(d) FROM Deck d WHERE d.user.id = :userId AND d.deleted = false")
    Page<DeckSummary> findSummariesByUserId(@Param("userId") Long userId, Pageable pageable);
}
