package com.example.flashcard_api.model.entity;

import com.example.flashcard_api.model.entity.base.AuditedBaseEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "deck")
public class Deck extends AuditedBaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private boolean deleted;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference(value = "user-decks")
    private User user;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    @JsonManagedReference(value = "deck-cards")
    private List<FlashCard> cards;

    @OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "deck-attempts")
    private List<DeckAttempt> attempts;

    public Deck() {
        this.cards = new ArrayList<>();
        this.attempts = new ArrayList<>();
    }

    public void addCard(@NonNull FlashCard card) {
        cards.add(card);
        card.setDeck(this);
    }

    public void addCards(@NonNull List<FlashCard> flashCards) {
        for (FlashCard card : flashCards) {
            addCard(card);
        }
    }
}
