package com.example.flashcard_api.controller;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.model.dto.LoginDto;
import com.example.flashcard_api.model.dto.TokenDto;
import com.example.flashcard_api.model.dto.UserDto;
import com.example.flashcard_api.model.dto.UserInfoDto;
import com.example.flashcard_api.security.service.CurrentUserService;
import com.example.flashcard_api.service.UserService;
import com.example.flashcard_api.utils.annotations.RateLimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping(value = "/api/{version}/user", version = "1")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CurrentUserService currentUserService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.access-expiration-minutes:30}")
    private long accessExpirationMinutes;

    @Value("${app.jwt.refresh-expiration-days:30}")
    private long refreshExpirationDays;

    @PostMapping("/register")
    @RateLimit(limit = 5, timeWindowSeconds = 3600)
    public ResponseEntity<UserInfoDto> register(final @Valid @RequestBody UserDto user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(user));
    }

    @PostMapping("/login")
    @RateLimit(limit = 10, timeWindowSeconds = 300)
    public ResponseEntity<UserInfoDto> login(
            final @Valid @RequestBody LoginDto user,
            @CookieValue(value = "refreshToken", required = false) String previousRefreshToken) {
        final TokenDto tokens = userService.authenticateUser(user, previousRefreshToken);
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie("accessToken", tokens.getAccessToken(), "/", Duration.ofMinutes(accessExpirationMinutes)));
        headers.add(HttpHeaders.SET_COOKIE, cookie("refreshToken", tokens.getRefreshToken(), "/api/v1/user", Duration.ofDays(refreshExpirationDays)));
        return ResponseEntity.ok().headers(headers).body(new UserInfoDto(user.getUsername().strip()));
    }

    @PostMapping("/refresh")
    @RateLimit(limit = 20, timeWindowSeconds = 300)
    public ResponseEntity<Void> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new FlashCardApiException(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again.");
        }
        final TokenDto tokens = userService.refreshToken(refreshToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("accessToken", tokens.getAccessToken(), "/", Duration.ofMinutes(accessExpirationMinutes)))
                .build();
    }

    @PostMapping("/logout")
    @RateLimit(limit = 20, timeWindowSeconds = 300)
    public ResponseEntity<Void> logout(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            @CookieValue(value = "accessToken", required = false) String accessToken) {
        userService.logout(refreshToken, accessToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie("accessToken", "", "/", Duration.ZERO))
                .header(HttpHeaders.SET_COOKIE, cookie("refreshToken", "", "/api/v1/user", Duration.ZERO))
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> me() {
        return ResponseEntity.ok(new UserInfoDto(currentUserService.getUsername()));
    }

    private String cookie(String name, String value, String path, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path(path)
                .maxAge(maxAge)
                .build().toString();
    }
}
