package com.example.flashcard_api.security.service;

import com.example.flashcard_api.exception.FlashCardApiException;
import com.example.flashcard_api.model.entity.User;
import com.example.flashcard_api.repository.UserRepository;
import com.example.flashcard_api.security.model.UserDetailsImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public @NonNull Long getId() {
        return getPrincipal().getId();
    }

    public @NonNull String getUsername() {
        return getPrincipal().getUsername();
    }

    /**
     * Loads the full entity from the database; prefer {@link #getId()} or {@link #getUsername()}
     * when only the identity is needed, as those are served from the JWT principal without a query.
     */
    public @NonNull User getUser() {
        return userRepository.findById(getId()).orElseThrow(
                () -> new FlashCardApiException(UNAUTHORIZED, "Authenticated user no longer exists!")
        );
    }

    private @NonNull UserDetailsImpl getPrincipal() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken
                || !(auth.getPrincipal() instanceof UserDetailsImpl principal)) {
            throw new FlashCardApiException(UNAUTHORIZED, "No authentication provided!");
        }

        return principal;
    }
}
