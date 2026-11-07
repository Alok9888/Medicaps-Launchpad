
package com.project.backend.dto.response;

import com.project.backend.model.Difficulty;

public record QuestionSummaryResponse(
        Long id,
        String title,
        String slug,
        Difficulty difficulty
) {}
