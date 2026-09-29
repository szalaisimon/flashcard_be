package com.example.flashcard_api.model.entity;

import com.example.flashcard_api.model.entity.base.AuditedBaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deck_attempt")
public class DeckAttempt extends AuditedBaseEntity {

    @Column(nullable = false, updatable = false)
    private Instant attemptedAt;

    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeckAttemptStatus status;

    @Column(nullable = false, length = 100, updatable = false)
    private String deckName;

    @Column(unique = true)
    private Long activeDeckId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false, updatable = false)
    @JsonBackReference(value = "deck-attempts")
    private Deck deck;

    @OneToMany(mappedBy = "deckAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    @JsonManagedReference(value = "attempt-cardattempts")
    private List<CardAttempt> cardAttempts = new ArrayList<>();
}
