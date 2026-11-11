package com.project.backend.question.dto.response;

import com.project.backend.question.domain.Difficulty;
import java.util.List;

public record QuestionSummaryResponse(
        Long id,
        String title,
        String slug,
        Difficulty difficulty,
        List<String> topics,
        List<CompanySummary> companies
) {
    public record CompanySummary(Long id, String name, Integer frequency) {}
}