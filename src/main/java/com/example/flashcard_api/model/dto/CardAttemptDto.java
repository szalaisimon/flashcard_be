package com.example.flashcard_api.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardAttemptDto {
    private Long id;

    @NotNull(message = "Flash card ID is required")
    private Long flashCardId;

    @NotNull(message = "Assessment is required")
    private Boolean correct;
}
