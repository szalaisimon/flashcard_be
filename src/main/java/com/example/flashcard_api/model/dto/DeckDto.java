package com.example.flashcard_api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeckDto {
    private Long id;
    private Long userId;

    @NotBlank(message = "Deck name is required")
    @Size(max = 100, message = "Deck name must not exceed 100 characters")
    private String name;

    private List<FlashCardDto> cards;
    private Integer numberOfCards;
    private Integer attempts;
    private Long activeAttemptId;
}
