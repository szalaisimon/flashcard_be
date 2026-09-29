package com.example.flashcard_api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlashCardDto {
    private Long id;
    private Long deckId;

    @NotBlank(message = "Question is required")
    @Size(max = 1000, message = "Question must not exceed 1000 characters")
    private String question;

    @NotBlank(message = "Answer is required")
    @Size(max = 1000, message = "Answer must not exceed 1000 characters")
    private String answer;
}
