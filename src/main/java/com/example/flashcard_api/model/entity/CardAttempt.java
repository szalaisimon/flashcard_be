package com.example.flashcard_api.model.entity;

import com.example.flashcard_api.model.entity.base.AuditedBaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "card_attempt", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"deck_attempt_id", "flashcard_id"}),
        @UniqueConstraint(columnNames = {"deck_attempt_id", "position"})
})
public class CardAttempt extends AuditedBaseEntity {

    @Column(name = "correct")
    private Boolean correct;

    @Column(nullable = false, length = 1000, updatable = false)
    private String question;

    @Column(name = "answer", nullable = false, length = 1000, updatable = false)
    private String answer;

    @Column(nullable = false, updatable = false)
    private int position;

    private Instant attemptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_attempt_id", nullable = false, updatable = false)
    @JsonBackReference(value = "attempt-cardattempts")
    private DeckAttempt deckAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flashcard_id", nullable = false, updatable = false)
    private FlashCard flashCard;
}
