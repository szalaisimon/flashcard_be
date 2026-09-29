package com.example.flashcard_api.security.model;

import com.example.flashcard_api.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public final class UserDetailsImpl implements UserDetails {

    private final @NonNull Long id;
    private final @NonNull String username;
    private final @NonNull String password;
    private final @Nullable String email;

    public static @NonNull UserDetailsImpl fromEntity(final @NonNull User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail()
        );
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public @NonNull String getPassword() {
        return password;
    }

    @Override
    public @NonNull String getUsername() {
        return username;
    }
}
