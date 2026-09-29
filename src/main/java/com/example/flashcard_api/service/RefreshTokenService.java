package com.example.flashcard_api.service;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.model.entity.RefreshToken;
import com.example.flashcard_api.model.entity.User;
import com.example.flashcard_api.repository.RefreshTokenRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-days:30}")
    private long refreshExpirationDays;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken create(final @NonNull User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpirationDays, ChronoUnit.DAYS));
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validate(final @NonNull String token) {
        final RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new FlashCardApiException(UNAUTHORIZED, "Your session has expired. Please log in again."));

        if (!refreshToken.getExpiresAt().isAfter(Instant.now())) {
            throw new FlashCardApiException(UNAUTHORIZED, "Your session has expired. Please log in again.");
        }

        return refreshToken;
    }

    @Transactional(readOnly = true)
    public boolean isActive(final Long sessionId, final String username) {
        return sessionId != null && username != null
                && refreshTokenRepository.existsByIdAndUserUsernameAndExpiresAtAfter(sessionId, username, Instant.now());
    }

    @Transactional
    public void revoke(final String token) {
        if (token != null && !token.isBlank()) {
            refreshTokenRepository.deleteByToken(token);
        }
    }

    @Transactional
    public void revoke(final Long sessionId) {
        if (sessionId != null) {
            refreshTokenRepository.deleteById(sessionId);
        }
    }
}
