package com.example.flashcard_api.repository;

import com.example.flashcard_api.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);

    boolean existsByIdAndUserUsernameAndExpiresAtAfter(Long id, String username, Instant now);

}
