package com.example.flashcard_api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FullCardAttemptDto {
    private Long cardAttemptId;
    private Long flashCardId;
    private Boolean correct;
    private String question;
    private String answer;
    private int position;
}
