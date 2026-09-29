package com.example.flashcard_api.repository;

import com.example.flashcard_api.model.entity.CardAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardAttemptRepository extends JpaRepository<CardAttempt, Long> {
}
