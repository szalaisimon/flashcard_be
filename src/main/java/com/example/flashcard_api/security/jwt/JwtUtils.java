package com.example.flashcard_api.security.jwt;

import com.example.flashcard_api.exception.FlashCardApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class JwtUtils {

    @Value("${jwt.secret}")
    private @NonNull String secretKeyString;

    @Value("${app.jwt.access-expiration-minutes:30}")
    private long accessExpirationMinutes;

    public String generateJwtToken(final @NonNull String username, final @NonNull Long sessionId) {
        return Jwts.builder()
                .subject(username)
                .claim("sid", sessionId)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(accessExpirationMinutes))))
                .signWith(key())
                .compact();
    }

    public Claims extractClaims(String token) {
        try {
            return parseClaims(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new FlashCardApiException(UNAUTHORIZED, "Invalid or expired session. Please log in again.");
        }
    }

    public Long extractSessionIdForLogout(String token) {
        try {
            return parseClaims(token).get("sid", Long.class);
        } catch (ExpiredJwtException e) {
            return e.getClaims().get("sid", Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKeyString));
    }
}
