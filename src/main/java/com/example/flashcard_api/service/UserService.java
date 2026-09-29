package com.example.flashcard_api.service;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.mapping.UserMapper;
import com.example.flashcard_api.model.dto.LoginDto;
import com.example.flashcard_api.model.dto.TokenDto;
import com.example.flashcard_api.model.dto.UserDto;
import com.example.flashcard_api.model.dto.UserInfoDto;
import com.example.flashcard_api.model.entity.RefreshToken;
import com.example.flashcard_api.model.entity.User;
import com.example.flashcard_api.repository.UserRepository;
import com.example.flashcard_api.security.jwt.JwtUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Transactional
    public @NonNull UserInfoDto register(final @NonNull UserDto user) {
        validatePasswordLength(user.getPassword());
        final String username = user.getUsername().strip();
        final String email = user.getEmail().strip().toLowerCase(Locale.ROOT);
        if (username.length() < 3) {
            throw new FlashCardApiException(BAD_REQUEST, "Username must be between 3 and 50 characters.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new FlashCardApiException(CONFLICT, "Username already exists.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new FlashCardApiException(CONFLICT, "Email already exists.");
        }

        final UserDto copy = new UserDto();
        copy.setEmail(email);
        copy.setUsername(username);
        copy.setPassword(passwordEncoder.encode(user.getPassword()));
        final User entity = userMapper.from(copy, new ArrayList<>());
        try {
            userRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new FlashCardApiException(CONFLICT, "Username or email already exists.");
        }
        return new UserInfoDto(entity.getUsername());
    }

    @Transactional
    public @NonNull TokenDto authenticateUser(final @NonNull LoginDto user, final String previousRefreshToken) {
        validatePasswordLength(user.getPassword());
        final String username = user.getUsername().strip();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, user.getPassword()));
        final User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new FlashCardApiException(UNAUTHORIZED, "Invalid username or password."));

        refreshTokenService.revoke(previousRefreshToken);
        final RefreshToken session = refreshTokenService.create(userEntity);
        return new TokenDto(jwtUtils.generateJwtToken(username, session.getId()), session.getToken());
    }

    @Transactional(readOnly = true)
    public @NonNull TokenDto refreshToken(final @NonNull String refreshToken) {
        final RefreshToken session = refreshTokenService.validate(refreshToken);
        return new TokenDto(jwtUtils.generateJwtToken(session.getUser().getUsername(), session.getId()), null);
    }

    @Transactional
    public void logout(final String refreshToken, final String accessToken) {
        refreshTokenService.revoke(refreshToken);
        if (accessToken != null && !accessToken.isBlank()) {
            refreshTokenService.revoke(jwtUtils.extractSessionIdForLogout(accessToken));
        }
    }

    private void validatePasswordLength(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new FlashCardApiException(BAD_REQUEST, "Password must not exceed 72 UTF-8 bytes.");
        }
    }
}
