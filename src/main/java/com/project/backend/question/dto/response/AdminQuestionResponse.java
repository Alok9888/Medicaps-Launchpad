package com.project.backend.question.dto.response;

import com.project.backend.question.domain.Difficulty;
import com.project.backend.question.domain.ProblemStatus;
import java.time.Instant;
import java.util.List;

public record AdminQuestionResponse(
        Long id,
        String title,
        String slug,
        String description,
        Difficulty difficulty,
        ProblemStatus status,
        String constraints,
        String inputFormat,
        String outputFormat,
        Integer timeLimitMs,
        Integer memoryLimitKb,
        String comparator,
        List<String> topics,
        List<CompanySummary> companies,
        List<TestCaseSummary> testCases,
        List<LanguageSummary> languages,
        List<HintSummary> hints,
        Integer contentRevision,
        Long rowVersion,
        Instant createdAt,
        Instant updatedAt,
        Instant publishedAt,
        Long createdBy,
        Long updatedBy
) {
    public record CompanySummary(Long id, String name, Integer frequency) {}
    public record TestCaseSummary(Long id, String input, String expectedOutput, boolean hidden, Integer sequenceOrder, String explanation) {}
    public record LanguageSummary(Long id, Long languageId, String languageName, String fileExtension, String starterCode, Boolean enabled) {}
    public record HintSummary(Long id, Integer order, String content) {}
}