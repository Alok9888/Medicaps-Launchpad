package com.project.backend.question.dto.response;

import com.project.backend.question.domain.Difficulty;

public record QuestionSummaryResponse(
        Long id,
        String title,
        String slug,
        Difficulty difficulty
) {}