package com.example.flashcard_api.repository;

import com.example.flashcard_api.model.entity.DeckAttempt;
import com.example.flashcard_api.model.projection.DeckAttemptScore;
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
public interface DeckAttemptRepository extends JpaRepository<DeckAttempt, Long> {

    Page<DeckAttempt> findAllByDeckUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT da FROM DeckAttempt da
            LEFT JOIN FETCH da.cardAttempts
            WHERE da.id = :id AND da.deck.user.id = :userId
            """)
    Optional<DeckAttempt> findOwnedByIdWithCards(@Param("id") Long id, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT da FROM DeckAttempt da WHERE da.id = :id AND da.deck.user.id = :userId")
    Optional<DeckAttempt> findOwnedByIdForUpdate(@Param("id") Long id, @Param("userId") Long userId);

    Optional<DeckAttempt> findByActiveDeckId(Long deckId);

    @Query("SELECT da.id FROM DeckAttempt da WHERE da.activeDeckId = :deckId")
    Optional<Long> findActiveIdByDeckId(@Param("deckId") Long deckId);

    long countByDeckId(Long deckId);

    @Query(value = """
            SELECT new com.example.flashcard_api.model.projection.DeckAttemptScore(
                da.id,
                d.id,
                d.user.id,
                da.deckName,
                (SELECT COUNT(a) FROM DeckAttempt a WHERE a.deck = d),
                da.attemptedAt,
                da.endedAt,
                da.status,
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da AND ca.correct = true),
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da AND ca.correct = false),
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da AND ca.correct IS NULL),
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da)
            )
            FROM DeckAttempt da
            JOIN da.deck d
            WHERE d.user.id = :userId
            """,
            countQuery = "SELECT COUNT(da) FROM DeckAttempt da WHERE da.deck.user.id = :userId")
    Page<DeckAttemptScore> findScoresByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT new com.example.flashcard_api.model.projection.DeckAttemptScore(
                da.id,
                d.id,
                d.user.id,
                da.deckName,
                (SELECT COUNT(a) FROM DeckAttempt a WHERE a.deck = d),
                da.attemptedAt,
                da.endedAt,
                da.status,
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da AND ca.correct = true),
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da AND ca.correct = false),
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da AND ca.correct IS NULL),
                (SELECT COUNT(ca) FROM CardAttempt ca WHERE ca.deckAttempt = da)
            )
            FROM DeckAttempt da
            JOIN da.deck d
            WHERE da.id = :id AND d.user.id = :userId
            """)
    Optional<DeckAttemptScore> findOwnedScoreById(@Param("id") Long id, @Param("userId") Long userId);
}
