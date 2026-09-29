package com.example.flashcard_api.mapping;

import com.example.flashcard_api.model.dto.UserDto;
import com.example.flashcard_api.model.entity.Deck;
import com.example.flashcard_api.model.entity.User;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public @NonNull User from(final @NonNull UserDto dto, final @NonNull List<Deck> decks) {
        final @NonNull User entity = new User();

        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        entity.setUsername(dto.getUsername());
        entity.setDecks(decks);

        return entity;
    }

    public @NonNull UserDto from(final @NonNull User entity) {
        final @NonNull UserDto dto = new UserDto();

        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());

        return dto;
    }
}
