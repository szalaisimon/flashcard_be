package com.example.flashcard_api.model.entity;

import com.example.flashcard_api.model.entity.base.AuditedBaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "flashcard")
public class FlashCard extends AuditedBaseEntity {

    @Column(name = "question", nullable = false, length = 1000)
    private String question;

    @Column(name = "answer", nullable = false, length = 1000)
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id", nullable = false)
    @JsonBackReference(value = "deck-cards")
    private Deck deck;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;
}
